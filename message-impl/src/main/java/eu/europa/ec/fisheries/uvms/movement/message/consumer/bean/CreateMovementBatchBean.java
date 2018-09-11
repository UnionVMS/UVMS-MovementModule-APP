package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchResponse;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleResponseMapper;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementServiceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by thofan on 2017-04-21.
 */
@Stateless
@LocalBean
public class CreateMovementBatchBean {

    private final static Logger LOG = LoggerFactory.getLogger(CreateMovementBatchBean.class);

    @EJB
    private MovementServiceBean movementService;

    @EJB
    private MessageProducer messageProducer;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    public void createMovementBatch(TextMessage jmsMessage) {
        try {
            CreateMovementBatchRequest request = JAXBMarshaller.unmarshallTextMessage(jmsMessage, CreateMovementBatchRequest.class);
            CreateMovementBatchResponse createdMovement = movementService.createMovementBatch(request.getMovement(), request.getUsername());
            String responseString = MovementModuleResponseMapper.mapToCreateMovementBatchResponse(createdMovement);
            messageProducer.sendMessageBackToRecipient(jmsMessage, responseString);
            LOG.info("Response sent back to requestor on queue [ {} ]", jmsMessage!= null ? jmsMessage.getJMSReplyTo() : "Null!!!");
        } catch (EJBException | MovementMessageException | JMSException | MovementModelException ex) {
            LOG.error("[ Error when creating movement batch ] ", ex);
            errorEvent.fire(new EventMessage(jmsMessage, "Error when receiving message in movement: " + ex.getMessage()));
            throw new EJBException(ex);
        }
    }
}
