package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.europa.ec.fisheries.schema.exchange.module.v1.ProcessedMovementResponse;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefTypeType;
import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetMTEnrichmentRequest;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetMTEnrichmentResponse;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.mapper.IncomingMovementMapper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.validation.MovementSanityValidatorBean;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.MovementDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.*;
import java.util.UUID;

public class MovementCreateConsumerBean implements MessageListener {

    private static final int MAXIMUM_REDELIVERIES = 6;

    private static final Logger LOG = LoggerFactory.getLogger(MovementCreateConsumerBean.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Inject
    private MovementSanityValidatorBean movementSanityValidatorBean;

    @Inject
    private MovementService movementService;

    @Resource(mappedName = "java:/" + MessageConstants.QUEUE_MOVEMENTRULES_EVENT)
    private Queue movementRulesEventQueue;

    @Resource(mappedName = "java:/" + MessageConstants.QUEUE_EXCHANGE_EVENT)
    private Queue exchangeEventQueue;

    @Inject
    @JMSConnectionFactory("java:/ConnectionFactory")
    private JMSContext context;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    @EJB
    private AssetClient assetClient;


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
                        boolean isOk = movementSanityValidatorBean.evaluateSanity(incomingMovement);
                        if(isOk) {
                            Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
                            Movement createdMovement = movementService.createMovement(movement);

                            //send to MovementRules
                            AssetMTEnrichmentRequest request = createRequest(incomingMovement, incomingMovement.getUpdatedBy());
                            AssetMTEnrichmentResponse response = assetClient.collectAssetMT(request);

                            MovementDetails movementDetails = IncomingMovementMapper.mapMovementDetails(incomingMovement, createdMovement, response);
                            String movementDetailJson = mapper.writeValueAsString(movementDetails);
                            // TODO: Constant + Tracer
                            context.createProducer().send(movementRulesEventQueue, movementDetailJson).setProperty(MessageConstants.JMS_FUNCTION_PROPERTY, "EVALUATE_RULES");
                            // report ok to Exchange...
                            // Tracer Id
                            ProcessedMovementResponse processedMovementResponse = new ProcessedMovementResponse();
                            MovementRefType movementRefType = new MovementRefType();
                            movementRefType.setAckResponseMessageID(incomingMovement.getAckResponseMessageId());
                            movementRefType.setMovementRefGuid(createdMovement.getGuid().toString());
                            movementRefType.setType(MovementRefTypeType.MOVEMENT);
                            processedMovementResponse.setMovementRefType(movementRefType);
                            String xml = JAXBMarshaller.marshallJaxBObjectToString(processedMovementResponse);
                            context.createProducer().send(exchangeEventQueue, xml);
                        } else {
                            ProcessedMovementResponse processedMovementResponse = new ProcessedMovementResponse();
                            MovementRefType movementRefType = new MovementRefType();
                            movementRefType.setAckResponseMessageID(incomingMovement.getAckResponseMessageId());
                            movementRefType.setType(MovementRefTypeType.ALARM);
                            processedMovementResponse.setMovementRefType(movementRefType);
                            String xml = JAXBMarshaller.marshallJaxBObjectToString(processedMovementResponse);
                            context.createProducer().send(exchangeEventQueue, xml);
                        }
                        break;
                    /*
                    case "CREATE_BATCH" : break;
                    */
                    default:
                        LOG.warn("NOOP");
                }
            }
        } catch (Exception ex) {
            LOG.error("Something went wrong", ex);
            if (maxRedeliveriesReached(textMessage)) {
                EventMessage eventMessage = new EventMessage(textMessage, ex.getMessage());
                errorEvent.fire(eventMessage);
            }
            throw new EJBException(ex);
        }
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