package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetType;
import eu.europa.ec.fisheries.schema.movement.v1.*;
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
import java.util.Calendar;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by roblar on 2017-03-07.
 */
@RunWith(Arquillian.class)
public class CreateBatchMovementEventIntTest {

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

        MovementBaseType movementBaseType = createMovementBaseType();
        MovementBaseType movementBaseType2 = createMovementBaseType();
        List<MovementBaseType> movementTypeList = Arrays.asList(movementBaseType, movementBaseType2);

        String text = MovementModuleRequestMapper.mapToCreateMovementBatchRequest(movementTypeList);
        TextMessage textMessage = createTextMessage(text);

        try {
            createMovementBatchEvent.fire(new EventMessage(textMessage));
        } catch (EJBException ex) {
            Assert.assertTrue("Should not reach me!", false);
        }
    }


    @Test
    public void triggerBatchEventWithBrokenJMS() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "true");

        MovementBaseType movementBaseType = createMovementBaseType();
        MovementBaseType movementBaseType2 = createMovementBaseType();
        List<MovementBaseType> movementTypeList = Arrays.asList(movementBaseType, movementBaseType2);

        String text = MovementModuleRequestMapper.mapToCreateMovementBatchRequest(movementTypeList);
        TextMessage textMessage = createTextMessage(text);

        try {
            createMovementBatchEvent.fire(new EventMessage(textMessage));
            Assert.assertTrue("Should not reach me!", false);
        } catch (EJBException ignore) {}
    }

    private MovementBaseType createMovementBaseType() {
        MovementActivityType activityType = new MovementActivityType();
        activityType.setCallback("TEST");
        activityType.setMessageId("TEST");
        activityType.setMessageType(MovementActivityTypeType.AUT);

        AssetId assetId = new AssetId();
        assetId.setAssetType(AssetType.VESSEL);
        assetId.setIdType(AssetIdType.GUID);
        assetId.setValue("TEST");

        MovementPoint movementPoint = new MovementPoint();
        movementPoint.setLongitude(0D);
        movementPoint.setLatitude(0D);
        movementPoint.setAltitude(0D);



        MovementBaseType movementBaseType = new MovementBaseType();
        //movementBaseType.setGuid("");
        movementBaseType.setMovementType(MovementTypeType.POS);
        movementBaseType.setActivity(activityType);
        movementBaseType.setConnectId("TEST");
        movementBaseType.setAssetId(assetId);
        movementBaseType.setDuplicates("false");
        movementBaseType.setInternalReferenceNumber("TEST");
        movementBaseType.setPosition(movementPoint);
        movementBaseType.setReportedCourse(0d);
        movementBaseType.setReportedSpeed(0d);
        movementBaseType.setSource(MovementSourceType.NAF);
        movementBaseType.setStatus("TEST");
        movementBaseType.setPositionTime(Calendar.getInstance().getTime());
        movementBaseType.setTripNumber(0d);

        return movementBaseType;
    }

    private TextMessage createTextMessage(String text) throws JMSException {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(text);
        return  textMessage;
    }
}
