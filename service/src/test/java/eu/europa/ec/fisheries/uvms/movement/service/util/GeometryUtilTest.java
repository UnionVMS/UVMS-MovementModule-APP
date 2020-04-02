package eu.europa.ec.fisheries.uvms.movement.service.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.bean.IncomingMovementBean;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

@RunWith(Arquillian.class)
public class GeometryUtilTest extends TransactionalTests {

	@EJB
	private MovementBatchModelBean movementBatchModelBean;

	@EJB
	private MovementDao movementDao;

	@EJB
	private IncomingMovementBean incomingMovementBean;

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

	@Test
	public void testGetCoordinateSequenceFromMovements() throws MovementServiceException {
		MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
		String connectId = UUID.randomUUID().toString();
		Instant dateStartMovement = DateUtil.nowUTC();
		
		Coordinate[] input = new Coordinate[2];
		input[0] = new Coordinate(11.641982, 57.632304);
		input[1] = new Coordinate(11.629729, 57.618091);
		
		
		Movement start =  movementHelpers.createMovement(input[0].x, input[0].y, connectId, "ONE", dateStartMovement);
		Movement end =  movementHelpers.createMovement(input[1].x, input[1].y, connectId, "ONE", dateStartMovement.plusSeconds(10));
		
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
	public void testGetLineStringFromMovments() throws MovementServiceException {
		MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
		String connectId = UUID.randomUUID().toString();
		
		List<Movement> varbergGrenaTourReverse = movementHelpers.createVarbergGrenaMovements(2, 100, connectId);
		LineString output = GeometryUtil.getLineStringFromMovements(varbergGrenaTourReverse);
		List<Movement> varbergGrenaTourOrdered = movementHelpers.createVarbergGrenaMovements(1, 100, connectId);
		System.out.println(varbergGrenaTourOrdered.size() + " " + output.getNumPoints());
		for(int i = 0 ; i < varbergGrenaTourOrdered.size() ; i++) {
			assertEquals(varbergGrenaTourOrdered.get(i).getLocation().getX(), output.getCoordinateN(i).x, 0);
			assertEquals(varbergGrenaTourOrdered.get(i).getLocation().getY(), output.getCoordinateN(i).y, 0);
		}
		varbergGrenaTourOrdered = null;
		try {
			output = GeometryUtil.getLineStringFromMovements(varbergGrenaTourOrdered);
			fail("Null should result in an exception");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}
}
