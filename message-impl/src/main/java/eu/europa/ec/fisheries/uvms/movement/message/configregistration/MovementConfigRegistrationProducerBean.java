package eu.europa.ec.fisheries.uvms.movement.message.configregistration;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JMSUtils;
import eu.europa.ec.fisheries.uvms.config.message.ConfigMessageProducer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Queue;

@Stateless
@LocalBean
public class MovementConfigRegistrationProducerBean extends AbstractProducer implements ConfigMessageProducer {

    private static final Logger LOG = LoggerFactory.getLogger(MovementConfigRegistrationProducerBean.class);

    private Queue movRespQueue;

    @PostConstruct
    public void initMovementQueue(){
        movRespQueue = JMSUtils.lookupQueue(MessageConstants.QUEUE_MOVEMENT);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String sendConfigMessage(String textMsg) {
        try {
            return sendModuleMessage(textMsg, movRespQueue);
        } catch (MessageException e) {
            LOG.error("[ERROR] Error while trying to send message to Config! Check MdrConfigProducerBeanImpl..");
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String getDestinationName() {
        return MessageConstants.QUEUE_CONFIG;
    }
}
