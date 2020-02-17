package eu.europa.ec.fisheries.uvms.movement.service.util;

import eu.europa.ec.fisheries.uvms.movement.service.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.bean.IncomingMovementBean;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.SegmentCalculations;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class CalculationUtilTest extends TransactionalTests {

	@EJB
	private MovementService movementService;

	@EJB
	private MovementDao movementDao;

	@EJB
	private IncomingMovementBean incomingMovementBean;

	@Test
	@OperateOnDeployment("movementservice")
	public void bigIntegerToUUIDTest(){
		UUID uuid = UUID.randomUUID();
		BigInteger bigInt = CalculationUtil.convertToBigInteger(uuid);
		UUID output = CalculationUtil.convertFromBigInteger(bigInt);
		assertEquals(uuid, output);
	}

	@Test
    @OperateOnDeployment("movementservice")
	public void testCalculateDistance() {
		double startLon = 11.695033;
		double startLat = 57.678582;
		
		double endLon = 11.703635;
		double endLat = 57.600009;
		
		double distance = CalculationUtil.calculateDistance(startLat, startLon, endLat, endLon);
		
		assertEquals(8752D, distance,1D);
		
		//equator
		startLon = 11.695033;
		startLat = 7.678582;
		
		endLon = 11.703635;
		endLat = -7.600009;
		
		distance = CalculationUtil.calculateDistance(startLat, startLon, endLat, endLon);
		
		assertEquals(1699000D, distance,500D);
		
		//dateline
		startLon = 179.695033;
		startLat = 7.678582;
		
		endLon = -179.703635;
		endLat = -7.600009;
		
		distance = CalculationUtil.calculateDistance(startLat, startLon, endLat, endLon);
		
		assertEquals(1700000D, distance,500D);
		
		//North of the north pole, questionable......
		startLon = 179.695033;
		startLat = 91.678582;
		
		endLon = -179.703635;
		endLat = 92.600009;
		
		distance = CalculationUtil.calculateDistance(startLat, startLon, endLat, endLon);
		
		assertEquals(102500, distance,500D);
		
		try {
			distance = CalculationUtil.calculateDistance(startLat, startLon, null, endLon);
			fail("We schould never get here due to null pointer");
		}
		catch (NullPointerException e) {
			assertTrue(true);
		}
	}

	@Test
    @OperateOnDeployment("movementservice")
	public void testCalculateCourse() {
		double startLon = 11.695033;
		double startLat = 57.678582;
		
		double endLon = 11.703635;
		double endLat = 57.600009;
		
		double course = CalculationUtil.calculateCourse(startLat, startLon, endLat, endLon);
		
		assertEquals(176D, course,1D);
	}
	
	@Test
    @OperateOnDeployment("movementservice")
	public void testGetNauticalMilesFromMeters() {
		double startLon = 11.695033;
		double startLat = 57.678582;
		
		double endLon = 11.703635;
		double endLat = 57.600009;
		
		double distanceNautical = CalculationUtil.getNauticalMilesFromMeter(CalculationUtil.calculateDistance(startLat, startLon, endLat, endLon));
		
		assertEquals(4.73, distanceNautical,0.01D);
	}
	
	@Test
    @OperateOnDeployment("movementservice")
	public void testGetPositionCalculations () {
		MovementHelpers movementHelpers = new MovementHelpers(movementService);
		UUID connectId = UUID.randomUUID();
		Instant dateStartMovement = Instant.now();
		
		double startLon = 11.695033;
		double startLat = 57.678582;
		
		double endLon = 11.703635;
		double endLat = 57.600009;
		
		Movement start =  movementHelpers.createMovement(startLon, startLat, connectId, "ONE", dateStartMovement);
		Movement end =  movementHelpers.createMovement(endLon, endLat, connectId, "ONE", dateStartMovement.plusSeconds(10));
		
		SegmentCalculations segmentCalc = CalculationUtil.getPositionCalculations(start, end);
		
		assertNotNull(segmentCalc);
		assertEquals(1701D, segmentCalc.getAvgSpeed(),1D);
		assertEquals(10D, segmentCalc.getDurationBetweenPoints(),0D);
		
		//0 time difference
		end.setTimestamp(dateStartMovement);
		
		segmentCalc = CalculationUtil.getPositionCalculations(start, end);
		assertTrue(Double.isInfinite(segmentCalc.getAvgSpeed()));
		
		//null location
		start.setLocation(null);
		try {
			segmentCalc = CalculationUtil.getPositionCalculations(start, end);
			fail("Should not work due tu null location");
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		//null movement
		try {
			segmentCalc = CalculationUtil.getPositionCalculations(start, null);
			fail();
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}

	@Test
	@OperateOnDeployment("movementservice")
	public void testGetPositionCalculationsMillisecondTimespan () {
		MovementHelpers movementHelpers = new MovementHelpers(movementService);
		UUID connectId = UUID.randomUUID();
		Instant dateStartMovement = Instant.now();

		double startLon = 15.4479166666667;
		double startLat = 54.9316516666667;

		double endLon = 15.4479433333333;
		double endLat = 54.9314733333333;

		Movement start =  movementHelpers.createMovement(startLon, startLat, connectId, "ONE", dateStartMovement);
		Movement end =  movementHelpers.createMovement(endLon, endLat, connectId, "ONE", dateStartMovement.plusMillis(42));

		SegmentCalculations segmentCalc = CalculationUtil.getPositionCalculations(start, end);

		assertNotNull(segmentCalc);
		assertFalse(Double.isInfinite(segmentCalc.getAvgSpeed()));
		assertEquals(0.042D, segmentCalc.getDurationBetweenPoints(),0);

	}
	
	@Test
    @OperateOnDeployment("movementservice")
    public void north() {
        Double expected = 0.0;

        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = 0.0;
        Double thisLat = 1.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);

        assertEquals(expected, actual);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void northEast() {
        Double expected = 44.99563645534488;

        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = 1.0;
        Double thisLat = 1.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);

        assertEquals(expected, actual);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void east() {
        Double expected = 90.0;

        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = 1.0;
        Double thisLat = 0.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);

        assertEquals(expected, actual);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void southEast() {
        Double expected = 135.00436354465512;

        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = 1.0;
        Double thisLat = -1.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);

        assertEquals(expected, actual);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void south() {
        Double expected = 180.0;

        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = 0.0;
        Double thisLat = -1.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);
        assertEquals(expected, actual);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void southWest() {
        Double expected = 224.99563645534485;

        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = -1.0;
        Double thisLat = -1.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);
        assertEquals(expected, actual);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void west() {
        Double expected = 270.0;

        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = -1.0;
        Double thisLat = 0.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);
        assertEquals(expected, actual);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void noMovement() {
        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = 0.0;
        Double thisLat = 0.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);
        assertNull(actual);
    }
}
