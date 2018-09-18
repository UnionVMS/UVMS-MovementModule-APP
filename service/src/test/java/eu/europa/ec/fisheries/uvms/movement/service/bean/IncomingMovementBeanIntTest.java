package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.time.Instant;
import java.util.Date;    //leave be for now
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;

import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Areatransition;

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

    @Test
    public void testCreatingMovement() {
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = MockData.createMovementType(0d, 1d, 0d, SegmentCategoryType.EXIT_PORT, uuid,0);
        movementType = movementBatchModelBean.createMovement(movementType, "TEST");
        assertNotNull("MovementType creation was successful.", movementType.getGuid());
        em.flush();

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getConnectId());
        assertNotNull("MovementConnect creation was successful.", movementConnect);
        List<Movement> movementList = movementConnect.getMovementList();
        assertNotNull("List of Movement creation was successful.", movementList);
        assertTrue("The list of Movement contains exactly one Movement object. It has: " + movementList.size() + " movements", movementList.size() == 1);

        Movement movement = movementDao.getMovementById(movementList.get(0).getId());
        assertNotNull("Movement object was successfully created.", movement);
        LOG.info(" [ testCreatingMovement: Movement object was successfully created. ] ");
    }

    @Test
    public void testProcessingMovement() {

        // Given: Get the id for a persisted movement entity.

        String uuid = UUID.randomUUID().toString();

        MovementType movementType = MockData.createMovementType(0d, 1d, 0d, SegmentCategoryType.EXIT_PORT, uuid,0);
        movementType = movementBatchModelBean.createMovement(movementType, "TEST");
        em.flush();

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getConnectId());
        List<Movement> movementList = movementConnect.getMovementList();
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

        MovementType movementType = MockData.createMovementType(0d, 1d, 0d, SegmentCategoryType.EXIT_PORT, uuid,0);
        movementType = movementBatchModelBean.createMovement(movementType, "TEST");
        em.flush();

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getConnectId());
        List<Movement> movementList = movementConnect.getMovementList();
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

        MovementType firstMovementType = MockData.createMovementType(0d, 1d, 0d, SegmentCategoryType.EXIT_PORT, firstUuid,0);
        firstMovementType = movementBatchModelBean.createMovement(firstMovementType, "TEST");
        em.flush();

        MovementConnect firstMovementConnect = movementDao.getMovementConnectByConnectId(firstMovementType.getConnectId());

        List<Movement> firstMovementList = firstMovementConnect.getMovementList();

        Movement firstMovement = firstMovementList.get(0);
        Long firstMovementId = firstMovementList.get(0).getId();


        /* Setting same timestamp + duplicate flag set to false + same movement type. */
		//since we now process stuff as we create them, this part will not work
        firstMovement.setTimestamp(Instant.ofEpochMilli(1490708331790L));
        // Fields will be null by default in postgres if not set instead of false which means duplicate timestamp Movements
        // will not be found by the processMovement method via the isDateAlreadyInserted method in MovementDaoBean.
        firstMovement.setDuplicate(false);
        firstMovement.setMovementType(MovementTypeType.ENT);

        movementDao.persist(firstMovement);
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
    public void testMovementAndSegmentRelation() {
    	String connectId = UUID.randomUUID().toString();
    	MovementType firstMovementType = MockData.createMovementType(0d, 1d, 0d, SegmentCategoryType.EXIT_PORT, connectId, 0);
        firstMovementType = movementBatchModelBean.createMovement(firstMovementType, "TEST");
        assertNotNull(firstMovementType);
       
        MovementType secondMovementType = MockData.createMovementType(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, 0);
        secondMovementType = movementBatchModelBean.createMovement(secondMovementType, "TEST");
        assertNotNull(secondMovementType);
        
        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementConnect.getMovementList();
        
        Movement firstMovement = movementList.get(0);
        Movement secondMovement = movementList.get(1);
        
        assertNotNull(firstMovement.getToSegment());
        assertThat(firstMovement.getToSegment(), is(secondMovement.getFromSegment()));
        
        Segment segment = firstMovement.getToSegment();
        
        assertThat(segment.getFromMovement(), is(firstMovement));
        assertThat(segment.getToMovement(), is(secondMovement));
    }
    
    @Test
    public void testMovementAndSegmentRelationThreeMovements() {
    	String connectId = UUID.randomUUID().toString();
    	MovementType firstMovementType = MockData.createMovementType(0d, 1d, 0d, SegmentCategoryType.EXIT_PORT, connectId, 0);
        firstMovementType = movementBatchModelBean.createMovement(firstMovementType, "TEST");
        assertNotNull(firstMovementType);
       
        MovementType secondMovementType = MockData.createMovementType(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, 0);
        secondMovementType = movementBatchModelBean.createMovement(secondMovementType, "TEST");
        assertNotNull(secondMovementType);

        MovementType thirdMovementType = MockData.createMovementType(1d, 2d, 0d, SegmentCategoryType.GAP, connectId, 0);
        thirdMovementType = movementBatchModelBean.createMovement(thirdMovementType, "TEST");
        assertNotNull(thirdMovementType);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementConnect.getMovementList();
        
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
    public void testMovementAndSegmentRelationThreeMovementsNonOrdered() {
    	int tenMinutes = 600000;
    	String connectId = UUID.randomUUID().toString();
    	Instant positionTime = Instant.now();
    	MovementType firstMovementType = MockData.createMovementType(0d, 1d, 0d, SegmentCategoryType.EXIT_PORT, connectId, 0);
    	firstMovementType.setPositionTime(Date.from(positionTime));
        firstMovementType = movementBatchModelBean.createMovement(firstMovementType, "TEST");
        assertNotNull(firstMovementType);

        MovementType thirdMovementType = MockData.createMovementType(1d, 2d, 0d, SegmentCategoryType.GAP, connectId, 0);
        thirdMovementType.setPositionTime(Date.from(positionTime.plusMillis(2*tenMinutes)));
        thirdMovementType = movementBatchModelBean.createMovement(thirdMovementType, "TEST");
        assertNotNull(thirdMovementType);

        MovementType secondMovementType = MockData.createMovementType(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, 0);
        secondMovementType.setPositionTime(Date.from(positionTime.plusMillis(tenMinutes)));
        secondMovementType = movementBatchModelBean.createMovement(secondMovementType, "TEST");
        assertNotNull(secondMovementType);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementConnect.getMovementList();

        Movement firstMovement = movementList.get(0);
        Movement thirdMovement = movementList.get(1);
        Movement secondMovement = movementList.get(2);
        
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
    public void testTrackWithThreeMovements() {
    	String connectId = UUID.randomUUID().toString();
    	MovementType firstMovementType = MockData.createMovementType(0d, 1d, 0d, SegmentCategoryType.EXIT_PORT, connectId, 0);
        firstMovementType = movementBatchModelBean.createMovement(firstMovementType, "TEST");
        assertNotNull(firstMovementType);
       
        MovementType secondMovementType = MockData.createMovementType(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, 0);
        secondMovementType = movementBatchModelBean.createMovement(secondMovementType, "TEST");
        assertNotNull(secondMovementType);

        MovementType thirdMovementType = MockData.createMovementType(1d, 2d, 0d, SegmentCategoryType.GAP, connectId, 0);
        thirdMovementType = movementBatchModelBean.createMovement(thirdMovementType, "TEST");
        assertNotNull(thirdMovementType);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementConnect.getMovementList();
        
        Movement firstMovement = movementList.get(0);
        Movement secondMovement = movementList.get(1);
        Movement thirdMovement = movementList.get(2);
        
        Track track = firstMovement.getTrack();
        assertThat(track, is(secondMovement.getTrack()));
        assertThat(track, is(thirdMovement.getTrack()));
        
        assertThat(track.getMovementList().size(), is(3));
        assertThat(track.getMovementList(), hasItem(firstMovement));
        assertThat(track.getMovementList(), hasItem(secondMovement));
        assertThat(track.getMovementList(), hasItem(thirdMovement));
    }
    
    @Test
    public void testTrackWithThreeMovementsNonOrdered() {
    	int tenMinutes = 600000;
    	String connectId = UUID.randomUUID().toString();
    	Date positionTime = new Date();
    	MovementType firstMovementType = MockData.createMovementType(0d, 1d, 0d, SegmentCategoryType.EXIT_PORT, connectId, 0);
    	firstMovementType.setPositionTime(positionTime);
        firstMovementType = movementBatchModelBean.createMovement(firstMovementType, "TEST");
        assertNotNull(firstMovementType);
       
        MovementType secondMovementType = MockData.createMovementType(2d, 1d, 0d, SegmentCategoryType.GAP, connectId, 0);
		secondMovementType.setPositionTime(new Date(positionTime.getTime() + 2*tenMinutes));
        secondMovementType = movementBatchModelBean.createMovement(secondMovementType, "TEST");
        assertNotNull(secondMovementType);

        MovementType thirdMovementType = MockData.createMovementType(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, 0);
        thirdMovementType.setPositionTime(new Date(positionTime.getTime() + tenMinutes));
        thirdMovementType = movementBatchModelBean.createMovement(thirdMovementType, "TEST");
        assertNotNull(thirdMovementType);

        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        List<Movement> movementList = movementConnect.getMovementList();
 
        Movement firstMovement = movementList.get(0);
        Movement secondMovement = movementList.get(1);
        Movement thirdMovement = movementList.get(2);
        
        Track track = firstMovement.getTrack();
        assertThat(track, is(secondMovement.getTrack()));
        assertThat(track, is(thirdMovement.getTrack()));
        
        assertThat(track.getMovementList().size(), is(3));
        assertThat(track.getMovementList(), hasItem(firstMovement));
        assertThat(track.getMovementList(), hasItem(secondMovement));
        assertThat(track.getMovementList(), hasItem(thirdMovement));
    }
}