package eu.europa.ec.fisheries.uvms.movement.service.message;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetType;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.RangeCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.RangeKeyType;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.v1.*;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;

public class MovementTestHelper {

    public static IncomingMovement createIncomingMovement(Double longitude, Double latitude) {
        IncomingMovement im = new IncomingMovement();
        im.setMovementType(MovementTypeType.POS.value());
        im.setAssetHistoryId(UUID.randomUUID().toString());
        im.setAssetGuid(UUID.randomUUID().toString());
        im.setAssetType(AssetType.VESSEL.toString());
        im.setLatitude(latitude);
        im.setLongitude(longitude);
        im.setAltitude(2D);
        im.setActivityCallback("TEST");
        im.setActivityMessageId("TEST");
        im.setActivityMessageType(MovementActivityTypeType.AUT.value());
        im.setInternalReferenceNumber("TEST");
        im.setReportedCourse(0d);
        im.setReportedSpeed(0d);
        im.setMovementSourceType(MovementSourceType.NAF.value());
        im.setPluginType(PluginType.NAF.value());
        im.setStatus("TEST");
        im.setPositionTime(Instant.now());
        im.setTripNumber(0d);
        im.setComChannelType(MovementComChannelType.NAF.value());
        im.setUpdatedBy("TEST");

        return im;
    }

    public static IncomingMovement createIncomingMovementType() {
        return MovementTestHelper.createIncomingMovement(0D,0D);
    }

    public static IncomingMovement createIncomingMovementType(Double longitude , Double latitude) {

        IncomingMovement movementBase = new IncomingMovement();
        movementBase.setMovementType(MovementTypeType.POS.value());

        movementBase.setAssetType(AssetType.VESSEL.value());

        movementBase.setLatitude(latitude);
        movementBase.setLongitude(longitude);
        movementBase.setAltitude(2D);

        movementBase.setActivityCallback("TEST");
        movementBase.setActivityMessageId("TEST");
        movementBase.setActivityMessageType(MovementActivityTypeType.AUT.value());

        movementBase.setPluginType(PluginType.NAF.value());

        movementBase.setInternalReferenceNumber("TEST");
        movementBase.setReportedCourse(0d);
        movementBase.setReportedSpeed(0d);
        movementBase.setMovementSourceType(MovementSourceType.NAF.value());
        movementBase.setStatus("TEST");
        movementBase.setPositionTime(Instant.now());
        movementBase.setTripNumber(0d);

        return movementBase;
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
