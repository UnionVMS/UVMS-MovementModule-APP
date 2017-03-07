package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetType;
import eu.europa.ec.fisheries.schema.movement.v1.*;
import eu.europa.ec.fisheries.uvms.movement.message.event.CreateMovementEvent;
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
import javax.enterprise.event.ObserverException;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Calendar;
import java.util.Enumeration;

/**
 * Created by andreasw on 2017-03-07.
 */
@RunWith(Arquillian.class)
public class CreateMovementEventIntTest {


    @Inject
    @CreateMovementEvent
    Event<EventMessage> createMovementEvent;


    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createEventDeployment();
    }

    @Test
    public void triggerEvent() throws JMSException, ModelMarshallException {
        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        MovementBaseType movementBaseType = createMovementBaseType();
        String text = MovementModuleRequestMapper.mapToCreateMovementRequest(movementBaseType, "TEST");

        TextMessage textMessage = createTextMessage(text);
        try {
            createMovementEvent.fire(new EventMessage(textMessage));
        } catch (EJBException EX) {
            Assert.assertTrue("Should not reach me!", false);
        }
    }

    @Test
    public void triggerEventWithBrokenJMS() throws JMSException, ModelMarshallException {
        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "true");

        MovementBaseType movementBaseType = createMovementBaseType();
        String text = MovementModuleRequestMapper.mapToCreateMovementRequest(movementBaseType, "TEST");

        TextMessage textMessage = createTextMessage(text);
        try {
            createMovementEvent.fire(new EventMessage(textMessage));
            Assert.assertTrue("Should not reach me!", false);
        } catch (EJBException ignore) {
        }
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
        TextMessage textMessage = new TextMessage() {
            private String text;

            @Override
            public void setText(String s) throws JMSException {
                this.text = s;
            }

            @Override
            public String getText() throws JMSException {
                return text;
            }

            @Override
            public String getJMSMessageID() throws JMSException {
                return null;
            }

            @Override
            public void setJMSMessageID(String s) throws JMSException {

            }

            @Override
            public long getJMSTimestamp() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSTimestamp(long l) throws JMSException {

            }

            @Override
            public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
                return new byte[0];
            }

            @Override
            public void setJMSCorrelationIDAsBytes(byte[] bytes) throws JMSException {

            }

            @Override
            public void setJMSCorrelationID(String s) throws JMSException {

            }

            @Override
            public String getJMSCorrelationID() throws JMSException {
                return null;
            }

            @Override
            public Destination getJMSReplyTo() throws JMSException {
                return null;
            }

            @Override
            public void setJMSReplyTo(Destination destination) throws JMSException {

            }

            @Override
            public Destination getJMSDestination() throws JMSException {
                return null;
            }

            @Override
            public void setJMSDestination(Destination destination) throws JMSException {

            }

            @Override
            public int getJMSDeliveryMode() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSDeliveryMode(int i) throws JMSException {

            }

            @Override
            public boolean getJMSRedelivered() throws JMSException {
                return false;
            }

            @Override
            public void setJMSRedelivered(boolean b) throws JMSException {

            }

            @Override
            public String getJMSType() throws JMSException {
                return null;
            }

            @Override
            public void setJMSType(String s) throws JMSException {

            }

            @Override
            public long getJMSExpiration() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSExpiration(long l) throws JMSException {

            }

            @Override
            public long getJMSDeliveryTime() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSDeliveryTime(long l) throws JMSException {

            }

            @Override
            public int getJMSPriority() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSPriority(int i) throws JMSException {

            }

            @Override
            public void clearProperties() throws JMSException {

            }

            @Override
            public boolean propertyExists(String s) throws JMSException {
                return false;
            }

            @Override
            public boolean getBooleanProperty(String s) throws JMSException {
                return false;
            }

            @Override
            public byte getByteProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public short getShortProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public int getIntProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public long getLongProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public float getFloatProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public double getDoubleProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public String getStringProperty(String s) throws JMSException {
                return null;
            }

            @Override
            public Object getObjectProperty(String s) throws JMSException {
                return null;
            }

            @Override
            public Enumeration getPropertyNames() throws JMSException {
                return null;
            }

            @Override
            public void setBooleanProperty(String s, boolean b) throws JMSException {

            }

            @Override
            public void setByteProperty(String s, byte b) throws JMSException {

            }

            @Override
            public void setShortProperty(String s, short i) throws JMSException {

            }

            @Override
            public void setIntProperty(String s, int i) throws JMSException {

            }

            @Override
            public void setLongProperty(String s, long l) throws JMSException {

            }

            @Override
            public void setFloatProperty(String s, float v) throws JMSException {

            }

            @Override
            public void setDoubleProperty(String s, double v) throws JMSException {

            }

            @Override
            public void setStringProperty(String s, String s1) throws JMSException {

            }

            @Override
            public void setObjectProperty(String s, Object o) throws JMSException {

            }

            @Override
            public void acknowledge() throws JMSException {

            }

            @Override
            public void clearBody() throws JMSException {

            }

            @Override
            public <T> T getBody(Class<T> aClass) throws JMSException {
                return null;
            }

            @Override
            public boolean isBodyAssignableTo(Class aClass) throws JMSException {
                return false;
            }
        };

        textMessage.setText(text);
        return textMessage;
    }


}
