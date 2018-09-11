package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementMapByQueryRequest;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
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
import javax.jms.JMSException;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
public class GetMovementMapByQueryBean {

    private static final Logger LOG = LoggerFactory.getLogger(GetMovementMapByQueryBean.class);

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    @EJB
    private MovementService movementService;

    @EJB
    private MessageProducer messageProducer;

    public void getMovementMapByQuery(TextMessage jmsMessage) {
        try {
            LOG.info("Get Movement By Query Received.. processing request in MovementEventServiceBean : {}", jmsMessage.getText());
            GetMovementMapByQueryRequest request = JAXBMarshaller.unmarshallTextMessage(jmsMessage, GetMovementMapByQueryRequest.class);
            GetMovementMapByQueryResponse movementList = movementService.getMapByQuery(request.getQuery());
            String responseString = MovementModuleResponseMapper.mapToMovementMapResponse(movementList.getMovementMap());

            messageProducer.sendMessageBackToRecipient(jmsMessage, responseString);
            LOG.info("Response sent back to requestor on queue [ {} ]", jmsMessage!= null ? jmsMessage.getJMSReplyTo() : "Null!!!");
        } catch (MovementModelException | MovementMessageException | MovementServiceException | JMSException ex) {
            LOG.error("[ Error when creating getMovementMapByQuery ] ", ex);
            EventMessage eventMessage = new EventMessage(jmsMessage, ex.getMessage());
            errorEvent.fire(eventMessage);
            throw new EJBException(ex);
        }
    }
}
