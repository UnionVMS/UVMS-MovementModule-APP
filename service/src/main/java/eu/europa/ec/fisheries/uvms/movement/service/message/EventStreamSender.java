package eu.europa.ec.fisheries.uvms.movement.service.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.context.MappedDiagnosticContext;
import eu.europa.ec.fisheries.uvms.commons.service.exception.ObjectMapperContextResolver;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedMovement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.jms.*;

@Stateless
public class EventStreamSender {

    private final static Logger LOG = LoggerFactory.getLogger(EventStreamSender.class);

    @Resource(mappedName = "java:/" + MessageConstants.EVENT_STREAM_TOPIC)
    private Destination destination;

    @Inject
    @JMSConnectionFactory("java:/ConnectionFactory")
    JMSContext context;

    private ObjectMapper om = new ObjectMapper();

    @PostConstruct
    public void init() {
        ObjectMapperContextResolver resolver = new ObjectMapperContextResolver();
        om = resolver.getContext(null);  //class does not matter, it is just needed
    }

    public void createdMovement(@Observes(during = TransactionPhase.AFTER_SUCCESS) @CreatedMovement Movement move){
        try {
            if (move != null) {
                MicroMovementExtended micro = new MicroMovementExtended(move.getLocation(),
                        move.getHeading(), move.getId(), move.getMovementConnect().getId(), move.getTimestamp(), move.getSpeed(), move.getMovementSource());
                String outgoingJson = om.writeValueAsString(micro);

                TextMessage message = this.context.createTextMessage(outgoingJson);
                message.setStringProperty(MessageConstants.EVENT_STREAM_EVENT, "Movement");
                message.setStringProperty(MessageConstants.EVENT_STREAM_SUBSCRIBER_LIST, null);
                message.setStringProperty(MessageConstants.EVENT_STREAM_MOVEMENT_SOURCE, micro.getMicroMove().getSource().value());
                MappedDiagnosticContext.addThreadMappedDiagnosticContextToMessageProperties(message);

                context.createProducer().setDeliveryMode(1).setTimeToLive(5000L).send(destination, message);

            }
        }catch (Exception e){
            LOG.error("Error while sending update event to event stream topic: ", e);
            throw new RuntimeException(e);
        }
    }
}
