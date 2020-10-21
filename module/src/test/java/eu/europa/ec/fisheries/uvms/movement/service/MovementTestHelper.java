package eu.europa.ec.fisheries.uvms.movement.service;

import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.RangeCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.RangeKeyType;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class MovementTestHelper {

    public static final String ZERO_GUID = "00000000-0000-0000-0000-000000000000";

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
        listPagination.setPage(BigInteger.ZERO);
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

}
