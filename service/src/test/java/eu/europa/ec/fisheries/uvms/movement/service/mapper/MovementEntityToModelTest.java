package eu.europa.ec.fisheries.uvms.movement.service.mapper;

import eu.europa.ec.fisheries.schema.movement.v1.*;
import eu.europa.ec.fisheries.uvms.movement.service.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.bean.IncomingMovementBean;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.*;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import javax.ejb.EJB;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class MovementEntityToModelTest extends TransactionalTests {

	@EJB
    private MovementService movementService;

    @EJB
    private MovementDao movementDao;

    @EJB
    private IncomingMovementBean incomingMovementBean;
	
	@Test
    @OperateOnDeployment("movementservice")
	public void testMovementBaseType() {
		MovementHelpers movementHelpers = new MovementHelpers(movementService);
		UUID connectId = UUID.randomUUID();
		Instant dateStartMovement = Instant.now();
		Instant lesTime = dateStartMovement;
		double lon = 11.641982;
		double lat = 57.632304;
		Movement movement =  movementHelpers.createMovement(lon, lat, connectId, "ONE", dateStartMovement);
		
		MovementBaseType output = MovementEntityToModelMapper.mapToMovementBaseType(movement);
		
		assertEquals(0.0, output.getReportedSpeed(), 0D);
		assertEquals(0.0, output.getReportedCourse(), 0D);
		assertEquals(movement.getId().toString(), output.getGuid());
		assertEquals(lat, output.getPosition().getLatitude(), 0D);
		assertEquals(lon, output.getPosition().getLongitude(), 0D);
		assertEquals(connectId.toString(), output.getConnectId());
		assertEquals(lesTime.truncatedTo(ChronoUnit.MILLIS),
				output.getLesReportTime().toInstant().truncatedTo(ChronoUnit.MILLIS));
		try {
			output = MovementEntityToModelMapper.mapToMovementBaseType(null);
			fail("null input should result in a nullpointer");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}
	
	@Test
    @OperateOnDeployment("movementservice")
	public void testMapToMovementTypeWithMinimalMovementInput() {
		UUID connectId = UUID.randomUUID();
		double lon = 11.641982;
		double lat = 57.632304;
		Movement movement = new Movement();
		GeometryFactory gf = new GeometryFactory();
		movement.setLocation(gf.createPoint(new Coordinate(lon, lat)));
		movement.setMovementSource(MovementSourceType.IRIDIUM);
		movement.setMovementType(MovementTypeType.POS);
		MovementConnect movementConnect = new MovementConnect();
		movementConnect.setId(connectId);
        movement.setMovementConnect(movementConnect);
        movement.setTimestamp(Instant.now());
        movement.setId(UUID.randomUUID());
		//movement.setStatus(status);
		MovementType movementType = MovementEntityToModelMapper.mapToMinimalMovementType(movement);
		assertEquals(movement.getId().toString(), movementType.getGuid());
        assertEquals(lat, movementType.getPosition().getLatitude(), 0D);
        assertEquals(lon, movementType.getPosition().getLongitude(), 0D);
        assertEquals(connectId.toString(), movementType.getConnectId());
	}
	
	@Test
    @OperateOnDeployment("movementservice")
	public void testMapToMovementTypeWithMovementInput() {
		MovementHelpers movementHelpers = new MovementHelpers(movementService);
		UUID connectId = UUID.randomUUID();
		Instant dateStartMovement = Instant.now();
		double lon = 11.641982;
		double lat = 57.632304;
		Movement movement =  movementHelpers.createMovement(lon, lat, connectId, "ONE", dateStartMovement);
		
		MovementType output = MovementEntityToModelMapper.mapToMovementType(movement);
		
		assertEquals(0.0, output.getReportedSpeed(), 0D);
		assertEquals(0.0, output.getReportedCourse(), 0D);
		assertEquals(movement.getId().toString(), output.getGuid());
		assertEquals(lat, output.getPosition().getLatitude(), 0D);
		assertEquals(lon, output.getPosition().getLongitude(), 0D);
		assertEquals(connectId.toString(), output.getConnectId());
		assertEquals("POINT ( 11.641982 57.632304 )" , output.getWkt());
		assertTrue(!output.isDuplicate());
		
		movement = null;
		output = MovementEntityToModelMapper.mapToMovementType(movement);
		assertNull(output);
	}
	
	@Test
    @OperateOnDeployment("movementservice")
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
    @OperateOnDeployment("movementservice")
	public void testMapToMovementTypeWithAListOfMovements() {
		//Most of the method is tested by testMapToMovementType
		MovementHelpers movementHelpers = new MovementHelpers(movementService);
		UUID connectId = UUID.randomUUID();
		Instant dateStartMovement = Instant.now();
		
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
    @OperateOnDeployment("movementservice")
	public void testMapToMovementSegment() {
		MovementHelpers movementHelpers = new MovementHelpers(movementService);
		UUID connectId = UUID.randomUUID();
		Instant dateStartMovement = Instant.now();
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
    @OperateOnDeployment("movementservice")
	public void testOrderMovementsByConnectId() {
		MovementHelpers movementHelpers = new MovementHelpers(movementService);
		List<UUID> connectId = new ArrayList<>();
		List<Movement> input = new ArrayList<>();
		UUID ID;
		for(int i = 0 ; i < 20 ; i++) {
			ID = UUID.randomUUID();
			connectId.add(ID);
			input.add(movementHelpers.createMovement(Math.random()* 90, Math.random()* 90, ID, "ONE", Instant.now().plusMillis((long)(Math.random() * 5000))));
		}
		
		Map<UUID, List<Movement>> output = MovementEntityToModelMapper.orderMovementsByConnectId(input);
		
		assertEquals(connectId.size(),output.keySet().size());
		for(UUID s : connectId) {
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
    @OperateOnDeployment("movementservice")
	public void testExtractSegments() {
		MovementHelpers movementHelpers = new MovementHelpers(movementService);
		UUID connectId = UUID.randomUUID();
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
    @OperateOnDeployment("movementservice")
	public void testExtractTracks() {
		MovementHelpers movementHelpers = new MovementHelpers(movementService);
		UUID connectId = UUID.randomUUID();
		ArrayList<Movement> movementList = new ArrayList<>(movementHelpers.createFishingTourVarberg(1, connectId));

		List<Segment> input = new ArrayList<>(MovementEntityToModelMapper.extractSegments(movementList, true));
		List<Track> output = MovementEntityToModelMapper.extractTracks(input);
		
		assertEquals(1, output.size());
		assertEquals(movementList.get(0).getTrack().getDuration(), output.get(0).getDuration(),0D);
		assertEquals(movementList.get(0).getTrack().getId(), output.get(0).getId());
		
		try {
			output = MovementEntityToModelMapper.extractTracks(null);
			fail("Null as invalue");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}
}
