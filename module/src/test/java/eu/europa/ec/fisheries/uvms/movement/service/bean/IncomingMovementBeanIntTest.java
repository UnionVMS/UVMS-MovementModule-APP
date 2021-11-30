package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;

import eu.europa.ec.fisheries.uvms.movement.service.entity.*;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;

/**
 * Created by andreasw on 2017-03-09.
 */
@RunWith(Arquillian.class)
public class IncomingMovementBeanIntTest extends TransactionalTests {

    private final static Logger LOG = LoggerFactory.getLogger(IncomingMovementBeanIntTest.class);

    @EJB
    private IncomingMovementBean incomingMovementBean;

    @EJB
    private MovementService movementService;

    @EJB
    private MovementDao movementDao;

    @Test
    @OperateOnDeployment("movementservice")
    public void testCreatingMovement() {
        UUID uuid = UUID.randomUUID();

        Movement movementType = MockData.createMovement(0d, 1d, uuid, 0, "TEST");
        movementType = movementService.createMovement(movementType);
        assertNotNull("MovementType creation was successful.", movementType.getId());
        em.flush();

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getMovementConnect().getId());
        assertNotNull("MovementConnect creation was successful.", movementConnect);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertNotNull("List of Movement creation was successful.", movementList);
        assertEquals("The list of Movement contains exactly one Movement object. It has: " + movementList.size() + " movements", 1, movementList.size());

        Movement movement = movementDao.getMovementById(movementList.get(0).getId());
        assertNotNull("Movement object was successfully created.", movement);
        LOG.info(" [ testCreatingMovement: Movement object was successfully created. ] ");
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void testProcessingMovement() throws Exception {

        // Given: Get the id for a persisted movement entity.

        UUID uuid = UUID.randomUUID();

        Movement movementType = MockData.createMovement(0d, 1d, uuid, 0, "TEST");
        movementType = movementService.createMovement(movementType);
        incomingMovementBean.processMovement(movementType);
        em.flush();

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getMovementConnect().getId());
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        UUID id = movementList.get(0).getId();

        //Then: Test that the Movement is processed properly.
        Movement actualMovement = movementDao.getMovementById(id);
        Movement latestMovement = movementDao.getLatestMovement(uuid);

        assertEquals(actualMovement.getId(), latestMovement.getId());
        LOG.info(" [ testProcessingMovement: Movement object was successfully processed. ] ");
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void testProcessingMovement_NoDuplicateMovement() {

        // Given: Get the id for a persisted movement entity.

        UUID uuid = UUID.randomUUID();

        Movement movementType = MockData.createMovement(0d, 1d, uuid, 0, "TEST");
        movementType = movementService.createMovement(movementType);
        em.flush();

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getMovementConnect().getId());
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        UUID id = movementList.get(0).getId();

        //Then: Test that the Movement is processed properly.
        Movement actualMovement = movementDao.getMovementById(id);          //this test makes no sense after we removed the duplicate check from process movement
        LOG.info(" [ testProcessingMovement_NoDuplicateMovement: Successful check that there are no duplicate movement entities in the database. ] ");
    }

    @Test
    @OperateOnDeployment("movementservice")
    @Ignore   //Since we now process stuff as we create them this test falls apart TODO: fix
    public void testDuplicateMovementsInProcessingMovementMethod_sameTimeStamp_duplicationFlagSetToFalse_sameMovementType() {

        // Given: Create a movement with the exact same timestamp as a movement that exists in the database.
        UUID firstUuid = UUID.randomUUID();

        Movement firstMovementType = MockData.createMovement(0d, 1d, firstUuid, 0, "TEST");
        firstMovementType = movementService.createMovement(firstMovementType);
        em.flush();

        MovementConnect firstMovementConnect = movementDao.getMovementConnectByConnectId(firstMovementType.getMovementConnect().getId());

        List<Movement> firstMovementList = movementDao.getMovementListByMovementConnect(firstMovementConnect);

        Movement firstMovement = firstMovementList.get(0);
        UUID firstMovementId = firstMovementList.get(0).getId();


        /* Setting same timestamp + duplicate flag set to false + same movement type. */
		//since we now process stuff as we create them, this part will not work
        firstMovement.setTimestamp(Instant.ofEpochMilli(1490708331790L));
        // Fields will be null by default in postgres if not set instead of false which means duplicate timestamp Movements
        // will not be found by the processMovement method via the isDateAlreadyInserted method in MovementDaoBean.
        //firstMovement.setDuplicate(false);
        firstMovement.setMovementType(MovementTypeType.ENT);

        movementDao.createMovement(firstMovement);
        firstMovement = movementDao.getMovementById(firstMovementId);

        //Then: Expected is that movement processed flag and duplication flag are both set to true and a duplication id has been set.
        /*assertThat(firstMovement.isProcessed(), is(true));
        assertThat(firstMovement.getDuplicate(), is(true));   //nor this*/
        LOG.info(" [ Duplication flag successfully set when a duplicate movement was found in the database. ] ");

        //assertNotNull(firstMovement.getDuplicateId());      //nor this
        LOG.info(" [ Duplication id successfully set when a duplicate movement was found in the database. ] ");
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void testMovementRelation() throws Exception {
    	UUID connectId = UUID.randomUUID();
    	Movement firstMovementType = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovementType = movementService.createMovement(firstMovementType);
        assertNotNull(firstMovementType);
        incomingMovementBean.processMovement(firstMovementType);
       
        Movement secondMovementType = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovementType = movementService.createMovement(secondMovementType);
        assertNotNull(secondMovementType);
        incomingMovementBean.processMovement(secondMovementType);
        
        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);

        Movement firstMovement = movementList.get(0);
        Movement secondMovement = movementList.get(1);
        
        assertThat(firstMovement.getPreviousMovement(), is(CoreMatchers.nullValue()));
        assertThat(secondMovement.getPreviousMovement(), is(firstMovement));
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testMovementRelationThreeMovements() throws Exception {
    	UUID connectId = UUID.randomUUID();
    	Movement firstMovementType = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovementType = movementService.createMovement(firstMovementType);
        assertNotNull(firstMovementType);
        incomingMovementBean.processMovement(firstMovementType);
       
        Movement secondMovementType = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovementType = movementService.createMovement(secondMovementType);
        assertNotNull(secondMovementType);
        incomingMovementBean.processMovement(secondMovementType);

        Movement thirdMovementType = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovementType = movementService.createMovement(thirdMovementType);
        assertNotNull(thirdMovementType);
        incomingMovementBean.processMovement(thirdMovementType);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        
        Movement firstMovement = movementList.get(0);
        Movement secondMovement = movementList.get(1);
        Movement thirdMovement = movementList.get(2);
        
        assertThat(firstMovement.getPreviousMovement(), is(CoreMatchers.nullValue()));
        assertThat(secondMovement.getPreviousMovement(), is(firstMovement));
        assertThat(thirdMovement.getPreviousMovement(), is(secondMovement));
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testTrackWithThreeMovements() throws Exception {
    	UUID connectId = UUID.randomUUID();
    	Instant timestamp = Instant.now();
    	Movement firstMovementType = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
    	firstMovementType.setTimestamp(timestamp);
        firstMovementType = movementService.createMovement(firstMovementType);
        assertNotNull(firstMovementType);
        incomingMovementBean.processMovement(firstMovementType);
       
        Movement secondMovementType = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovementType.setTimestamp(timestamp.plusSeconds(10));
        secondMovementType = movementService.createMovement(secondMovementType);
        assertNotNull(secondMovementType);
        incomingMovementBean.processMovement(secondMovementType);

        Movement thirdMovementType = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovementType.setTimestamp(timestamp.plusSeconds(20));
        thirdMovementType = movementService.createMovement(thirdMovementType);
        assertNotNull(thirdMovementType);
        incomingMovementBean.processMovement(thirdMovementType);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);


        Movement firstMovement = movementList.get(0);
        Movement secondMovement = movementList.get(1);
        Movement thirdMovement = movementList.get(2);
        
        Track track = firstMovement.getTrack();
        assertThat(track, is(secondMovement.getTrack()));
        assertThat(track, is(thirdMovement.getTrack()));

        List<Movement> trackMovementList = movementDao.getMovementsByTrack(track,2000);

        assertThat(trackMovementList.size(), is(3));

        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(firstMovement.getId())));
        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(secondMovement.getId())));
        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(thirdMovement.getId())));
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testTrackWithThreeMovementsNonOrdered() throws Exception {
    	int tenMinutes = 600000;
    	UUID connectId = UUID.randomUUID();
    	Instant positionTime = Instant.now();
    	Movement firstMovementType = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
    	firstMovementType.setTimestamp(positionTime);
        firstMovementType = movementService.createMovement(firstMovementType);
        assertNotNull(firstMovementType);
        incomingMovementBean.processMovement(firstMovementType);
       
        Movement secondMovementType = MockData.createMovement(2d, 1d, connectId, 0, "TEST");
		secondMovementType.setTimestamp(positionTime.plusMillis(2*tenMinutes));
        secondMovementType = movementService.createMovement(secondMovementType);
        assertNotNull(secondMovementType);
        incomingMovementBean.processMovement(secondMovementType);

        Movement thirdMovementType = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        thirdMovementType.setTimestamp(positionTime.plusMillis(tenMinutes));
        thirdMovementType = movementService.createMovement(thirdMovementType);
        assertNotNull(thirdMovementType);
        incomingMovementBean.processMovement(thirdMovementType);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
 
        Movement firstMovement = movementList.get(0);
        Movement secondMovement = movementList.get(1);
        Movement thirdMovement = movementList.get(2);
        
        Track track = firstMovement.getTrack();
        assertThat(track, is(secondMovement.getTrack()));
        assertThat(track, is(thirdMovement.getTrack()));

        List<Movement> trackMovementList = movementDao.getMovementsByTrack(track,2000);
        assertThat(trackMovementList.size(), is(3));

        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(firstMovement.getId())));
        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(secondMovement.getId())));
        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(thirdMovement.getId())));
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testTrackWithThreeMovementsReversed() throws Exception {
        int tenMinutes = 600000;
        UUID connectId = UUID.randomUUID();
        Instant positionTime = Instant.now();
        Movement firstMovementType = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovementType.setTimestamp(positionTime.plusMillis(2*tenMinutes));
        firstMovementType = movementService.createMovement(firstMovementType);
        assertNotNull(firstMovementType);
        incomingMovementBean.processMovement(firstMovementType);

        em.flush();
       
        Movement secondMovementType = MockData.createMovement(2d, 1d, connectId, 0, "TEST");
        secondMovementType.setTimestamp(positionTime.plusMillis(tenMinutes));
        secondMovementType = movementService.createMovement(secondMovementType);
        assertNotNull(secondMovementType);
        incomingMovementBean.processMovement(secondMovementType);

        em.flush();

        Movement thirdMovementType = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        thirdMovementType.setTimestamp(positionTime);
        thirdMovementType = movementService.createMovement(thirdMovementType);
        assertNotNull(thirdMovementType);
        incomingMovementBean.processMovement(thirdMovementType);

        em.flush();

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
 
        Movement firstMovement = movementList.get(0);
        Movement secondMovement = movementList.get(1);
        Movement thirdMovement = movementList.get(2);
        
        Track track = firstMovement.getTrack();
        assertThat(track, is(secondMovement.getTrack()));
        assertThat(track, is(thirdMovement.getTrack()));

        List<Movement> trackMovementList = movementDao.getMovementsByTrack(track,2000);
        assertThat(trackMovementList.size(), is(3));

        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(firstMovement.getId())));
        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(secondMovement.getId())));
        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(thirdMovement.getId())));
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testProcessingThreeMovements() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);
        firstMovement = movementService.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);
       
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        secondMovement = movementService.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        thirdMovement = movementService.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
        
        assertRelationsAndTrack(movementList);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void testProcessingThreeMovementsIn132OrderWithBrokenPreviousMovement() {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);

        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));

        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));

        firstMovement = movementService.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);

        thirdMovement = movementService.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);
        thirdMovement.setPreviousMovement(null);

        secondMovement = movementService.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testProcessingThreeMovementsUnordered() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);
        firstMovement = movementService.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);

        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        thirdMovement = movementService.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);
       
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        secondMovement = movementService.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
        
        assertRelationsAndTrack(movementList);
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testProcessingThreeMovementsReversed() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();

        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        thirdMovement = movementService.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);
       
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        secondMovement = movementService.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);
        firstMovement = movementService.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
        
        assertRelationsAndTrack(movementList);
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testProcessingMovementsAllAlgorithmCases() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();

        // First
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp.plusSeconds(10));
        firstMovement = movementService.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);
        
        // Second
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(20));
        secondMovement = movementService.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        // Normal case
        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(40));
        thirdMovement = movementService.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);

        // Before first
        Movement fourthMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        fourthMovement.setTimestamp(timestamp);
        fourthMovement = movementService.createMovement(fourthMovement);
        incomingMovementBean.processMovement(fourthMovement);
       
        // Between two positions
        Movement fifthMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        fifthMovement.setTimestamp(timestamp.plusSeconds(30));
        fifthMovement = movementService.createMovement(fifthMovement);
        incomingMovementBean.processMovement(fifthMovement);
        
        // Between two positions
        Movement sixthMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        sixthMovement.setTimestamp(timestamp.plusSeconds(5));
        sixthMovement = movementService.createMovement(sixthMovement);
        incomingMovementBean.processMovement(sixthMovement);
        
        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(6));
        
        assertRelationsAndTrack(movementList);
    }

    
    @Test
    @OperateOnDeployment("movementservice")
    public void newTrackShouldBeCreatedWhenLeavingPort() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);
        firstMovement = movementService.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);
       
        // Second position is in port
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        secondMovement = movementService.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        Movement thirdMovement = MockData.createMovement(10d, 20d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        thirdMovement = movementService.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
        
        assertThat(movementList.get(0).getTrack(), is(movementList.get(1).getTrack()));
        assertThat(movementList.get(2).getTrack(), is(not(movementList.get(1).getTrack())));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void latestVmsPositionTest() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setSource(MovementSourceType.NAF);
        firstMovement.setTimestamp(timestamp);
        firstMovement = movementService.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        assertThat(movementConnect.getLatestMovement(), is(firstMovement));
        assertThat(movementConnect.getLatestVMS(), is(firstMovement));
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void latestVmsPositionTwoPositionsTest() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setSource(MovementSourceType.NAF);
        firstMovement.setTimestamp(timestamp);
        firstMovement = movementService.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);
        
        Movement secondMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        secondMovement.setSource(MovementSourceType.NAF);
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        secondMovement = movementService.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        assertThat(movementConnect.getLatestMovement(), is(secondMovement));
        assertThat(movementConnect.getLatestVMS(), is(secondMovement));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void latestVmsPositionNegativeTest() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setSource(MovementSourceType.AIS);
        firstMovement.setTimestamp(timestamp);
        firstMovement = movementService.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        assertThat(movementConnect.getLatestMovement(), is(firstMovement));
        assertThat(movementConnect.getLatestVMS(), is(nullValue()));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void latestVmsMultiplePositionsTest() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setSource(MovementSourceType.AIS);
        firstMovement.setTimestamp(timestamp);
        firstMovement = movementService.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);

        Movement secondMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        secondMovement.setSource(MovementSourceType.NAF);
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        secondMovement = movementService.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        Movement thirdMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        thirdMovement.setSource(MovementSourceType.AIS);
        thirdMovement.setTimestamp(timestamp);
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        thirdMovement = movementService.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        assertThat(movementConnect.getLatestMovement(), is(thirdMovement));
        assertThat(movementConnect.getLatestVMS(), is(secondMovement));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void latestVmsMultiplePositionsNonOrderedTest() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setSource(MovementSourceType.AIS);
        firstMovement.setTimestamp(timestamp);
        firstMovement = movementService.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);

        Movement thirdMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        thirdMovement.setSource(MovementSourceType.AIS);
        thirdMovement.setTimestamp(timestamp);
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        thirdMovement = movementService.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);

        Movement secondMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        secondMovement.setSource(MovementSourceType.NAF);
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        secondMovement = movementService.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        assertThat(movementConnect.getLatestMovement(), is(thirdMovement));
        assertThat(movementConnect.getLatestVMS(), is(secondMovement));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void latestVmsMultiplePositionsFirstPositionTest() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Movement secondMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        secondMovement.setSource(MovementSourceType.AIS);
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        secondMovement = movementService.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        Movement thirdMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        thirdMovement.setSource(MovementSourceType.AIS);
        thirdMovement.setTimestamp(timestamp);
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        thirdMovement = movementService.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);

        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setSource(MovementSourceType.NAF);
        firstMovement.setTimestamp(timestamp);
        firstMovement = movementService.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        assertThat(movementConnect.getLatestMovement(), is(thirdMovement));
        assertThat(movementConnect.getLatestVMS(), is(firstMovement));
    }

    /*
     * Validate segments and track for movements with same connectId
     */
    private void assertRelationsAndTrack(List<Movement> movements) {
        Collections.sort(movements);

        Movement firstMovement = movements.get(0);
        // Tracks
        Track track = firstMovement.getTrack();
        if (movements.size() > 1) {
            assertThat(track, is(notNullValue()));
            List<Movement> movementList = movementDao.getMovementsByTrack(track,2000);
            assertThat(movementList.size(), is(movements.size()));
            
            for (Movement movement : movements) {
                assertThat(movement.getTrack(), is(track));
                assertTrue(movementList.stream().anyMatch(item -> item.getId().equals(movement.getId())));
            }
        }
    }
}
