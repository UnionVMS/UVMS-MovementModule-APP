package eu.europa.ec.fisheries.uvms.movement.message.bean;

import eu.europa.ec.fisheries.schema.exchange.module.v1.ProcessedMovementResponse;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;

@Stateless
public class ExchangeBean {

    //@Inject
    @JMSConnectionFactory("java:/ConnectionFactory")
    private JMSContext context;

    @Resource(mappedName = "java:/" + MessageConstants.QUEUE_EXCHANGE_EVENT)
    private Queue exchangeEventQueue;

    public void send(ProcessedMovementResponse processedMovementResponse) throws ExchangeModelMarshallException {
        String xml = JAXBMarshaller.marshallJaxBObjectToString(processedMovementResponse);
        context.createProducer().send(exchangeEventQueue, xml);
    }


}
