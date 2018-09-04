package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.bean.SegmentBean;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.persistence.TypedQuery;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class SegmentBeanIntTest extends TransactionalTests {

    @EJB
    private SegmentBean segmentBean;

    @EJB
    private MovementBatchModelBean movementBatchModelBean;

    @EJB
    private MovementDao movementDao;

    @Test
    @OperateOnDeployment("normal")
    public void createSegmentOnFirstMovement() throws MovementDomainException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        String connectId = UUID.randomUUID().toString();

        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        Date date2 = cal.getTime();

        Movement fromMovement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        Movement toMovement = movementHelpers.createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);
        segmentBean.createSegmentAndTrack(fromMovement, toMovement);
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
        Movement movement1FromList = fetchedSegment.getTrack().getMovementList().get(0);
        Movement movement2FromList = fetchedSegment.getTrack().getMovementList().get(1);

        // verify that the id:s are different
        assertNotEquals(movement1FromList.getId(), movement2FromList.getId());
    }

    @Test
    @OperateOnDeployment("normal")
    public void createSegmentOnFirstMovement_OnlyOneMovement() throws MovementDomainException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        String connectId = UUID.randomUUID().toString();

        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();

        Movement movement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        segmentBean.createSegmentAndTrack(movement, movement);
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
        Movement movement1FromList = fetchedSegment.getTrack().getMovementList().get(0);
        Movement movement2FromList = fetchedSegment.getTrack().getMovementList().get(1);

        // verify that the id:s are same
        assertEquals(movement1FromList.getId(), movement2FromList.getId());
    }

    @Test
    @OperateOnDeployment("normal")
    public void splitSegment() throws MovementDomainException, MovementModelException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        // TODO nothing indicates that this splitFunction actually works
        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        Date date2 = cal.getTime();
        cal.set(1925, 06, 06);
        Date date3 = cal.getTime();

        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = movementHelpers.createMovement(2d, 2d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        Movement toMovement = movementHelpers.createMovement(3d, 3d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);
        Segment segment = segmentBean.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);
        fromMovement.setTrack(track);
        toMovement.setTrack(track);

        em.flush();

        assertNotNull(toMovement.getTrack());
        assertEquals(1, toMovement.getTrack().getSegmentList().size());
        assertEquals(2, toMovement.getTrack().getMovementList().size());

//--------------------------------------------------------------------------

        Movement newMovement = movementHelpers.createMovement(.5d, .5d, 0d, SegmentCategoryType.GAP, connectId, "THREE", date3);
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
    public void createNewTrack() throws MovementDomainException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        String connectId = UUID.randomUUID().toString();

        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        Date date2 = cal.getTime();

        Movement fromMovement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "one", date1);
        Movement toMovement = movementHelpers.createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "two", date2);

        Segment segment = segmentBean.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);

        fromMovement.setTrack(track);
        toMovement.setTrack(track);

        movementDao.upsertLatestMovement(fromMovement, fromMovement.getMovementConnect());
        movementDao.upsertLatestMovement(toMovement, toMovement.getMovementConnect());

        em.flush();
        assertNotNull(track);
        assertEquals(1, track.getSegmentList().size());
        assertEquals(2, track.getMovementList().size());

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
    public void createNewTrack_onSegmentMovement() throws MovementDomainException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "TEST", DateUtil.nowUTC());
        Movement toMovement = movementHelpers.createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TEST", DateUtil.nowUTC());

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
    public void upsertTrack() throws MovementDomainException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        Date date2 = cal.getTime();
        cal.set(1935, 06, 06);
        Date date3 = cal.getTime();

        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "one", date1);
        Movement toMovement = movementHelpers.createMovement(5d, 5d, 0d, SegmentCategoryType.GAP, connectId, "two", date2);
        Segment segment = segmentBean.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);
        assertNotNull(track);

        fromMovement.setTrack(track);
        toMovement.setTrack(track);

        em.flush();

        Movement newMovement = movementHelpers.createMovement(10d, 10d, 0d, SegmentCategoryType.GAP, connectId, "three", date3);
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
    public void upsertTrack5() throws MovementDomainException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();
        cal.set(1925, 06, 06);
        Date date2 = cal.getTime();
        cal.set(1930, 06, 06);
        Date date3 = cal.getTime();
        cal.set(1935, 06, 06);
        Date date4 = cal.getTime();
        cal.set(1940, 06, 06);
        Date date5 = cal.getTime();
        cal.set(1945, 06, 06);
        Date date6 = cal.getTime();

        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "one", date1);
        Movement toMovement = movementHelpers.createMovement(5d, 5d, 0d, SegmentCategoryType.GAP, connectId, "two", date2);
        Segment segment = segmentBean.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);
        fromMovement.setTrack(track);
        toMovement.setTrack(track);
        em.flush();
        assertNotNull(track);

        Movement newMovement = movementHelpers.createMovement(10d, 10d, 0d, SegmentCategoryType.GAP, connectId, "three", date3);
        segment = segmentBean.createSegment(toMovement, newMovement);
        Track createdTrack = segmentBean.upsertTrack(track, segment, newMovement);
        assertEquals(createdTrack.getId(), track.getId());

        Movement oldMovement = newMovement;
        newMovement = movementHelpers.createMovement(20d, 20d, 0d, SegmentCategoryType.GAP, connectId, "four", date4);
        segment = segmentBean.createSegment(oldMovement, newMovement);
        segmentBean.upsertTrack(track, segment, newMovement);

        oldMovement = newMovement;
        newMovement = movementHelpers.createMovement(30d, 30d, 0d, SegmentCategoryType.GAP, connectId, "five", date5);
        segment = segmentBean.createSegment(oldMovement, newMovement);
        segmentBean.upsertTrack(track, segment, newMovement);

        oldMovement = newMovement;
        newMovement = movementHelpers.createMovement(40d, 40d, 0d, SegmentCategoryType.GAP, connectId, "six", date6);
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
    public void updateTrack() throws MovementDomainException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();
        cal.set(1925, 06, 06);
        Date date2 = cal.getTime();
        cal.set(1930, 06, 06);
        Date date3 = cal.getTime();

        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = movementHelpers.createMovement(0d, 0d, 1d, SegmentCategoryType.EXIT_PORT, connectId, "user1", date1);
        Movement toMovement = movementHelpers.createMovement(5d, 5d, 2d, SegmentCategoryType.GAP, connectId, "user2", date2);
        Segment segment = segmentBean.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);
        fromMovement.setTrack(track);
        toMovement.setTrack(track);
        assertNotNull(track);

        Movement newMovement = movementHelpers.createMovement(10d, 10d, 3d, SegmentCategoryType.GAP, connectId, "user3", date3);
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
    public void addMovementBeforeFirst() throws MovementDomainException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        // TODO better evaluation of results

        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();
        cal.set(1925, 06, 06);
        Date date2 = cal.getTime();
        cal.set(1910, 06, 06);
        Date date_before = cal.getTime();

        String connectId = UUID.randomUUID().toString();

        Movement firstMovement = movementHelpers.createMovement(2d, 2d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        Movement secondMovement = movementHelpers.createMovement(3d, 3d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);
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
        assertEquals(1, secondMovement.getTrack().getSegmentList().size());
        assertEquals(2, secondMovement.getTrack().getMovementList().size());

        Movement beforeFirstMovement = movementHelpers.createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "BEFORE_ONE", date_before);

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
