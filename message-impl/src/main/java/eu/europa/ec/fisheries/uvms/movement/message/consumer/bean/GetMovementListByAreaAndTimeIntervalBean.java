package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByAreaAndTimeIntervalRequest;
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
 * Created by thofan on 2017-04-24.
 */

@Stateless
@LocalBean
public class GetMovementListByAreaAndTimeIntervalBean {

    private final static Logger LOG = LoggerFactory.getLogger(GetMovementListByAreaAndTimeIntervalBean.class);

    @EJB
    private MovementService movementService;

    @EJB
    private MessageProducer messageProducer;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    public void getMovementListByAreaAndTimeInterval(TextMessage jmsMessage) {
        try {
            GetMovementListByAreaAndTimeIntervalRequest request = JAXBMarshaller.unmarshallTextMessage(jmsMessage, GetMovementListByAreaAndTimeIntervalRequest.class);
            eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByAreaAndTimeIntervalResponse response = movementService.getMovementListByAreaAndTimeInterval(request.getMovementAreaAndTimeIntervalCriteria());
            String responseString = MovementModuleResponseMapper.mapTogetMovementListByAreaAndTimeIntervalResponse(response.getMovement());
            messageProducer.sendMessageBackToRecipient(jmsMessage, responseString);
        } catch (MovementMessageException | MovementServiceException | MovementModelException ex) {

            LOG.error("[ Error in GetMovementListByAreaAndTimeIntervalBean.getMovementListByAreaAndTimeInterval ] ", ex);
            EventMessage eventMessage = new EventMessage(jmsMessage, ex.getMessage());
            errorEvent.fire(eventMessage);
            throw new EJBException(ex);
        }
    }
}
