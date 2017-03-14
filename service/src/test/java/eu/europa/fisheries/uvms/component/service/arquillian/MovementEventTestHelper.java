package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetType;
import eu.europa.ec.fisheries.schema.movement.search.v1.*;
import eu.europa.ec.fisheries.schema.movement.v1.*;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by roblar on 2017-03-08.
 */
class MovementEventTestHelper {

    static MovementBaseType createMovementBaseType() {
        return MovementEventTestHelper.createMovementBaseType(0D,0D);
    }

    static MovementBaseType createMovementBaseType(Double longitude , Double latitude) {

        MovementActivityType activityType = new MovementActivityType();
        activityType.setCallback("TEST");
        activityType.setMessageId("TEST");
        activityType.setMessageType(MovementActivityTypeType.AUT);

        AssetId assetId = new AssetId();
        assetId.setAssetType(AssetType.VESSEL);
        assetId.setIdType(AssetIdType.GUID);
        assetId.setValue("TEST");

        MovementPoint movementPoint = new MovementPoint();
        movementPoint.setLongitude(longitude);
        movementPoint.setLatitude(latitude);
        movementPoint.setAltitude(2D);



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

    static MovementQuery createBasicMovementQuery() {

        MovementQuery movementQuery = new MovementQuery();
        movementQuery.setExcludeFirstAndLastSegment(true);

        return movementQuery;
    }

    static MovementQuery createErroneousMovementQuery(String fieldSelection) {

        MovementQuery movementQuery = createBasicMovementQuery();

        switch(fieldSelection) {
            case "listPagination":
                // Setting list pagination is not allowed. Can be a negative test.
                ListPagination listPagination = new ListPagination();
                listPagination.setPage(BigInteger.ZERO);
                listPagination.setListSize(BigInteger.TEN);
                movementQuery.setPagination(listPagination);
                break;
            case "listCriteria":
                // Arbitrary string not allowed for ListCriteria field called value. It must match allowed enum values for MOVEMENT_TYPE. This enum is mapped by SearchField.java towards MovementTypeType.java. Can be a negative test.
                ListCriteria listCriteria1 = new ListCriteria();
                listCriteria1.setKey(SearchKey.MOVEMENT_TYPE);
                listCriteria1.setValue("testListCriteria1");
                //listCriteria1.setValue("POS"); //Correct enum value.

                ListCriteria listCriteria2 = new ListCriteria();
                listCriteria2.setKey(SearchKey.MOVEMENT_TYPE);
                listCriteria2.setValue("testListCriteria2");
                //listCriteria2.setValue("POS"); //Correct enum value.

                List<ListCriteria> listOfListCriterias = Arrays.asList(listCriteria1, listCriteria2);
                movementQuery.getMovementSearchCriteria().addAll(listOfListCriterias);
                break;
            case "rangeCriteria":
                // Arbitrary string not allowed for RangeCriteria field called RangeKeyType. It can only match allowed enum values. Can be a negative test.

                RangeCriteria rangeCriteria1 = new RangeCriteria();
                rangeCriteria1.setKey(RangeKeyType.MOVEMENT_SPEED); //Correct enum value.
                rangeCriteria1.setFrom("testRangeCriteria1_from");
                rangeCriteria1.setTo("testRangeCriteria1_to");

                RangeCriteria rangeCriteria2 = new RangeCriteria();
                rangeCriteria2.setKey(RangeKeyType.MOVEMENT_SPEED);
                rangeCriteria2.setFrom("testRangeCriteria2_from");
                rangeCriteria2.setTo("testRangeCriteria2_to");

                List<RangeCriteria> listOfRangeCriteria = Arrays.asList(rangeCriteria1, rangeCriteria2);
                movementQuery.getMovementRangeSearchCriteria().addAll(listOfRangeCriteria);
                break;
        }
        return movementQuery;
    }

    static TextMessage createTextMessage(String text) throws JMSException {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(text);
        return textMessage;
    }
}
