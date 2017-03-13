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

    public static final String ZERO_GUID = "00000000-0000-0000-0000-000000000000";


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
        movementBaseType.setGuid(ZERO_GUID);
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

    static MovementQuery createMovementQuery() {

        MovementQuery movementQuery = new MovementQuery();

        ListPagination listPagination = new ListPagination();
        listPagination.setPage(BigInteger.ZERO);
        listPagination.setListSize(BigInteger.TEN);

        ListCriteria listCriteria1 = new ListCriteria();
        listCriteria1.setKey(SearchKey.MOVEMENT_TYPE);
        listCriteria1.setValue("testListCriteria1");

        ListCriteria listCriteria2 = new ListCriteria();
        listCriteria2.setKey(SearchKey.MOVEMENT_TYPE);
        listCriteria2.setValue("testListCriteria2");

        List<ListCriteria> listOfListCriterias = Arrays.asList(listCriteria1, listCriteria2);
        movementQuery.getMovementSearchCriteria().addAll(listOfListCriterias);

        RangeCriteria rangeCriteria1 = new RangeCriteria();
        rangeCriteria1.setKey(RangeKeyType.MOVEMENT_SPEED);
        rangeCriteria1.setFrom("testRangeCriteria1_from");
        rangeCriteria1.setTo("testRangeCriteria1_to");

        RangeCriteria rangeCriteria2 = new RangeCriteria();
        rangeCriteria2.setKey(RangeKeyType.MOVEMENT_SPEED);
        rangeCriteria2.setFrom("testRangeCriteria2_from");
        rangeCriteria2.setTo("testRangeCriteria2_to");

        List<RangeCriteria> listOfRangeCriteria = Arrays.asList(rangeCriteria1, rangeCriteria2);
        movementQuery.getMovementRangeSearchCriteria().addAll(listOfRangeCriteria);

        movementQuery.setExcludeFirstAndLastSegment(true);
        movementQuery.setPagination(listPagination);

        return movementQuery;
    }

    static TextMessage createTextMessage(String text) throws JMSException {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(text);
        return textMessage;
    }
}
