package eu.europa.ec.fisheries.uvms.movement.service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.MovementDetails;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.*;

@Stateless
public class MovementRulesBean extends AbstractProducer {

    @Resource(mappedName = "java:/" + MessageConstants.QUEUE_MOVEMENTRULES_EVENT)
    private Queue destination;

    private ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    private void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public void send(MovementDetails movementDetails) throws JsonProcessingException, JMSException {
        String movementDetailJson = mapper.writeValueAsString(movementDetails);
        sendMessageToSpecificQueueWithFunction(movementDetailJson, getDestination(), null, "EVALUATE_RULES", null);
    }

    @Override
    public Destination getDestination() {
        return destination;
    }
}
