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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.inject.Inject;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movementmetadata;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.util.MovementComparator;

/**
 * Created by andreasw on 2017-03-09.
 */
@RunWith(Arquillian.class)
public class IncomingMovementBeanIntTest extends TransactionalTests {

    private final static Logger LOG = LoggerFactory.getLogger(IncomingMovementBeanIntTest.class);

    @EJB
    private IncomingMovementBean incomingMovementBean;

    @EJB
    private MovementBatchModelBean movementBatchModelBean;

    @EJB
    private MovementDao movementDao;
    
    @Inject
    private AreaDao areaDao;

    @Test
    public void testCreatingMovement() {
        String uuid = UUID.randomUUID().toString();

        Movement movementType = MockData.createMovement(0d, 1d, uuid, 0, "TEST");
        movementType = movementBatchModelBean.createMovement(movementType);
        assertNotNull("MovementType creation was successful.", movementType.getGuid());
        em.flush();

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getMovementConnect().getValue());
        assertNotNull("MovementConnect creation was successful.", movementConnect);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertNotNull("List of Movement creation was successful.", movementList);
        assertTrue("The list of Movement contains exactly one Movement object. It has: " + movementList.size() + " movements", movementList.size() == 1);

        Movement movement = movementDao.getMovementById(movementList.get(0).getId());
        assertNotNull("Movement object was successfully created.", movement);
        LOG.info(" [ testCreatingMovement: Movement object was successfully created. ] ");
    }

    @Test
    public void testProcessingMovement() throws MovementServiceException {

        // Given: Get the id for a persisted movement entity.

        String uuid = UUID.randomUUID().toString();

        Movement movementType = MockData.createMovement(0d, 1d, uuid, 0, "TEST");
        movementType = movementBatchModelBean.createMovement(movementType);
        incomingMovementBean.processMovement(movementType);
        em.flush();

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getMovementConnect().getValue());
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        Long id = movementList.get(0).getId();

        //Then: Test that the Movement is processed properly.
        Movement actualMovement = movementDao.getMovementById(id);
        boolean actualProcessedValue = actualMovement.isProcessed();

        assertThat(actualProcessedValue, is(true));
        LOG.info(" [ testProcessingMovement: Movement object was successfully processed. ] ");
    }

    @Test
    public void testProcessingMovement_NoDuplicateMovement() {

        // Given: Get the id for a persisted movement entity.

        String uuid = UUID.randomUUID().toString();

        Movement movementType = MockData.createMovement(0d, 1d, uuid, 0, "TEST");
        movementType = movementBatchModelBean.createMovement(movementType);
        em.flush();

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getMovementConnect().getValue());
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        Long id = movementList.get(0).getId();

        //Then: Test that the Movement is processed properly.
        Movement actualMovement = movementDao.getMovementById(id);
        boolean actualDuplicateValue = actualMovement.getDuplicate();

        assertThat(actualDuplicateValue, is(false));
        LOG.info(" [ testProcessingMovement_NoDuplicateMovement: Successful check that there are no duplicate movement entities in the database. ] ");
    }

    @Test
    @Ignore   //Since we now process stuff as we create them this test falls apart TODO: fix
    public void testDuplicateMovementsInProcessingMovementMethod_sameTimeStamp_duplicationFlagSetToFalse_sameMovementType() {

        // Given: Create a movement with the exact same timestamp as a movement that exists in the database.
        String firstUuid = UUID.randomUUID().toString();

        Movement firstMovementType = MockData.createMovement(0d, 1d, firstUuid, 0, "TEST");
        firstMovementType = movementBatchModelBean.createMovement(firstMovementType);
        em.flush();

        MovementConnect firstMovementConnect = movementDao.getMovementConnectByConnectId(firstMovementType.getMovementConnect().getValue());

        List<Movement> firstMovementList = movementDao.getMovementListByMovementConnect(firstMovementConnect);

        Movement firstMovement = firstMovementList.get(0);
        Long firstMovementId = firstMovementList.get(0).getId();


        /* Setting same timestamp + duplicate flag set to false + same movement type. */
		//since we now process stuff as we create them, this part will not work
        firstMovement.setTimestamp(Instant.ofEpochMilli(1490708331790L));
        // Fields will be null by default in postgres if not set instead of false which means duplicate timestamp Movements
        // will not be found by the processMovement method via the isDateAlreadyInserted method in MovementDaoBean.
        firstMovement.setDuplicate(false);
        firstMovement.setMovementType(MovementTypeType.ENT);

        movementDao.createMovement(firstMovement);
        firstMovement = movementDao.getMovementById(firstMovementId);

        //Then: Expected is that movement processed flag and duplication flag are both set to true and a duplication id has been set.
        assertThat(firstMovement.isProcessed(), is(true));
        assertThat(firstMovement.getDuplicate(), is(true));   //nor this
        LOG.info(" [ Duplication flag successfully set when a duplicate movement was found in the database. ] ");

        assertNotNull(firstMovement.getDuplicateId());      //nor this
        LOG.info(" [ Duplication id successfully set when a duplicate movement was found in the database. ] ");
    }

    /**
     * Same area exists in previous movement and is set as an Entry. This should
     * be a simple Position
     */
    @Test
    public void testPopulateTransitions_SAME_ENT() {

        Movement current = MockData.getCurrentMovement(1);
        Movement previous = MockData.getPreviousMovement(1, MovementTypeType.ENT);

        List<Areatransition> transitions = incomingMovementBean.populateTransitions(current, previous);

        assertNotNull(transitions);
        assertEquals(1, transitions.size());
        assertEquals(MovementTypeType.POS, transitions.get(0).getMovementType());
    }

    /**
     * Same area exists in previous movement and is set as an Position. This
     * should be a simple Position
     */
    @Test
    public void testPopulateTransitions_SAME_POS() {

        Movement current = MockData.getCurrentMovement(1);
        Movement previous = MockData.getPreviousMovement(1, MovementTypeType.POS);

        List<Areatransition> transitions = incomingMovementBean.populateTransitions(current, previous);

        assertNotNull(transitions);
        assertEquals(1, transitions.size());
        assertEquals(MovementTypeType.POS, transitions.get(0).getMovementType());
    }

    /**
     * new Area does not exist in the previous movments areatransitions. Previos
     * area should be added as an exit in the current areatransitions
     */
    @Test
    public void testPopulateTransitions_NOT_SAME_ENT() {

        Movement current = MockData.getCurrentMovement(1);
        Movement previous = MockData.getPreviousMovement(2, MovementTypeType.ENT);

        List<Areatransition> transitions = incomingMovementBean.populateTransitions(current, previous);

        assertNotNull(transitions);
        assertEquals(2, transitions.size());

        assertEquals(MovementTypeType.ENT, transitions.get(0).getMovementType());

        assertEquals(MovementTypeType.EXI, transitions.get(1).getMovementType());
        assertTrue(transitions.get(1).getAreatranAreaId().getAreaId() == 2);
    }

    /**
     * new Area does not exist in the previous movments areatransitions. Previos
     * area should be added as an exit in the current areatransitions
     */
    @Test
    public void testPopulateTransitions_NOT_SAME_POS() {

        Movement current = MockData.getCurrentMovement(1);
        Movement previous = MockData.getPreviousMovement(2, MovementTypeType.POS);

        List<Areatransition> transitions = incomingMovementBean.populateTransitions(current, previous);

        assertNotNull(transitions);
        assertEquals(2, transitions.size());

        assertEquals(MovementTypeType.ENT, transitions.get(0).getMovementType());

        assertEquals(MovementTypeType.EXI, transitions.get(1).getMovementType());
        assertTrue(transitions.get(1).getAreatranAreaId().getAreaId() == 2);
    }

    /**
     * If there are no previois transitions all areaTransitions shall be created
     * as new entitites and set to TransitionType ENT
     */
    @Test
    public void testPopulateTransitionsNoPrevMovement() {

        Movement current = MockData.getCurrentMovement(1);

        List<Areatransition> transitions = incomingMovementBean.populateTransitions(current, null);

        assertNotNull(transitions);
        assertEquals(1, transitions.size());

        assertEquals(MovementTypeType.ENT, transitions.get(0).getMovementType());
        assertTrue(transitions.get(0).getAreatranAreaId().getAreaId() == 1);
    }
    
    @Test
    public void testMovementAndSegmentRelation() throws MovementServiceException {
    	String connectId = UUID.randomUUID().toString();
    	Movement firstMovementType = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovementType = movementBatchModelBean.createMovement(firstMovementType);
        assertNotNull(firstMovementType);
        incomingMovementBean.processMovement(firstMovementType);
       
        Movement secondMovementType = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovementType = movementBatchModelBean.createMovement(secondMovementType);
        assertNotNull(secondMovementType);
        incomingMovementBean.processMovement(secondMovementType);
        
        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);

        Movement firstMovement = movementList.get(0);
        Movement secondMovement = movementList.get(1);
        
        assertNotNull(firstMovement.getToSegment());
        assertThat(firstMovement.getToSegment(), is(secondMovement.getFromSegment()));
        
        Segment segment = firstMovement.getToSegment();
        
        assertThat(segment.getFromMovement(), is(firstMovement));
        assertThat(segment.getToMovement(), is(secondMovement));
    }
    
    @Test
    public void testMovementAndSegmentRelationThreeMovements() throws MovementServiceException {
    	String connectId = UUID.randomUUID().toString();
    	Movement firstMovementType = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovementType = movementBatchModelBean.createMovement(firstMovementType);
        assertNotNull(firstMovementType);
        incomingMovementBean.processMovement(firstMovementType);
       
        Movement secondMovementType = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovementType = movementBatchModelBean.createMovement(secondMovementType);
        assertNotNull(secondMovementType);
        incomingMovementBean.processMovement(secondMovementType);

        Movement thirdMovementType = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovementType = movementBatchModelBean.createMovement(thirdMovementType);
        assertNotNull(thirdMovementType);
        incomingMovementBean.processMovement(thirdMovementType);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        
        Movement firstMovement = movementList.get(0);
        Movement secondMovement = movementList.get(1);
        Movement thirdMovement = movementList.get(2);
        
        // First segment
        assertNotNull(firstMovement.getToSegment());
        assertThat(firstMovement.getToSegment(), is(secondMovement.getFromSegment()));
        
        Segment segment = firstMovement.getToSegment();
        
        assertThat(segment.getFromMovement(), is(firstMovement));
        assertThat(segment.getToMovement(), is(secondMovement));
        
        // Second segment
        assertNotNull(secondMovement.getToSegment());
        assertThat(secondMovement.getToSegment(), is(thirdMovement.getFromSegment()));
        
        Segment secondSegment = secondMovement.getToSegment();
        
        assertThat(secondSegment.getFromMovement(), is(secondMovement));
        assertThat(secondSegment.getToMovement(), is(thirdMovement));
    }
    
    @Test
    public void testMovementAndSegmentRelationThreeMovementsNonOrdered() throws MovementServiceException {
    	int tenMinutes = 600000;
    	String connectId = UUID.randomUUID().toString();
    	Instant positionTime = Instant.now();
    	Movement firstMovementType = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
    	firstMovementType.setTimestamp(positionTime);
        firstMovementType = movementBatchModelBean.createMovement(firstMovementType);
        assertNotNull(firstMovementType);
        incomingMovementBean.processMovement(firstMovementType);

        Movement thirdMovementType = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovementType.setTimestamp(positionTime.plusMillis(2*tenMinutes));
        thirdMovementType = movementBatchModelBean.createMovement(thirdMovementType);
        assertNotNull(thirdMovementType);
        incomingMovementBean.processMovement(thirdMovementType);

        Movement secondMovementType = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovementType.setTimestamp(positionTime.plusMillis(tenMinutes));
        secondMovementType = movementBatchModelBean.createMovement(secondMovementType);
        assertNotNull(secondMovementType);
        incomingMovementBean.processMovement(secondMovementType);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        // This list is now sorted by timestamp now...
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);

        Movement firstMovement = movementList.get(0);
        Movement secondMovement = movementList.get(1);
        Movement thirdMovement = movementList.get(2);
        
        // First segment
        assertNotNull(firstMovement.getToSegment());
        assertThat(firstMovement.getToSegment(), is(secondMovement.getFromSegment()));
        
        Segment segment = firstMovement.getToSegment();
        
        assertThat(segment.getFromMovement(), is(firstMovement));
        assertThat(segment.getToMovement(), is(secondMovement));
        
        // Second segment
        assertNotNull(secondMovement.getToSegment());
        assertThat(secondMovement.getToSegment(), is(thirdMovement.getFromSegment()));
        
        Segment secondSegment = secondMovement.getToSegment();
        
        assertThat(secondSegment.getFromMovement(), is(secondMovement));
        assertThat(secondSegment.getToMovement(), is(thirdMovement));
    }
    
    @Test
    public void testTrackWithThreeMovements() throws MovementServiceException {
    	String connectId = UUID.randomUUID().toString();
    	Instant timestamp = Instant.now();
    	Movement firstMovementType = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
    	firstMovementType.setTimestamp(timestamp);
        firstMovementType = movementBatchModelBean.createMovement(firstMovementType);
        assertNotNull(firstMovementType);
        incomingMovementBean.processMovement(firstMovementType);
       
        Movement secondMovementType = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovementType.setTimestamp(timestamp.plusSeconds(10));
        secondMovementType = movementBatchModelBean.createMovement(secondMovementType);
        assertNotNull(secondMovementType);
        incomingMovementBean.processMovement(secondMovementType);

        Movement thirdMovementType = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovementType.setTimestamp(timestamp.plusSeconds(20));
        thirdMovementType = movementBatchModelBean.createMovement(thirdMovementType);
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

        List<Movement> trackMovementList = movementDao.getMovementsByTrack(track);

        assertThat(trackMovementList.size(), is(3));

        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(firstMovement.getId())));
        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(secondMovement.getId())));
        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(thirdMovement.getId())));
    }
    
    @Test
    public void testTrackWithThreeMovementsNonOrdered() throws MovementServiceException {
    	int tenMinutes = 600000;
    	String connectId = UUID.randomUUID().toString();
    	Instant positionTime = Instant.now();
    	Movement firstMovementType = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
    	firstMovementType.setTimestamp(positionTime);
        firstMovementType = movementBatchModelBean.createMovement(firstMovementType);
        assertNotNull(firstMovementType);
        incomingMovementBean.processMovement(firstMovementType);
       
        Movement secondMovementType = MockData.createMovement(2d, 1d, connectId, 0, "TEST");
		secondMovementType.setTimestamp(positionTime.plusMillis(2*tenMinutes));
        secondMovementType = movementBatchModelBean.createMovement(secondMovementType);
        assertNotNull(secondMovementType);
        incomingMovementBean.processMovement(secondMovementType);

        Movement thirdMovementType = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        thirdMovementType.setTimestamp(positionTime.plusMillis(tenMinutes));
        thirdMovementType = movementBatchModelBean.createMovement(thirdMovementType);
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

        List<Movement> trackMovementList = movementDao.getMovementsByTrack(track);
        assertThat(trackMovementList.size(), is(3));

        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(firstMovement.getId())));
        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(secondMovement.getId())));
        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(thirdMovement.getId())));
    }
    
    @Test
    public void testTrackWithThreeMovementsReversed() throws MovementServiceException {
        int tenMinutes = 600000;
        String connectId = UUID.randomUUID().toString();
        Instant positionTime = Instant.now();
        Movement firstMovementType = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovementType.setTimestamp(positionTime.plusMillis(2*tenMinutes));
        firstMovementType = movementBatchModelBean.createMovement(firstMovementType);
        assertNotNull(firstMovementType);
        incomingMovementBean.processMovement(firstMovementType);
       
        Movement secondMovementType = MockData.createMovement(2d, 1d, connectId, 0, "TEST");
        secondMovementType.setTimestamp(positionTime.plusMillis(tenMinutes));
        secondMovementType = movementBatchModelBean.createMovement(secondMovementType);
        assertNotNull(secondMovementType);
        incomingMovementBean.processMovement(secondMovementType);

        Movement thirdMovementType = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        thirdMovementType.setTimestamp(positionTime);
        thirdMovementType = movementBatchModelBean.createMovement(thirdMovementType);
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

        List<Movement> trackMovementList = movementDao.getMovementsByTrack(track);
        assertThat(trackMovementList.size(), is(3));

        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(firstMovement.getId())));
        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(secondMovement.getId())));
        assertTrue(trackMovementList.stream().anyMatch(item -> item.getId().equals(thirdMovement.getId())));
    }
    
    @Test
    public void testProcessingThreeMovements() throws MovementServiceException {
        String connectId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);
        firstMovement = movementBatchModelBean.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);
       
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        secondMovement = movementBatchModelBean.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        thirdMovement = movementBatchModelBean.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
        
        assertSegmentsAndTrack(movementList);
    }
    
    @Test
    public void testProcessingThreeMovementsUnordered() throws MovementServiceException {
        String connectId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);
        firstMovement = movementBatchModelBean.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);

        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        thirdMovement = movementBatchModelBean.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);
       
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        secondMovement = movementBatchModelBean.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
        
        assertSegmentsAndTrack(movementList);
    }
    
    @Test
    public void testProcessingThreeMovementsReversed() throws MovementServiceException {
        String connectId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();

        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        thirdMovement = movementBatchModelBean.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);
       
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        secondMovement = movementBatchModelBean.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);
        firstMovement = movementBatchModelBean.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
        
        assertSegmentsAndTrack(movementList);
    }
    
    @Test
    public void testProcessingMovementsAllAlgorithmCases() throws MovementServiceException {
        String connectId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();

        // First
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp.plusSeconds(10));
        firstMovement = movementBatchModelBean.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);
        
        // Second
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(20));
        secondMovement = movementBatchModelBean.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        // Normal case
        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(40));
        thirdMovement = movementBatchModelBean.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);

        // Before first
        Movement fourthMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        fourthMovement.setTimestamp(timestamp);
        fourthMovement = movementBatchModelBean.createMovement(fourthMovement);
        incomingMovementBean.processMovement(fourthMovement);
       
        // Between two positions
        Movement fifthMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        fifthMovement.setTimestamp(timestamp.plusSeconds(30));
        fifthMovement = movementBatchModelBean.createMovement(fifthMovement);
        incomingMovementBean.processMovement(fifthMovement);
        
        // Between two positions
        Movement sixthMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        sixthMovement.setTimestamp(timestamp.plusSeconds(5));
        sixthMovement = movementBatchModelBean.createMovement(sixthMovement);
        incomingMovementBean.processMovement(sixthMovement);
        
        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(6));
        
        assertSegmentsAndTrack(movementList);
    }
    
    @Test
    public void testProcessinghreeMovementsValidateAreaTransitions() throws MovementServiceException {
        
        // Areas
        AreaType areaType = MockData.createAreaType();
        Area areaA = areaDao.createMovementArea(MockData.createArea(areaType));
        Area areaB = areaDao.createMovementArea(MockData.createArea(areaType));
        Area areaC = areaDao.createMovementArea(MockData.createArea(areaType));
        
        String connectId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);
        Movementarea movementArea1 = MockData.getMovementArea(areaA, firstMovement);
        firstMovement.setMovementareaList(Arrays.asList(movementArea1));
        firstMovement = movementBatchModelBean.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);
       
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        Movementarea movementArea2 = MockData.getMovementArea(areaB, secondMovement);
        secondMovement.setMovementareaList(Arrays.asList(movementArea2));
        secondMovement = movementBatchModelBean.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        Movementarea movementArea3 = MockData.getMovementArea(areaC, thirdMovement);
        thirdMovement.setMovementareaList(Arrays.asList(movementArea3));
        thirdMovement = movementBatchModelBean.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
        
        movementList.sort(MovementComparator.MOVEMENT);
        Movement firstMovementProcessed = movementList.get(0);
        Movement secondMovementProcessed = movementList.get(1);
        Movement thirdMovementProcessed = movementList.get(2);
        
        List<Areatransition> firstMovementAreatransitionList = firstMovementProcessed.getAreatransitionList();
        assertThat(firstMovementAreatransitionList.size(), is(1));
        assertThat(firstMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(firstMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        
        List<Areatransition> secondMovementAreatransitionList = secondMovementProcessed.getAreatransitionList();
        assertThat(secondMovementAreatransitionList.size(), is(2));
        assertThat(secondMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaB.getAreaCode()));
        assertThat(secondMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        assertThat(secondMovementAreatransitionList.get(1).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(secondMovementAreatransitionList.get(1).getMovementType(), is(MovementTypeType.EXI));
        
        List<Areatransition> thirdMovementAreatransitionList = thirdMovementProcessed.getAreatransitionList();
        assertThat(thirdMovementAreatransitionList.size(), is(2));
        assertThat(thirdMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaC.getAreaCode()));
        assertThat(thirdMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        assertThat(thirdMovementAreatransitionList.get(1).getAreatranAreaId().getAreaCode(), is(areaB.getAreaCode()));
        assertThat(thirdMovementAreatransitionList.get(1).getMovementType(), is(MovementTypeType.EXI));
    }
    
    @Test
    public void testProcessingThreeMovementsOneAreaValidateAreaTransitions() throws MovementServiceException {
        
        // Areas
        AreaType areaType = MockData.createAreaType();
        Area areaA = areaDao.createMovementArea(MockData.createArea(areaType));
        
        String connectId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);
        Movementarea movementArea1 = MockData.getMovementArea(areaA, firstMovement);
        firstMovement.setMovementareaList(Arrays.asList(movementArea1));
        firstMovement = movementBatchModelBean.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);
       
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        Movementarea movementArea2 = MockData.getMovementArea(areaA, secondMovement);
        secondMovement.setMovementareaList(Arrays.asList(movementArea2));
        secondMovement = movementBatchModelBean.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        Movementarea movementArea3 = MockData.getMovementArea(areaA, thirdMovement);
        thirdMovement.setMovementareaList(Arrays.asList(movementArea3));
        thirdMovement = movementBatchModelBean.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
        
        movementList.sort(MovementComparator.MOVEMENT);
        Movement firstMovementProcessed = movementList.get(0);
        Movement secondMovementProcessed = movementList.get(1);
        Movement thirdMovementProcessed = movementList.get(2);
        
        List<Areatransition> firstMovementAreatransitionList = firstMovementProcessed.getAreatransitionList();
        assertThat(firstMovementAreatransitionList.size(), is(1));
        assertThat(firstMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(firstMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        
        List<Areatransition> secondMovementAreatransitionList = secondMovementProcessed.getAreatransitionList();
        assertThat(secondMovementAreatransitionList.size(), is(1));
        assertThat(secondMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(secondMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.POS));
        
        List<Areatransition> thirdMovementAreatransitionList = thirdMovementProcessed.getAreatransitionList();
        assertThat(thirdMovementAreatransitionList.size(), is(1));
        assertThat(thirdMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(thirdMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.POS));
    }
    
    @Test
    public void testProcessingThreeMovementsValidateAreaTransitionsUnordered() throws MovementServiceException {
        // Areas
        AreaType areaType = MockData.createAreaType();
        Area areaA = areaDao.createMovementArea(MockData.createArea(areaType));
        Area areaB = areaDao.createMovementArea(MockData.createArea(areaType));
        Area areaC = areaDao.createMovementArea(MockData.createArea(areaType));
        
        String connectId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);
        Movementarea movementArea1 = MockData.getMovementArea(areaA, firstMovement);
        firstMovement.setMovementareaList(Arrays.asList(movementArea1));
        firstMovement = movementBatchModelBean.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);

        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        Movementarea movementArea3 = MockData.getMovementArea(areaC, thirdMovement);
        thirdMovement.setMovementareaList(Arrays.asList(movementArea3));
        thirdMovement = movementBatchModelBean.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);
       
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        Movementarea movementArea2 = MockData.getMovementArea(areaB, secondMovement);
        secondMovement.setMovementareaList(Arrays.asList(movementArea2));
        secondMovement = movementBatchModelBean.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
        
        movementList.sort(MovementComparator.MOVEMENT);
        Movement firstMovementProcessed = movementList.get(0);
        Movement secondMovementProcessed = movementList.get(1);
        Movement thirdMovementProcessed = movementList.get(2);
        
        List<Areatransition> firstMovementAreatransitionList = firstMovementProcessed.getAreatransitionList();
        assertThat(firstMovementAreatransitionList.size(), is(1));
        assertThat(firstMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(firstMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        
        List<Areatransition> secondMovementAreatransitionList = secondMovementProcessed.getAreatransitionList();
        assertThat(secondMovementAreatransitionList.size(), is(2));
        assertThat(secondMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaB.getAreaCode()));
        assertThat(secondMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        assertThat(secondMovementAreatransitionList.get(1).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(secondMovementAreatransitionList.get(1).getMovementType(), is(MovementTypeType.EXI));
        
        List<Areatransition> thirdMovementAreatransitionList = thirdMovementProcessed.getAreatransitionList();
        assertThat(thirdMovementAreatransitionList.size(), is(2));
        assertThat(thirdMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaC.getAreaCode()));
        assertThat(thirdMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        assertThat(thirdMovementAreatransitionList.get(1).getAreatranAreaId().getAreaCode(), is(areaB.getAreaCode()));
        assertThat(thirdMovementAreatransitionList.get(1).getMovementType(), is(MovementTypeType.EXI));
    }
    
    @Test
    public void testProcessingThreeMovementsOneAreaValidateAreaTransitionsUnordered() throws MovementServiceException {
        
        // Areas
        AreaType areaType = MockData.createAreaType();
        Area areaA = areaDao.createMovementArea(MockData.createArea(areaType));
        
        String connectId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);
        Movementarea movementArea1 = MockData.getMovementArea(areaA, firstMovement);
        firstMovement.setMovementareaList(Arrays.asList(movementArea1));
        firstMovement = movementBatchModelBean.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);
       
        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        Movementarea movementArea3 = MockData.getMovementArea(areaA, thirdMovement);
        thirdMovement.setMovementareaList(Arrays.asList(movementArea3));
        thirdMovement = movementBatchModelBean.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);

        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        Movementarea movementArea2 = MockData.getMovementArea(areaA, secondMovement);
        secondMovement.setMovementareaList(Arrays.asList(movementArea2));
        secondMovement = movementBatchModelBean.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
        
        movementList.sort(MovementComparator.MOVEMENT);
        Movement firstMovementProcessed = movementList.get(0);
        Movement secondMovementProcessed = movementList.get(1);
        Movement thirdMovementProcessed = movementList.get(2);
        
        List<Areatransition> firstMovementAreatransitionList = firstMovementProcessed.getAreatransitionList();
        assertThat(firstMovementAreatransitionList.size(), is(1));
        assertThat(firstMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(firstMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        
        List<Areatransition> secondMovementAreatransitionList = secondMovementProcessed.getAreatransitionList();
        assertThat(secondMovementAreatransitionList.size(), is(1));
        assertThat(secondMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(secondMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.POS));
        
        List<Areatransition> thirdMovementAreatransitionList = thirdMovementProcessed.getAreatransitionList();
        assertThat(thirdMovementAreatransitionList.size(), is(1));
        assertThat(thirdMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(thirdMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.POS));
    }
    
    @Test
    public void testProcessingThreeMovementsValidateAreaTransitionsReversed() throws MovementServiceException {
        // Areas
        AreaType areaType = MockData.createAreaType();
        Area areaA = areaDao.createMovementArea(MockData.createArea(areaType));
        Area areaB = areaDao.createMovementArea(MockData.createArea(areaType));
        Area areaC = areaDao.createMovementArea(MockData.createArea(areaType));
        
        String connectId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();

        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        Movementarea movementArea3 = MockData.getMovementArea(areaC, thirdMovement);
        thirdMovement.setMovementareaList(Arrays.asList(movementArea3));
        thirdMovement = movementBatchModelBean.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);
       
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        Movementarea movementArea2 = MockData.getMovementArea(areaB, secondMovement);
        secondMovement.setMovementareaList(Arrays.asList(movementArea2));
        secondMovement = movementBatchModelBean.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);
        Movementarea movementArea1 = MockData.getMovementArea(areaA, firstMovement);
        firstMovement.setMovementareaList(Arrays.asList(movementArea1));
        firstMovement = movementBatchModelBean.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
        
        movementList.sort(MovementComparator.MOVEMENT);
        Movement firstMovementProcessed = movementList.get(0);
        Movement secondMovementProcessed = movementList.get(1);
        Movement thirdMovementProcessed = movementList.get(2);

        List<Areatransition> firstMovementAreatransitionList = firstMovementProcessed.getAreatransitionList();
        assertThat(firstMovementAreatransitionList.size(), is(1));
        assertThat(firstMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(firstMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        
        List<Areatransition> secondMovementAreatransitionList = secondMovementProcessed.getAreatransitionList();
        assertThat(secondMovementAreatransitionList.size(), is(2));
        assertThat(secondMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaB.getAreaCode()));
        assertThat(secondMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        assertThat(secondMovementAreatransitionList.get(1).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(secondMovementAreatransitionList.get(1).getMovementType(), is(MovementTypeType.EXI));
        
        List<Areatransition> thirdMovementAreatransitionList = thirdMovementProcessed.getAreatransitionList();
        assertThat(thirdMovementAreatransitionList.size(), is(2));
        assertThat(thirdMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaC.getAreaCode()));
        assertThat(thirdMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        assertThat(thirdMovementAreatransitionList.get(1).getAreatranAreaId().getAreaCode(), is(areaB.getAreaCode()));
        assertThat(thirdMovementAreatransitionList.get(1).getMovementType(), is(MovementTypeType.EXI));
    }
    
    @Test
    public void testProcessingThreeMovementsOneAreaValidateAreaTransitionsReversed() throws MovementServiceException {
        
        // Areas
        AreaType areaType = MockData.createAreaType();
        Area areaA = areaDao.createMovementArea(MockData.createArea(areaType));
        
        String connectId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();
       
        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        Movementarea movementArea3 = MockData.getMovementArea(areaA, thirdMovement);
        thirdMovement.setMovementareaList(Arrays.asList(movementArea3));
        thirdMovement = movementBatchModelBean.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);

        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        Movementarea movementArea2 = MockData.getMovementArea(areaA, secondMovement);
        secondMovement.setMovementareaList(Arrays.asList(movementArea2));
        secondMovement = movementBatchModelBean.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);
        Movementarea movementArea1 = MockData.getMovementArea(areaA, firstMovement);
        firstMovement.setMovementareaList(Arrays.asList(movementArea1));
        firstMovement = movementBatchModelBean.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
        
        movementList.sort(MovementComparator.MOVEMENT);
        Movement firstMovementProcessed = movementList.get(0);
        Movement secondMovementProcessed = movementList.get(1);
        Movement thirdMovementProcessed = movementList.get(2);
        
        List<Areatransition> firstMovementAreatransitionList = firstMovementProcessed.getAreatransitionList();
        assertThat(firstMovementAreatransitionList.size(), is(1));
        assertThat(firstMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(firstMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        
        List<Areatransition> secondMovementAreatransitionList = secondMovementProcessed.getAreatransitionList();
        assertThat(secondMovementAreatransitionList.size(), is(1));
        assertThat(secondMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(secondMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.POS));
        
        List<Areatransition> thirdMovementAreatransitionList = thirdMovementProcessed.getAreatransitionList();
        assertThat(thirdMovementAreatransitionList.size(), is(1));
        assertThat(thirdMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(thirdMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.POS));
    }
    
    @Test
    public void testProcessingMovementsAllAlgorithmCasesValidateAreaTransitions() throws MovementServiceException {
        String connectId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();

        AreaType areaType = MockData.createAreaType();
        Area areaA = areaDao.createMovementArea(MockData.createArea(areaType));
        Area areaB = areaDao.createMovementArea(MockData.createArea(areaType));
        Area areaC = areaDao.createMovementArea(MockData.createArea(areaType));
        Area areaD = areaDao.createMovementArea(MockData.createArea(areaType));
        Area areaE = areaDao.createMovementArea(MockData.createArea(areaType));
        Area areaF = areaDao.createMovementArea(MockData.createArea(areaType));
        
        // First
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp.plusSeconds(10));
        Movementarea movementArea1 = MockData.getMovementArea(areaC, firstMovement);
        firstMovement.setMovementareaList(Arrays.asList(movementArea1));
        firstMovement = movementBatchModelBean.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);
        
        // Second
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(20));
        Movementarea movementArea2 = MockData.getMovementArea(areaD, secondMovement);
        secondMovement.setMovementareaList(Arrays.asList(movementArea2));
        secondMovement = movementBatchModelBean.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        // Normal case
        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(40));
        Movementarea movementArea3 = MockData.getMovementArea(areaF, thirdMovement);
        thirdMovement.setMovementareaList(Arrays.asList(movementArea3));
        thirdMovement = movementBatchModelBean.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);

        // Before first
        Movement fourthMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        fourthMovement.setTimestamp(timestamp);
        Movementarea movementArea4 = MockData.getMovementArea(areaA, fourthMovement);
        fourthMovement.setMovementareaList(Arrays.asList(movementArea4));
        fourthMovement = movementBatchModelBean.createMovement(fourthMovement);
        incomingMovementBean.processMovement(fourthMovement);
       
        // Between two positions
        Movement fifthMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        fifthMovement.setTimestamp(timestamp.plusSeconds(30));
        Movementarea movementArea5 = MockData.getMovementArea(areaE, fifthMovement);
        fifthMovement.setMovementareaList(Arrays.asList(movementArea5));
        fifthMovement = movementBatchModelBean.createMovement(fifthMovement);
        incomingMovementBean.processMovement(fifthMovement);
        
        // Between two positions
        Movement sixthMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        sixthMovement.setTimestamp(timestamp.plusSeconds(5));
        Movementarea movementArea6 = MockData.getMovementArea(areaB, sixthMovement);
        sixthMovement.setMovementareaList(Arrays.asList(movementArea6));
        sixthMovement = movementBatchModelBean.createMovement(sixthMovement);
        incomingMovementBean.processMovement(sixthMovement);
        
        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(6));
        
        movementList.sort(MovementComparator.MOVEMENT);
        Movement firstMovementProcessed = movementList.get(0);
        Movement secondMovementProcessed = movementList.get(1);
        Movement thirdMovementProcessed = movementList.get(2);
        Movement fourthMovementProcessed = movementList.get(3);
        Movement fifthMovementProcessed = movementList.get(4);
        Movement sixthMovementProcessed = movementList.get(5);
        
        List<Areatransition> firstMovementAreatransitionList = firstMovementProcessed.getAreatransitionList();
        assertThat(firstMovementAreatransitionList.size(), is(1));
        assertThat(firstMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(firstMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        
        List<Areatransition> secondMovementAreatransitionList = secondMovementProcessed.getAreatransitionList();
        assertThat(secondMovementAreatransitionList.size(), is(2));
        assertThat(secondMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaB.getAreaCode()));
        assertThat(secondMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        assertThat(secondMovementAreatransitionList.get(1).getAreatranAreaId().getAreaCode(), is(areaA.getAreaCode()));
        assertThat(secondMovementAreatransitionList.get(1).getMovementType(), is(MovementTypeType.EXI));
        
        List<Areatransition> thirdMovementAreatransitionList = thirdMovementProcessed.getAreatransitionList();
        assertThat(thirdMovementAreatransitionList.size(), is(2));
        assertThat(thirdMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaC.getAreaCode()));
        assertThat(thirdMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        assertThat(thirdMovementAreatransitionList.get(1).getAreatranAreaId().getAreaCode(), is(areaB.getAreaCode()));
        assertThat(thirdMovementAreatransitionList.get(1).getMovementType(), is(MovementTypeType.EXI));
        
        List<Areatransition> fourthMovementAreatransitionList = fourthMovementProcessed.getAreatransitionList();
        assertThat(fourthMovementAreatransitionList.size(), is(2));
        assertThat(fourthMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaD.getAreaCode()));
        assertThat(fourthMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        assertThat(fourthMovementAreatransitionList.get(1).getAreatranAreaId().getAreaCode(), is(areaC.getAreaCode()));
        assertThat(fourthMovementAreatransitionList.get(1).getMovementType(), is(MovementTypeType.EXI));
        
        List<Areatransition> fifthMovementAreatransitionList = fifthMovementProcessed.getAreatransitionList();
        assertThat(fifthMovementAreatransitionList.size(), is(2));
        assertThat(fifthMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaE.getAreaCode()));
        assertThat(fifthMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        assertThat(fifthMovementAreatransitionList.get(1).getAreatranAreaId().getAreaCode(), is(areaD.getAreaCode()));
        assertThat(fifthMovementAreatransitionList.get(1).getMovementType(), is(MovementTypeType.EXI));
        
        List<Areatransition> sixthMovementAreatransitionList = sixthMovementProcessed.getAreatransitionList();
        assertThat(sixthMovementAreatransitionList.size(), is(2));
        assertThat(sixthMovementAreatransitionList.get(0).getAreatranAreaId().getAreaCode(), is(areaF.getAreaCode()));
        assertThat(sixthMovementAreatransitionList.get(0).getMovementType(), is(MovementTypeType.ENT));
        assertThat(sixthMovementAreatransitionList.get(1).getAreatranAreaId().getAreaCode(), is(areaE.getAreaCode()));
        assertThat(sixthMovementAreatransitionList.get(1).getMovementType(), is(MovementTypeType.EXI));
    }
    
    @Test
    public void newTrackShouldBeCreatedWhenLeavingPort() throws MovementServiceException {
        String connectId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();
        Movement firstMovement = MockData.createMovement(0d, 1d, connectId, 0, "TEST");
        firstMovement.setTimestamp(timestamp);
        firstMovement = movementBatchModelBean.createMovement(firstMovement);
        incomingMovementBean.processMovement(firstMovement);
       
        // Second position is in port
        Movement secondMovement = MockData.createMovement(1d, 1d, connectId, 0, "TEST");
        secondMovement.setTimestamp(timestamp.plusSeconds(10));
        secondMovement.getMetadata().setClosestPortDistance(0d);
        secondMovement = movementBatchModelBean.createMovement(secondMovement);
        incomingMovementBean.processMovement(secondMovement);

        Movement thirdMovement = MockData.createMovement(1d, 2d, connectId, 0, "TEST");
        thirdMovement.setTimestamp(timestamp.plusSeconds(20));
        thirdMovement = movementBatchModelBean.createMovement(thirdMovement);
        incomingMovementBean.processMovement(thirdMovement);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementDao.getMovementListByMovementConnect(movementConnect);
        assertThat(movementList.size(), is(3));
        
        assertThat(movementList.get(0).getTrack(), is(movementList.get(1).getTrack()));
        assertThat(movementList.get(2).getTrack(), is(not(movementList.get(1).getTrack())));
    }
    
    /*
     * Validate segments and track for movements with same connectId
     */
    private void assertSegmentsAndTrack(List<Movement> movements) {
        movements.sort(MovementComparator.MOVEMENT);

        Movement firstMovement = movements.get(0);
        // Tracks
        Track track = firstMovement.getTrack();
        if (movements.size() > 1) {
            assertThat(track, is(notNullValue()));
            List<Movement> movementList = movementDao.getMovementsByTrack(track);
            assertThat(movementList.size(), is(movements.size()));
            
            for (Movement movement : movements) {
                assertThat(movement.getTrack(), is(track));
                assertTrue(movementList.contains(movement));
            }

            List<Segment> segmentList = movementDao.getSegmentsByTrack(track);
            assertThat(segmentList.size(), is(movements.size() - 1));
            for (Movement movement : movements.subList(0, movements.size() - 1)) {
                assertTrue(segmentList.contains(movement.getToSegment()));
            }
        }

        // Segments
        assertThat(firstMovement.getFromSegment(), is(nullValue()));
        if (movements.size() > 1) {
            Movement previous = null;
            for (Movement movement : movements) {
                if (previous == null) {
                    previous = movement;
                } else {
                    Segment segment = previous.getToSegment();
                    assertThat(movement.getFromSegment(), is(segment));
                    assertThat(segment.getFromMovement(), is(previous));
                    assertThat(segment.getToMovement(), is(movement));
                    assertThat(segment.getTrack(), is(track));
                    previous = movement;
                }
            }
        }
        assertThat(movements.get(movements.size()-1).getToSegment(), is(nullValue()));
    }
}
