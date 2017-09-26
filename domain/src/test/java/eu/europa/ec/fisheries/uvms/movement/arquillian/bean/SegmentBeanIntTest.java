package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import eu.europa.ec.fisheries.schema.movement.v1.*;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.bean.SegmentBean;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.persistence.TypedQuery;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RunWith(Arquillian.class)
public class SegmentBeanIntTest extends TransactionalTests {

    @EJB
    SegmentBean segmentBean;

    @EJB
    MovementBatchModelBean movementBatchModelBean;

    @EJB
    MovementDao movementDao;


    

    @Test
    @OperateOnDeployment("normal")
    public void createSegmentOnFirstMovement() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        String connectId = UUID.randomUUID().toString();

        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        Date date2 = cal.getTime();

        Movement fromMovement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        Movement toMovement = movementHelpers.createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);
        segmentBean.createSegmentOnFirstMovement(fromMovement, toMovement);
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
        Assert.assertFalse(movement1FromList.getId().equals(movement2FromList.getId()));

    }







    @Test
    @OperateOnDeployment("normal")
    public void createSegmentOnFirstMovement_OnlyOneMovement() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        String connectId = UUID.randomUUID().toString();

        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();

        Movement movement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        segmentBean.createSegmentOnFirstMovement(movement, movement);
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
        Assert.assertTrue(movement1FromList.getId().equals(movement2FromList.getId()));


    }

    @Test
    @OperateOnDeployment("normal")
    public void splitSegment() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException {
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


        Assert.assertNotNull(toMovement.getTrack());
        Assert.assertEquals(1, toMovement.getTrack().getSegmentList().size());
        Assert.assertEquals(2, toMovement.getTrack().getMovementList().size());


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
        List<Segment> rs = qry.getResultList();

        Assert.assertTrue(rs != null);
        Assert.assertTrue(rs.size() == 2);

        Segment rsSegment1 = rs.get(0);
        Segment rsSegment2 = rs.get(1);
        Long id1 = rsSegment1.getFromMovement().getId();
        Long id2 = rsSegment1.getToMovement().getId();
        Long id3 = rsSegment2.getFromMovement().getId();
        Long id4 = rsSegment2.getToMovement().getId();


        // this is how it works no - but it is not OK
        Assert.assertTrue(id1.equals(fromMovement.getId()));
        Assert.assertTrue(id2.equals(toMovement.getId()));
        Assert.assertTrue(id3.equals(toMovement.getId()));
        Assert.assertTrue(id4.equals(newMovement.getId())); ///


    }

    @Test
    @OperateOnDeployment("normal")
    public void createNewTrack() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException {
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
        Assert.assertNotNull(track);
        Assert.assertEquals(1, track.getSegmentList().size());
        Assert.assertEquals(2, track.getMovementList().size());

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
        Assert.assertTrue(fetchedFromMovement != null);
        Assert.assertTrue(fetchedToMovement != null);
        Assert.assertTrue(fetchedFromMovement.getTrack() != null);
        Assert.assertTrue(fetchedToMovement.getTrack() != null);
        Assert.assertTrue(fetchedFromMovement.getTrack().getId() != null);
        Assert.assertTrue(fetchedToMovement.getTrack().getId() != null);

        Long trackFromId = fetchedFromMovement.getTrack().getId();
        Long trackToId = fetchedToMovement.getTrack().getId();

        Assert.assertTrue(trackFromId.equals(trackToId));
    }


    @Test
    @OperateOnDeployment("normal")
    public void createNewTrack_onSegmentMovement() throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId);
        Movement toMovement = movementHelpers.createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId);

        Segment segment = segmentBean.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);

        fromMovement.setTrack(track);
        toMovement.setTrack(track);

        em.flush();

        Movement newMovement = movementHelpers.createMovement(2d, 2d, 0d, SegmentCategoryType.GAP, connectId);

        Track theNewTrack = segmentBean.createNewTrack(segment);

        em.flush();

        Assert.assertFalse(track.getId().equals(theNewTrack.getId()));
    }


    @Test
    @OperateOnDeployment("normal")
    public void upsertTrack() throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {
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
        Assert.assertNotNull(track);

        fromMovement.setTrack(track);
        toMovement.setTrack(track);

        em.flush();

        Movement newMovement = movementHelpers.createMovement(10d, 10d, 0d, SegmentCategoryType.GAP, connectId, "three", date3);
        segment = segmentBean.createSegment(toMovement, newMovement);

        Track createdTrack = segmentBean.upsertTrack(track, segment, newMovement);
        Assert.assertNotNull(createdTrack);

        // get movement from db
        TypedQuery<Segment> qry =
                em.createQuery("select s from Segment s where s.track = :track", Segment.class);

        qry.setParameter("track", track);
        List<Segment> rs = qry.getResultList();

        Assert.assertTrue(rs != null);
        Assert.assertTrue(rs.size() == 2);
    }

    @Test
    @OperateOnDeployment("normal")
    public void upsertTrack5() throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {
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
        Assert.assertNotNull(track);

        Movement newMovement = movementHelpers.createMovement(10d, 10d, 0d, SegmentCategoryType.GAP, connectId, "three", date3);
        segment = segmentBean.createSegment(toMovement, newMovement);
        Track createdTrack = segmentBean.upsertTrack(track, segment, newMovement);

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
        List<Segment> rs = qry.getResultList();

        Assert.assertTrue(rs != null);
        Assert.assertTrue(rs.size() == 5);
    }


    @Test
    @OperateOnDeployment("normal")
    public void updateTrack() throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {
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
        Assert.assertNotNull(track);

        Movement newMovement = movementHelpers.createMovement(10d, 10d, 3d, SegmentCategoryType.GAP, connectId, "user3", date3);
        segment = segmentBean.createSegment(toMovement, newMovement);
        segmentBean.updateTrack(track, newMovement, segment);

        // get movement from db
        TypedQuery<Segment> qry =
                em.createQuery("select s from Segment s where s.track = :track", Segment.class);

        qry.setParameter("track", track);
        List<Segment> rs = qry.getResultList();

        Assert.assertTrue(rs != null);
        Assert.assertTrue(rs.size() == 2);



    }

    @Test
    @OperateOnDeployment("normal")
    public void addMovementBeforeFirst() throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {
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
        Track track = segmentBean.createNewTrack(segment);
        firstMovement.setTrack(track);
        secondMovement.setTrack(track);

        Segment fromSegment = firstMovement.getFromSegment();
        Segment toSegment = secondMovement.getToSegment();
        em.flush();


        Assert.assertNotNull(secondMovement.getTrack());
        Assert.assertEquals(1, secondMovement.getTrack().getSegmentList().size());
        Assert.assertEquals(2, secondMovement.getTrack().getMovementList().size());

        Movement beforeFirstMovement = movementHelpers.createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "BEFORE_ONE", date_before);

        segmentBean.addMovementBeforeFirst(firstMovement, beforeFirstMovement);

        em.flush();

        // get movement from db
        TypedQuery<Segment> qry =
                em.createQuery("select s from Segment s where s.track = :track order by s.updated desc", Segment.class);

        qry.setParameter("track", track);
        List<Segment> rs = qry.getResultList();

        Assert.assertTrue(rs != null);
        Assert.assertTrue(rs.size() == 2);



        Segment  rsSegment1 = rs.get(0);
        Segment  rsSegment2 = rs.get(1);

        Long id1 = rsSegment1.getFromMovement().getId();
        Long id2 = rsSegment1.getToMovement().getId();
        Long id3 = rsSegment2.getFromMovement().getId();
        Long id4 = rsSegment2.getToMovement().getId();



        Assert.assertTrue(id1.equals(beforeFirstMovement.getId()));
        Assert.assertTrue(id2.equals(firstMovement.getId()));
        Assert.assertTrue(id3.equals(firstMovement.getId()));
        Assert.assertTrue(id4.equals(secondMovement.getId())); ///
    }
    
}
