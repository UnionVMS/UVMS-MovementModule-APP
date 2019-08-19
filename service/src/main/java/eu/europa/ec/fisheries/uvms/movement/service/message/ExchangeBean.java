package eu.europa.ec.fisheries.uvms.movement.service.message;

import java.util.UUID;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.*;

import eu.europa.ec.fisheries.schema.exchange.module.v1.ExchangeModuleMethod;
import eu.europa.ec.fisheries.schema.exchange.module.v1.ProcessedMovementResponse;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefTypeType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.context.MappedDiagnosticContext;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;

@Stateless
public class ExchangeBean {

    @Resource(mappedName = "java:/jms/queue/UVMSMovement")
    private Queue replyToQueue;

    @Resource(mappedName = "java:/" + MessageConstants.QUEUE_EXCHANGE_EVENT)
    private Destination destination;

    @Inject
    @JMSConnectionFactory("java:/JmsXA")
    private JMSContext context;
    
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
        String xml = JAXBMarshaller.marshallJaxBObjectToString(processedMovementResponse);

        TextMessage message = this.context.createTextMessage(xml);
        message.setStringProperty("FUNCTION", processedMovementResponse.getMethod().toString());
        MappedDiagnosticContext.addThreadMappedDiagnosticContextToMessageProperties(message);
        this.context.createProducer().send(destination, message);

    }

    public String sendModuleMessage(String text, String function) throws JMSException {
        TextMessage message = this.context.createTextMessage(text);
        message.setJMSReplyTo(replyToQueue);
        message.setStringProperty("FUNCTION", function);
        MappedDiagnosticContext.addThreadMappedDiagnosticContextToMessageProperties(message);
        this.context.createProducer().send(destination, message);
        return message.getJMSMessageID();

    }
}
