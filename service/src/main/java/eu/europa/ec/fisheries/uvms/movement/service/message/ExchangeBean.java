package eu.europa.ec.fisheries.uvms.movement.service.message;

import javax.ejb.Stateless;
import eu.europa.ec.fisheries.schema.exchange.module.v1.ExchangeModuleMethod;
import eu.europa.ec.fisheries.schema.exchange.module.v1.ProcessedMovementResponse;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;

@Stateless
public class ExchangeBean extends AbstractProducer {

    public void send(ProcessedMovementResponse processedMovementResponse) throws ExchangeModelMarshallException, MessageException {
        processedMovementResponse.setMethod(ExchangeModuleMethod.PROCESSED_MOVEMENT);
        processedMovementResponse.setUsername("");
        String xml = JAXBMarshaller.marshallJaxBObjectToString(processedMovementResponse);
        sendModuleMessage(xml, null);
    }

    @Override
    public String getDestinationName() {
        return MessageConstants.QUEUE_EXCHANGE_EVENT;
    }
}
