package eu.europa.ec.fisheries.uvms.movement.service.message;

import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.movement.service.util.JsonBConfiguratorMovement;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.MovementDetails;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.json.bind.Jsonb;

@Stateless
public class MovementRulesBean extends AbstractProducer {

    @Resource(mappedName = "java:/" + MessageConstants.QUEUE_MOVEMENTRULES_EVENT)
    private Queue destination;

    private Jsonb jsonb;

    @PostConstruct
    private void init() {
        jsonb = new JsonBConfiguratorMovement().getContext(null);
    }

    public void send(MovementDetails movementDetails) throws JMSException {
        String movementDetailJson = jsonb.toJson(movementDetails);
        sendMessageToSpecificQueueWithFunction(movementDetailJson, getDestination(), null, "EVALUATE_RULES", null);
    }

    @Override
    public Destination getDestination() {
        return destination;
    }
}
