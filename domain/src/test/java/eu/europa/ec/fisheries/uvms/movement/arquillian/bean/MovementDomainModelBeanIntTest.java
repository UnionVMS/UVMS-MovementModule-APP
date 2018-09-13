package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import eu.europa.ec.fisheries.schema.movement.search.v1.*;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSegment;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTrack;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.bean.IncomingMovementBean;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementDomainModelBean;
import eu.europa.ec.fisheries.uvms.movement.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchValue;
import eu.europa.ec.fisheries.uvms.movement.model.dto.ListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.model.exception.*;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by thofan on 2017-02-25.
 */

@RunWith(Arquillian.class)
public class MovementDomainModelBeanIntTest extends TransactionalTests {

    private Random rnd = new Random();

    private final static String TEST_USER_NAME = "Arquillian";

    @EJB
    private MovementDomainModelBean movementDomainModelBean;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /******************************************************************************************************************
     *   TEST FUNCTIONS
     ******************************************************************************************************************/

    @Test
    @OperateOnDeployment("normal")
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

        ArrayList<MovementSegment> segments = movementDomainModelBean.filterSegments(movementSegments, searchKeyValuesRange);
        assertEquals(1,segments.size());
    }

    @Test
    @OperateOnDeployment("normal")
    public void getAreas() {
        List<eu.europa.ec.fisheries.schema.movement.area.v1.AreaType> areas = movementDomainModelBean.getAreas();
        assertNotNull(areas);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_0() {
        List<MovementType> movementTypes = movementDomainModelBean.getLatestMovements(0);
        assertNotNull(movementTypes);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_5() {
        List<MovementType> movementTypes =movementDomainModelBean.getLatestMovements(5);
        assertNotNull(movementTypes);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_5000000() {
        List<MovementType> movementTypes =movementDomainModelBean.getLatestMovements(5000000);
        assertNotNull(movementTypes);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_NULL() {
        expectedException.expect(EJBTransactionRolledbackException.class);

        movementDomainModelBean.getLatestMovements(null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_neg5() {
        expectedException.expect(EJBTransactionRolledbackException.class);

        movementDomainModelBean.getLatestMovements(-5);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovementsByConnectIds_crashOnEmpty() {
        expectedException.expect(EJBTransactionRolledbackException.class);
        expectedException.expectMessage("could not extract ResultSet");

        List<String> connectIds = new ArrayList<>();
        movementDomainModelBean.getLatestMovementsByConnectIds(connectIds);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMinimalMovementListByQuery_NULL() throws MovementModelException, MovementDomainException {
        expectedException.expect(EJBTransactionRolledbackException.class);
        expectedException.expectMessage("Movement list query is null");

        movementDomainModelBean.getMinimalMovementListByQuery(null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementByGUID_NULL() {
        assertNull(movementDomainModelBean.getMovementByGUID(null));
    }

     @Test
     @OperateOnDeployment("normal")
    public void getMovementListByAreaAndTimeInterval_NULL() {
        expectedException.expect(EJBTransactionRolledbackException.class);
        movementDomainModelBean.getMovementListByAreaAndTimeInterval(null);
     }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementListByQuery_NULL() throws ParseException, MovementDomainException {
        expectedException.expect(EJBTransactionRolledbackException.class);
        expectedException.expectMessage("Movement list query is null");

        movementDomainModelBean.getMovementListByQuery(null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementMapByQuery_NULL() throws MovementDomainException {
        expectedException.expect(EJBTransactionRolledbackException.class);
        expectedException.expectMessage("Movement list query is null");

        movementDomainModelBean.getMovementMapByQuery(null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void keepSegment_NULL() {
        expectedException.expect(EJBTransactionRolledbackException.class);
        expectedException.expectMessage("MovementSegment or SearchValue list is null");

        movementDomainModelBean.keepSegment(null, null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void removeTrackMismatches_NULL() {
        expectedException.expect(EJBTransactionRolledbackException.class);
        expectedException.expectMessage("MovementTrack list or Movement list is null");

        movementDomainModelBean.removeTrackMismatches(null, null);
    }
    
    @EJB
    MovementBatchModelBean movementBatchModelBean;
    
    @EJB
    MovementDao movementDao;
    
    @EJB
    IncomingMovementBean incomingMovementBean;
    
    @Test
    public void testGetMovementListByQuery() throws ParseException, MovementDomainException {
    	ListResponseDto output;
    	try {
    		output = movementDomainModelBean.getMovementListByQuery(null);
    		fail("Null as input should result in an exception");
		} catch (RuntimeException e) {
			assertTrue(true);
		}

    	MovementQuery input = new MovementQuery();
    	input.setExcludeFirstAndLastSegment(true);
    	
    	try {
    		output = movementDomainModelBean.getMovementListByQuery(input);
    		fail("pagination is not set");
		} catch (RuntimeException e) {
			assertTrue(true);
		}
    	ListPagination listPagination = new ListPagination();
    	listPagination.setListSize(new BigInteger("100"));
    	listPagination.setPage(new BigInteger("1")); //this can not be 0 or lower....
    	input.setPagination(listPagination);
    	
    	try {
    		output = movementDomainModelBean.getMovementListByQuery(input);
    		fail("No searchcriteria in input");
		} catch (RuntimeException e) {
			assertTrue(true);
		}
    	
    	String connectID = UUID.randomUUID().toString();
    	String connectID2 = UUID.randomUUID().toString();
    	createAndProcess10MovementsFromVarbergGrena(connectID);
    	createAndProcess10MovementsFromVarbergGrena(connectID2);
    	
    	ListCriteria listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID);
    	input.getMovementSearchCriteria().add(listCriteria);
    	
    	output = movementDomainModelBean.getMovementListByQuery(input);
    	assertEquals(10, output.getMovementList().size());
    	
    	listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID2);
    	input.getMovementSearchCriteria().add(listCriteria);
    	output = movementDomainModelBean.getMovementListByQuery(input);
    	assertEquals(20, output.getMovementList().size());
    	
    	input.getMovementSearchCriteria().remove(listCriteria);
    	
    	RangeCriteria rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.DATE);
    	rangeCriteria.setFrom(DateUtil.parseUTCDateToString(new Date(System.currentTimeMillis())));
    	rangeCriteria.setTo(DateUtil.parseUTCDateToString(new Date((System.currentTimeMillis() + 1800000L)))); //1 800 000 is the time for 6 of the movements in the list 
    	input.getMovementRangeSearchCriteria().add(rangeCriteria);
    	
    	output = movementDomainModelBean.getMovementListByQuery(input);
    	assertEquals(6, output.getMovementList().size());
    	
    	/* Apparently speed vary to much to be a valid test
    	rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.MOVEMENT_SPEED);
    	rangeCriteria.setFrom("45.552000");
    	rangeCriteria.setTo("45.554000");
    	input.getMovementRangeSearchCriteria().add(rangeCriteria);
    	
    	output = movementDomainModelBean.getMovementListByQuery(input);
    	assertEquals(2, output.getMovementList().size(),1);
    	*/
    	input.getMovementRangeSearchCriteria().add(new RangeCriteria());
    	try {
    		output = movementDomainModelBean.getMovementListByQuery(input);
    		fail("crap as input");
		} catch (EJBTransactionRolledbackException e) {
			assertTrue(true);
		}
    }
    
    @Test
    public void testGetMinimalMovementListByQuery() throws MovementModelException, MovementDomainException {
    	ListResponseDto output;
    	try {
    		output = movementDomainModelBean.getMinimalMovementListByQuery(null);
    		fail("Null as input should result in an exception");
		} catch (RuntimeException e) {
			assertTrue(true);
		}
    	
    	MovementQuery input = new MovementQuery();
    	input.setExcludeFirstAndLastSegment(true);
    	
    	try {
    		output = movementDomainModelBean.getMinimalMovementListByQuery(input);
    		fail("pagination is not set");
		} catch (RuntimeException e) {
			assertTrue(true);
		}
    	ListPagination listPagination = new ListPagination();
    	listPagination.setListSize(new BigInteger("100"));
    	listPagination.setPage(new BigInteger("1")); //this can not be 0 or lower....
    	input.setPagination(listPagination);


/*
//TODO: Look over this.
    	try {
    		output = movementDomainModelBean.getMinimalMovementListByQuery(input);
    		fail("No searchcriteria in input");
		} catch (InputArgumentException e) {
			assertTrue(true);
		}
*/
    	String connectID = UUID.randomUUID().toString();
    	String connectID2 = UUID.randomUUID().toString();
    	createAndProcess10MovementsFromVarbergGrena(connectID);
    	createAndProcess10MovementsFromVarbergGrena(connectID2);
    	
    	ListCriteria listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID);
    	input.getMovementSearchCriteria().add(listCriteria);
    	
    	output = movementDomainModelBean.getMinimalMovementListByQuery(input);
    	assertEquals(10, output.getMovementList().size());
    	
    	listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID2);
    	input.getMovementSearchCriteria().add(listCriteria);
    	output = movementDomainModelBean.getMinimalMovementListByQuery(input);
    	assertEquals(20, output.getMovementList().size());
    	
    	input.getMovementSearchCriteria().remove(listCriteria);
    	
    	RangeCriteria rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.DATE);
    	rangeCriteria.setFrom(DateUtil.parseUTCDateToString(new Date(System.currentTimeMillis())));
    	rangeCriteria.setTo(DateUtil.parseUTCDateToString(new Date((System.currentTimeMillis() + 1800000L)))); //1 800 000 is the time for 6 of the movements in the list 
    	input.getMovementRangeSearchCriteria().add(rangeCriteria);
    	
    	output = movementDomainModelBean.getMinimalMovementListByQuery(input);
    	assertEquals(6, output.getMovementList().size());
    	
    	/*Apparently speed vary to much to be a valid test
    	rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.MOVEMENT_SPEED);
    	rangeCriteria.setFrom("45.5520");
    	rangeCriteria.setTo("45.5540");
    	input.getMovementRangeSearchCriteria().add(rangeCriteria);
    	
    	output = movementDomainModelBean.getMovementListByQuery(input);
    	assertEquals(2, output.getMovementList().size(),1);
    	*/
    	
    	input.getMovementRangeSearchCriteria().add(new RangeCriteria());
    	try {
    		output = movementDomainModelBean.getMinimalMovementListByQuery(input);
    		fail("crap as input");
		} catch (RuntimeException e) {
			assertTrue(true);
		}
    }
    
    @Test
    public void testGetMovementMapByQuery() throws MovementDomainException {
    	List<MovementMapResponseType> output;
    	try {
    		output = movementDomainModelBean.getMovementMapByQuery(null);
    		fail("Null as input should result in an exception");
		} catch (RuntimeException e) {
			assertTrue(true);
		}
    	
    	MovementQuery input = new MovementQuery();
    	input.setExcludeFirstAndLastSegment(true);
    	
    	ListPagination listPagination = new ListPagination();
    	listPagination.setListSize(new BigInteger("100"));
    	listPagination.setPage(new BigInteger("1")); //this can not be 0 or lower....
    	input.setPagination(listPagination);
    	
    	try {
    		output = movementDomainModelBean.getMovementMapByQuery(input);
    		fail("Pagination not supported on this one");
		} catch (RuntimeException e) {
			assertTrue(true);
		}
    	
    	input.setPagination(null);
    	
    	String connectID = UUID.randomUUID().toString();
    	String connectID2 = UUID.randomUUID().toString();
    	createAndProcess10MovementsFromVarbergGrena(connectID);
    	createAndProcess10MovementsFromVarbergGrena(connectID2);
    	
    	ListCriteria listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID);
    	input.getMovementSearchCriteria().add(listCriteria);
    	
    	output = movementDomainModelBean.getMovementMapByQuery(input);
    	assertEquals(1, output.size());
    	assertEquals(10, output.get(0).getMovements().size());
    	
    	listCriteria = new ListCriteria();
    	listCriteria.setKey(SearchKey.CONNECT_ID);
    	listCriteria.setValue(connectID2);
    	input.getMovementSearchCriteria().add(listCriteria);
    	output = movementDomainModelBean.getMovementMapByQuery(input);
    	assertEquals(2, output.size());
    	assertEquals(10, output.get(1).getMovements().size());
    	
    	input.getMovementSearchCriteria().remove(listCriteria);
    	
    	RangeCriteria rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.DATE);
    	rangeCriteria.setFrom(DateUtil.parseUTCDateToString(new Date(System.currentTimeMillis())));
    	rangeCriteria.setTo(DateUtil.parseUTCDateToString(new Date((System.currentTimeMillis() + 1800000L)))); //1 800 000 is the time for 6 of the movements in the list 
    	input.getMovementRangeSearchCriteria().add(rangeCriteria);
    	
    	output = movementDomainModelBean.getMovementMapByQuery(input);
    	assertEquals(6, output.get(0).getMovements().size());
    	
    	/*
    	rangeCriteria = new RangeCriteria();
    	rangeCriteria.setKey(RangeKeyType.MOVEMENT_SPEED);
    	rangeCriteria.setFrom("45.552000");
    	rangeCriteria.setTo("45.554000");
    	input.getMovementRangeSearchCriteria().add(rangeCriteria);
    	
    	output = movementDomainModelBean.getMovementMapByQuery(input);
    	assertEquals(2, output.get(0).getMovements().size(),1);
    	*/
    	
    	input.getMovementRangeSearchCriteria().add(new RangeCriteria());
    	try {
    		output = movementDomainModelBean.getMovementMapByQuery(input);
    		fail("crap as input");
		} catch (RuntimeException e) {
			assertTrue(true);
		}
    }
    
    @Test
    public void testRemoveTrackMismatches() throws MovementDomainException {
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
    	
    	movementDomainModelBean.removeTrackMismatches(input, varbergGrena);
    	
    	assertEquals(1, input.size());
    	
    	try {
    		movementDomainModelBean.removeTrackMismatches(null, varbergGrena);
    		fail("null as input");
		} catch (RuntimeException e) {
			assertTrue(true);
		}
    }
    
    @Test
    public void testGetLatestMovementsByConnectID() throws MovementDomainException {
    	String connectID = UUID.randomUUID().toString();
    	String connectID2 = UUID.randomUUID().toString();
    	List<Movement> control = createAndProcess10MovementsFromVarbergGrena(connectID);
    	createAndProcess10MovementsFromVarbergGrena(connectID2);
    	
    	List<String> input = new ArrayList<>();
    	input.add(connectID);
    	
    	List<MovementType> output = movementDomainModelBean.getLatestMovementsByConnectIds(input);
    	
    	assertEquals(1, output.size());
    	assertEquals(control.get(9).getGuid(), output.get(0).getGuid());
    	
    	input.add(connectID2);
    	output = movementDomainModelBean.getLatestMovementsByConnectIds(input);
    	assertEquals(2, output.size());
    	
    	output = movementDomainModelBean.getLatestMovementsByConnectIds(null);
    	assertTrue(output.isEmpty());
    }
    
    @Test
    public void getMovementByGuid() throws MovementDomainException {
    	String connectID = UUID.randomUUID().toString();
    	List<Movement> control = createAndProcess10MovementsFromVarbergGrena(connectID);
    	
    	MovementType output = movementDomainModelBean.getMovementByGUID(control.get(5).getGuid());
    	assertEquals(control.get(5).getInternalReferenceNumber(), output.getInternalReferenceNumber());
    	assertEquals(control.get(5).getHeading(), output.getReportedCourse());

    	output = movementDomainModelBean.getMovementByGUID("42");
    	assertNull(output);
    }
    
    @EJB
    private AreaDao areaDao;
    
    @Test
    public void testGetAreas() throws MovementDomainException {
    	AreaType areaType = new AreaType();
		String input = "TestAreaType";
		areaType.setName(input);
		areaType.setUpdatedTime(new Date(System.currentTimeMillis()));
		areaType.setUpdatedUser("TestUser");
		em.persist(areaType);
		em.flush();
		
		Area area = new Area();
		area.setAreaName("TestArea");
		area.setAreaCode(input);
		area.setRemoteId("TestRemoteId");
		area.setAreaUpdattim(new Date(System.currentTimeMillis()));
		area.setAreaUpuser("TestUser");
		area.setAreaType(areaType);
		
		Area createdArea = areaDao.createMovementArea(area);
        areaDao.flushMovementAreas();
        
        List<eu.europa.ec.fisheries.schema.movement.area.v1.AreaType> output = movementDomainModelBean.getAreas();
        assertEquals(1, output.size());
        
        area = new Area();
        area.setAreaName("TestArea2");
		area.setAreaCode(input + "2");
		area.setRemoteId("TestRemoteId2");
		area.setAreaUpdattim(new Date(System.currentTimeMillis()));
		area.setAreaUpuser("TestUser2");
		area.setAreaType(areaType);
		
		createdArea = areaDao.createMovementArea(area);
        areaDao.flushMovementAreas();
        
        output = movementDomainModelBean.getAreas();
        assertEquals(2, output.size());
        
    }
    
    @Test
    public void testGetMovementListByAreaAndTimeInterval() throws MovementDomainException {
    	String connectID = UUID.randomUUID().toString();
    	
    	MovementAreaAndTimeIntervalCriteria movementAreaAndTimeIntervalCriteria = new MovementAreaAndTimeIntervalCriteria();
    	movementAreaAndTimeIntervalCriteria.setFromDate(DateUtil.parseUTCDateToString(new Date(System.currentTimeMillis())));
    	movementAreaAndTimeIntervalCriteria.setToDate(DateUtil.parseUTCDateToString(new Date(System.currentTimeMillis() + 2100000L))); //2 100 000 is the time for 7 of the movements in the list

    	AreaType areaType = new AreaType();
		String input = "TestAreaType";
		areaType.setName(input);
		areaType.setUpdatedTime(new Date(System.currentTimeMillis()));
		areaType.setUpdatedUser("TestUser");
		em.persist(areaType);
		em.flush();
		
		Area area = new Area();
		area.setAreaName("TestArea");
		area.setAreaCode(input);
		area.setRemoteId("TestRemoteId");
		area.setAreaUpdattim(new Date(System.currentTimeMillis()));
		area.setAreaUpuser("TestUser");
		area.setAreaType(areaType);
		
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
    	List<Movement> varbergGrena = movementHelpers.createVarbergGrenaMovements(1, 10, connectID);
		
		Area createdArea = areaDao.createMovementArea(area);
        areaDao.flushMovementAreas();
    	List<Movementarea> movementAreaList = new ArrayList<Movementarea>();
    	Movementarea movementarea = new Movementarea();
    	movementarea.setMovareaUpdattim(new Date());
    	movementarea.setMovareaUpuser("TestUser");
    	movementarea.setMovareaAreaId(area);
    	movementarea.setMovareaMoveId(varbergGrena.get(6));
    	movementAreaList.add(movementarea);
    	varbergGrena.get(6).setMovementareaList(movementAreaList);
    	varbergGrena.get(7).setMovementareaList(movementAreaList);
    	
    	for(Movement move : varbergGrena) {
    		incomingMovementBean.processMovement(move);
    	}
        
    	em.persist(movementarea);
    	em.flush();
    	
    	movementAreaAndTimeIntervalCriteria.setAreaCode(input);

		List<MovementType> output = movementDomainModelBean.getMovementListByAreaAndTimeInterval(movementAreaAndTimeIntervalCriteria);
    	
    	assertEquals(1, output.size());
    	assertEquals(varbergGrena.get(6).getGuid(), output.get(0).getGuid());
    	
    	try {
    		output = movementDomainModelBean.getMovementListByAreaAndTimeInterval(null);
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

    private List<Movement> createAndProcess10MovementsFromVarbergGrena(String connectID) throws MovementDomainException {
    	MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
    	List<Movement> varbergGrena = movementHelpers.createVarbergGrenaMovements(1, 10, connectID);
    	for(Movement move : varbergGrena) {
    		incomingMovementBean.processMovement(move);
    	}
    	return varbergGrena;
    }
}
