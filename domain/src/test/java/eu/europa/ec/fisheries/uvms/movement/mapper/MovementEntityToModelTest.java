package eu.europa.ec.fisheries.uvms.movement.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Point;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ejb.EJB;
import javax.transaction.SystemException;

import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;

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
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.bean.IncomingMovementBean;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Activity;
import eu.europa.ec.fisheries.uvms.movement.entity.LatestMovement;
import eu.europa.ec.fisheries.uvms.movement.entity.MinimalMovement;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Movementmetadata;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.util.WKTUtil;

@RunWith(Arquillian.class)
public class MovementEntityToModelTest extends TransactionalTests {

	@EJB
    private MovementBatchModelBean movementBatchModelBean;

    @EJB
    private MovementDao movementDao;

    @EJB
    private IncomingMovementBean incomingMovementBean;
	
	@Test
	public void testMovementBaseType() throws MovementDaoException, MovementModelException, MovementDuplicateException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		OffsetDateTime dateStartMovement = DateUtil.nowUTC();
		double lon = 11.641982;
		double lat = 57.632304;
		Movement movement =  movementHelpers.createMovement(lon, lat, 0d, SegmentCategoryType.GAP, connectId, "ONE", dateStartMovement);
		
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
	public void testMapToMovementTypeWithMinimalMovementInput() throws MovementDaoException, MovementModelException, MovementDuplicateException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		OffsetDateTime dateStartMovement = DateUtil.nowUTC();
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
	public void testMapToMovementTypeWithMovementInput() throws MovementDaoException, MovementModelException, MovementDuplicateException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		OffsetDateTime dateStartMovement = DateUtil.nowUTC();
		double lon = 11.641982;
		double lat = 57.632304;
		Movement movement =  movementHelpers.createMovement(lon, lat, 0d, SegmentCategoryType.GAP, connectId, "ONE", dateStartMovement);
		
		MovementType output = MovementEntityToModelMapper.mapToMovementType(movement);
		
		assertEquals(0.0, output.getReportedSpeed(), 0D);
		assertEquals(0.0, output.getReportedCourse(), 0D);
		assertEquals(movement.getGuid(), output.getGuid());
		assertEquals(lat, output.getPosition().getLatitude(), 0D);
		assertEquals(lon, output.getPosition().getLongitude(), 0D);
		assertEquals(connectId, output.getConnectId());
		assertEquals("POINT ( 11.641982 57.632304 )" , output.getWkt());
		assertEquals("MovementMetaData[closestPort=ClosestLocationType[distance=<null>,remoteId=<null>,code=<null>,name=<null>],closestCountry=ClosestLocationType"
				+ "[distance=<null>,remoteId=<null>,code=<null>,name=<null>],areas=[],previousMovementId=<null>,fromSegmentType=<null>]" , output.getMetaData().toString());
		assertTrue(!output.isDuplicate());
		
		movement = null;
		try {
			output = MovementEntityToModelMapper.mapToMovementType(movement);
			fail("null input should result in a nullpointer");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
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
	public void testMapToMovementTypeWithAListOfMovements() throws MovementDaoException, MovementDuplicateException, MovementModelException {
		//Most of the method is tested by testMapToMovementType
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		OffsetDateTime dateStartMovement = DateUtil.nowUTC();
		
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
	public void testMapToMovementTypeWithAListOfLatestMovements() throws MovementDaoException, MovementDuplicateException, MovementModelException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		OffsetDateTime dateStartMovement = DateUtil.nowUTC();
		
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
	public void testMapToMovementSegment() throws MovementDaoException, MovementDuplicateException, MovementModelException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		OffsetDateTime dateStartMovement = DateUtil.nowUTC();
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
	public void testOrderMovementsByConnectId() throws MovementDaoException, MovementDuplicateException, MovementModelException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		List<String> connectId = new ArrayList();
		List<Movement> input = new ArrayList();
		String ID;
		for(int i = 0 ; i < 20 ; i++) {
			ID = UUID.randomUUID().toString();
			connectId.add(ID);
			input.add(movementHelpers.createMovement(Math.random()* 90, Math.random()* 90, 0 , SegmentCategoryType.GAP, ID, "ONE", OffsetDateTime.ofInstant(Instant.now().plusMillis((long)(Math.random() * 5000)), ZoneId.of("UTC"))));
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
	public void testExtractSegments() throws MovementDaoException, MovementDuplicateException, MovementModelException {
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
	public void testExtractTracks() throws MovementDaoException, MovementDuplicateException, MovementModelException, GeometryUtilException, MovementDaoMappingException, SystemException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		ArrayList<Movement> movementList = new ArrayList(movementHelpers.createFishingTourVarberg(1, connectId));
		
		for(Movement move : movementList) {
			incomingMovementBean.processMovement(move);
			assertTrue(move.getProcessed());
		}
		
		List<Segment> input = new ArrayList(MovementEntityToModelMapper.extractSegments(movementList, true));
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
	
}
