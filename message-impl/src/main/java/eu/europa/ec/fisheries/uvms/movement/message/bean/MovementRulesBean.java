package eu.europa.ec.fisheries.uvms.movement.message.bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.MovementDetails;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;

@Stateless
public class MovementRulesBean {

    private ObjectMapper mapper = new ObjectMapper();

    //@Inject
    @JMSConnectionFactory("java:/ConnectionFactory")
    private JMSContext context;

    @Resource(mappedName = "java:/" + MessageConstants.QUEUE_MOVEMENTRULES_EVENT)
    private Queue movementRulesEventQueue;


    @PostConstruct
    private void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public void send(MovementDetails movementDetails) throws JsonProcessingException {
        String movementDetailJson = mapper.writeValueAsString(movementDetails);
        // TODO: Constant + Tracer
        context.createProducer().send(movementRulesEventQueue, movementDetailJson).setProperty(MessageConstants.JMS_FUNCTION_PROPERTY, "EVALUATE_RULES");
    }



}
