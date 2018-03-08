package eu.europa.ec.fisheries.uvms.movement.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.ejb.EJB;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.peertopark.java.geocalc.Coordinate;
import com.peertopark.java.geocalc.Point;

import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.bean.IncomingMovementBean;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.dto.SegmentCalculations;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;


@RunWith(Arquillian.class)
public class CalculationUtilTest extends TransactionalTests {

	
	@Test
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
	public void testCalculateCourse() {
		double startLon = 11.695033;
		double startLat = 57.678582;
		
		double endLon = 11.703635;
		double endLat = 57.600009;
		
		double course = CalculationUtil.calculateCourse(startLat, startLon, endLat, endLon);
		
		assertEquals(176D, course,1D);
	}
	
	
	@Test
	public void testGetNauticalMilesFromMeters() {
		double startLon = 11.695033;
		double startLat = 57.678582;
		
		double endLon = 11.703635;
		double endLat = 57.600009;
		
		double distanceNautical = CalculationUtil.getNauticalMilesFromMeter(CalculationUtil.calculateDistance(startLat, startLon, endLat, endLon));
		
		assertEquals(4.73, distanceNautical,0.01D);
		
	}
	
	@EJB
    private MovementBatchModelBean movementBatchModelBean;

    @EJB
    private MovementDao movementDao;

    @EJB
    private IncomingMovementBean incomingMovementBean;
	
	@Test
	public void testGetPositionCalculations () throws MovementDaoException, MovementModelException, MovementDuplicateException, GeometryUtilException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		Date dateStartMovement = Calendar.getInstance().getTime();
		
		double startLon = 11.695033;
		double startLat = 57.678582;
		
		double endLon = 11.703635;
		double endLat = 57.600009;
		
		Movement start =  movementHelpers.createMovement(startLon, startLat, 0d, SegmentCategoryType.GAP, connectId, "ONE", dateStartMovement);
		Movement end =  movementHelpers.createMovement(endLon, endLat, 0d, SegmentCategoryType.GAP, connectId, "ONE", new Date(dateStartMovement.getTime()+10000));
		
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
		} catch (GeometryUtilException e) {
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
}
