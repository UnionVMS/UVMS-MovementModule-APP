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

import eu.europa.ec.fisheries.schema.movement.common.v1.SimpleResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementRequest;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;

/**
 * Created by thofan on 2017-04-21.
 */


@Stateless
@LocalBean
public class CreateMovementBatchBean {


    final static Logger LOG = LoggerFactory.getLogger(CreateMovementBatchBean.class);

    @EJB
    private MovementService movementService;

    @EJB
    private MessageProducer messageProducer;

    @Inject
    @ErrorEvent
    Event<EventMessage> errorEvent;



    public void createMovementBatch(TextMessage textMessage) {
        LOG.debug("createMovementBatch Received.. processing request in CreateMovementBatchBean");
        try {

            CreateMovementBatchRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, CreateMovementRequest.class);
            SimpleResponse createdMovement = movementService.createMovementBatch(request.getMovement());
            String responseString = MovementModuleResponseMapper.mapToCreateMovementBatchResponse(createdMovement);
            messageProducer.sendMessageBackToRecipient(textMessage, responseString);

        } catch (EJBException | ModelMarshallException | MovementMessageException ex) {
            LOG.error("[ Error when creating movement batch ] ", ex);
            errorEvent.fire(new EventMessage(textMessage, "Error when receiving message in movement: " + ex.getMessage()));
            throw new EJBException(ex);
        }
    }






}
