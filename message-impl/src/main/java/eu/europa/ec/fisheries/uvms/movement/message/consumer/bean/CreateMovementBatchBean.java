package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.uvms.movement.service.bean.AuditService;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementEntityToModelMapper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
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
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementModelToEntityMapper;
import eu.europa.ec.fisheries.schema.movement.common.v1.SimpleResponse;

/**
 * Created by thofan on 2017-04-21.
 */
@Stateless
@LocalBean
public class CreateMovementBatchBean {

    private final static Logger LOG = LoggerFactory.getLogger(CreateMovementBatchBean.class);

    @EJB
    private MovementService movementService;

    @EJB
    private MessageProducer messageProducer;

    @Inject
    private AuditService auditService;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    public void createMovementBatch(TextMessage jmsMessage) {
        try {
            CreateMovementBatchRequest request = JAXBMarshaller.unmarshallTextMessage(jmsMessage, CreateMovementBatchRequest.class);
            List<Movement> movements = new ArrayList<>();
            for (MovementBaseType movementBaseType : request.getMovement()) {
                movements.add(MovementModelToEntityMapper.mapNewMovementEntity(movementBaseType, request.getUsername()));
            }
            List<Movement> movementBatch = movementService.createMovementBatch(movements);
            SimpleResponse simpleResponse = CollectionUtils.isNotEmpty(movementBatch) ? SimpleResponse.OK : SimpleResponse.NOK;
            auditService.sendMovementBatchCreatedAudit(simpleResponse.name(), request.getUsername());
            CreateMovementBatchResponse createMovementBatchResponse = new CreateMovementBatchResponse();
            createMovementBatchResponse.setResponse(simpleResponse);
            createMovementBatchResponse.getMovements().addAll(MovementEntityToModelMapper.mapToMovementType(movementBatch));
            String responseString = MovementModuleResponseMapper.mapToCreateMovementBatchResponse(createMovementBatchResponse);
            messageProducer.sendMessageBackToRecipient(jmsMessage, responseString);
            LOG.info("Response sent back to requestor on queue [ {} ]", jmsMessage!= null ? jmsMessage.getJMSReplyTo() : "Null!!!");
        } catch (EJBException | MovementMessageException | JMSException | MovementModelException ex) {
            LOG.error("[ Error when creating movement batch ] ", ex);
            errorEvent.fire(new EventMessage(jmsMessage, "Error when receiving message in movement: " + ex.getMessage()));
            throw new EJBException(ex);
        } catch (MovementServiceException e) {
            e.printStackTrace();
        }
    }
}
