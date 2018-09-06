package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ejb.EJB;
import javax.transaction.SystemException;

import eu.europa.ec.fisheries.uvms.movement.dao.exception.MissingMovementConnectException;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.bean.IncomingMovementBean;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDaoBean;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.AreaDaoException;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;

@RunWith(Arquillian.class)
public class MovementDaoBeanTest extends TransactionalTests {
	
	@EJB
    private MovementBatchModelBean movementBatchModelBean;

    @EJB
    private MovementDao movementDao;

    @EJB
    private IncomingMovementBean incomingMovementBean;
	
	@EJB
	MovementDaoBean movementDaoBean;
	
	@Test
	public void testGetMovementsByGUID() throws MovementDaoException, MovementModelException, MissingMovementConnectException, GeometryUtilException, MovementDaoMappingException, SystemException {
		Movement output = movementDaoBean.getMovementsByGUID(""); 
		assertNull(output);
		
		String connectId = UUID.randomUUID().toString();
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		Movement move = movementHelpers.createMovement(20D, 20D, 0, SegmentCategoryType.OTHER, connectId, "TEST", new Date(System.currentTimeMillis()));
		move.setGuid();
		incomingMovementBean.processMovement(move);
		em.flush();
		
		output = movementDaoBean.getMovementsByGUID(move.getGuid());
		System.out.println(output);
		assertEquals(move.getGuid(), output.getGuid());
	}
	
	@Test
	public void testGetLatestMovementByConnectIdList() throws MovementDaoException, MissingMovementConnectException, MovementModelException, GeometryUtilException, MovementDaoMappingException, SystemException {
		String connectID = UUID.randomUUID().toString();
		String connectID2 = UUID.randomUUID().toString();
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		Movement move1 = movementHelpers.createMovement(20D, 20D, 0, SegmentCategoryType.OTHER, connectID, "TEST", new Date(System.currentTimeMillis()));
		Movement move2 = movementHelpers.createMovement(21D, 21D, 0, SegmentCategoryType.OTHER, connectID, "TEST", new Date(System.currentTimeMillis() + 100L));
		Movement move3 = movementHelpers.createMovement(22D, 22D, 0, SegmentCategoryType.OTHER, connectID2, "TEST", new Date(System.currentTimeMillis() + 200L));
		move1.setGuid();
		incomingMovementBean.processMovement(move1);
		move2.setGuid();
		incomingMovementBean.processMovement(move2);
		move3.setGuid();
		incomingMovementBean.processMovement(move3);
		em.flush();
		
		List<String> input = new ArrayList<String>();
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
		input = new ArrayList<String>();
		input.add(UUID.randomUUID().toString());
		output = movementDaoBean.getLatestMovementsByConnectIdList(input);
		assertTrue(output.isEmpty());
	}
	
	@Test
	public void testGetLatestMovementsByConnectID() throws MovementDaoException, MovementModelException, MissingMovementConnectException,  GeometryUtilException, MovementDaoMappingException, SystemException {
		String connectID = UUID.randomUUID().toString();
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		Movement move1 = movementHelpers.createMovement(20D, 20D, 0, SegmentCategoryType.OTHER, connectID, "TEST", new Date(System.currentTimeMillis()));
		Movement move2 = movementHelpers.createMovement(21D, 21D, 0, SegmentCategoryType.OTHER, connectID, "TEST", new Date(System.currentTimeMillis() + 100L));
		Movement move3 = movementHelpers.createMovement(22D, 22D, 0, SegmentCategoryType.OTHER, connectID, "TEST42", new Date(System.currentTimeMillis() + 200L));
		move1.setGuid();
		incomingMovementBean.processMovement(move1);
		move2.setGuid();
		incomingMovementBean.processMovement(move2);
		move3.setGuid();
		incomingMovementBean.processMovement(move3);
		em.flush();
		
		System.out.println(connectID);
		List<Movement> output = movementDaoBean.getLatestMovementsByConnectId(connectID, 1);
		assertEquals(1, output.size());
		assertEquals(move3.getGuid(), output.get(0).getGuid());
		
		output = movementDaoBean.getLatestMovementsByConnectId(connectID, 3);
		assertEquals(3, output.size());
		
		try {
			output = movementDaoBean.getLatestMovementsByConnectId(connectID, -3);
			fail("negative value as input should result in an exception");
		} catch (MovementDaoException e) {
			assertTrue(true);
		}
		//should result in a no result output akka null
		output = movementDaoBean.getLatestMovementsByConnectId("0", 1);
		assertNull(output);
		
		//funnily enough this is only true if you are only expecting 1 result.......
		output = movementDaoBean.getLatestMovementsByConnectId("0", 2);
		assertTrue(output.isEmpty());
	}
	
	@Test
	public void testGetAreaTypeByCode() throws MovementDaoException {
		AreaType areaType = new AreaType();
		areaType.setName("TestAreaType");
		areaType.setUpdatedTime(new Date(System.currentTimeMillis()));
		areaType.setUpdatedUser("TestUser");
		em.persist(areaType);
		em.flush();
		
		AreaType output = movementDaoBean.getAreaTypeByCode("TestAreaType");
		assertEquals(areaType, output);
		
		output = movementDaoBean.getAreaTypeByCode("TestAreaType2");// should result in a null return
		assertNull(output);
		
		
		try {
			//trying to create a duplicate
			AreaType areaTypeDuplicate = new AreaType();
			areaTypeDuplicate.setName("TestAreaType");
			areaTypeDuplicate.setUpdatedTime(new Date(System.currentTimeMillis()));
			areaTypeDuplicate.setUpdatedUser("TestUser");
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
	public void testGetAreaByRemoteIDAndCode() throws MovementDaoException, AreaDaoException {
		AreaType areaType = new AreaType();
		String input = "TestAreaType";
		areaType.setName(input);
		areaType.setUpdatedTime(new Date(System.currentTimeMillis()));
		areaType.setUpdatedUser("TestUser");
		em.persist(areaType);
		em.flush();
		
		Area area = new Area();
		area.setAreaName("TestArea");
		area.setAreaCode(input);
		area.setRemoteId("TestRemoteId");
		area.setAreaUpdattim(new Date(System.currentTimeMillis()));
		area.setAreaUpuser("TestUser");
		area.setAreaType(areaType);
		
		Area createdArea = areaDao.createMovementArea(area);
        areaDao.flushMovementAreas();

		
		Area output = movementDaoBean.getAreaByRemoteIdAndCode(input, null); //remoteId is not used at all  TestAreaCode
		assertEquals(area.getAreaId(), output.getAreaId());
		
		output = movementDaoBean.getAreaByRemoteIdAndCode("ShouldNotExist", null);
		assertNull(output);
		
		output = movementDaoBean.getAreaByRemoteIdAndCode(null, null);
		assertNull(output);
	}
	
	@Test
	public void testIsDateAlreadyInserted() {
		//only testing the no result part since the rest of teh function is tested elsewhere
		List<Movement> output = movementDaoBean.isDateAlreadyInserted("ShouldNotExist", new Date(System.currentTimeMillis()));
		assertTrue(output.isEmpty());
	}
}
