package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementRequest;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementEntityToModelMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementModelToEntityMapper;

/**
 * Created by thofan on 2017-04-20.
 */
@Stateless
@LocalBean
public class CreateMovementBean {

    private static final Logger LOG = LoggerFactory.getLogger(CreateMovementBean.class);

    @EJB
    private MovementService movementService;

    @EJB
    private MessageProducer messageProducer;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    public void createMovement(TextMessage jmsMessage) {
        try {
            CreateMovementRequest createMovementRequest = JAXBMarshaller.unmarshallTextMessage(jmsMessage, CreateMovementRequest.class);
            Movement movement = MovementModelToEntityMapper.mapNewMovementEntity(createMovementRequest.getMovement(), createMovementRequest.getUsername());
            Movement createdMovement = movementService.createMovement(movement, createMovementRequest.getUsername());
            String responseString = MovementModuleResponseMapper.mapToCreateMovementResponse(MovementEntityToModelMapper.mapToMovementType(createdMovement));

            messageProducer.sendMessageBackToRecipient(jmsMessage, responseString);
            LOG.info("Response sent back to requestor on queue [ {} ]", jmsMessage!= null ? jmsMessage.getJMSReplyTo() : "Null!!!");
        } catch (EJBException | MovementMessageException | JMSException | MovementModelException | MovementServiceException ex) {
            LOG.error("[ Error when creating movement ] ", ex);
            EventMessage eventMessage = new EventMessage(jmsMessage, ex.getMessage());
            errorEvent.fire(eventMessage);
            throw new EJBException(ex);
        }
    }
}
