package eu.europa.ec.fisheries.uvms.movement.message.configregistration;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractConsumer;
import eu.europa.ec.fisheries.uvms.config.exception.ConfigMessageException;
import eu.europa.ec.fisheries.uvms.config.message.ConfigMessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
@LocalBean
public class MovementConfigRegistrationConsumerBean extends AbstractConsumer implements ConfigMessageConsumer {

    private final static Logger LOG = LoggerFactory.getLogger(MovementConfigRegistrationConsumerBean.class);

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public <T> T getConfigMessage(String correlationId, Class type) throws ConfigMessageException {
        try {
            return getMessage(correlationId, type);
        } catch (MessageException e) {
            LOG.error("[ERROR] Error when getting config message {}", e.getMessage());
            throw new ConfigMessageException("[ Error when getting config message. ]");
        }
    }

    @Override
    public String getDestinationName() {
        return MessageConstants.QUEUE_MOVEMENT;
    }

}