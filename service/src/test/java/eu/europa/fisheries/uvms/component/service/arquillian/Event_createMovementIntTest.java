package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.*;
import eu.europa.ec.fisheries.uvms.movement.message.event.CreateMovementEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.producer.bean.MessageProducerBean;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import java.math.BigInteger;
import java.util.Random;

@RunWith(Arquillian.class)
public class Event_createMovementIntTest extends TransactionalTests {

    private static int NumberOfMovements = 3;
    Random rnd = new Random();

    @Inject
    @CreateMovementEvent
    Event<EventMessage> createMovementEvent;

    @EJB
    MovementService movementService;

    @Test
    @OperateOnDeployment("movementevent")
    public void triggerEvent() throws JMSException, ModelMarshallException {
        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");
        Double longitude = rnd.nextDouble();
        Double latitude = rnd.nextDouble();
        try {
            for (int i = 0; i < NumberOfMovements; i++) {
                MovementBaseType movementBaseType = MovementEventTestHelper.createMovementBaseType(longitude, latitude);
                String text = MovementModuleRequestMapper.mapToCreateMovementRequest(movementBaseType, "TEST");
                TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);
                createMovementEvent.fire(new EventMessage(textMessage));
                longitude = longitude + 0.1;
                latitude = latitude + 0.1;
            }
        } catch(EJBException e){
            Assert.fail("Should not reach me!");
        }
        em.flush();
        // Verify that there are as least as many as we created
        MovementQuery query = createMovementQuery(true);
        try {
            GetMovementListByQueryResponse getMovementListByQueryResponse = movementService.getList(query);
            Assert.assertTrue(getMovementListByQueryResponse != null);
            Assert.assertTrue(getMovementListByQueryResponse.getMovement()!=  null);
            Assert.assertTrue(getMovementListByQueryResponse.getMovement().size() >= NumberOfMovements);
        } catch (MovementServiceException e) {
            Assert.fail();
        } catch (MovementDuplicateException e) {
            Assert.fail();
        }
    }

    @Test
    @OperateOnDeployment("movementevent")
    public void triggerEventWithBrokenJMS() throws JMSException, ModelMarshallException {
        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "true");

        MovementBaseType movementBaseType = MovementEventTestHelper.createMovementBaseType();
        String text = MovementModuleRequestMapper.mapToCreateMovementRequest(movementBaseType, "TEST");

        TextMessage textMessage = MovementEventTestHelper.createTextMessage(text);
        try {
            createMovementEvent.fire(new EventMessage(textMessage));
            Assert.fail("Should not reach me!");
        } catch (EJBException ignore) {
        }
    }

    private MovementQuery createMovementQuery(boolean usePagination) {
        MovementQuery query = new MovementQuery();
        if (usePagination) {
            BigInteger listSize = BigInteger.valueOf(100L);
            BigInteger page = BigInteger.valueOf(1L);
            ListPagination listPagination = new ListPagination();
            listPagination.setListSize(listSize);
            listPagination.setPage(page);
            query.setPagination(listPagination);
        }
        return query;
    }


}
