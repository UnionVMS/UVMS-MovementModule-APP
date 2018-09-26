package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.uvms.movement.service.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Track;
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
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);

        String connectId = UUID.randomUUID().toString();

        Instant dateFirstMovement = Instant.now();
        Instant dateSecondMovement = dateFirstMovement.plusMillis(300000);
        Instant dateThirdMovement = dateSecondMovement.plusMillis(300000);

        Movement firstMovement = movementHelpers.createMovement(0d, 0d, connectId, "ONE", dateFirstMovement);
        Movement secondMovement = movementHelpers.createMovement(1d, 1d, connectId, "TWO", dateSecondMovement);
        Movement thirdMovement = movementHelpers.createMovement(2d, 2d, connectId, "THREE", dateThirdMovement);

        incomingMovementBean.processMovement(firstMovement);
        incomingMovementBean.processMovement(secondMovement);
        incomingMovementBean.processMovement(thirdMovement);
        
        em.flush();

        Movement firstAfter = movementDao.getMovementById(firstMovement.getId());

        Track track = firstAfter.getTrack();

        List<Segment> segments = movementDao.getSegmentsByTrack(track);
        segments.sort((s1, s2) -> s1.getFromMovement().compareTo(s2.getFromMovement()));
        assertEquals(2, segments.size());

        Segment s1 = segments.get(0);
        assertEquals(s1.getFromMovement().getId(), firstMovement.getId());
        assertEquals(s1.getToMovement().getId(), secondMovement.getId());

        Segment s2 = segments.get(1);
        assertEquals(s2.getFromMovement().getId(), secondMovement.getId());
        assertEquals(s2.getToMovement().getId(), thirdMovement.getId());
    }

    @Test
    public void createFourMovementTrackInOrder() throws MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);

        String connectId = UUID.randomUUID().toString();

        Instant firstMovementDate = Instant.now();
        Instant secondMovementDate = firstMovementDate.plusMillis(300000);
        Instant thirdMovementDate  = firstMovementDate.plusMillis(600000);
        Instant forthMovementDate  = firstMovementDate.plusMillis(900000);

        Movement firstMovement = movementHelpers.createMovement(0d, 0d, connectId, "ONE", firstMovementDate);
        Movement secondMovement = movementHelpers.createMovement(1d, 1d, connectId, "TWO", secondMovementDate);
        Movement thirdMovement = movementHelpers.createMovement(2d, 2d, connectId, "THREE", thirdMovementDate);
        Movement forthMovement = movementHelpers.createMovement(3d, 3d, connectId, "FORTH", forthMovementDate);

        incomingMovementBean.processMovement(firstMovement);
        incomingMovementBean.processMovement(secondMovement);
        incomingMovementBean.processMovement(thirdMovement);
        incomingMovementBean.processMovement(forthMovement);
        em.flush();

        Movement firstAfter = movementDao.getMovementById(firstMovement.getId());
        Track track = firstAfter.getTrack();

        List<Segment> segments = movementDao.getSegmentsByTrack(track);
        segments.sort((s1, s2) -> s1.getFromMovement().compareTo(s2.getFromMovement()));
        assertEquals(3, segments.size());

        Segment s1 = segments.get(0);
        assertEquals(s1.getFromMovement().getId(), firstMovement.getId());
        assertEquals(s1.getToMovement().getId(), secondMovement.getId());

        Segment s2 = segments.get(1);
        assertEquals(s2.getFromMovement().getId(), secondMovement.getId());
        assertEquals(s2.getToMovement().getId(), thirdMovement.getId());

        Segment s3 = segments.get(2);
        assertEquals(s3.getFromMovement().getId(), thirdMovement.getId());
        assertEquals(s3.getToMovement().getId(), forthMovement.getId());
    }

    @Test
    public void createFourMovementTrackOutOfOrder() throws MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);

        String connectId = UUID.randomUUID().toString();

        Instant firstMovementDate = Instant.now();
        Instant secondMovementDate = firstMovementDate.plusMillis(300000);
        Instant thirdMovementDate  = firstMovementDate.plusMillis(600000);
        Instant forthMovementDate  = firstMovementDate.plusMillis(900000);

        Movement firstMovement = movementHelpers.createMovement(0d, 0d, connectId, "ONE", firstMovementDate);
        Movement secondMovement = movementHelpers.createMovement(2d, 2d, connectId, "THREE", thirdMovementDate);
        Movement thirdMovement = movementHelpers.createMovement(1d, 1d, connectId, "TWO", secondMovementDate);
        Movement forthMovement = movementHelpers.createMovement(3d, 3d, connectId, "FORTH", forthMovementDate);

        incomingMovementBean.processMovement(firstMovement);
        incomingMovementBean.processMovement(forthMovement);
        incomingMovementBean.processMovement(thirdMovement);
        incomingMovementBean.processMovement(secondMovement);
        
        Movement firstAfter = movementDao.getMovementById(firstMovement.getId());


        Track track = firstAfter.getTrack();
        List<Segment> segments = movementDao.getSegmentsByTrack(track);
        segments.sort((s1, s2) -> s1.getFromMovement().compareTo(s2.getFromMovement()));
        Segment s1 = segments.get(0);
        assertEquals(s1.getFromMovement().getId(), firstMovement.getId());
        assertEquals(s1.getToMovement().getId(), thirdMovement.getId());

        Segment s2 = segments.get(1);
        assertEquals(s2.getFromMovement().getId(), thirdMovement.getId());
        assertEquals(s2.getToMovement().getId(), secondMovement.getId());

        Segment s3 = segments.get(2);
        assertEquals(s3.getFromMovement().getId(), secondMovement.getId());
        assertEquals(s3.getToMovement().getId(), forthMovement.getId());
    }

    @Test
    public void createVarbergGrenaNormal() throws MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
        String connectId = UUID.randomUUID().toString();

        List<Movement> movementList = movementHelpers.createVarbergGrenaMovements(ORDER_NORMAL, ALL, connectId);
        for (Movement movement : movementList) {
            incomingMovementBean.processMovement(movement);
        }
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
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
        String connectId = UUID.randomUUID().toString();

        List<Movement> movementList = movementHelpers.createVarbergGrenaMovements(order, ALL, connectId);
        for (Movement movement : movementList) {
            incomingMovementBean.processMovement(movement);
        }
        
        Movement aMovement = movementList.get(movementList.size() - 1);
        Track track = aMovement.getTrack();
        List<Segment> segmentList = movementDao.getSegmentsByTrack(track);
        int n = segmentList.size();
        int i = 0;
        assertEquals(segmentList.size(), movementList.size() - 1);

        Collections.sort(segmentList, Comparator.comparing(s -> s.getFromMovement().getTimestamp()));

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
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);
        String connectId = UUID.randomUUID().toString();

        List<Movement> movementList = movementHelpers.createFishingTourVarberg(ORDER_NORMAL ,connectId);
        for (Movement movement : movementList) {
            incomingMovementBean.processMovement(movement);
        }
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
            List<Segment> segments = movementDao.getSegmentsByTrack(track);
            segments.sort((s1, s2) -> s1.getFromMovement().compareTo(s2.getFromMovement()));
            Segment segment = segments.get(i - 1);
            assertEquals(segment.getFromMovement().getId(), previousMovement.getId());
            assertEquals(segment.getToMovement().getId(), currentMovement.getId());
            i++;
            previousMovement = currentMovement;
        }
        assertEquals(movementList.size(), i);
    }
}
