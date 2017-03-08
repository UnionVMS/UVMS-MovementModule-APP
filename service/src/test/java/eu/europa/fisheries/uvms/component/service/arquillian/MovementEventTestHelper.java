package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetType;
import eu.europa.ec.fisheries.schema.movement.v1.*;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Calendar;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by roblar on 2017-03-08.
 */
class MovementEventTestHelper {

    static MovementBaseType createMovementBaseType() {
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

    static TextMessage createTextMessage(String text) throws JMSException {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(text);
        return textMessage;
    }
}
