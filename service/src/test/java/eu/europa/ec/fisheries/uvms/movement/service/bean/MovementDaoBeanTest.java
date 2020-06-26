package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;

import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Movementarea;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.uvms.movement.service.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
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
	private AreaDao areaDao;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testGetMovementsByGUID() throws MovementServiceException {
		Movement output = movementDao.getMovementByGUID(UUID.randomUUID().toString());
		assertNull(output);

		String connectId = UUID.randomUUID().toString();
		MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
		Movement move = movementHelpers.createMovement(20D, 20D, connectId, "TEST", Instant.now());

		output = movementDao.getMovementByGUID(move.getGuid());
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
		input.add(UUID.randomUUID().toString());
		output = movementDao.getLatestMovementsByConnectIdList(input);
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
		output = movementDao.getLatestMovementsByConnectId(UUID.randomUUID().toString(), 1);
		assertTrue(output.isEmpty());

		//funnily enough this is only true if you are only expecting 1 result.......
		output = movementDao.getLatestMovementsByConnectId(UUID.randomUUID().toString(), 2);
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

		List<Movement> output = movementDao.getLatestMovementsByConnectId(connectID, 1);
		assertEquals(1, output.size());
		assertEquals(move3.getGuid(), output.get(0).getGuid());

		movementDao.getLatestMovementsByConnectId(connectID, -3);
	}

	@Test
	public void testFindMovementAreaIdsByAreaRemoteIdAndNameList() {

		final String AREA_TYPE_1 = "AreaType1";
		final String AREA_TYPE_2 = "AreaType2";
		final long REMOTE_ID_10 = 10;
		final long REMOTE_ID_20 = 20;
		final long REMOTE_ID_30 = 30;
		AreaType areaType = MockData.createAreaType(AREA_TYPE_1);
		AreaType areaType2 = MockData.createAreaType(AREA_TYPE_2);
		Area areaA = areaDao.createMovementArea(MockData.createArea(areaType,String.valueOf(REMOTE_ID_10)));
		Area areaB = areaDao.createMovementArea(MockData.createArea(areaType,String.valueOf(REMOTE_ID_10)));
		Area areaC = areaDao.createMovementArea(MockData.createArea(areaType2,String.valueOf(REMOTE_ID_30)));

		String connectId = UUID.randomUUID().toString();
		Instant timestamp = Instant.now();
		Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
		firstMovement.setTimestamp(timestamp);
		Movementarea movementArea1 = MockData.getMovementArea(areaA, firstMovement);
		firstMovement.setMovementareaList(Arrays.asList(movementArea1));
		movementBatchModelBean.createMovement(firstMovement);


		Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
		secondMovement.setTimestamp(timestamp.plusSeconds(10));
		Movementarea movementArea2 = MockData.getMovementArea(areaB, secondMovement);
		secondMovement.setMovementareaList(Arrays.asList(movementArea2));
		movementBatchModelBean.createMovement(secondMovement);

		Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
		thirdMovement.setTimestamp(timestamp.plusSeconds(20));
		Movementarea movementArea3 = MockData.getMovementArea(areaC, thirdMovement);
		thirdMovement.setMovementareaList(Arrays.asList(movementArea3));
		movementBatchModelBean.createMovement(thirdMovement);

		List<eu.europa.ec.fisheries.schema.movement.area.v1.AreaType> areaTypes = new ArrayList<>();
		eu.europa.ec.fisheries.schema.movement.area.v1.AreaType areaType11 = new eu.europa.ec.fisheries.schema.movement.area.v1.AreaType();
		areaType11.setAreaName(AREA_TYPE_1);
		areaType11.setAreaId(REMOTE_ID_10);

		eu.europa.ec.fisheries.schema.movement.area.v1.AreaType areaType22 = new eu.europa.ec.fisheries.schema.movement.area.v1.AreaType();
		areaType22.setAreaName(AREA_TYPE_2);
		areaType22.setAreaId(REMOTE_ID_20);
		areaTypes.add(areaType11);
		areaTypes.add(areaType22);

		List<Long> maIds = movementDao.findMovementAreaIdsByAreaRemoteIdAndNameList(areaTypes);
		assertNotNull("MovementArea ids list wasn't suppose to be null", maIds);
		assertEquals("MovementArea ids list size wasn't as expected" , maIds.size(),2);
	}

	@Test
	public void testCheckMovementExistence() {

		final LocalDate todayLD = LocalDate.now();
		final Instant twoMonthsAgo = todayLD.minusMonths(2).atStartOfDay(ZoneId.systemDefault()).toInstant();
		final Instant lastMonth = todayLD.minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
		final Instant today = todayLD.atStartOfDay(ZoneId.systemDefault()).toInstant();
		final Instant nextMonth = todayLD.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

		final String AREA_TYPE_1 = "AreaType1";
		final String AREA_TYPE_2 = "AreaType2";
		final long REMOTE_ID_10 = 10;
		final long REMOTE_ID_20 = 20;
		AreaType areaType = MockData.createAreaType(AREA_TYPE_1);
		AreaType areaType2 = MockData.createAreaType(AREA_TYPE_2);
		Area areaA = areaDao.createMovementArea(MockData.createArea(areaType,String.valueOf(REMOTE_ID_10)));
		Area areaB = areaDao.createMovementArea(MockData.createArea(areaType,String.valueOf(REMOTE_ID_10)));
		Area areaC = areaDao.createMovementArea(MockData.createArea(areaType2,String.valueOf(REMOTE_ID_20)));

		String connectId = UUID.randomUUID().toString();
		Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
		firstMovement.setTimestamp(today);
		Movementarea movementArea1 = MockData.getMovementArea(areaA, firstMovement);
		firstMovement.setMovementareaList(Arrays.asList(movementArea1));
		movementBatchModelBean.createMovement(firstMovement);

		Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
		secondMovement.setTimestamp(lastMonth);
		Movementarea movementArea2 = MockData.getMovementArea(areaB, secondMovement);
		secondMovement.setMovementareaList(Arrays.asList(movementArea2));
		movementBatchModelBean.createMovement(secondMovement);

		Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
		thirdMovement.setTimestamp(nextMonth);
		Movementarea movementArea3 = MockData.getMovementArea(areaC, thirdMovement);
		thirdMovement.setMovementareaList(Arrays.asList(movementArea3));
		movementBatchModelBean.createMovement(thirdMovement);

		List<eu.europa.ec.fisheries.schema.movement.area.v1.AreaType> areaTypes = new ArrayList<>();
		eu.europa.ec.fisheries.schema.movement.area.v1.AreaType areaType11 = new eu.europa.ec.fisheries.schema.movement.area.v1.AreaType();
		areaType11.setAreaName(AREA_TYPE_1);
		areaType11.setAreaId(REMOTE_ID_10);

		eu.europa.ec.fisheries.schema.movement.area.v1.AreaType areaType22 = new eu.europa.ec.fisheries.schema.movement.area.v1.AreaType();
		areaType22.setAreaName(AREA_TYPE_2);
		areaType22.setAreaId(REMOTE_ID_20);
		areaTypes.add(areaType11);
		areaTypes.add(areaType22);

		List<Long> maIds = movementDao.findMovementAreaIdsByAreaRemoteIdAndNameList(areaTypes);

		boolean result1 = movementDao.checkMovementExistence(connectId, Date.from(lastMonth),Date.from(nextMonth),maIds);
		assertTrue("Movement should be found",result1);
		boolean result2 = movementDao.checkMovementExistence(connectId, Date.from(twoMonthsAgo),Date.from(lastMonth),maIds);
		assertFalse("Where did that movement came from?",result2);
	}

	@Test
	public void testIsDateAlreadyInserted() {
		//only testing the no result part since the rest of teh function is tested elsewhere
		List<Movement> output = movementDao.isDateAlreadyInserted(UUID.randomUUID().toString(), Instant.now());
		assertTrue(output.isEmpty());
	}
}
