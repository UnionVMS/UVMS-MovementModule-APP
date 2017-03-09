package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.uvms.movement.message.event.CreateMovementBatchEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.producer.bean.MessageProducerBean;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleRequestMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJBException;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Arrays;
import java.util.List;

/**
 * Created by roblar on 2017-03-07.
 */
@RunWith(Arquillian.class)
public class Event_createBatchMovementIntTest extends TransactionalTests {

    @Inject
    @CreateMovementBatchEvent
    Event<EventMessage> createBatchMovementEvent;

    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createEventDeployment();
    }

    @Test
    public void triggerBatchEvent() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementBaseType movementBaseType = MovementEventTestHelper.createMovementBaseType();
        MovementBaseType movementBaseType2 = MovementEventTestHelper.createMovementBaseType();
        List<MovementBaseType> movementTypeList = Arrays.asList(movementBaseType, movementBaseType2);

        String text = MovementModuleRequestMapper.mapToCreateMovementBatchRequest(movementTypeList);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            createBatchMovementEvent.fire(new EventMessage(textMessage));
        } catch (EJBException ex) {
            Assert.assertTrue("Should not reach me!", false);
        }
    }


    @Test
    public void triggerBatchEventWithBrokenJMS() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "true");

        MovementBaseType movementBaseType = MovementEventTestHelper.createMovementBaseType();
        MovementBaseType movementBaseType2 = MovementEventTestHelper.createMovementBaseType();
        List<MovementBaseType> movementTypeList = Arrays.asList(movementBaseType, movementBaseType2);

        String text = MovementModuleRequestMapper.mapToCreateMovementBatchRequest(movementTypeList);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            createBatchMovementEvent.fire(new EventMessage(textMessage));
            Assert.assertTrue("Should not reach me!", false);
        } catch (EJBException ignore) {}
    }
}
