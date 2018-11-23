package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.uvms.movement.service.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;

@RunWith(Arquillian.class)
public class MovementDaoBeanTest extends TransactionalTests {
	
	@EJB
    private MovementBatchModelBean movementBatchModelBean;
	
	@Inject
	private IncomingMovementBean incomingMovementBean;

    @EJB
    private MovementDao movementDao;

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
    @OperateOnDeployment("movementservice")
	public void testGetMovementsByGUID() throws Exception {
		Movement output = movementDao.getMovementByGUID(UUID.randomUUID());
		assertNull(output);
		
		UUID connectId = UUID.randomUUID();
		MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
		Movement move = movementHelpers.createMovement(20D, 20D, connectId, "TEST", Instant.now());
		
		output = movementDao.getMovementByGUID(move.getGuid());
		System.out.println(output);
		assertEquals(move.getGuid(), output.getGuid());
	}
	
	@Test
	@OperateOnDeployment("movementservice")
	public void testGetLatestMovementByConnectIdList() throws Exception {
		UUID connectID = UUID.randomUUID();
		UUID connectID2 = UUID.randomUUID();
		MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
		Movement move1 = movementHelpers.createMovement(20D, 20D, connectID, "TEST", Instant.now());
		Movement move2 = movementHelpers.createMovement(21D, 21D, connectID, "TEST", Instant.now().plusSeconds(1));
		Movement move3 = movementHelpers.createMovement(22D, 22D, connectID2, "TEST", Instant.now().plusSeconds(2));
		
		incomingMovementBean.processMovement(move1);
		incomingMovementBean.processMovement(move2);
		incomingMovementBean.processMovement(move3);
		
		List<UUID> input = new ArrayList<>();
		input.add(connectID);
		List<Movement> output = movementDao.getLatestMovementsByConnectIdList(input);
		assertEquals(1, output.size());
		
		//add the same id again just to see if we get duplicates
		input.add(connectID);
		output = movementDao.getLatestMovementsByConnectIdList(input);
		assertEquals(1, output.size());
		assertEquals(move2.getGuid(), output.get(0).getGuid());
		
		input.add(connectID2);
		output = movementDao.getLatestMovementsByConnectIdList(input);
		assertEquals(2, output.size());
		
		//null as input should return an empty set
		output = movementDao.getLatestMovementsByConnectIdList(null);
		assertTrue(output.isEmpty());
			
		//random input should result in an empty set
		input = new ArrayList<>();
		input.add(UUID.randomUUID());
		output = movementDao.getLatestMovementsByConnectIdList(input);
		assertTrue(output.isEmpty());
	}
	
	@Test
	@OperateOnDeployment("movementservice")
	public void testGetLatestMovementsByConnectID() throws Exception {
		UUID connectID = UUID.randomUUID();
		MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
		Movement move1 = movementHelpers.createMovement(20D, 20D, connectID, "TEST", Instant.now());
		Movement move2 = movementHelpers.createMovement(21D, 21D, connectID, "TEST", Instant.now().plusSeconds(1));
		Movement move3 = movementHelpers.createMovement(22D, 22D, connectID, "TEST42", Instant.now().plusSeconds(2));

        incomingMovementBean.processMovement(move1);
        incomingMovementBean.processMovement(move2);
        incomingMovementBean.processMovement(move3);
		
		List<Movement> output = movementDao.getLatestMovementsByConnectId(connectID, 1);
		assertEquals(1, output.size());
		assertEquals(move3.getGuid(), output.get(0).getGuid());
		
		output = movementDao.getLatestMovementsByConnectId(connectID, 3);
		assertEquals(3, output.size());
		
//		try {
//			output = movementDao.getLatestMovementsByConnectId(connectID, -3);
//			fail("negative value as input should result in an exception");
//		} catch (MovementDomainRuntimeException e) {
//			assertTrue(true);
//		}
		output = movementDao.getLatestMovementsByConnectId(UUID.randomUUID(), 1);
		assertTrue(output.isEmpty());
		
		//funnily enough this is only true if you are only expecting 1 result.......
		output = movementDao.getLatestMovementsByConnectId(UUID.randomUUID(), 2);
		assertTrue(output.isEmpty());
	}

	@Test
	@OperateOnDeployment("movementservice")
	public void testGetLatestMovementsByConnectID_willFail() throws Exception {

		thrown.expect(EJBTransactionRolledbackException.class);

		UUID connectID = UUID.randomUUID();
		MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
		Movement move1 = movementHelpers.createMovement(20D, 20D, connectID, "TEST", Instant.now());
		Movement move2 = movementHelpers.createMovement(21D, 21D, connectID, "TEST", Instant.ofEpochMilli(System.currentTimeMillis() + 100L));
		Movement move3 = movementHelpers.createMovement(22D, 22D, connectID, "TEST42", Instant.ofEpochMilli(System.currentTimeMillis() + 200L));

		incomingMovementBean.processMovement(move1);
		incomingMovementBean.processMovement(move2);
		incomingMovementBean.processMovement(move3);
		
		List<Movement> output = movementDao.getLatestMovementsByConnectId(connectID, 1);
		assertEquals(1, output.size());
		assertEquals(move3.getGuid(), output.get(0).getGuid());

		movementDao.getLatestMovementsByConnectId(connectID, -3);
	}
	
	
	@Test
	@OperateOnDeployment("movementservice")
	public void testIsDateAlreadyInserted() {
		//only testing the no result part since the rest of teh function is tested elsewhere
		List<Movement> output = movementDao.isDateAlreadyInserted(UUID.randomUUID(), Instant.now());
		assertTrue(output.isEmpty());
	}
}
