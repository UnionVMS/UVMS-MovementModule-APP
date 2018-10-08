package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.persistence.TypedQuery;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

@RunWith(Arquillian.class)
@Ignore //Since we process movements as we create them, theses tests need changing to not try to create the same segments.
public class SegmentBeanIntTest extends TransactionalTests {

    @EJB
    private SegmentBean segmentBean;

    @EJB
    private MovementBatchModelBean movementBatchModelBean;

    @EJB
    private MovementDao movementDao;

    @Test
    @OperateOnDeployment("normal")
    public void createSegmentOnFirstMovement() throws MovementServiceException, MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);

        String connectId = UUID.randomUUID().toString();



        Instant date1 = OffsetDateTime.of(1920, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant date2 = OffsetDateTime.of(1930, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();

        Movement fromMovement = movementHelpers.createMovement(0d, 0d, connectId, "ONE", date1);
        Movement toMovement = movementHelpers.createMovement(1d, 1d, connectId, "TWO", date2);
        segmentBean.newSegment(fromMovement, toMovement);
        em.flush();

        TypedQuery<Movement> queryMovement =
                em.createQuery("select m from Movement m where m.id = :id", Movement.class);

        // get first movement from db
        queryMovement.setParameter("id", fromMovement.getId());
        Movement fetchedFromMovement = queryMovement.getSingleResult();

        // get second movement from db
        queryMovement.setParameter("id", toMovement.getId());
        Movement fetchedToMovement = queryMovement.getSingleResult();

        // get the segment from the db
        TypedQuery<Segment> querySegment =
                em.createQuery("select s from Segment s where s.fromMovement = :fromMovement and s.toMovement= :toMovement", Segment.class);

        querySegment.setParameter("fromMovement", fetchedFromMovement);
        querySegment.setParameter("toMovement", fetchedToMovement);
        Segment fetchedSegment = querySegment.getSingleResult();
        List<Movement> movements = movementDao.getMovementsByTrack(fetchedSegment.getTrack());
        Movement movement1FromList = movements.get(0);
        Movement movement2FromList = movements.get(1);

        // verify that the id:s are different
        assertNotEquals(movement1FromList.getId(), movement2FromList.getId());
    }

    @Test
    @OperateOnDeployment("normal")
    public void createSegmentOnFirstMovement_OnlyOneMovement() throws MovementServiceException, MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);

        String connectId = UUID.randomUUID().toString();

        Instant date1 = OffsetDateTime.of(1920, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();;

        Movement movement = movementHelpers.createMovement(0d, 0d, connectId, "ONE", date1);
        segmentBean.newSegment(movement, movement);
        em.flush();

        // get movement from db
        TypedQuery<Movement> queryMovement =
                em.createQuery("select m from Movement m where m.id = :id", Movement.class);

        // get frommovement from db
        // obs they should be the same
        queryMovement.setParameter("id", movement.getId());
        Movement fetchedFromMovement = queryMovement.getSingleResult();

        // get tpmovement from db
        // obs they should be the same
        queryMovement.setParameter("id", movement.getId());
        Movement fetchedToMovement = queryMovement.getSingleResult();

        // get the segment
        TypedQuery<Segment> querySegment =
                em.createQuery("select s from Segment s where s.fromMovement = :fromMovement and s.toMovement= :toMovement", Segment.class);

        querySegment.setParameter("fromMovement", fetchedFromMovement);
        querySegment.setParameter("toMovement", fetchedToMovement);
        Segment fetchedSegment = querySegment.getSingleResult();
        List<Movement> movements = movementDao.getMovementsByTrack(fetchedSegment.getTrack());
        Movement movement1FromList = movements.get(0);
        Movement movement2FromList = movements.get(1);

        // verify that the id:s are same
        assertEquals(movement1FromList.getId(), movement2FromList.getId());
    }

    @Test
    @OperateOnDeployment("normal")
    public void splitSegment() throws MovementServiceException, MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);

        // TODO nothing indicates that this splitFunction actually works
        Instant date1 = OffsetDateTime.of(1920, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();

        Instant date2 = OffsetDateTime.of(1930, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();

        Instant date3 = OffsetDateTime.of(1925, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();

        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = movementHelpers.createMovement(2d, 2d, connectId, "ONE", date1);
        Movement toMovement = movementHelpers.createMovement(3d, 3d, connectId, "TWO", date2);
        Segment segment = segmentBean.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);
        fromMovement.setTrack(track);
        toMovement.setTrack(track);

        em.flush();

        assertNotNull(toMovement.getTrack());
        List<Segment> segments = movementDao.getSegmentsByTrack(toMovement.getTrack());
        assertEquals(1, segments.size());

        List<Movement> movements = movementDao.getMovementsByTrack(toMovement.getTrack());
        assertEquals(2, movements.size());

//--------------------------------------------------------------------------

        Movement newMovement = movementHelpers.createMovement(.5d, .5d, connectId, "THREE", date3);
        em.flush();

        segmentBean.splitSegment(toMovement, newMovement);
        //splitSegmentHelper(newMovement);
        em.flush();

        // get movement from db
        TypedQuery<Segment> qry =
                em.createQuery("select s from Segment s where s.track = :track order by s.updated ", Segment.class);

        qry.setParameter("track", track);
        List<Segment> segmentList = qry.getResultList();

        assertNotNull(segmentList);
        assertEquals(2, segmentList.size());

        Segment rsSegment1 = segmentList.get(0);
        Segment rsSegment2 = segmentList.get(1);
        Long id1 = rsSegment1.getFromMovement().getId();
        Long id2 = rsSegment1.getToMovement().getId();
        Long id3 = rsSegment2.getFromMovement().getId();
        Long id4 = rsSegment2.getToMovement().getId();

        // this is how it works no - but it is not OK
        assertEquals(id1, fromMovement.getId());
        assertEquals(id2, toMovement.getId());
        assertEquals(id3, toMovement.getId());
        assertEquals(id4, newMovement.getId());
    }

    @Test
    @OperateOnDeployment("normal")
    public void createNewTrack() throws MovementServiceException, MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);

        String connectId = UUID.randomUUID().toString();

        Instant date1 = OffsetDateTime.of(1920, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();

        Instant date2 = OffsetDateTime.of(1930, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();;

        Movement fromMovement = movementHelpers.createMovement(0d, 0d, connectId, "one", date1);
        Movement toMovement = movementHelpers.createMovement(1d, 1d, connectId, "two", date2);

        Segment segment = segmentBean.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);

        fromMovement.setTrack(track);
        toMovement.setTrack(track);

        movementDao.upsertLatestMovement(fromMovement, fromMovement.getMovementConnect());
        movementDao.upsertLatestMovement(toMovement, toMovement.getMovementConnect());

        em.flush();
        assertNotNull(track);
        List<Segment> segments = movementDao.getSegmentsByTrack(track);
        assertEquals(1, segments.size());
        List<Movement> movements = movementDao.getMovementsByTrack(track);
        assertEquals(2, movements.size());

        // get movement from db
        TypedQuery<Movement> queryMovement =
                em.createQuery("select m from Movement m where m.id = :id", Movement.class);

        // get frommovement from db
        // obs they should be the same
        queryMovement.setParameter("id", fromMovement.getId());
        Movement fetchedFromMovement = queryMovement.getSingleResult();

        queryMovement.setParameter("id", toMovement.getId());
        Movement fetchedToMovement = queryMovement.getSingleResult();

        // nullchecks
        assertNotNull(fetchedFromMovement);
        assertNotNull(fetchedToMovement);
        assertNotNull(fetchedFromMovement.getTrack());
        assertNotNull(fetchedToMovement.getTrack());
        assertNotNull(fetchedFromMovement.getTrack().getId());
        assertNotNull(fetchedToMovement.getTrack().getId());

        Long trackFromId = fetchedFromMovement.getTrack().getId();
        Long trackToId = fetchedToMovement.getTrack().getId();

        assertEquals(trackFromId, trackToId);
    }

    @Test
    @OperateOnDeployment("normal")
    public void createNewTrack_onSegmentMovement() throws MovementServiceException, MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);

        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = movementHelpers.createMovement(0d, 0d, connectId, "TEST", DateUtil.nowUTC());
        Movement toMovement = movementHelpers.createMovement(1d, 1d, connectId, "TEST", DateUtil.nowUTC());

        Segment segment = segmentBean.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);

        fromMovement.setTrack(track);
        toMovement.setTrack(track);
        em.flush();

        Track theNewTrack = segmentBean.createNewTrack(segment);
        em.flush();

        assertNotEquals(track.getId(), theNewTrack.getId());
    }

    @Test
    @OperateOnDeployment("normal")
    public void upsertTrack() throws MovementServiceException, MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);

        Instant date1 = OffsetDateTime.of(1920, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant date2 = OffsetDateTime.of(1930, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant date3 = OffsetDateTime.of(1935, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();

        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = movementHelpers.createMovement(0d, 0d, connectId, "one", date1);
        Movement toMovement = movementHelpers.createMovement(5d, 5d, connectId, "two", date2);
        Segment segment = segmentBean.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);
        assertNotNull(track);

        fromMovement.setTrack(track);
        toMovement.setTrack(track);

        em.flush();

        Movement newMovement = movementHelpers.createMovement(10d, 10d, connectId, "three", date3);
        segment = segmentBean.createSegment(toMovement, newMovement);

        Track createdTrack = segmentBean.upsertTrack(track, segment, newMovement);
        assertNotNull(createdTrack);

        // get movement from db
        TypedQuery<Segment> qry =
                em.createQuery("select s from Segment s where s.track = :track", Segment.class);

        qry.setParameter("track", track);
        List<Segment> segmentList = qry.getResultList();

        assertNotNull(segmentList);
        assertNotEquals(0, segmentList.size());
    }

    @Test
    @OperateOnDeployment("normal")
    public void upsertTrack5() throws MovementServiceException, MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);

        Instant date1 = OffsetDateTime.of(1920, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant date2 = OffsetDateTime.of(1925, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant date3 = OffsetDateTime.of(1930, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant date4 = OffsetDateTime.of(1935, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant date5 = OffsetDateTime.of(1940, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant date6 = OffsetDateTime.of(1945, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();

        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = movementHelpers.createMovement(0d, 0d, connectId, "one", date1);
        Movement toMovement = movementHelpers.createMovement(5d, 5d, connectId, "two", date2);
        Segment segment = segmentBean.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);
        fromMovement.setTrack(track);
        toMovement.setTrack(track);
        em.flush();
        assertNotNull(track);

        Movement newMovement = movementHelpers.createMovement(10d, 10d, connectId, "three", date3);
        segment = segmentBean.createSegment(toMovement, newMovement);
        Track createdTrack = segmentBean.upsertTrack(track, segment, newMovement);
        assertEquals(createdTrack.getId(), track.getId());

        Movement oldMovement = newMovement;
        newMovement = movementHelpers.createMovement(20d, 20d, connectId, "four", date4);
        segment = segmentBean.createSegment(oldMovement, newMovement);
        segmentBean.upsertTrack(track, segment, newMovement);

        oldMovement = newMovement;
        newMovement = movementHelpers.createMovement(30d, 30d, connectId, "five", date5);
        segment = segmentBean.createSegment(oldMovement, newMovement);
        segmentBean.upsertTrack(track, segment, newMovement);

        oldMovement = newMovement;
        newMovement = movementHelpers.createMovement(40d, 40d, connectId, "six", date6);
        segment = segmentBean.createSegment(oldMovement, newMovement);
        segmentBean.upsertTrack(track, segment, newMovement);

        // get Segment from db
        TypedQuery<Segment> qry =
                em.createQuery("select s from Segment s where s.track = :track", Segment.class);

        qry.setParameter("track", track);
        List<Segment> segmentList = qry.getResultList();

        assertNotNull(segmentList);
        assertEquals(5, segmentList.size());
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateTrack() throws MovementServiceException, MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);

        Instant date1 = OffsetDateTime.of(1920, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant date2 = OffsetDateTime.of(1925, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant date3 = OffsetDateTime.of(1930, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();

        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = movementHelpers.createMovement(0d, 0d, connectId, "user1", date1);
        Movement toMovement = movementHelpers.createMovement(5d, 5d, connectId, "user2", date2);
        Segment segment = segmentBean.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);
        fromMovement.setTrack(track);
        toMovement.setTrack(track);
        assertNotNull(track);

        Movement newMovement = movementHelpers.createMovement(10d, 10d, connectId, "user3", date3);
        segment = segmentBean.createSegment(toMovement, newMovement);
        segmentBean.updateTrack(track, newMovement, segment);

        // get movement from db
        TypedQuery<Segment> qry =
                em.createQuery("select s from Segment s where s.track = :track", Segment.class);

        qry.setParameter("track", track);
        List<Segment> segmentList = qry.getResultList();

        assertNotNull(segmentList);
        assertEquals(2, segmentList.size());
    }

    @Test
    @OperateOnDeployment("normal")
    public void addMovementBeforeFirst() throws MovementServiceException, MovementServiceException {
        MovementHelpers movementHelpers = new MovementHelpers(movementBatchModelBean);

        // TODO better evaluation of results

        Instant date1 = OffsetDateTime.of(1920, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant date2 = OffsetDateTime.of(1925, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant date_before = OffsetDateTime.of(1910, 06, 06, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();

        String connectId = UUID.randomUUID().toString();

        Movement firstMovement = movementHelpers.createMovement(2d, 2d, connectId, "ONE", date1);
        Movement secondMovement = movementHelpers.createMovement(3d, 3d, connectId, "TWO", date2);
        Segment segment = segmentBean.createSegment(firstMovement, secondMovement);
        assertNotNull(segment);
        Track track = segmentBean.createNewTrack(segment);
        assertNotNull(track);
        firstMovement.setTrack(track);
        secondMovement.setTrack(track);

        Segment fromSegment = firstMovement.getToSegment();
        Segment toSegment = secondMovement.getFromSegment();

        assertNotNull(fromSegment);
        assertNotNull(toSegment);

        em.flush();

        assertNotNull(secondMovement.getTrack());
        List<Segment> segments = movementDao.getSegmentsByTrack(track);
        assertEquals(1, segments.size());
        List<Movement> movements = movementDao.getMovementsByTrack(track);
        assertEquals(2, movements.size());

        Movement beforeFirstMovement = movementHelpers.createMovement(1d, 1d, connectId, "BEFORE_ONE", date_before);

        segmentBean.addMovementBeforeFirst(firstMovement, beforeFirstMovement);
        em.flush();

        // get movement from db
        TypedQuery<Segment> qry =
                em.createQuery("select s from Segment s where s.track = :track order by s.updated desc", Segment.class);

        qry.setParameter("track", track);
        List<Segment> segmentList = qry.getResultList();

        assertNotNull(segmentList);
        assertEquals(2, segmentList.size());

        Segment  rsSegment1 = segmentList.get(0);
        Segment  rsSegment2 = segmentList.get(1);

        Long id1 = rsSegment1.getFromMovement().getId();
        Long id2 = rsSegment1.getToMovement().getId();
        Long id3 = rsSegment2.getFromMovement().getId();
        Long id4 = rsSegment2.getToMovement().getId();

        assertEquals(id1, beforeFirstMovement.getId());
        assertEquals(id2, firstMovement.getId());
        assertEquals(id3, firstMovement.getId());
        assertEquals(id4, secondMovement.getId());
    }
}
