package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.message.event.GetMovementListByQueryEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.producer.bean.MessageProducerBean;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementServiceBean;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
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

import static org.junit.Assert.assertNotNull;

/**
 * Created by roblar on 2017-03-08.
 */
@RunWith(Arquillian.class)
public class Event_getMovementListByQueryIntTest extends TransactionalTests {

    final static Logger LOG = LoggerFactory.getLogger(Event_getMovementListByQueryIntTest.class);

    @Inject
    @GetMovementListByQueryEvent
    Event<EventMessage> getMovementListByQueryEvent;

    @Inject
    MovementServiceBean movementServiceBean;

    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createEventDeployment();
    }

    @Test
    public void testTriggerGetMovementListByQuery() throws JMSException, ModelMarshallException, MovementServiceException, MovementDuplicateException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementQuery movementQuery = MovementEventTestHelper.createBasicMovementQuery();

        String text = MovementModuleRequestMapper.mapToGetMovementListByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementListByQueryEvent.fire(new EventMessage(textMessage));

            GetMovementListByQueryResponse getMovementListByQueryResponse = movementServiceBean.getList(movementQuery);

            assertNotNull(getMovementListByQueryResponse);
            LOG.info(" [ Firing a GetMovementListByQuery event request successfully returns an event response that is not empty. ] ");
            assertNotNull(getMovementListByQueryResponse.getMovement());
            LOG.info(" [ Firing a GetMovementListByQuery event request successfully returns an event response that contains a list of movement types. ] ");

        } catch (EJBException ex) {
            Assert.assertTrue("Should not reach me!", false);
        }
    }

    @Test
    public void testTriggerGetMovementListByQueryWithBrokenJMS() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "true");

        MovementQuery movementQuery = MovementEventTestHelper.createBasicMovementQuery();

        String text = MovementModuleRequestMapper.mapToGetMovementListByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementListByQueryEvent.fire(new EventMessage(textMessage));
            Assert.assertTrue("Should not reach me!", false);
        } catch (EJBException ignore) {}
    }
}
