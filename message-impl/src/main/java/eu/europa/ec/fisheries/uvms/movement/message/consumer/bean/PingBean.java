package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import eu.europa.ec.fisheries.schema.movement.module.v1.PingResponse;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import javax.ejb.EJB;
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
public class PingBean {

    private static final Logger LOG = LoggerFactory.getLogger(PingBean.class);

    @EJB
    private MessageProducer messageProducer;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    public void ping(TextMessage message) {
        try {
            PingResponse pingResponse = new PingResponse();
            pingResponse.setResponse("pong");
            messageProducer.sendMessageBackToRecipient(message, JAXBMarshaller.marshallJaxBObjectToString(pingResponse));
        } catch (MovementMessageException | MovementModelException e) {
            LOG.error("[ Error when responding to ping. ] ", e);
            errorEvent.fire(new EventMessage(message, "Error when responding to ping CD ..SSS: " + e.getMessage()));
        }
    }
}
