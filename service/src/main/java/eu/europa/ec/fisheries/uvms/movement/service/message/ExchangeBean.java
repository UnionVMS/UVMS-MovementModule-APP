package eu.europa.ec.fisheries.uvms.movement.service.message;

import javax.ejb.Stateless;
import eu.europa.ec.fisheries.schema.exchange.module.v1.ExchangeModuleMethod;
import eu.europa.ec.fisheries.schema.exchange.module.v1.ProcessedMovementResponse;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;

@Stateless
public class ExchangeBean extends AbstractProducer {

    public void sendAckToExchange(MovementRefTypeType refType, Movement movement, String ackResponseMessageId) throws ExchangeModelMarshallException, MessageException {
        // Do not send acknowledge for AIS
        if (movement != null && movement.getMovementSource().equals(MovementSourceType.AIS)) {
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
    
    public void send(ProcessedMovementResponse processedMovementResponse) throws ExchangeModelMarshallException, MessageException {
        String xml = JAXBMarshaller.marshallJaxBObjectToString(processedMovementResponse);
        sendModuleMessage(xml, null);
    }

    @Override
    public String getDestinationName() {
        return MessageConstants.QUEUE_EXCHANGE_EVENT;
    }
}
