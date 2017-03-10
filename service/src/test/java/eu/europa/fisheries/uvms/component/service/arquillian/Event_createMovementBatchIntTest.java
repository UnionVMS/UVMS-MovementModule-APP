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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by roblar on 2017-03-07.
 */
@RunWith(Arquillian.class)
public class Event_createMovementBatchIntTest extends TransactionalTests {

    Random rnd = new Random();

    @Inject
    @CreateMovementBatchEvent
    Event<EventMessage> createMovementBatchEvent;

    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createEventDeployment();
    }

    @Test
    public void triggerBatchEvent() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");
        Double longitude = rnd.nextDouble();
        Double latitude = rnd.nextDouble();
        List<MovementBaseType> movementTypeList = new ArrayList<>();

        for(int i = 0 ; i < 250 ; i++){
            movementTypeList.add(MovementEventTestHelper.createMovementBaseType(longitude, latitude));
            longitude = longitude  + 0.05;
            latitude = latitude +  0.05;
        }

        String text = MovementModuleRequestMapper.mapToCreateMovementBatchRequest(movementTypeList);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            createMovementBatchEvent.fire(new EventMessage(textMessage));
        } catch (EJBException ex) {
            Assert.assertTrue("Should not reach me!", false);
        }
    }


    @Test
    public void triggerBatchEventWithBrokenJMS() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "true");

        Double longitude = rnd.nextDouble();
        Double latitude = rnd.nextDouble();
        List<MovementBaseType> movementTypeList = new ArrayList<>();

        for(int i = 0 ; i < 250 ; i++){
            movementTypeList.add(MovementEventTestHelper.createMovementBaseType(longitude, latitude));
            longitude += 0.05;
            latitude += 0.05;
        }

        String text = MovementModuleRequestMapper.mapToCreateMovementBatchRequest(movementTypeList);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            createMovementBatchEvent.fire(new EventMessage(textMessage));
            Assert.assertTrue("Should not reach me!", false);
        } catch (EJBException ignore) {}
    }
}
