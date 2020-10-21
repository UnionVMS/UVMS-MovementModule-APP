package eu.europa.ec.fisheries.uvms.movement.service.message;

import eu.europa.ec.fisheries.schema.exchange.module.v1.ExchangeModuleMethod;
import eu.europa.ec.fisheries.schema.exchange.module.v1.ProcessedMovementResponse;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefTypeType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.UUID;

@Stateless
public class ExchangeBean extends AbstractProducer {

    @Resource(mappedName = "java:/jms/queue/UVMSMovement")
    private Queue replyToQueue;

    @Resource(mappedName = "java:/" + MessageConstants.QUEUE_EXCHANGE_EVENT)
    private Destination destination;

    public void sendAckToExchange(MovementRefTypeType refType, UUID refGuid, String ackResponseMessageId) throws JMSException {
        if (ackResponseMessageId == null) {
            return;
        }
        ProcessedMovementResponse processedMovementResponse = new ProcessedMovementResponse();
        processedMovementResponse.setMethod(ExchangeModuleMethod.PROCESSED_MOVEMENT);
        processedMovementResponse.setUsername("");
        MovementRefType movementRefType = new MovementRefType();
        movementRefType.setAckResponseMessageID(ackResponseMessageId);
        movementRefType.setType(refType);
        movementRefType.setMovementRefGuid(refGuid.toString());
        processedMovementResponse.setMovementRefType(movementRefType);
        send(processedMovementResponse);
    }
    
    public void send(ProcessedMovementResponse processedMovementResponse) throws JMSException {
        String xml = JAXBMarshaller.marshallJaxBObjectToString(processedMovementResponse);
        sendMessageToSpecificQueueWithFunction(xml, getDestination(), null, processedMovementResponse.getMethod().toString(), null);
    }

    public String sendModuleMessage(String text, String function) throws JMSException {
        return sendMessageToSpecificQueueWithFunction(text, getDestination(), replyToQueue, function, null);
    }
    
    @Override
    public Destination getDestination() {
        return destination;
    }
}
