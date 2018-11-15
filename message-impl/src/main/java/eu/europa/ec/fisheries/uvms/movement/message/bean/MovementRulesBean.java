package eu.europa.ec.fisheries.uvms.movement.message.bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.MovementDetails;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

@Stateless
public class MovementRulesBean extends AbstractProducer {

    private ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    private void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public void send(MovementDetails movementDetails) throws JsonProcessingException, MessageException {
        String movementDetailJson = mapper.writeValueAsString(movementDetails);
        sendMessageToSpecificQueueWithFunction(movementDetailJson, getDestination(), null, "EVALUATE_RULES", "");
    }


    @Override
    public String getDestinationName() {
        return MessageConstants.QUEUE_MOVEMENTRULES_EVENT;
    }
}
