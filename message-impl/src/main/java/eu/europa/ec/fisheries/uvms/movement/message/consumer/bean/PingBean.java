package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import eu.europa.ec.fisheries.schema.movement.module.v1.PingResponse;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.TextMessage;

/**
 * Created by thofan on 2017-04-21.
 */
@Stateless
@LocalBean
public class PingBean {


    final static Logger LOG = LoggerFactory.getLogger(PingBean.class);


    @EJB
    private MessageProducer messageProducer;

    @Inject
    @ErrorEvent
    Event<EventMessage> errorEvent;


    public void ping(TextMessage message) {
        try {
            PingResponse pingResponse = new PingResponse();
            pingResponse.setResponse("pong");
            messageProducer.sendMessageBackToRecipient(message, JAXBMarshaller.marshallJaxBObjectToString(pingResponse));
        } catch (ModelMarshallException | MovementMessageException e) {
            LOG.error("[ Error when responding to ping. ] ", e);
            errorEvent.fire(new EventMessage(message, "Error when responding to ping CD ..SSS: " + e.getMessage()));
        }
    }


}
