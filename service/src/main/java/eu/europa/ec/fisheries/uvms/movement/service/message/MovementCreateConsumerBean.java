package eu.europa.ec.fisheries.uvms.movement.service.message;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.uvms.commons.service.exception.ObjectMapperContextResolver;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.fisheries.schema.movement.module.v1.MovementBaseRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.MovementModuleMethod;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementCreateBean;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;

import java.util.List;

public class MovementCreateConsumerBean implements MessageListener {

    private static final int MAXIMUM_REDELIVERIES = 6;

    private static final Logger LOG = LoggerFactory.getLogger(MovementCreateConsumerBean.class);

    private ObjectMapper mapper;
    
    @Inject
    private MovementCreateBean movementCreate;

    @Inject
    private MovementEventBean movementEventBean;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    @PostConstruct
    private void init() {
        ObjectMapperContextResolver resolver = new ObjectMapperContextResolver();
        mapper = resolver.getContext(null);
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
                        movementCreate.processIncomingMovement(incomingMovement);
                        break;

                    case "CREATE_BATCH" :
                    List<IncomingMovement> movementList = mapper.readValue(textMessage.getText(), mapper.getTypeFactory().constructCollectionType(List.class, IncomingMovement.class));
                    for (IncomingMovement im: movementList) {
                        movementCreate.processIncomingMovement(im);
                    }
                        break;
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
                case MOVEMENT_MAP:
                    movementEventBean.getMovementMapByQuery(textMessage);
                    break;
                case PING:
                    movementEventBean.ping(textMessage);
                    break;
                case CREATE:
                case CREATE_BATCH:
                case GET_SEGMENT_BY_ID:
                case GET_TRIP_BY_ID:
                default:
                    LOG.error("[ Request method {} is not implemented ]", movementMethod.name());
                    errorEvent.fire(new EventMessage(textMessage, "[ Request method " + movementMethod.name() + "  is not implemented ]"));
            }
        } catch (NullPointerException | ClassCastException e) {
            LOG.error("[ Error when receiving message in movement: ] {}", e);
            errorEvent.fire(new EventMessage(textMessage, "Error when receiving message in movement: " + e.getMessage()));
        }
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