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
import org.junit.Ignore;
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
import static org.junit.Assert.assertTrue;

/**
 * Created by roblar on 2017-03-08.
 */
@RunWith(Arquillian.class)
@Ignore
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

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery(true, false, false);

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

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery(true, false, false);

        String text = MovementModuleRequestMapper.mapToGetMovementListByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementListByQueryEvent.fire(new EventMessage(textMessage));
            assertTrue("Should not reach me!", false);
        } catch (EJBException e) {
            assertTrue(true);
            LOG.error(" [ Negative test: Firing a GetMovementListByQuery event request using a non-functioning JMS queue fails. ] ");
        }
    }

    @Test
    public void testTriggerGetMovementListByQuery_mappingToWrongMovementEventType() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery(true, false, false);

        // Introducing mapping error here by using the wrong mapper method causing MovementModuleMethod.MOVEMENT_MAP
        // to be used instead of MovementModuleMethod.MOVEMENT_LIST
        String text = MovementModuleRequestMapper.mapToGetMovementMapByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementListByQueryEvent.fire(new EventMessage(textMessage));
            assertTrue("Should not reach me!", false);
        } catch (EJBException e) {
            assertTrue(e.getMessage().contains("GetMovementMapByQueryRequest cannot be cast to eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByQueryRequest"));
            //ToDo: Evaluate if logging should be more generic by using %s to allow for any logging framework to be used instead of only slf4j.
            LOG.error(" [ Negative test: Mapping to the wrong movement event type throws an exception when firing a GetMovementListByQuery event request. ] {}", e.getMessage());
        }
    }

    @Test
    public void testTriggerGetMovementListByQuery_notSettingPaginationOnAMovementListQueryFails() throws JMSException, ModelMarshallException, MovementServiceException, MovementDuplicateException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery(false, false, false);

        String text = MovementModuleRequestMapper.mapToGetMovementListByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementListByQueryEvent.fire(new EventMessage(textMessage));
            GetMovementListByQueryResponse getMovementListByQueryResponse = movementServiceBean.getList(movementQuery);
        } catch (MovementServiceException e) {
            assertTrue(true);
            //ToDo: Evaluate if logging should be more generic by using %s to allow for any logging framework to be used instead of only slf4j.
            LOG.error(" [ Negative test: Not setting pagination on a movement list query causes an exception when firing a GetMovementListByQuery event request. ] {}", e.getMessage());
        }
    }

    @Test
    public void testTriggerGetMovementListByQuery_mustUseEnumValueFromMovementTypeTypeClassWhenSettingSearchKeyTypeValueTo_MOVEMENT_TYPE() throws JMSException, ModelMarshallException, MovementServiceException, MovementDuplicateException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery(true, true, false);

        String text = MovementModuleRequestMapper.mapToGetMovementListByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementListByQueryEvent.fire(new EventMessage(textMessage));
            GetMovementListByQueryResponse getMovementListByQueryResponse = movementServiceBean.getList(movementQuery);
        } catch (EJBException e) {
            assertTrue(e.getMessage().contains("No enum constant eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType"));
            //ToDo: Evaluate if logging should be more generic by using %s to allow for any logging framework to be used instead of only slf4j.
            LOG.error(" [ Negative test: GetMovementListByQuery - Setting the value of the SearchKey type called MOVEMENT_TYPE must be an enum with value POS, ENT, EXI or MAN. ] {}", e.getMessage());
        }
    }

    @Test
    public void testTriggerGetMovementListByQuery_settingField_setFrom_inRangeCriteriaToArbitraryStringValueWillCausePSQLException() throws JMSException, ModelMarshallException, MovementServiceException, MovementDuplicateException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery(true, false, true);

        String text = MovementModuleRequestMapper.mapToGetMovementListByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementListByQueryEvent.fire(new EventMessage(textMessage));
        } catch (EJBException e) {
            assertTrue(e.getMessage().contains("SQLGrammarException: could not extract ResultSet"));
            //ToDo: Evaluate if logging should be more generic by using %s to allow for any logging framework to be used instead of only slf4j.
            LOG.error(" [ Negative test: GetMovementListByQuery - Setting the range criteria setFrom and/or setTo to an arbitrary String will cause a SQL exception. ] {}", e.getMessage());
        }
    }
}
