package eu.europa.ec.fisheries.uvms.movement.service.bean;

import eu.europa.ec.fisheries.schema.movement.search.v1.*;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSegment;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTrack;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.movement.service.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchValue;
import org.hamcrest.core.StringContains;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.ThrowableMessageMatcher;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Transferred from domain layer. Should be integrated with MovementServiceIntTest.
 */

@RunWith(Arquillian.class)
public class MovementDomainModelBeanIntTest extends TransactionalTests {

    private Random rnd = new Random();

	@Inject
	private IncomingMovementBean incomingMovementBean;

	@EJB
	private MovementMapResponseHelper mapResponseHelper;
	
	@EJB
    private MovementService movementService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    @OperateOnDeployment("movementservice")
    public void filterSegments() {
        List<SearchValue> searchKeyValuesRange = new ArrayList<>();
        searchKeyValuesRange.add(createSearchValueDurationHelper());
        searchKeyValuesRange.add(createSearchValueLengthHelper());
        searchKeyValuesRange.add(createSearchValueSpeedHelper());

        // just try to satisfy all paths in the filter
        List<MovementSegment> movementSegments = new ArrayList<>();
        movementSegments.add(createMovementSegmentDurationHelper(9,900, 90));
        movementSegments.add(createMovementSegmentDurationHelper(15,1500, 150));
        movementSegments.add(createMovementSegmentDurationHelper(21,2100, 210));

        List<MovementSegment> segments = mapResponseHelper.filterSegments(movementSegments, searchKeyValuesRange);
        assertEquals(1,segments.size());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovements_0() {
        List<Movement> movements = movementService.getLatestMovements(0);
        assertNotNull(movements);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovements_5() {
        List<Movement> movements = movementService.getLatestMovements(5);
        assertNotNull(movements);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovements_5000000() {
        List<Movement> movements = movementService.getLatestMovements(5000000);
        assertNotNull(movements);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovements_NULL() {
        thrown.expect(EJBTransactionRolledbackException.class);
        movementService.getLatestMovements(null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovements_neg5() {
        thrown.expect(EJBTransactionRolledbackException.class);
        movementService.getLatestMovements(-5);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMinimalMovementListByQuery_NULL() {
        thrown.expect(EJBTransactionRolledbackException.class);
        expectedMessage("Movement list query is null");

        movementService.getMinimalList(null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementListByQuery_NULL() {
        thrown.expect(EJBTransactionRolledbackException.class);
		expectedMessage("Movement list query is null");

        movementService.getList(null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementMapByQuery_NULL() {
        thrown.expect(EJBTransactionRolledbackException.class);
        expectedMessage("Movement list query is null");

        movementService.getMapByQuery(null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void keepSegment_NULL() {
        thrown.expect(EJBTransactionRolledbackException.class);
        expectedMessage("MovementSegment or SearchValue list is null");

		mapResponseHelper.keepSegment(null, null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void removeTrackMismatches_NULL() {
        thrown.expect(EJBTransactionRolledbackException.class);
        expectedMessage("MovementTrack list or Movement list is null");

		mapResponseHelper.removeTrackMismatches(null, null);
    }

    @Test
    @OperateOnDeployment("movementservice")
	public void testGetMovementListByQuery_WillFailWithNullParameter() {
    	thrown.expect(EJBTransactionRolledbackException.class);
    	expectedMessage("Movement list query is null");

		movementService.getList(null);
	}

	@Test
    @OperateOnDeployment("movementservice")
	public void testGetMovementListByQuery_WillFailNoPaginationSet() {
		thrown.expect(EJBTransactionRolledbackException.class);
		expectedMessage("Pagination in movementlist query is null");

		MovementQuery input = new MovementQuery();
		input.setExcludeFirstAndLastSegment(true);
		movementService.getList(input);
	}

	@Test
    @OperateOnDeployment("movementservice")
	public void testGetMovementListByQuery_WillFailNoSearchCriteria() {
		thrown.expect(EJBTransactionRolledbackException.class);
		expectedMessage("No search criterias in MovementList query");

		MovementQuery input = new MovementQuery();
		input.setExcludeFirstAndLastSegment(true);

		ListPagination listPagination = new ListPagination();
		listPagination.setListSize(new BigInteger("100"));
		listPagination.setPage(new BigInteger("1")); //this can not be 0 or lower....
		input.setPagination(listPagination);

		movementService.getList(input);
	}
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testGetMovementListByQuery() {
		GetMovementListByQueryResponse output;
		MovementQuery input = new MovementQuery();
		input.setExcludeFirstAndLastSegment(true);

    	ListPagination listPagination = new ListPagination();
    	listPagination.setListSize(new BigInteger("100"));
    	listPagination.setPage(new BigInteger("1")); //this can not be 0 or lower....
    	input.setPagination(listPagination);
    	
    	UUID connectID = UUID.randomUUID();
    	UUID connectID2 = UUID.randomUUID();
    	createAndProcess10MovementsFromVarbergGrena(connectID);
    	createAndProcess10MovementsFromVarbergGrena(connectID2);
    	
    	ListCriteria listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID.toString());
    	input.getMovementSearchCriteria().add(listCriteria);
    	
    	output = movementService.getList(input);
    	assertEquals(10, output.getMovement().size());
    	
    	listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID2.toString());
    	input.getMovementSearchCriteria().add(listCriteria);
    	output = movementService.getList(input);
    	assertEquals(20, output.getMovement().size());
    	
    	input.getMovementSearchCriteria().remove(listCriteria);
    	
    	RangeCriteria rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.DATE);
    	rangeCriteria.setFrom(DateUtils.dateToEpochMilliseconds(Instant.now()));
    	rangeCriteria.setTo(DateUtils.dateToEpochMilliseconds(Instant.now().plusSeconds(1800))); //1 800 000 is the time for 6 of the movements in the list  aka 1 800
    	input.getMovementRangeSearchCriteria().add(rangeCriteria);
    	
    	output = movementService.getList(input);
    	assertEquals(6, output.getMovement().size());
    	
    	/* Apparently speed vary to much to be a valid test
    	rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.MOVEMENT_SPEED);
    	rangeCriteria.setFrom("45.552000");
    	rangeCriteria.setTo("45.554000");
    	input.getMovementRangeSearchCriteria().add(rangeCriteria);
    	
    	output = movementService.getList(input);
    	assertEquals(2, output.getMovementList().size(),1);
    	*/
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void testGetMovementListByQuery_WillFailEmptyRangeSearchCriteria() {
		thrown.expect(EJBTransactionRolledbackException.class);

		MovementQuery input = new MovementQuery();
		input.setExcludeFirstAndLastSegment(true);

		ListPagination listPagination = new ListPagination();
		listPagination.setListSize(new BigInteger("100"));
		listPagination.setPage(new BigInteger("1")); //this can not be 0 or lower....
		input.setPagination(listPagination);

		UUID connectID = UUID.randomUUID();
		UUID connectID2 = UUID.randomUUID();
		createAndProcess10MovementsFromVarbergGrena(connectID);
		createAndProcess10MovementsFromVarbergGrena(connectID2);

		ListCriteria listCriteria = new ListCriteria();
		listCriteria.setKey(SearchKey.CONNECT_ID);
		listCriteria.setValue(connectID.toString());
		input.getMovementSearchCriteria().add(listCriteria);

		input.getMovementRangeSearchCriteria().add(new RangeCriteria());

		movementService.getList(input);
	}

	@Test
    @OperateOnDeployment("movementservice")
	public void testGetMinimalMovementListByQuery_WillFailWithNullAsQuery() {
		thrown.expect(EJBTransactionRolledbackException.class);
		expectedMessage("Movement list query is null");

		movementService.getMinimalList(null);
	}

	@Test
    @OperateOnDeployment("movementservice")
	public void testGetMinimalMovementListByQuery_WillFailNoPaginationSet() {
    	thrown.expect(EJBTransactionRolledbackException.class);
		expectedMessage("Pagination in movementlist query is null");

		MovementQuery input = new MovementQuery();
		input.setExcludeFirstAndLastSegment(true);

		movementService.getMinimalList(input);
	}

	@Test
    @OperateOnDeployment("movementservice")
	public void testGetMinimalMovementListByQuery_WillFailNoSearchCriteriaSet() {
		thrown.expect(EJBTransactionRolledbackException.class);

		MovementQuery input = new MovementQuery();
		input.setExcludeFirstAndLastSegment(true);

		ListPagination listPagination = new ListPagination();
		listPagination.setListSize(new BigInteger("100"));
		listPagination.setPage(new BigInteger("1")); //this can not be 0 or lower....
		input.setPagination(listPagination);
		input.getMovementRangeSearchCriteria().add(new RangeCriteria());

		movementService.getMinimalList(input);
	}
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testGetMinimalMovementListByQuery() {
    	GetMovementListByQueryResponse output;
    	
    	MovementQuery input = new MovementQuery();
    	input.setExcludeFirstAndLastSegment(true);

    	ListPagination listPagination = new ListPagination();
    	listPagination.setListSize(new BigInteger("100"));
    	listPagination.setPage(new BigInteger("1")); //this can not be 0 or lower....
    	input.setPagination(listPagination);


    	UUID connectID = UUID.randomUUID();
    	UUID connectID2 = UUID.randomUUID();
    	createAndProcess10MovementsFromVarbergGrena(connectID);
    	createAndProcess10MovementsFromVarbergGrena(connectID2);
    	
    	ListCriteria listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID.toString());
    	input.getMovementSearchCriteria().add(listCriteria);
    	
    	output = movementService.getMinimalList(input);
    	assertEquals(10, output.getMovement().size());
    	
    	listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID2.toString());
    	input.getMovementSearchCriteria().add(listCriteria);
    	output = movementService.getMinimalList(input);
    	assertEquals(20, output.getMovement().size());
    	
    	input.getMovementSearchCriteria().remove(listCriteria);
    	
    	RangeCriteria rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.DATE);
		rangeCriteria.setFrom(DateUtils.dateToEpochMilliseconds(Instant.now()));
		rangeCriteria.setTo(DateUtils.dateToEpochMilliseconds(Instant.now().plusSeconds(1800))); //1 800 000 is the time for 6 of the movements in the list  aka 1 800
    	input.getMovementRangeSearchCriteria().add(rangeCriteria);
    	
    	output = movementService.getMinimalList(input);
    	assertEquals(6, output.getMovement().size());
    }

	@Test
    @OperateOnDeployment("movementservice")
	public void testGetMovementMapByQuery_WillFailWIthNullAsQuery() {
    	thrown.expect(EJBTransactionRolledbackException.class);
    	expectedMessage("Movement list query is null");

		movementService.getMapByQuery(null);
	}

	@Test
    @OperateOnDeployment("movementservice")
	public void testGetMovementMapByQuery_WillFailWIthPaginationNotSupported() {
		thrown.expect(EJBTransactionRolledbackException.class);
		expectedMessage("Pagination not supported in get movement map by query");

		MovementQuery input = new MovementQuery();
		input.setExcludeFirstAndLastSegment(true);

		ListPagination listPagination = new ListPagination();
		listPagination.setListSize(new BigInteger("100"));
		listPagination.setPage(new BigInteger("1")); //this can not be 0 or lower....
		input.setPagination(listPagination);

		movementService.getMapByQuery(input);
	}
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testGetMovementMapByQuery() {
    	GetMovementMapByQueryResponse output;

    	MovementQuery input = new MovementQuery();
    	input.setExcludeFirstAndLastSegment(true);
    	
    	UUID connectID = UUID.randomUUID();
    	UUID connectID2 = UUID.randomUUID();
    	createAndProcess10MovementsFromVarbergGrena(connectID);
    	createAndProcess10MovementsFromVarbergGrena(connectID2);
    	
    	ListCriteria listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID.toString());
    	input.getMovementSearchCriteria().add(listCriteria);
    	
    	output = movementService.getMapByQuery(input);
    	assertEquals(1, output.getMovementMap().size());
    	assertEquals(10, output.getMovementMap().get(0).getMovements().size());
    	
    	listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID2.toString());
    	input.getMovementSearchCriteria().add(listCriteria);
    	output = movementService.getMapByQuery(input);
    	assertEquals(2, output.getMovementMap().size());
    	assertEquals(10, output.getMovementMap().get(1).getMovements().size());
    	
    	input.getMovementSearchCriteria().remove(listCriteria);
    	
    	RangeCriteria rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.DATE);
		rangeCriteria.setFrom(DateUtils.dateToEpochMilliseconds(Instant.now()));
		rangeCriteria.setTo(DateUtils.dateToEpochMilliseconds(Instant.now().plusSeconds(1800))); //1 800 000 is the time for 6 of the movements in the list  aka 1 800
    	input.getMovementRangeSearchCriteria().add(rangeCriteria);
    	
    	output = movementService.getMapByQuery(input);
    	assertEquals(6, output.getMovementMap().get(0).getMovements().size());
    }

	@Test
    @OperateOnDeployment("movementservice")
	public void testGetMinimalMovementMapByQuery_WillFailNoSearchCriteriaSet() {
		thrown.expect(EJBTransactionRolledbackException.class);

		MovementQuery input = new MovementQuery();
		input.setExcludeFirstAndLastSegment(true);

		ListPagination listPagination = new ListPagination();
		listPagination.setListSize(new BigInteger("100"));
		listPagination.setPage(new BigInteger("1")); //this can not be 0 or lower....
		input.setPagination(listPagination);
		input.getMovementRangeSearchCriteria().add(new RangeCriteria());

		movementService.getMapByQuery(input);
	}
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testRemoveTrackMismatches() {
    	UUID connectID = UUID.randomUUID();
    	List<Movement> varbergGrena = createAndProcess10MovementsFromVarbergGrena(connectID);
    	List<MovementTrack> input = new ArrayList<>();
    	MovementTrack movementTrack = new MovementTrack();
    	movementTrack.setId("" + varbergGrena.get(0).getTrack().getId());
    	input.add(movementTrack);
    	
    	movementTrack = new MovementTrack();
    	movementTrack.setId("42");
    	input.add(movementTrack);
    	
    	movementTrack = new MovementTrack();
    	movementTrack.setId("99999999");
    	input.add(movementTrack);

		mapResponseHelper.removeTrackMismatches(input, varbergGrena);
    	
    	assertEquals(1, input.size());
    	
    	try {
			mapResponseHelper.removeTrackMismatches(null, varbergGrena);
    		fail("null as input");
		} catch (RuntimeException e) {
			assertTrue(true);
		}
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testGetLatestMovementsByConnectID() {
        UUID connectID = UUID.randomUUID();
        UUID connectID2 = UUID.randomUUID();
    	List<Movement> control = createAndProcess10MovementsFromVarbergGrena(connectID);
    	createAndProcess10MovementsFromVarbergGrena(connectID2);
    	
    	List<UUID> input = new ArrayList<>();
    	input.add(connectID);
    	
    	List<Movement> output = movementService.getLatestMovementsByConnectIds(input);
    	
    	assertEquals(1, output.size());
    	assertEquals(control.get(9).getId(), output.get(0).getId());
    	
    	input.add(connectID2);
    	output = movementService.getLatestMovementsByConnectIds(input);
    	assertEquals(2, output.size());
    	
    	output = movementService.getLatestMovementsByConnectIds(null);
    	assertTrue(output.isEmpty());
    }

    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/

    private MovementSegment createMovementSegmentDurationHelper(double durationValue, double distanceValue, double speedValue) {
        MovementSegment movementSegment = new MovementSegment();
        movementSegment.setId(String.valueOf(rnd.nextInt(1000)));
        movementSegment.setTrackId("TrackId_42");
        movementSegment.setCategory(SegmentCategoryType.OTHER);
        movementSegment.setDistance(distanceValue);
        movementSegment.setDuration(durationValue);
        movementSegment.setSpeedOverGround(speedValue);
        movementSegment.setCourseOverGround(0.6);
        movementSegment.setWkt("wkt");
        return movementSegment;
    }

    private SearchValue createSearchValueDurationHelper() {
        return new SearchValue(SearchField.SEGMENT_DURATION, "10", "20");
    }

    private SearchValue createSearchValueLengthHelper() {
        return new SearchValue(SearchField.SEGMENT_LENGTH, "1000", "2000");
    }

    private SearchValue createSearchValueSpeedHelper() {
        return new SearchValue(SearchField.SEGMENT_SPEED, "100", "200");
    }

    private List<Movement> createAndProcess10MovementsFromVarbergGrena(UUID connectID) {
    	MovementHelpers movementHelpers = new MovementHelpers(movementService);
    	List<Movement> varbergGrena = movementHelpers.createVarbergGrenaMovements(1, 10, connectID);
    	return varbergGrena;
    }

	private void expectedMessage(String message) {
		thrown.expect(new ThrowableMessageMatcher(new StringContains(message)));
	}
}
