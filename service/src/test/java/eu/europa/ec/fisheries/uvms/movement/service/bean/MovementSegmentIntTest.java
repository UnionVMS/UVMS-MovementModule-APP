package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.service.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

@RunWith(Arquillian.class)
public class MovementSegmentIntTest extends TransactionalTests {

    private static final int ALL = -1;
    private static final int ORDER_NORMAL = 1;
    private static final int ORDER_REVERSED = 2;
    private static final int ORDER_RANDOM = 3;

    @EJB
    private MovementBatchModelBean movementBatchModelBean;

    @EJB
    private MovementDao movementDao;

    @EJB
    private IncomingMovementBean incomingMovementBean;

    @Test
    public void createThreeMovementTrackInOrder() throws MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        String connectId = UUID.randomUUID().toString();

        Instant dateFirstMovement = Instant.now();
        Instant dateSecondMovement = dateFirstMovement.plusMillis(300000);
        Instant dateThirdMovement = dateSecondMovement.plusMillis(300000);

        Movement firstMovement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", dateFirstMovement);
        Movement secondMovement = movementHelpers.createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", dateSecondMovement);
        Movement thirdMovement = movementHelpers.createMovement(2d, 2d, 0d, SegmentCategoryType.GAP, connectId, "THREE", dateThirdMovement);

        em.flush();

        Movement firstAfter = movementDao.getMovementById(firstMovement.getId());

        assertEquals(2, firstAfter.getTrack().getSegmentList().size());

        Track track = firstAfter.getTrack();
        Segment s1 = track.getSegmentList().get(0);
        assertEquals(s1.getFromMovement().getId(), firstMovement.getId());
        assertEquals(s1.getToMovement().getId(), secondMovement.getId());

        Segment s2 = track.getSegmentList().get(1);
        assertEquals(s2.getFromMovement().getId(), secondMovement.getId());
        assertEquals(s2.getToMovement().getId(), thirdMovement.getId());
    }

    @Test
    public void createFourMovementTrackInOrder() throws MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        String connectId = UUID.randomUUID().toString();

        Instant firstMovementDate = Instant.now();
        Instant secondMovementDate = firstMovementDate.plusMillis(300000);
        Instant thirdMovementDate  = firstMovementDate.plusMillis(600000);
        Instant forthMovementDate  = firstMovementDate.plusMillis(900000);

        Movement firstMovement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", firstMovementDate);
        Movement secondMovement = movementHelpers.createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", secondMovementDate);
        Movement thirdMovement = movementHelpers.createMovement(2d, 2d, 0d, SegmentCategoryType.GAP, connectId, "THREE", thirdMovementDate);
        Movement forthMovement = movementHelpers.createMovement(3d, 3d, 0d, SegmentCategoryType.GAP, connectId, "FORTH", forthMovementDate);

        em.flush();

        Movement firstAfter = movementDao.getMovementById(firstMovement.getId());

        assertEquals(3, firstAfter.getTrack().getSegmentList().size());

        Track track = firstAfter.getTrack();
        Segment s1 = track.getSegmentList().get(0);
        assertEquals(s1.getFromMovement().getId(), firstMovement.getId());
        assertEquals(s1.getToMovement().getId(), secondMovement.getId());

        Segment s2 = track.getSegmentList().get(1);
        assertEquals(s2.getFromMovement().getId(), secondMovement.getId());
        assertEquals(s2.getToMovement().getId(), thirdMovement.getId());

        Segment s3 = track.getSegmentList().get(2);
        assertEquals(s3.getFromMovement().getId(), thirdMovement.getId());
        assertEquals(s3.getToMovement().getId(), forthMovement.getId());
    }

    @Test
    public void createFourMovementTrackOutOfOrder() throws MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        String connectId = UUID.randomUUID().toString();

        Instant firstMovementDate = Instant.now();
        Instant secondMovementDate = firstMovementDate.plusMillis(300000);
        Instant thirdMovementDate  = firstMovementDate.plusMillis(600000);
        Instant forthMovementDate  = firstMovementDate.plusMillis(900000);

        Movement firstMovement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", firstMovementDate);
        Movement secondMovement = movementHelpers.createMovement(2d, 2d, 0d, SegmentCategoryType.GAP, connectId, "THREE", thirdMovementDate);
        Movement thirdMovement = movementHelpers.createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", secondMovementDate);
        Movement forthMovement = movementHelpers.createMovement(3d, 3d, 0d, SegmentCategoryType.GAP, connectId, "FORTH", forthMovementDate);

        Movement firstAfter = movementDao.getMovementById(firstMovement.getId());

        assertEquals(3, firstAfter.getTrack().getSegmentList().size());

        Track track = firstAfter.getTrack();
        Segment s1 = track.getSegmentList().get(0);
        assertEquals(s1.getFromMovement().getId(), firstMovement.getId());
        assertEquals(s1.getToMovement().getId(), thirdMovement.getId());

        Segment s2 = track.getSegmentList().get(1);
        assertEquals(s2.getFromMovement().getId(), thirdMovement.getId());
        assertEquals(s2.getToMovement().getId(), secondMovement.getId());

        Segment s3 = track.getSegmentList().get(2);
        assertEquals(s3.getFromMovement().getId(), secondMovement.getId());
        assertEquals(s3.getToMovement().getId(), forthMovement.getId());
    }

    @Test
    public void createVarbergGrenaNormal() throws MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
        String connectId = UUID.randomUUID().toString();

        List<Movement> movementList = movementHelpers.createVarbergGrenaMovements(ORDER_NORMAL, ALL, connectId);

        assertSegmentMovementIds(movementList);
    }

    @Test
    public void createVarbergGrenaBasedOnReversedOrder() throws MovementServiceException {
        testVarbergGrenaBasedOnOrdering(ORDER_REVERSED);
    }

    @Test
    public void createVarbergGrenaBasedOnRandomOrder() throws MovementServiceException {
        testVarbergGrenaBasedOnOrdering(ORDER_RANDOM);
    }

    private void testVarbergGrenaBasedOnOrdering(int order) throws MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
        String connectId = UUID.randomUUID().toString();

        List<Movement> movementList = movementHelpers.createVarbergGrenaMovements(order, ALL, connectId);

        Movement aMovement = movementList.get(movementList.size() - 1);
        Track track = aMovement.getTrack();
        List<Segment> segmentList = track.getSegmentList();
        int n = segmentList.size();
        int i = 0;
        assertEquals(segmentList.size(), movementList.size() - 1);

        Collections.sort(segmentList, new Comparator<Segment>(){
            public int compare(Segment s1, Segment s2) {
                return s1.getFromMovement().getTimestamp().compareTo(s2.getFromMovement().getTimestamp());
            }
        });

        Segment previousSegment = null;
        Segment currentSegment = null;
        while(i < n){
            currentSegment = segmentList.get(i);
            if(i == 0){
                previousSegment = currentSegment;
                i++;
                continue;
            }
            assertEquals(currentSegment.getFromMovement().getId(), previousSegment.getToMovement().getId());
            i++;
            previousSegment = currentSegment;
        }
        assertEquals(segmentList.size(), i);
    }

    @Test
    public void createFishingTourVarberg() throws MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
        String connectId = UUID.randomUUID().toString();

        List<Movement> movementList = movementHelpers.createFishingTourVarberg(ORDER_NORMAL ,connectId);
        assertSegmentMovementIds(movementList);
    }

    private void assertSegmentMovementIds(List<Movement> movementList) {
        int n = movementList.size();
        int i = 0;

        Movement previousMovement = null;
        while(i < n){
            Movement currentMovement = movementList.get(i);
            if(i == 0){
                previousMovement = currentMovement;
                i++;
                continue;
            }
            Track track = currentMovement.getTrack();
            Segment segment = track.getSegmentList().get(i - 1);
            assertEquals(segment.getFromMovement().getId(), previousMovement.getId());
            assertEquals(segment.getToMovement().getId(), currentMovement.getId());
            i++;
            previousMovement = currentMovement;
        }
        assertEquals(movementList.size(), i);
    }
}