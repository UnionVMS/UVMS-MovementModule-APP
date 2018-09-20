package eu.europa.ec.fisheries.uvms.movement.service.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.ejb.EJB;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSegment;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTrack;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.bean.IncomingMovementBean;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Activity;
import eu.europa.ec.fisheries.uvms.movement.service.entity.LatestMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MinimalMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movementmetadata;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

@RunWith(Arquillian.class)
public class MovementEntityToModelTest extends TransactionalTests {

    private Random rnd = new Random();
    
	@EJB
    private MovementBatchModelBean movementBatchModelBean;

    @EJB
    private MovementDao movementDao;

    @EJB
    private IncomingMovementBean incomingMovementBean;
	
	@Test
	public void testMovementBaseType() throws MovementServiceException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		Instant dateStartMovement = DateUtil.nowUTC();
		double lon = 11.641982;
		double lat = 57.632304;
		Movement movement =  movementHelpers.createMovement(lon, lat, 0, SegmentCategoryType.GAP, connectId, "ONE", dateStartMovement);
		
		MovementBaseType output = MovementEntityToModelMapper.mapToMovementBaseType(movement);
		
		assertEquals(0.0, output.getReportedSpeed(), 0D);
		assertEquals(0.0, output.getReportedCourse(), 0D);
		assertEquals(movement.getGuid(), output.getGuid());
		assertEquals(lat, output.getPosition().getLatitude(), 0D);
		assertEquals(lon, output.getPosition().getLongitude(), 0D);
		assertEquals(connectId, output.getConnectId());
		
		try {
			output = MovementEntityToModelMapper.mapToMovementBaseType(null);
			fail("null input should result in a nullpointer");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
		
	}
	
	@Test //TODO make this into an actual test, just need to understand minimal movement first
	public void testMapToMovementTypeWithMinimalMovementInput() {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		Instant dateStartMovement = DateUtil.nowUTC();
		double lon = 11.641982;
		double lat = 57.632304;
		MinimalMovement movement = new MinimalMovement();
		GeometryFactory gf = new GeometryFactory();
		movement.setLocation(gf.createPoint(new Coordinate(lon, lat)));
		movement.setMovementSource(MovementSourceType.IRIDIUM);
		movement.setMovementType(MovementTypeType.POS);
		//movement.setStatus(status);
	}
	@Test
	public void testMapToMovementTypeWithMovementInput() throws MovementServiceException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		Instant dateStartMovement = DateUtil.nowUTC();
		double lon = 11.641982;
		double lat = 57.632304;
		Movement movement =  movementHelpers.createMovement(lon, lat, 0, SegmentCategoryType.GAP, connectId, "ONE", dateStartMovement);
		
		MovementType output = MovementEntityToModelMapper.mapToMovementType(movement);
		
		assertEquals(0.0, output.getReportedSpeed(), 0D);
		assertEquals(0.0, output.getReportedCourse(), 0D);
		assertEquals(movement.getGuid(), output.getGuid());
		assertEquals(lat, output.getPosition().getLatitude(), 0D);
		assertEquals(lon, output.getPosition().getLongitude(), 0D);
		assertEquals(connectId, output.getConnectId());
		assertEquals("POINT ( 11.641982 57.632304 )" , output.getWkt());
//		assertEquals("MovementMetaData[closestPort=ClosestLocationType[distance=<null>,remoteId=<null>,code=<null>,name=<null>],closestCountry=ClosestLocationType"
//				+ "[distance=<null>,remoteId=<null>,code=<null>,name=<null>],areas=[],previousMovementId=<null>,fromSegmentType=<null>]" , output.getMetaData().toString());
		assertTrue(!output.isDuplicate());
		
		movement = null;
		output = MovementEntityToModelMapper.mapToMovementType(movement);
		assertNull(output);
	}
	
	@Test
	public void testMapToActivityType() {
		Activity input = new Activity();
		
		MovementActivityType output = MovementEntityToModelMapper.mapToActivityType(input);
		output.setMessageType(MovementActivityTypeType.COB);
		output.setMessageId("42....");
		assertEquals("MovementActivityType[messageType=COB,messageId=42....,callback=<null>]" ,output.toString());
		
		output = MovementEntityToModelMapper.mapToActivityType(null); //null is basically an empty return 
		assertEquals("MovementActivityType[messageType=<null>,messageId=<null>,callback=<null>]" ,output.toString());
	}
	
	@Test
	public void testMapToMovementMetaData() {
		Movementmetadata input = new Movementmetadata();
		input.setClosestCountryCode("DK");
		input.setClosestCountryDistance(42.42D);
		input.setClosestPortCode("AUCBR");
		input.setClosestPortDistance(1234D);
		
		MovementMetaData output = MovementEntityToModelMapper.mapToMovementMetaData(input);
		
		assertEquals("MovementMetaData[closestPort=ClosestLocationType[distance=1234.0,remoteId=<null>,code=AUCBR,name=<null>],"
				+ "closestCountry=ClosestLocationType[distance=42.42,remoteId=<null>,code=DK,name=<null>],areas=<null>,"
				+ "previousMovementId=<null>,fromSegmentType=<null>]", output.toString());
		
		try {
			output = MovementEntityToModelMapper.mapToMovementMetaData(null);
			fail("null as input");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testMapToMovementTypeWithAListOfMovements() throws MovementServiceException {
		//Most of the method is tested by testMapToMovementType
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		Instant dateStartMovement = DateUtil.nowUTC();
		
		List<Movement> input = movementHelpers.createFishingTourVarberg(1, connectId);
		
		List<MovementType> output = MovementEntityToModelMapper.mapToMovementType(input);
		
		assertEquals(input.size(),output.size());
		
		input = null;
		try {
			output = MovementEntityToModelMapper.mapToMovementType(input);
			fail("Null as input");
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testMapToMovementTypeWithAListOfLatestMovements() throws MovementServiceException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		Instant dateStartMovement = DateUtil.nowUTC();
		
		List<Movement> movementList = movementHelpers.createFishingTourVarberg(1, connectId);
		List<LatestMovement> input = new ArrayList();
		LatestMovement lm;
		for(Movement mov : movementList) {
			lm = new LatestMovement();
			lm.setMovement(mov);
			input.add(lm);
		}
		
		List<MovementType> output = MovementEntityToModelMapper.mapToMovementTypeFromLatestMovement(input);
		
		assertEquals(input.size(),output.size());
		
		input = null;
		try {
			output = MovementEntityToModelMapper.mapToMovementTypeFromLatestMovement(input);
			fail("Null as input");
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testMapToMovementSegment() throws MovementServiceException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		Instant dateStartMovement = DateUtil.nowUTC();
		List<Movement> movementList = movementHelpers.createFishingTourVarberg(1, connectId);
		
		List<Segment> input = new ArrayList<Segment>();
		Segment seg;
		Movement prevMove = null;
		for(Movement mov : movementList) {
			seg = new Segment();
			if(prevMove != null) {
				seg.setFromMovement(prevMove);
			}else {
				seg.setFromMovement(mov);
			}
			seg.setToMovement(mov);
		}
		
		List<MovementSegment> output = MovementEntityToModelMapper.mapToMovementSegment(input);
		assertEquals(input.size(),output.size());
		
		input = null;
		try {
			output = MovementEntityToModelMapper.mapToMovementSegment(input);
			fail("Null as input");
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testOrderMovementsByConnectId() throws MovementServiceException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		List<String> connectId = new ArrayList<>();
		List<Movement> input = new ArrayList<>();
		String ID;
		for(int i = 0 ; i < 20 ; i++) {
			ID = UUID.randomUUID().toString();
			connectId.add(ID);
			input.add(movementHelpers.createMovement(Math.random()* 90, Math.random()* 90, 0 , SegmentCategoryType.GAP, ID, "ONE", Instant.now().plusMillis((long)(Math.random() * 5000))));
		}
		
		Map<String, List<Movement>> output = MovementEntityToModelMapper.orderMovementsByConnectId(input);
		
		assertEquals(connectId.size(),output.keySet().size());
		for(String s : connectId) {
			assertTrue(output.containsKey(s));
		}
		
		try {
			output = MovementEntityToModelMapper.orderMovementsByConnectId(null);
			fail("Null as input");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testExtractSegments() throws MovementServiceException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		List<Movement> movementList = movementHelpers.createFishingTourVarberg(1, connectId);
		//srsly......
		ArrayList<Movement> input = new ArrayList<>(movementList);
		Movement prevMove = null;
		Segment prevSeg = null;
		Segment seg = null;
		for(Movement move : input) {
			seg = new Segment();
			if(prevMove != null) {
				seg.setFromMovement(prevMove);
			}else {
				seg.setFromMovement(move);
			}
			seg.setToMovement(move);
			move.setFromSegment(prevSeg);
			move.setToSegment(seg);
			prevMove = move;
			prevSeg = seg;
		}
		
		List<Segment> output = MovementEntityToModelMapper.extractSegments(input, true);
		assertEquals(input.size() - 1, output.size());
		
		output = MovementEntityToModelMapper.extractSegments(input, false);
		assertEquals(input.size(), output.size());
		
		input.set(42, null);
		try {
			output = MovementEntityToModelMapper.extractSegments(input, false);
			fail("Null in the middle of the input");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
		
		try {
			output = MovementEntityToModelMapper.extractSegments(null, false);
			fail("Null as input");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testExtractTracks() throws MovementServiceException, MovementDomainException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		ArrayList<Movement> movementList = new ArrayList<>(movementHelpers.createFishingTourVarberg(1, connectId));
		for (Movement movement : movementList) {
            incomingMovementBean.processMovement(movement);
        }
		
		for(Movement move : movementList) {
			assertTrue(move.isProcessed());
		}
		
		List<Segment> input = new ArrayList<>(MovementEntityToModelMapper.extractSegments(movementList, true));
		List<MovementTrack> output = MovementEntityToModelMapper.extractTracks(input);
		
		assertEquals(1, output.size());
		assertEquals(movementList.get(0).getTrack().getDuration(), output.get(0).getDuration(),0D);
		assertEquals(movementList.get(0).getTrack().getId().toString(), output.get(0).getId());
		
		try {
			output = MovementEntityToModelMapper.extractTracks(null);
			fail("Null as invalue");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}
	
	/**
     * Update movement metadata with added transitions that is not yet added to
     * movement metadata When the area is same no addition should be made to
     * the areas in metadata when areas are equal
     */
    @Test
    public void testEnrichAreasSameArea() {
        MovementType mappedMovement = MockData.getMappedMovement(2);
        List<Areatransition> transitions = new ArrayList<>();
        transitions.add(MockData.getAreaTransition("AREA1", MovementTypeType.ENT));
        MovementEntityToModelMapper.enrichAreas(mappedMovement, transitions);
        assertEquals(" AreaSize should be 2", 2, mappedMovement.getMetaData().getAreas().size());
    }

    /**
     * Update movement metadata with added transitions that is not yet added to
     * movement metadata When the area is not same an addition should be made
     * to the areas in metadata when areas are not equal
     */
    @Test
    public void testEnrichAreasNotSameArea() {
        MovementType mappedMovement = MockData.getMappedMovement(2);
        List<Areatransition> transitions = new ArrayList<>();
        transitions.add(MockData.getAreaTransition("AREA3", MovementTypeType.ENT));
        MovementEntityToModelMapper.enrichAreas(mappedMovement, transitions);
        assertEquals("AreaSize should be 3", 3,mappedMovement.getMetaData().getAreas().size());
    }
    
    @Test
    public void enrichAreasSameArea() {

        // this test is migrated from the old testsuite
        Instant now = DateUtil.nowUTC();
        double longitude = rnd.nextDouble();
        double latitude = rnd.nextDouble();

        MovementType movementType = MockData.getMappedMovement(2);

        List<Areatransition> areaTransitionList = new ArrayList<>();
        areaTransitionList.add(getAreaTransition("AREA1", MovementTypeType.ENT));
        MovementEntityToModelMapper.enrichAreas(movementType, areaTransitionList);
        assertEquals(" AreaSize should be 2", 2, movementType.getMetaData().getAreas().size());
    }

    @Test
    public void enrichAreasNotSameArea() {

        // this test is migrated from the old testsuite
        Instant now = DateUtil.nowUTC();
        double longitude = rnd.nextDouble();
        double latitude = rnd.nextDouble();

        MovementType movementType = MockData.getMappedMovement(2);

        List<Areatransition> areaTransitionList = new ArrayList<>();
        areaTransitionList.add(getAreaTransition("AREA3", MovementTypeType.ENT));
        MovementEntityToModelMapper.enrichAreas(movementType, areaTransitionList);
        assertEquals(" AreaSize should be 3", 3, movementType.getMetaData().getAreas().size());
    }


    @Test
    public void mapToMovementMetaDataAreaType() {

        // TODO  maybe like this ?
        Areatransition areaTransition = getAreaTransition("AREA51", MovementTypeType.ENT);
        areaTransition.setMovementType(MovementTypeType.MAN);
        areaTransition.setAreatranAreaId(areaTransition.getAreatranAreaId());
        MovementMetaDataAreaType movementMetaDataAreaType = MovementEntityToModelMapper.mapToMovementMetaDataAreaType(areaTransition);
        assertNotNull(movementMetaDataAreaType);
    }
    
    private Areatransition getAreaTransition(String code, MovementTypeType transitionType) {
        Areatransition transition = new Areatransition();
        transition.setMovementType(transitionType);
        transition.setAreatranAreaId(getAreaHelper(code));
        return transition;
    }

    private Area getAreaHelper(String areaCode) {
        Area area = new Area();
        area.setAreaCode(areaCode);
        area.setAreaName(areaCode);
        area.setAreaType(getAraTypeHelper(areaCode));
        return area;
    }

    private AreaType getAraTypeHelper(String name) {
        AreaType areaType = new AreaType();
        areaType.setName(name);
        return areaType;
    }
}
