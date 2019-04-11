package eu.europa.ec.fisheries.uvms.movement.service.message;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;
import eu.europa.ec.fisheries.schema.exchange.module.v1.ExchangeModuleMethod;
import eu.europa.ec.fisheries.schema.exchange.module.v1.ProcessedMovementResponse;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefTypeType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;

import java.util.HashMap;
import java.util.Map;

@Stateless
public class ExchangeBean extends AbstractProducer {

    @Resource(mappedName = "java:/jms/queue/UVMSMovement")
    private Queue replyToQueue;
    
    public void sendAckToExchange(MovementRefTypeType refType, Movement movement, String ackResponseMessageId) throws MessageException {
        if (ackResponseMessageId == null) {
            return;
        }
        ProcessedMovementResponse processedMovementResponse = new ProcessedMovementResponse();
        processedMovementResponse.setMethod(ExchangeModuleMethod.PROCESSED_MOVEMENT);
        processedMovementResponse.setUsername("");
        MovementRefType movementRefType = new MovementRefType();
        movementRefType.setAckResponseMessageID(ackResponseMessageId);
        movementRefType.setType(refType);
        if (refType.equals(MovementRefTypeType.MOVEMENT) && movement != null) {
            movementRefType.setMovementRefGuid(movement.getId().toString());
        }
        processedMovementResponse.setMovementRefType(movementRefType);
        send(processedMovementResponse);
    }
    
    public void send(ProcessedMovementResponse processedMovementResponse) throws MessageException {
        String xml = JAXBMarshaller.marshallJaxBObjectToString(processedMovementResponse);
        Map<String, String> propMap = new HashMap<>();
        propMap.put("FUNCTION", processedMovementResponse.getMethod().toString());
        sendModuleMessageWithProps(xml, null, propMap);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String sendModuleMessage(String text, String function) throws MessageException {
        Map<String, String> propMap = new HashMap<>();
        propMap.put("FUNCTION", function);
        return sendModuleMessage(text, replyToQueue);
    }
    
    @Override
    public String getDestinationName() {
        return MessageConstants.QUEUE_EXCHANGE_EVENT;
    }
}
