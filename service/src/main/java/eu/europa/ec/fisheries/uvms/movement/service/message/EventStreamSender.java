package eu.europa.ec.fisheries.uvms.movement.service.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.context.MappedDiagnosticContext;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedMovement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.jms.*;

@ApplicationScoped
public class EventStreamSender {

    private final static Logger LOG = LoggerFactory.getLogger(EventStreamSender.class);

    @Resource(mappedName = "java:/" + MessageConstants.EVENT_STREAM_TOPIC)
    private Destination destination;

    private ObjectMapper om = new ObjectMapper();

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    private Session session;
    private MessageProducer messageProducer;

    @PostConstruct
    public void init() throws Exception {
        Connection connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        messageProducer = session.createProducer(destination);
        messageProducer.setDeliveryMode(1);
        messageProducer.setTimeToLive(5000L);

        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void createdMovement(@Observes(during = TransactionPhase.AFTER_SUCCESS) @CreatedMovement Movement move){
        try {
            if (move != null) {
                MicroMovementExtended micro = new MicroMovementExtended(move.getLocation(),
                        move.getHeading(), move.getId(), move.getMovementConnect().getId(), move.getTimestamp(), move.getSpeed(), move.getMovementSource());
                String outgoingJson = om.writeValueAsString(micro);

                TextMessage message = session.createTextMessage(outgoingJson);
                message.setStringProperty("event", "Movement");
                message.setStringProperty("subscribers", null);
                MappedDiagnosticContext.addThreadMappedDiagnosticContextToMessageProperties(message);

                messageProducer.send(message);

            }
        }catch (Exception e){
            LOG.error("Error while sending update event to event stream topic: ", e);
            throw new RuntimeException(e);
        }
    }
}
