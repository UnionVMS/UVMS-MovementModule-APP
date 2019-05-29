package eu.europa.ec.fisheries.uvms.movement.service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.context.MappedDiagnosticContext;
import eu.europa.ec.fisheries.uvms.commons.message2.impl.AbstractProducer2;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.MovementDetails;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.*;

@Stateless
public class MovementRulesBean extends AbstractProducer2 {

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/" + MessageConstants.QUEUE_MOVEMENTRULES_EVENT)
    private Destination  destination;


    private ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    private void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public void send(MovementDetails movementDetails) throws JsonProcessingException, JMSException {
        String movementDetailJson = mapper.writeValueAsString(movementDetails);
        sendMessageToSpecificQueueWithFunction(movementDetailJson, getDestination(), null, "EVALUATE_RULES");
    }


    public String sendMessageToSpecificQueueWithFunction(String messageToSend, Destination destination, Destination replyTo, String function) throws JMSException {

        try (Connection connection = connectionFactory.createConnection();
             Session session = connection.createSession(false, 1);
             MessageProducer producer = session.createProducer(destination);
        ) {
            final TextMessage message = session.createTextMessage(messageToSend);
            message.setJMSReplyTo(replyTo);
            message.setStringProperty(MessageConstants.JMS_FUNCTION_PROPERTY, function);
            producer.setTimeToLive(Message.DEFAULT_TIME_TO_LIVE);
            MappedDiagnosticContext.addThreadMappedDiagnosticContextToMessageProperties(message);
            producer.send(message);
            return message.getJMSMessageID();
        }
    }




    @Override
    public Destination getDestination() {
        return destination;
    }
}
