package eu.europa.ec.fisheries.uvms.movement.service.message;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.context.MappedDiagnosticContext;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedMovement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.movement.service.util.JsonBConfiguratorMovement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.jms.*;
import javax.json.bind.Jsonb;

@Stateless
public class EventStreamSender {

    private static final Logger LOG = LoggerFactory.getLogger(EventStreamSender.class);

    @Resource(mappedName = "java:/" + MessageConstants.EVENT_STREAM_TOPIC)
    private Destination destination;

    @Inject
    @JMSConnectionFactory("java:/ConnectionFactory")
    JMSContext context;

    private Jsonb jsonb;

    @PostConstruct
    public void init() {
        jsonb = new JsonBConfiguratorMovement().getContext(null);
    }

    public void createdMovement(@Observes(during = TransactionPhase.AFTER_SUCCESS) @CreatedMovement Movement move){
        try {
            if (move != null) {
                MovementDto dto = MovementMapper.mapToMovementDto(move);
                String outgoingJson = jsonb.toJson(dto);

                TextMessage message = this.context.createTextMessage(outgoingJson);
                message.setStringProperty(MessageConstants.EVENT_STREAM_EVENT, "Movement");
                message.setStringProperty(MessageConstants.EVENT_STREAM_SUBSCRIBER_LIST, null);
                message.setStringProperty(MessageConstants.EVENT_STREAM_MOVEMENT_SOURCE, dto.getSource().value());
                MappedDiagnosticContext.addThreadMappedDiagnosticContextToMessageProperties(message);

                context.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(destination, message);

            }
        }catch (Exception e){
            LOG.error("Error while sending update event to event stream topic: ", e);
            throw new RuntimeException(e);
        }
    }
}
