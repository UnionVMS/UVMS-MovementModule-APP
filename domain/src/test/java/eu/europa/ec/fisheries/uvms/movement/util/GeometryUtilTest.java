package eu.europa.ec.fisheries.uvms.movement.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ejb.EJB;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.LatLong;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.bean.IncomingMovementBean;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;

@RunWith(Arquillian.class)
public class GeometryUtilTest extends TransactionalTests {

	
	@Test
	public void testGetLineString() {
		Coordinate[] input = new Coordinate[5];
		input[0] = new Coordinate(57.632304, 11.641982);
		input[1] = new Coordinate(57.618091, 11.629729);
		input[2] = new Coordinate(57.531562, 11.762560);
		input[3] = new Coordinate(57.566825, 11.866760);
		input[4] = new Coordinate(57.668133, 11.808038);
		
		Coordinate testCoordinate = new Coordinate(57.531562, 11.762560);
		
		LineString output = GeometryUtil.getLineString(input);
		
		assertTrue(!output.isEmpty());
		
		assertTrue(output.isSimple());
		
		assertTrue(output.isCoordinate(testCoordinate));
		
		assertTrue(!output.isClosed());
		
		for(int i = 0; i < 5; i++)  {
			input[i] = null;
		}
		output = GeometryUtil.getLineString(input);
		
		try {
			String error = output.toString();
			fail("crap input should really kill things");
		} catch (NullPointerException e) {
			assertTrue(true);
			 
		}
		
		
		input = null;
		output = GeometryUtil.getLineString(input);
		
		assertTrue(output.isEmpty());
		
	}
	
	
	@EJB
    private MovementBatchModelBean movementBatchModelBean;

    @EJB
    private MovementDao movementDao;

    @EJB
    private IncomingMovementBean incomingMovementBean;
	
	
	@Test
	public void testGetCoordinateSequenceFromMovements() throws MovementDaoException, MovementModelException, MovementDuplicateException, GeometryUtilException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		Date dateStartMovement = Calendar.getInstance().getTime();
		
		Coordinate[] input = new Coordinate[2];
		input[0] = new Coordinate(11.641982, 57.632304);
		input[1] = new Coordinate(11.629729, 57.618091);
		
		
		Movement start =  movementHelpers.createMovement(input[0].x, input[0].y, 0d, SegmentCategoryType.GAP, connectId, "ONE", dateStartMovement);
		Movement end =  movementHelpers.createMovement(input[1].x, input[1].y, 0d, SegmentCategoryType.GAP, connectId, "ONE", new Date(dateStartMovement.getTime()+10000));
		
		Coordinate[] output = GeometryUtil.getCoordinateSequenceFromMovements(start, end);
		
		assertEquals(input, output);
		
		try {
			output = output = GeometryUtil.getCoordinateSequenceFromMovements(start, null);
			fail("null as input should result in an exception");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testGetLineStringFromMovments() throws MovementDaoException, MovementModelException, MovementDuplicateException, GeometryUtilException {
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		String connectId = UUID.randomUUID().toString();
		
		List<Movement> varbergGrenaTourReverse = movementHelpers.createVarbergGrenaMovements(2, 100, connectId);
		LineString output = GeometryUtil.getLineStringFromMovments(varbergGrenaTourReverse);
		List<Movement> varbergGrenaTourOrdered = movementHelpers.createVarbergGrenaMovements(1, 100, connectId);
		System.out.println(varbergGrenaTourOrdered.size() + " " + output.getNumPoints());
		for(int i = 0 ; i < varbergGrenaTourOrdered.size() ; i++) {
			assertEquals(varbergGrenaTourOrdered.get(i).getLocation().getX(), output.getCoordinateN(i).x, 0);
			assertEquals(varbergGrenaTourOrdered.get(i).getLocation().getY(), output.getCoordinateN(i).y, 0);
		}
		varbergGrenaTourOrdered = null;
		try {
			output = GeometryUtil.getLineStringFromMovments(varbergGrenaTourOrdered);
			fail("Null should result in an exception");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
		
	}
	
}
