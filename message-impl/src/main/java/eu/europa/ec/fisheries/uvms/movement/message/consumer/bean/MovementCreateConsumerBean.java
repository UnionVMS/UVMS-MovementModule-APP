package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import java.time.Instant;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.europa.ec.fisheries.schema.exchange.module.v1.ProcessedMovementResponse;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefTypeType;
import eu.europa.ec.fisheries.schema.movement.module.v1.MovementBaseRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.MovementModuleMethod;
import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetMTEnrichmentRequest;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetMTEnrichmentResponse;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.message.bean.ExchangeBean;
import eu.europa.ec.fisheries.uvms.movement.message.bean.MovementRulesBean;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.mapper.IncomingMovementMapper;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.validation.MovementSanityValidatorBean;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.MovementDetails;

public class MovementCreateConsumerBean implements MessageListener {

    private static final int MAXIMUM_REDELIVERIES = 6;

    private static final Logger LOG = LoggerFactory.getLogger(MovementCreateConsumerBean.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Inject
    private MovementSanityValidatorBean movementSanityValidatorBean;

    @Inject
    private MovementService movementService;

    @EJB
    private AssetClient assetClient;

    @EJB
    private MovementRulesBean movementRulesBean;

    @EJB
    private ExchangeBean exchangeBean;

    @Inject
    private MovementEventBean movementEventBean;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    @PostConstruct
    private void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }


    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;

        try {
            String propertyMethod = textMessage.getStringProperty(MessageConstants.JMS_FUNCTION_PROPERTY);
            if(propertyMethod != null) {
                switch (propertyMethod) {
                    case "CREATE" :
                        IncomingMovement incomingMovement = mapper.readValue(textMessage.getText(), IncomingMovement.class);
                        if(incomingMovement.getUpdated() == null) {
                            incomingMovement.setUpdated(Instant.now());
                        }

                        AssetMTEnrichmentRequest request = createRequest(incomingMovement, incomingMovement.getUpdatedBy());
                        AssetMTEnrichmentResponse response = assetClient.collectAssetMT(request);
                        enrichIncomingMovement(incomingMovement, response);

                        boolean isOk = movementSanityValidatorBean.evaluateSanity(incomingMovement);
                        if(isOk) {
                            Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
                            Movement createdMovement = movementService.createMovement(movement);


                            //send to MovementRules
                            MovementDetails movementDetails = IncomingMovementMapper.mapMovementDetails(incomingMovement, createdMovement, response);
                            int sumPositionReport = movementService.countNrOfMovementsLastDayForAsset(incomingMovement.getAssetHistoryId(), incomingMovement.getPositionTime());
                            movementDetails.setSumPositionReport(sumPositionReport);
                            movementRulesBean.send(movementDetails);
                            // report ok to Exchange...
                            // Tracer Id
                            ProcessedMovementResponse processedMovementResponse = new ProcessedMovementResponse();
                            MovementRefType movementRefType = new MovementRefType();
                            movementRefType.setAckResponseMessageID(incomingMovement.getAckResponseMessageId());
                            movementRefType.setMovementRefGuid(createdMovement.getGuid().toString());
                            movementRefType.setType(MovementRefTypeType.MOVEMENT);
                            processedMovementResponse.setMovementRefType(movementRefType);
                            exchangeBean.send(processedMovementResponse);
                        } else {
                            ProcessedMovementResponse processedMovementResponse = new ProcessedMovementResponse();
                            MovementRefType movementRefType = new MovementRefType();
                            movementRefType.setAckResponseMessageID(incomingMovement.getAckResponseMessageId());
                            movementRefType.setType(MovementRefTypeType.ALARM);
                            processedMovementResponse.setMovementRefType(movementRefType);
                            exchangeBean.send(processedMovementResponse);
                        }
                        break;
                    /*
                    case "CREATE_BATCH" : break;
                    */

                    case "PING":
                        movementEventBean.ping(textMessage);
                        break;
                    default:
                        LOG.warn("NOOP");
                }
            } else {
                onMessageLegacy(message);
            }
        } catch (Exception ex) {
            if (maxRedeliveriesReached(textMessage)) {
                LOG.error("maxRedeliveriesReached", ex);
            } else {
                LOG.error("Something went wrong", ex);
            }
            throw new EJBException(ex);
        }
    }

    private void onMessageLegacy(Message message) {
        TextMessage textMessage = null;
        try {
            textMessage = (TextMessage) message;
            MovementBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, MovementBaseRequest.class);
            MovementModuleMethod movementMethod = request.getMethod();
            LOG.info("Message received in movement with method [ {} ]", movementMethod);
            if (movementMethod == null) {
                LOG.error("[ Request method is null ]");
                errorEvent.fire(new EventMessage(textMessage, "Error when receiving message in movement: "));
                return;
            }
            switch (movementMethod) {
                case MOVEMENT_LIST:
                    movementEventBean.getMovementListByQuery(textMessage);
                    break;
                case CREATE:
                    movementEventBean.createMovement(textMessage);
                    break;
                case CREATE_BATCH:
                    movementEventBean.createMovementBatch(textMessage);
                    break;
                case MOVEMENT_MAP:
                    movementEventBean.getMovementMapByQuery(textMessage);
                    break;
                case PING:
                    movementEventBean.ping(textMessage);
                    break;
                case GET_SEGMENT_BY_ID:
                case GET_TRIP_BY_ID:
                default:
                    LOG.error("[ Request method {} is not implemented ]", movementMethod.name());
                    errorEvent.fire(new EventMessage(textMessage, "[ Request method " + movementMethod.name() + "  is not implemented ]"));
            }
        } catch (NullPointerException | ClassCastException | MovementModelException e) {
            LOG.error("[ Error when receiving message in movement: ] {}", e);
            errorEvent.fire(new EventMessage(textMessage, "Error when receiving message in movement: " + e.getMessage()));
        }
    }


    private void enrichIncomingMovement(IncomingMovement im, AssetMTEnrichmentResponse response) {
        im.setMobileTerminalConnectId(response.getMobileTerminalConnectId());
        im.setAssetGuid(response.getAssetUUID());
        im.setAssetHistoryId(response.getAssetHistoryId());
    }


    private AssetMTEnrichmentRequest createRequest(IncomingMovement ic, String username){
        // OBS OBS OBS
        // missing in AssetId
        // GFCM, UVI, ACCAT  = > belg req

        AssetMTEnrichmentRequest req = new AssetMTEnrichmentRequest();
        req.setIrcsValue(ic.getAssetIRCS());
        req.setCfrValue(ic.getAssetCFR());
        if(ic.getAssetGuid() != null) {
            req.setIdValue(UUID.fromString(ic.getAssetGuid()));
        }
        req.setImoValue(ic.getAssetIMO());
        req.setMmsiValue(ic.getAssetMMSI());

        req.setDnidValue(ic.getMobileTerminalDNID());
        req.setSerialNumberValue(ic.getMobileTerminalSerialNumber());
        req.setLesValue(ic.getMobileTerminalLES());
        req.setMemberNumberValue(ic.getMobileTerminalMemberNumber());

        req.setTranspondertypeValue(ic.getMovementSourceType());
        req.setPluginType(ic.getPluginType());
        req.setUser(username);

        return req;
    }


    private boolean maxRedeliveriesReached(TextMessage message) {
        try {
            if (message != null) {
                return message.getIntProperty("JMSXDeliveryCount") > MAXIMUM_REDELIVERIES;
            }
            return false;
        } catch (JMSException e) {
            return false;
        }
    }


}