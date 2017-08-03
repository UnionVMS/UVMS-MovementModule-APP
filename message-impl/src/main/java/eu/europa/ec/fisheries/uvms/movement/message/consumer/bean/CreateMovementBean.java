package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementRequest;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;

/**
 * Created by thofan on 2017-04-20.
 */
@Stateless
@LocalBean
public class CreateMovementBean {

    final static Logger LOG = LoggerFactory.getLogger(CreateMovementBean.class);

    @EJB
    private MovementService movementService;

    @EJB
    private MessageProducer messageProducer;

    @Inject
    @ErrorEvent
    Event<EventMessage> errorEvent;

    public void createMovement(TextMessage textMessage) {
        LOG.debug("createMovement Received.. processing request in CreateMovementBean");

        try {
            CreateMovementRequest createMovementRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, CreateMovementRequest.class);
            MovementType createdMovement = movementService.createMovement(createMovementRequest.getMovement(), createMovementRequest.getUsername());
            String responseString = MovementModuleResponseMapper.mapToCreateMovementResponse(createdMovement);
            messageProducer.sendMessageBackToRecipient(textMessage, responseString);
        } catch (EJBException | MovementMessageException | ModelMarshallException ex) {
            LOG.error("[ Error when creating movement ] ", ex);
            EventMessage eventMessage = new EventMessage(textMessage, ex.getMessage());
            errorEvent.fire(eventMessage);
            throw new EJBException(ex);
        }
    }
}
