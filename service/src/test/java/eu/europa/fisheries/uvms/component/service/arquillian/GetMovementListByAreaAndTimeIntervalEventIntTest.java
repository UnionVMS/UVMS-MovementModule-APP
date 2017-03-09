package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByAreaAndTimeIntervalRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.MovementModuleMethod;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.uvms.movement.message.event.GetMovementListByAreaAndTimeIntervalEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.producer.bean.MessageProducerBean;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
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

/**
 * Created by roblar on 2017-03-08.
 */
//@RunWith(Arquillian.class)
public class GetMovementListByAreaAndTimeIntervalEventIntTest extends TransactionalTests {

    /********
     * ToDo: No request mapper exists for getMovementListByAreaAndTimeInterval.
     * ToDo: Need to decide the best way forward:
     * ToDo: 1. Create a mapper method in the test class that handles object-to-string (xml) mapping,
     * ToDo: but this means having to change the class MovementModuleRequestMapper in the Model layer.
     * ToDo: 2. If this case is not handled/supported by the model in the first place, then why test it ?
     *******/
    /*

    @Inject
    @GetMovementListByAreaAndTimeIntervalEvent
    Event<EventMessage> getMovementListByAreaAndTimeIntervalEvent;

    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createEventMovementListByAreaAndTimeIntervalDeployment();
    }

    @Test
    public void testTriggerGetMovementListByAreaAndTimeInterval() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementAreaAndTimeIntervalCriteria movementAreaAndTimeIntervalCriteria = new MovementAreaAndTimeIntervalCriteria();

        movementAreaAndTimeIntervalCriteria.setAreaCode("areaCodeTest");
        movementAreaAndTimeIntervalCriteria.setFromDate("setFromDateTest");
        movementAreaAndTimeIntervalCriteria.setToDate("setToDateTest");

        //String text = MovementModuleRequestMapper.mapToGetMovementListByAreaAndTimeInterval(movementAreaAndTimeIntervalCriteria);
        //mock(MovementModuleRequestMapper.class);

        String text = mapToGetMovementListByAreaAndTimeInterval(movementAreaAndTimeIntervalCriteria);

        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementListByAreaAndTimeIntervalEvent.fire(new EventMessage(textMessage));
        } catch (EJBException ex) {
            Assert.assertTrue("Should not reach me!", false);
        }
    }

    @Test
    public void testTriggerGetMovementListByAreaAndTimeIntervalWithBrokenJMS() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "true");

        MovementAreaAndTimeIntervalCriteria movementAreaAndTimeIntervalCriteria = new MovementAreaAndTimeIntervalCriteria();

        movementAreaAndTimeIntervalCriteria.setAreaCode("areaCodeTest");
        movementAreaAndTimeIntervalCriteria.setFromDate("setFromDateTest");
        movementAreaAndTimeIntervalCriteria.setToDate("setToDateTest");

        //String text = MovementModuleRequestMapper.mapToGetMovementListByAreaAndTimeInterval(movementAreaAndTimeIntervalCriteria);
        String text = mapToGetMovementListByAreaAndTimeInterval(movementAreaAndTimeIntervalCriteria);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementListByAreaAndTimeIntervalEvent.fire(new EventMessage(textMessage));
            Assert.assertTrue("Should not reach me!", false);
        } catch (EJBException ignore) {}

    }

    private static String mapToGetMovementListByAreaAndTimeInterval(MovementAreaAndTimeIntervalCriteria movementAreaAndTimeIntervalCriteria) throws ModelMarshallException {
        GetMovementListByAreaAndTimeIntervalRequest request = new GetMovementListByAreaAndTimeIntervalRequest();
        request.setMethod(MovementModuleMethod.MOVEMENT_LIST_BY_AREA_TIME_INTERVAL);
        request.setMovementAreaAndTimeIntervalCriteria(movementAreaAndTimeIntervalCriteria);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }
    */
}
