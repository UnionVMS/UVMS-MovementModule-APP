package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.message.event.GetMovementMapByQueryEvent;
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
public class Event_getMovementMapByQueryIntTest extends TransactionalTests {


    final static Logger LOG = LoggerFactory.getLogger(Event_getMovementMapByQueryIntTest.class);

    @Inject
    @GetMovementMapByQueryEvent
    Event<EventMessage> getMovementMapByQueryEvent;

    @Inject
    MovementServiceBean movementServiceBean;

    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createEventDeployment();
    }

    @Test
    public void testTriggerGetMovementMapByQuery() throws JMSException, ModelMarshallException, MovementServiceException, MovementDuplicateException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery(false, false, false);

        String text = MovementModuleRequestMapper.mapToGetMovementMapByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementMapByQueryEvent.fire(new EventMessage(textMessage));
            GetMovementMapByQueryResponse getMovementMapByQueryResponse = movementServiceBean.getMapByQuery(movementQuery);

            assertNotNull(getMovementMapByQueryResponse);
            LOG.info(" [ Firing a GetMovementMapByQuery event request successfully returns an event response that is not empty. ] ");
            assertNotNull(getMovementMapByQueryResponse.getMovementMap());
            LOG.info(" [ Firing a GetMovementMapByQuery event request successfully returns an event response that contains a list of movements with corresponding list(s) of segment(s) and list(s) of track(s). ] ");
        } catch (EJBException ex) {
            assertTrue("Should not reach me!", false);
        }
    }

    @Test
    public void testTriggerGetMovementMapByQueryWithBrokenJMS() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "true");

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery(false, false, false);

        String text = MovementModuleRequestMapper.mapToGetMovementMapByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementMapByQueryEvent.fire(new EventMessage(textMessage));
            assertTrue("Should not reach me!", false);
        } catch (EJBException e) {
            assertTrue(true);
            LOG.error(" [ Negative test: Firing a GetMovementMapByQuery event request using a non-functioning JMS queue fails. ] ");
        }
    }

    @Test
    public void testTriggerGetMovementMapByQuery_mappingToWrongMovementEventType() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery(false, false, false);

        // Introducing mapping error here by using the wrong mapper method causing MovementModuleMethod.MOVEMENT_LIST
        // to be used instead of MovementModuleMethod.MOVEMENT_MAP
        String text = MovementModuleRequestMapper.mapToGetMovementListByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementMapByQueryEvent.fire(new EventMessage(textMessage));
            assertTrue("Should not reach me!", false);
        } catch (EJBException e) {
            assertTrue(true);
            //ToDo: Evaluate if logging should be more generic by using %s to allow for any logging framework to be used instead of only slf4j.
            LOG.error(" [ Negative test: Mapping to the wrong movement event type throws an exception when firing a GetMovementMapByQuery event request. ] {}", e.getMessage());
        }
    }

    @Test
    public void testTriggerGetMovementMapByQuery_settingPaginationOnAMovementMapQueryIsNotAllowed() throws JMSException, ModelMarshallException, MovementServiceException, MovementDuplicateException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery(true, false, false);

        String text = MovementModuleRequestMapper.mapToGetMovementMapByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementMapByQueryEvent.fire(new EventMessage(textMessage));
            GetMovementMapByQueryResponse getMovementMapByQueryResponse = movementServiceBean.getMapByQuery(movementQuery);
        } catch (MovementServiceException e) {
            assertTrue(true);
        //ToDo: Evaluate if logging should be more generic by using %s to allow for any logging framework to be used instead of only slf4j.
            LOG.error(" [ Negative test: Setting pagination on a movement map query is not allowed and causes an exception when firing a GetMovementMapByQuery event request. ] {}", e.getMessage());
        }
    }

    @Test(expected = MovementServiceException.class)
    public void testTriggerGetMovementMapByQuery_settingPaginationOnAMovementMapQueryThrowsMovementServiceException() throws JMSException, ModelMarshallException, MovementServiceException, MovementDuplicateException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery(true, false, false);

        String text = MovementModuleRequestMapper.mapToGetMovementMapByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        getMovementMapByQueryEvent.fire(new EventMessage(textMessage));
        GetMovementMapByQueryResponse getMovementMapByQueryResponse = movementServiceBean.getMapByQuery(movementQuery);
    }

    //ToDo: An arbitrary string value should not be allowed to be set for the ListCriteria field called 'value' by using a setter as the value *must* match only allowed enum values for the enum SearchKey.
    //ToDo: This enum is mapped by the SearchField enum toward the MovementTypeType enum. One solution could be to remove the setValue() method in the ListCriteria class.
    @Test
    public void testTriggerGetMovementMapByQuery_mustUseEnumValueFromMovementTypeTypeClassWhenSettingSearchKeyTypeValueTo_MOVEMENT_TYPE() throws JMSException, ModelMarshallException, MovementServiceException, MovementDuplicateException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery(false, true, false);

        String text = MovementModuleRequestMapper.mapToGetMovementMapByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementMapByQueryEvent.fire(new EventMessage(textMessage));
            GetMovementMapByQueryResponse getMovementMapByQueryResponse = movementServiceBean.getMapByQuery(movementQuery);
        } catch (EJBException e) {
            assertTrue(e.getMessage().contains("No enum constant eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType"));

            //ToDo: Evaluate if logging should be more generic by using %s to allow for any logging framework to be used instead of only slf4j.
            LOG.error(" [ Negative test: GetMovementMapByQuery - Setting the value of the SearchKey type called MOVEMENT_TYPE must be an enum with value POS, ENT, EXI or MAN. ] {}", e.getMessage());
        }
    }

    //ToDo: An arbitrary string value can be set in RangeCriteria fields setFrom() and setTo(). Evaluate if:
    //ToDo: 1. Using Date instead of String here.
    //ToDo: 2. Remove the option to set an arbitrary string value altogether by deleting these setters.
    @Test
    public void testTriggerGetMovementMapByQuery_settingField_setFrom_inRangeCriteriaToArbitraryStringValueWillCausePSQLException() throws JMSException, ModelMarshallException, MovementServiceException, MovementDuplicateException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementQuery movementQuery = MovementEventTestHelper.createMovementQuery(false, false, true);

        String text = MovementModuleRequestMapper.mapToGetMovementMapByQueryRequest(movementQuery);
        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);

        try {
            getMovementMapByQueryEvent.fire(new EventMessage(textMessage));
        } catch (EJBException e) {
            assertTrue(e.getMessage().contains("SQLGrammarException: could not extract ResultSet"));
            //ToDo: Evaluate if logging should be more generic by using %s to allow for any logging framework to be used instead of only slf4j.
            LOG.error(" [ Negative test: GetMovementMapByQuery - Setting the range criteria setFrom and/or setTo to an arbitrary String will cause a SQL exception. ] {}", e.getMessage());
        }
    }
}
