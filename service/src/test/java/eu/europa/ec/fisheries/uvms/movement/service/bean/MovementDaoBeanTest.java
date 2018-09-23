package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import org.hamcrest.core.StringContains;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.ThrowableMessageMatcher;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

@RunWith(Arquillian.class)
public class MovementDaoBeanTest extends TransactionalTests {
	
	@EJB
    private MovementBatchModelBean movementBatchModelBean;
	
	@Inject
	private IncomingMovementBean incomingMovementBean;

    @EJB
    private MovementDao movementDao;
	
	@EJB
	MovementDao movementDaoBean;

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testGetMovementsByGUID() throws MovementServiceException {
		Movement output = movementDaoBean.getMovementByGUID("");
		assertNull(output);
		
		String connectId = UUID.randomUUID().toString();
		MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
		Movement move = movementHelpers.createMovement(20D, 20D, connectId, "TEST", Instant.now());
		
		output = movementDaoBean.getMovementByGUID(move.getGuid());
		System.out.println(output);
		assertEquals(move.getGuid(), output.getGuid());
	}
	
	@Test
	public void testGetLatestMovementByConnectIdList() throws MovementServiceException {
		String connectID = UUID.randomUUID().toString();
		String connectID2 = UUID.randomUUID().toString();
		MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
		Movement move1 = movementHelpers.createMovement(20D, 20D, connectID, "TEST", Instant.now());
		Movement move2 = movementHelpers.createMovement(21D, 21D, connectID, "TEST", Instant.now().plusSeconds(1));
		Movement move3 = movementHelpers.createMovement(22D, 22D, connectID2, "TEST", Instant.now().plusSeconds(2));
		
		incomingMovementBean.processMovement(move1);
		incomingMovementBean.processMovement(move2);
		incomingMovementBean.processMovement(move3);
		
		List<String> input = new ArrayList<>();
		input.add(connectID);
		List<Movement> output = movementDaoBean.getLatestMovementsByConnectIdList(input);
		assertEquals(1, output.size());
		
		//add the same id again just to see if we get duplicates
		input.add(connectID);
		output = movementDaoBean.getLatestMovementsByConnectIdList(input);
		assertEquals(1, output.size());
		assertEquals(move2.getGuid(), output.get(0).getGuid());
		
		input.add(connectID2);
		output = movementDaoBean.getLatestMovementsByConnectIdList(input);
		assertEquals(2, output.size());
		
		//null as input should return an empty set
		output = movementDaoBean.getLatestMovementsByConnectIdList(null);
		assertTrue(output.isEmpty());
			
		//random input should result in an empty set
		input = new ArrayList<>();
		input.add(UUID.randomUUID().toString());
		output = movementDaoBean.getLatestMovementsByConnectIdList(input);
		assertTrue(output.isEmpty());
	}
	
	@Test
	public void testGetLatestMovementsByConnectID() throws MovementServiceException {
		String connectID = UUID.randomUUID().toString();
		MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
		Movement move1 = movementHelpers.createMovement(20D, 20D, connectID, "TEST", Instant.now());
		Movement move2 = movementHelpers.createMovement(21D, 21D, connectID, "TEST", Instant.now().plusSeconds(1));
		Movement move3 = movementHelpers.createMovement(22D, 22D, connectID, "TEST42", Instant.now().plusSeconds(2));

        incomingMovementBean.processMovement(move1);
        incomingMovementBean.processMovement(move2);
        incomingMovementBean.processMovement(move3);
		
		System.out.println(connectID);
		List<Movement> output = movementDaoBean.getLatestMovementsByConnectId(connectID, 1);
		assertEquals(1, output.size());
		assertEquals(move3.getGuid(), output.get(0).getGuid());
		
		output = movementDaoBean.getLatestMovementsByConnectId(connectID, 3);
		assertEquals(3, output.size());
		
//		try {
//			output = movementDaoBean.getLatestMovementsByConnectId(connectID, -3);
//			fail("negative value as input should result in an exception");
//		} catch (MovementDomainRuntimeException e) {
//			assertTrue(true);
//		}
		output = movementDaoBean.getLatestMovementsByConnectId("0", 1);
		assertTrue(output.isEmpty());
		
		//funnily enough this is only true if you are only expecting 1 result.......
		output = movementDaoBean.getLatestMovementsByConnectId("0", 2);
		assertTrue(output.isEmpty());
	}

	@Test
	public void testGetLatestMovementsByConnectID_willFail() throws MovementServiceException {

		thrown.expect(EJBTransactionRolledbackException.class);

		String connectID = UUID.randomUUID().toString();
		MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
		Movement move1 = movementHelpers.createMovement(20D, 20D, connectID, "TEST", Instant.now());
		Movement move2 = movementHelpers.createMovement(21D, 21D, connectID, "TEST", Instant.ofEpochMilli(System.currentTimeMillis() + 100L));
		Movement move3 = movementHelpers.createMovement(22D, 22D, connectID, "TEST42", Instant.ofEpochMilli(System.currentTimeMillis() + 200L));

		incomingMovementBean.processMovement(move1);
		incomingMovementBean.processMovement(move2);
		incomingMovementBean.processMovement(move3);
		
		System.out.println(connectID);
		List<Movement> output = movementDaoBean.getLatestMovementsByConnectId(connectID, 1);
		assertEquals(1, output.size());
		assertEquals(move3.getGuid(), output.get(0).getGuid());

		movementDaoBean.getLatestMovementsByConnectId(connectID, -3);
	}
	
	@Test
	public void testGetAreaTypeByCode() {
		AreaType areaType = MockData.createAreaType();
		em.persist(areaType);
		em.flush();
		
		AreaType output = movementDaoBean.getAreaTypeByCode("TestAreaType");
		assertEquals(areaType, output);
		
		output = movementDaoBean.getAreaTypeByCode("TestAreaType2");// should result in a null return
		assertNull(output);
		
		try {
			//trying to create a duplicate
			AreaType areaTypeDuplicate = MockData.createAreaType();
			em.persist(areaTypeDuplicate);
			em.flush();
			fail("duplicate namnes should not be allowed"); //thus the catch clause for multiple areas in the method is invalid
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	@EJB
    private AreaDao areaDao;
	
	@Test
	public void testGetAreaByRemoteIDAndCode() {
		AreaType areaType = MockData.createAreaType();
		em.persist(areaType);
		em.flush();

		Area area = MockData.createArea(areaType);
		
		Area createdArea = areaDao.createMovementArea(area);
        areaDao.flushMovementAreas();

		Area output = movementDaoBean.getAreaByRemoteIdAndCode(area.getAreaCode(), null); //remoteId is not used at all  TestAreaCode
		assertEquals(createdArea.getAreaId(), output.getAreaId());
		
		output = movementDaoBean.getAreaByRemoteIdAndCode("ShouldNotExist", null);
		assertNull(output);
	}

	@Test
	public void testGetAreaByRemoteIDAndCode_willFail() {

		thrown.expect(EJBTransactionRolledbackException.class);
		expectedMessage("No valid input parameters to method getAreaByRemoteIdAndCode");

		AreaType areaType = MockData.createAreaType();

		em.persist(areaType);
		em.flush();

		Area area = MockData.createArea(areaType);

		areaDao.createMovementArea(area);
		areaDao.flushMovementAreas();

		movementDaoBean.getAreaByRemoteIdAndCode(null, null);
	}
	
	@Test
	public void testIsDateAlreadyInserted() {
		//only testing the no result part since the rest of teh function is tested elsewhere
		List<Movement> output = movementDaoBean.isDateAlreadyInserted("ShouldNotExist", Instant.now());
		assertTrue(output.isEmpty());
	}

	private void expectedMessage(String message) {
		thrown.expect(new ThrowableMessageMatcher(new StringContains(message)));
	}
}
