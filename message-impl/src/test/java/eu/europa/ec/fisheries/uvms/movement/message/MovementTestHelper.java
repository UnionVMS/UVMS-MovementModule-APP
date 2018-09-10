package eu.europa.ec.fisheries.uvms.movement.message;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetType;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.RangeCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.RangeKeyType;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;

public class MovementTestHelper {

    public static MovementBaseType createMovementBaseType() {
        return MovementTestHelper.createMovementBaseType(0D,0D);
    }

    public static MovementBaseType createMovementBaseType(Double longitude , Double latitude) {

        MovementBaseType movementBaseType = new MovementBaseType();
        movementBaseType.setMovementType(MovementTypeType.POS);
        movementBaseType.setConnectId(UUID.randomUUID().toString());

        AssetId assetId = new AssetId();
        assetId.setAssetType(AssetType.VESSEL);
        assetId.setIdType(AssetIdType.GUID);
        assetId.setValue(UUID.randomUUID().toString());
        movementBaseType.setAssetId(assetId);

        MovementPoint movementPoint = new MovementPoint();
        movementPoint.setLongitude(longitude);
        movementPoint.setLatitude(latitude);
        movementPoint.setAltitude(2D);
        movementBaseType.setPosition(movementPoint);

        MovementActivityType activityType = new MovementActivityType();
        activityType.setCallback("TEST");
        activityType.setMessageId("TEST");
        activityType.setMessageType(MovementActivityTypeType.AUT);

        movementBaseType.setActivity(activityType);

        movementBaseType.setDuplicates("false");
        movementBaseType.setInternalReferenceNumber("TEST");
        movementBaseType.setReportedCourse(0d);
        movementBaseType.setReportedSpeed(0d);
        movementBaseType.setSource(MovementSourceType.NAF);
        movementBaseType.setStatus("TEST");
        movementBaseType.setPositionTime(Calendar.getInstance().getTime());
        movementBaseType.setTripNumber(0d);

        return movementBaseType;
    }

    public static MovementQuery createMovementQuery(boolean useListPagination, boolean useListCriteria, boolean useRangeCriteria) {

        MovementQuery movementQuery = new MovementQuery();
        movementQuery.setExcludeFirstAndLastSegment(true);

        if(useListPagination) {
            ListPagination listPagination = createListPagination();
            movementQuery.setPagination(listPagination);
        }

        if(useListCriteria) {
            List<ListCriteria> listOfListCriteria = createListOfListCriteria();
            movementQuery.getMovementSearchCriteria().addAll(listOfListCriteria);
        }

        if(useRangeCriteria) {
            List<RangeCriteria> listOfRangeCriteria = createListOfRangeCriteria();
            movementQuery.getMovementRangeSearchCriteria().addAll(listOfRangeCriteria);
        }

        return movementQuery;
    }

    public static ListPagination createListPagination() {

        //Pagination required for list query.
        ListPagination listPagination = new ListPagination();
        listPagination.setPage(BigInteger.ONE);
        listPagination.setListSize(BigInteger.TEN);

        return listPagination;
    }

    public static List<ListCriteria> createListOfListCriteria() {

        ListCriteria listCriteria1 = new ListCriteria();
        listCriteria1.setKey(SearchKey.MOVEMENT_TYPE);
        listCriteria1.setValue("testListCriteria1");
        //listCriteria1.setValue("POS"); //Correct enum value.

        ListCriteria listCriteria2 = new ListCriteria();
        listCriteria2.setKey(SearchKey.MOVEMENT_TYPE);
        listCriteria2.setValue("testListCriteria2");
        //listCriteria2.setValue("POS"); //Correct enum value.

        return Arrays.asList(listCriteria1, listCriteria2);
    }

    public static List<RangeCriteria> createListOfRangeCriteria() {

        RangeCriteria rangeCriteria1 = new RangeCriteria();
        rangeCriteria1.setKey(RangeKeyType.SEGMENT_SPEED);
        rangeCriteria1.setFrom(Long.toString(System.currentTimeMillis()));
        rangeCriteria1.setTo(Long.toString(System.currentTimeMillis() + 1L));

        RangeCriteria rangeCriteria2 = new RangeCriteria();
        rangeCriteria2.setKey(RangeKeyType.MOVEMENT_SPEED);
        rangeCriteria2.setFrom("testRangeCriteria2_from"); //Setting arbitrary string value will fail in SQL lookup.
        rangeCriteria2.setTo("testRangeCriteria2_to");

        return Arrays.asList(rangeCriteria1, rangeCriteria2);
    }

    public static TextMessage createTextMessage(String text) throws JMSException {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(text);
        return textMessage;
    }
}
