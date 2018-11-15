package eu.europa.ec.fisheries.uvms.movement.message.bean;

import eu.europa.ec.fisheries.schema.exchange.module.v1.ProcessedMovementResponse;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;

@Stateless
public class ExchangeBean extends AbstractProducer {

    public void send(ProcessedMovementResponse processedMovementResponse) throws ExchangeModelMarshallException, MessageException {
        String xml = JAXBMarshaller.marshallJaxBObjectToString(processedMovementResponse);
        sendModuleMessage(xml, null);
    }

    @Override
    public String getDestinationName() {
        return MessageConstants.QUEUE_EXCHANGE_EVENT;
    }
}
