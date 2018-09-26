package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;

import eu.europa.ec.fisheries.uvms.movement.service.entity.LatestMovement;
import org.hamcrest.core.StringContains;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.ThrowableMessageMatcher;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.RangeCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.RangeKeyType;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByAreaAndTimeIntervalResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSegment;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTrack;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchValue;

/**
 * Transferred from domain layer. Should be integrated with MovementServiceIntTest.
 */

@RunWith(Arquillian.class)
public class MovementDomainModelBeanIntTest extends TransactionalTests {

    private Random rnd = new Random();

	@EJB
	private MovementBatchModelBean movementBatchModelBean;
	
	@Inject
	private IncomingMovementBean incomingMovementBean;

	@EJB
	private MovementDao movementDao;
	
    @EJB
    private AreaDao areaDao;
	
	@EJB
    private MovementService movementService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /******************************************************************************************************************
     *   TEST FUNCTIONS
     ******************************************************************************************************************/

    @Test
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

        ArrayList<MovementSegment> segments = movementService.filterSegments(movementSegments, searchKeyValuesRange);
        assertEquals(1,segments.size());
    }

    @Test
    public void getAreas() {
        List<Area> areas = movementService.getAreas();
        assertNotNull(areas);
    }

    @Test
    public void getLatestMovements_0() {
        List<LatestMovement> movements = movementService.getLatestMovements(0);
        assertNotNull(movements);
    }

    @Test
    public void getLatestMovements_5() {
        List<LatestMovement> movements = movementService.getLatestMovements(5);
        assertNotNull(movements);
    }

    @Test
    public void getLatestMovements_5000000() {
        List<LatestMovement> movements = movementService.getLatestMovements(5000000);
        assertNotNull(movements);
    }

    @Test
    public void getLatestMovements_NULL() {
        thrown.expect(EJBTransactionRolledbackException.class);

        movementService.getLatestMovements(null);
    }

    @Test
    public void getLatestMovements_neg5() {
        thrown.expect(EJBTransactionRolledbackException.class);

        movementService.getLatestMovements(-5);
    }

    @Test
    public void getMinimalMovementListByQuery_NULL() throws MovementServiceException {
        thrown.expect(EJBTransactionRolledbackException.class);
        expectedMessage("Movement list query is null");

        movementService.getMinimalList(null);
    }

    @Test
    public void getMovementListByAreaAndTimeInterval_NULL() {
        thrown.expect(EJBTransactionRolledbackException.class);
        movementService.getMovementListByAreaAndTimeInterval(null);
     }

    @Test
    public void getMovementListByQuery_NULL() throws MovementServiceException {
        thrown.expect(EJBTransactionRolledbackException.class);
		expectedMessage("Movement list query is null");

        movementService.getList(null);
    }

    @Test
    public void getMovementMapByQuery_NULL() throws MovementServiceException {
        thrown.expect(EJBTransactionRolledbackException.class);
        expectedMessage("Movement list query is null");

        movementService.getMapByQuery(null);
    }

    @Test
    public void keepSegment_NULL() {
        thrown.expect(EJBTransactionRolledbackException.class);
        expectedMessage("MovementSegment or SearchValue list is null");

        movementService.keepSegment(null, null);
    }

    @Test
    public void removeTrackMismatches_NULL() {
        thrown.expect(EJBTransactionRolledbackException.class);
        expectedMessage("MovementTrack list or Movement list is null");

        movementService.removeTrackMismatches(null, null);
    }

    @Test
	public void testGetMovementListByQuery_WillFailWithNullParameter() throws MovementServiceException {

    	thrown.expect(EJBTransactionRolledbackException.class);
    	expectedMessage("Movement list query is null");

		movementService.getList(null);
	}

	@Test
	public void testGetMovementListByQuery_WillFailNoPaginationSet() throws MovementServiceException {

		thrown.expect(EJBTransactionRolledbackException.class);
		expectedMessage("Pagination in movementlist query is null");

		MovementQuery input = new MovementQuery();
		input.setExcludeFirstAndLastSegment(true);
		movementService.getList(input);
	}

	@Test
	public void testGetMovementListByQuery_WillFailNoSearchCriteria() throws MovementServiceException {

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
    public void testGetMovementListByQuery() throws MovementServiceException {
		GetMovementListByQueryResponse output;
		MovementQuery input = new MovementQuery();
		input.setExcludeFirstAndLastSegment(true);

    	ListPagination listPagination = new ListPagination();
    	listPagination.setListSize(new BigInteger("100"));
    	listPagination.setPage(new BigInteger("1")); //this can not be 0 or lower....
    	input.setPagination(listPagination);
    	
    	String connectID = UUID.randomUUID().toString();
    	String connectID2 = UUID.randomUUID().toString();
    	createAndProcess10MovementsFromVarbergGrena(connectID);
    	createAndProcess10MovementsFromVarbergGrena(connectID2);
    	
    	ListCriteria listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID);
    	input.getMovementSearchCriteria().add(listCriteria);
    	
    	output = movementService.getList(input);
    	assertEquals(10, output.getMovement().size());
    	
    	listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID2);
    	input.getMovementSearchCriteria().add(listCriteria);
    	output = movementService.getList(input);
    	assertEquals(20, output.getMovement().size());
    	
    	input.getMovementSearchCriteria().remove(listCriteria);
    	
    	RangeCriteria rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.DATE);
    	rangeCriteria.setFrom(DateUtil.parseUTCDateToString(DateUtil.nowUTC()));
    	rangeCriteria.setTo(DateUtil.parseUTCDateToString(DateUtil.nowUTC().plusSeconds(1800))); //1 800 000 is the time for 6 of the movements in the list  aka 1 800
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
    public void testGetMovementListByQuery_WillFailEmptyRangeSearchCriteria() throws MovementServiceException {

		thrown.expect(EJBTransactionRolledbackException.class);

		MovementQuery input = new MovementQuery();
		input.setExcludeFirstAndLastSegment(true);

		ListPagination listPagination = new ListPagination();
		listPagination.setListSize(new BigInteger("100"));
		listPagination.setPage(new BigInteger("1")); //this can not be 0 or lower....
		input.setPagination(listPagination);

		String connectID = UUID.randomUUID().toString();
		String connectID2 = UUID.randomUUID().toString();
		createAndProcess10MovementsFromVarbergGrena(connectID);
		createAndProcess10MovementsFromVarbergGrena(connectID2);

		ListCriteria listCriteria = new ListCriteria();
		listCriteria.setKey(SearchKey.CONNECT_ID);
		listCriteria.setValue(connectID);
		input.getMovementSearchCriteria().add(listCriteria);

		input.getMovementRangeSearchCriteria().add(new RangeCriteria());

		movementService.getList(input);
	}

	@Test
	public void testGetMinimalMovementListByQuery_WillFailWithNullAsQuery() throws MovementServiceException {
		thrown.expect(EJBTransactionRolledbackException.class);
		expectedMessage("Movement list query is null");

		movementService.getMinimalList(null);
	}

	@Test
	public void testGetMinimalMovementListByQuery_WillFailNoPaginationSet() throws MovementServiceException {

    	thrown.expect(EJBTransactionRolledbackException.class);
		expectedMessage("Pagination in movementList query is null");

		MovementQuery input = new MovementQuery();
		input.setExcludeFirstAndLastSegment(true);

		movementService.getMinimalList(input);
	}

	@Test
	public void testGetMinimalMovementListByQuery_WillFailNoSearchCriteriaSet() throws MovementServiceException {

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
    public void testGetMinimalMovementListByQuery() throws MovementServiceException {
    	GetMovementListByQueryResponse output;
    	
    	MovementQuery input = new MovementQuery();
    	input.setExcludeFirstAndLastSegment(true);

    	ListPagination listPagination = new ListPagination();
    	listPagination.setListSize(new BigInteger("100"));
    	listPagination.setPage(new BigInteger("1")); //this can not be 0 or lower....
    	input.setPagination(listPagination);


    	String connectID = UUID.randomUUID().toString();
    	String connectID2 = UUID.randomUUID().toString();
    	createAndProcess10MovementsFromVarbergGrena(connectID);
    	createAndProcess10MovementsFromVarbergGrena(connectID2);
    	
    	ListCriteria listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID);
    	input.getMovementSearchCriteria().add(listCriteria);
    	
    	output = movementService.getMinimalList(input);
    	assertEquals(10, output.getMovement().size());
    	
    	listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID2);
    	input.getMovementSearchCriteria().add(listCriteria);
    	output = movementService.getMinimalList(input);
    	assertEquals(20, output.getMovement().size());
    	
    	input.getMovementSearchCriteria().remove(listCriteria);
    	
    	RangeCriteria rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.DATE);
    	rangeCriteria.setFrom(DateUtil.parseUTCDateToString(DateUtil.nowUTC()));
    	rangeCriteria.setTo(DateUtil.parseUTCDateToString(DateUtil.nowUTC().plusSeconds(1800))); //1 800 000 is the time for 6 of the movements in the list aka 1800 seconds
    	input.getMovementRangeSearchCriteria().add(rangeCriteria);
    	
    	output = movementService.getMinimalList(input);
    	assertEquals(6, output.getMovement().size());
    	
    	/*Apparently speed vary to much to be a valid test
    	rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.MOVEMENT_SPEED);
    	rangeCriteria.setFrom("45.5520");
    	rangeCriteria.setTo("45.5540");
    	input.getMovementRangeSearchCriteria().add(rangeCriteria);
    	
    	output = movementService.getList(input);
    	assertEquals(2, output.getMovementList().size(),1);
    	*/
    }

	@Test
	public void testGetMovementMapByQuery_WillFailWIthNullAsQuery() throws MovementServiceException {

    	thrown.expect(EJBTransactionRolledbackException.class);
    	expectedMessage("Movement list query is null");

		movementService.getMapByQuery(null);
	}

	@Test
	public void testGetMovementMapByQuery_WillFailWIthPaginationNotSupported() throws MovementServiceException {

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
    public void testGetMovementMapByQuery() throws MovementServiceException {

    	GetMovementMapByQueryResponse output;

    	MovementQuery input = new MovementQuery();
    	input.setExcludeFirstAndLastSegment(true);
    	
    	String connectID = UUID.randomUUID().toString();
    	String connectID2 = UUID.randomUUID().toString();
    	createAndProcess10MovementsFromVarbergGrena(connectID);
    	createAndProcess10MovementsFromVarbergGrena(connectID2);
    	
    	ListCriteria listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID);
    	input.getMovementSearchCriteria().add(listCriteria);
    	
    	output = movementService.getMapByQuery(input);
    	assertEquals(1, output.getMovementMap().size());
    	assertEquals(10, output.getMovementMap().get(0).getMovements().size());
    	
    	listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID2);
    	input.getMovementSearchCriteria().add(listCriteria);
    	output = movementService.getMapByQuery(input);
    	assertEquals(2, output.getMovementMap().size());
    	assertEquals(10, output.getMovementMap().get(1).getMovements().size());
    	
    	input.getMovementSearchCriteria().remove(listCriteria);
    	
    	RangeCriteria rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.DATE);
    	rangeCriteria.setFrom(DateUtil.parseUTCDateToString(DateUtil.nowUTC()));
    	rangeCriteria.setTo(DateUtil.parseUTCDateToString(DateUtil.nowUTC().plusSeconds(1800) )); //1 800 000 is the time for 6 of the movements in the list aka 1800 seconds
    	input.getMovementRangeSearchCriteria().add(rangeCriteria);
    	
    	output = movementService.getMapByQuery(input);
    	assertEquals(6, output.getMovementMap().get(0).getMovements().size());
    	
    	/*
    	rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.MOVEMENT_SPEED);
    	rangeCriteria.setFrom("45.552000");
    	rangeCriteria.setTo("45.554000");
    	input.getMovementRangeSearchCriteria().add(rangeCriteria);
    	
    	output = movementService.getMovementMapByQuery(input);
    	assertEquals(2, output.get(0).getMovements().size(),1);
    	*/
    }

	@Test
	public void testGetMinimalMovementMapByQuery_WillFailNoSearchCriteriaSet() throws MovementServiceException {

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
    public void testRemoveTrackMismatches() throws MovementServiceException {
    	String connectID = UUID.randomUUID().toString();
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
    	
    	movementService.removeTrackMismatches(input, varbergGrena);
    	
    	assertEquals(1, input.size());
    	
    	try {
    		movementService.removeTrackMismatches(null, varbergGrena);
    		fail("null as input");
		} catch (RuntimeException e) {
			assertTrue(true);
		}
    }
    
    @Test
    public void testGetLatestMovementsByConnectID() throws MovementServiceException {
    	String connectID = UUID.randomUUID().toString();
    	String connectID2 = UUID.randomUUID().toString();
    	List<Movement> control = createAndProcess10MovementsFromVarbergGrena(connectID);
    	createAndProcess10MovementsFromVarbergGrena(connectID2);
    	
    	List<String> input = new ArrayList<>();
    	input.add(connectID);
    	
    	List<Movement> output = movementService.getLatestMovementsByConnectIds(input);
    	
    	assertEquals(1, output.size());
    	assertEquals(control.get(9).getGuid(), output.get(0).getGuid());
    	
    	input.add(connectID2);
    	output = movementService.getLatestMovementsByConnectIds(input);
    	assertEquals(2, output.size());
    	
    	output = movementService.getLatestMovementsByConnectIds(null);
    	assertTrue(output.isEmpty());
    }
    
    @Test
    public void testGetAreas() {
        int areasBefore = movementService.getAreas().size();
        
    	AreaType areaType = new AreaType();
		String input = "TestAreaType";
		areaType.setName(input);
		areaType.setUpdatedTime(DateUtil.nowUTC());
		areaType.setUpdatedUser("TestUser");
		
		Area area = new Area();
		area.setAreaName("TestArea");
		area.setAreaCode(input);
		area.setRemoteId("TestRemoteId");
		area.setAreaUpdattim(DateUtil.nowUTC());
		area.setAreaUpuser("TestUser");
		area.setAreaType(areaType);
		
		Area createdArea = areaDao.createMovementArea(area);
        
        List<Area> output = movementService.getAreas();
        assertEquals(areasBefore + 1, output.size());
        
        area = new Area();
        area.setAreaName("TestArea2");
		area.setAreaCode(input + "2");
		area.setRemoteId("TestRemoteId2");
		area.setAreaUpdattim(DateUtil.nowUTC());
		area.setAreaUpuser("TestUser2");
		area.setAreaType(areaType);
		
		createdArea = areaDao.createMovementArea(area);
        
        output = movementService.getAreas();
        assertEquals(areasBefore + 2, output.size());
        
    }
    
    @Test
    public void testGetMovementListByAreaAndTimeInterval() throws MovementServiceException {
    	String connectID = UUID.randomUUID().toString();
    	
    	MovementAreaAndTimeIntervalCriteria movementAreaAndTimeIntervalCriteria = new MovementAreaAndTimeIntervalCriteria();
    	movementAreaAndTimeIntervalCriteria.setFromDate(DateUtil.parseUTCDateToString(DateUtil.nowUTC()));
    	movementAreaAndTimeIntervalCriteria.setToDate(DateUtil.parseUTCDateToString(DateUtil.nowUTC().plusSeconds(2100))); //2 100 000 is the time for 7 of the movements in the list aka 2100 seconds

    	AreaType areaType = new AreaType();
		String input = "TestAreaType";
		areaType.setName(input);
		areaType.setUpdatedTime(DateUtil.nowUTC());
		areaType.setUpdatedUser("TestUser");
		
		Area area = new Area();
		area.setAreaName("TestArea");
		area.setAreaCode(input);
		area.setRemoteId("TestRemoteId");
		area.setAreaUpdattim(DateUtil.nowUTC());
		area.setAreaUpuser("TestUser");
		area.setAreaType(areaType);
		
		MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
    	List<Movement> varbergGrena = movementHelpers.createVarbergGrenaMovements(1, 10, connectID);
		
		Area createdArea = areaDao.createMovementArea(area);
    	List<Movementarea> movementAreaList = new ArrayList<Movementarea>();
    	Movementarea movementarea = new Movementarea();
    	movementarea.setMovareaUpdattim(DateUtil.nowUTC());
    	movementarea.setMovareaUpuser("TestUser");
    	movementarea.setMovareaAreaId(area);
    	movementarea.setMovareaMoveId(varbergGrena.get(6));
    	movementAreaList.add(movementarea);
    	varbergGrena.get(6).setMovementareaList(movementAreaList);
    	varbergGrena.get(7).setMovementareaList(movementAreaList);
    	
    	movementAreaAndTimeIntervalCriteria.setAreaCode(input);

		GetMovementListByAreaAndTimeIntervalResponse output = movementService.getMovementListByAreaAndTimeInterval(movementAreaAndTimeIntervalCriteria);
    	
    	assertEquals(1, output.getMovement().size());
    	assertEquals(varbergGrena.get(6).getGuid(), output.getMovement().get(0).getGuid());
    	
    	try {
    		output = movementService.getMovementListByAreaAndTimeInterval(null);
    		fail("null as input should result in an exception");
		} catch (EJBTransactionRolledbackException e) {
			assertTrue(true);
		}
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

    private List<Movement> createAndProcess10MovementsFromVarbergGrena(String connectID) throws MovementServiceException {
    	MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
    	List<Movement> varbergGrena = movementHelpers.createVarbergGrenaMovements(1, 10, connectID);
    	for (Movement movement : varbergGrena) {
            incomingMovementBean.processMovement(movement);
        }
    	return varbergGrena;
    }

	private void expectedMessage(String message) {
		thrown.expect(new ThrowableMessageMatcher(new StringContains(message)));
	}
}
