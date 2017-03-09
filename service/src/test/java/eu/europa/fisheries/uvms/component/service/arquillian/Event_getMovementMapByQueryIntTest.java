package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.uvms.movement.message.event.GetMovementMapByQueryEvent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJBException;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * Created by roblar on 2017-03-08.
 */
@RunWith(Arquillian.class)
public class Event_getMovementMapByQueryIntTest extends TransactionalTests {


    final static Logger LOG = LoggerFactory.getLogger(Event_getMovementMapByQueryIntTest.class);

    @Inject
    @GetMovementMapByQueryEvent
    Event<EventMessage> getMovementMapByQueryEvent;

    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createEventDeployment();
    }

    @Test
    public void testTriggerGetMovementMapByQuery() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery();

        String text = MovementModuleRequestMapper.mapToGetMovementMapByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementMapByQueryEvent.fire(new EventMessage(textMessage));
        } catch (EJBException ex) {
            Assert.assertTrue("Should not reach me!", false);
        }
    }

    @Test
    public void testTriggerGetMovementMapByQueryWithBrokenJMS() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "true");

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery();

        String text = MovementModuleRequestMapper.mapToGetMovementMapByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementMapByQueryEvent.fire(new EventMessage(textMessage));
            Assert.assertTrue("Should not reach me!", false);
        } catch (EJBException ignore) {}
    }

    @Test
    public void testTriggerGetMovementMapByQuery_mappingToWrongMovementEventType() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery();

        // Introducing mapping error here by using the wrong mapper method causing MovementModuleMethod.MOVEMENT_LIST
        // to be used instead of MovementModuleMethod.MOVEMENT_MAP
        String text = MovementModuleRequestMapper.mapToGetMovementListByQueryRequest(movementQuery);

        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {

            getMovementMapByQueryEvent.fire(new EventMessage(textMessage));

            Assert.fail("Negative test: Mapping by using the wrong movement event type should cause an exception when firing an event.");

        } catch (EJBException | ClassCastException e) {

            if (e instanceof ClassCastException) {
                Assert.assertTrue(true);
                //ToDo: Evaluate if logging should be more generic by using %s to allow for any logging framework to be used instead of only slf4j.
                LOG.error(" [ Negative test: Mapping by using the wrong movement event type should cause an exception when firing an event. ] {}", e.getMessage());
            }
        }
    }
}
