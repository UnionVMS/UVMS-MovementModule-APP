package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByQueryRequest;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by thofan on 2017-04-21.
 */
@Stateless
@LocalBean
public class GetMovementListByQueryBean {

    private static final Logger LOG = LoggerFactory.getLogger(GetMovementListByQueryBean.class);

    @EJB
    private MovementService movementService;

    @EJB
    private MessageProducer messageProducer;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    public void getMovementListByQuery(TextMessage textMessage) {
        LOG.debug("getMovementListByQuery Received.. processing request in GetMovementListByQueryBean");
        try {
            GetMovementListByQueryRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, GetMovementListByQueryRequest.class);
            GetMovementListByQueryResponse movementList = movementService.getList(request.getQuery());
            String responseString = MovementModuleResponseMapper.mapTogetMovementListByQueryResponse(movementList.getMovement());
            messageProducer.sendMessageBackToRecipient(textMessage, responseString);
        } catch (MovementMessageException | MovementServiceException | MovementModelException ex) {
            LOG.error("[ Error on getMovementListByQuery ] ", ex);
            EventMessage eventMessage = new EventMessage(textMessage, ex.getMessage());
            errorEvent.fire(eventMessage);
            throw new EJBException(ex);
        }
    }
}
