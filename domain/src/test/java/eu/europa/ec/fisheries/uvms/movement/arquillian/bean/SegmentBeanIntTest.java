package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import eu.europa.ec.fisheries.schema.movement.v1.*;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.TestUtil;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.bean.SegmentBean;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
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

    private TestUtil testUtil = new TestUtil();

    @Test
    @OperateOnDeployment("normal")
    public void createSegmentOnFirstMovement() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException {
        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE");
        Movement toMovement = createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO");
        segmentBean.createSegmentOnFirstMovement(fromMovement, toMovement);
        em.flush();
        Assert.assertNotNull(toMovement.getTrack());
        Assert.assertEquals(1, toMovement.getTrack().getSegmentList().size());
        Assert.assertEquals(2, toMovement.getTrack().getMovementList().size());
    }

    @Test
    @OperateOnDeployment("normal")
    public void splitSegment() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException {
        String connectId = UUID.randomUUID().toString();


        Calendar cal = Calendar.getInstance();
        cal.set(1920,06,06);
        Date date1 = cal.getTime();
        cal.set(1930,06,06);
        Date date2 = cal.getTime();
        cal.set(1925,06,06);
        Date date3 = cal.getTime();

        Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        movementDao.upsertLatestMovement(fromMovement, fromMovement.getMovementConnect());
        Movement toMovement = createMovement(10d, 10d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);
        movementDao.upsertLatestMovement(toMovement, toMovement.getMovementConnect());

        segmentBean.createSegmentOnFirstMovement(fromMovement, toMovement);

        Movement newMovement = createMovement(.5d, .5d, 0d, SegmentCategoryType.GAP, connectId, "THREE", date3);
        movementDao.upsertLatestMovement(toMovement, toMovement.getMovementConnect());
        segmentBean.splitSegment(toMovement, newMovement);
        em.flush();

        Track track = newMovement.getTrack();
        Assert.assertNotNull(track);
        Assert.assertEquals(2, track.getSegmentList().size());
        Assert.assertEquals(3, track.getMovementList().size());
    }

    @Test
    @OperateOnDeployment("normal")
    public void createNewTrack() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException {

        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId);
        Movement toMovement = createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId);

        Segment segment = MovementModelToEntityMapper.createSegment(fromMovement, toMovement);

        Track track = segmentBean.createNewTrack(segment);
        em.flush();
        Assert.assertNotNull(track);
        Assert.assertEquals(1, track.getSegmentList().size());
        Assert.assertEquals(2, track.getMovementList().size());
    }

    /*
    @Test
    @OperateOnDeployment("normal")
    public void createNewTrack_onSegmentMovement() throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {

        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId);
        Movement toMovement = createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId);

        Segment segment = MovementModelToEntityMapper.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);
        em.flush();


        // assume this is the firstmovement since current api does not have anything else
        segmentBean.createSegmentOnFirstMovement(fromMovement, toMovement);


        Movement newMovement = createMovement(2d, 2d, 0d, SegmentCategoryType.GAP, connectId);


        Track track = segmentBean.createNewTrack(segment, newMovement);

        em.flush();
        Assert.assertNotNull(track);
        Assert.assertEquals(1, track.getSegmentList().size());
        Assert.assertEquals(2, track.getMovementList().size());


    }
    */

    @Test
    @OperateOnDeployment("normal")
    public void upsertTrack_forceNew() throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {

        // TODO  this creates 2 records in track   only difference is the updatetime timestamp
        // TODO  it   creates 1 record  in segment
        // TODO  it   creates 3 records in movement

        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId);
        Movement toMovement = createMovement(5d, 5d, 0d, SegmentCategoryType.GAP, connectId);
        Segment segment = MovementModelToEntityMapper.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);
        em.flush();
        Assert.assertNotNull(track);

        Movement newMovement = createMovement(10d, 10d, 0d, SegmentCategoryType.GAP, connectId);

        // null forces new
        Track createdTrack = segmentBean.upsertTrack(null, segment, newMovement);
        Assert.assertNotNull(createdTrack);
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateTrack()  throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {

        // TODO update seems not to update . After running this method segment point from 1 to 2 movement   nr 3 ???


        String connectId = UUID.randomUUID().toString();

        Movement fromMovement = createMovement(0d, 0d, 1d, SegmentCategoryType.EXIT_PORT, connectId, "user1");
        Movement toMovement = createMovement(5d, 5d, 2d, SegmentCategoryType.GAP, connectId, "user2");
        Segment segment = MovementModelToEntityMapper.createSegment(fromMovement, toMovement);
        Track track = segmentBean.createNewTrack(segment);
        em.flush();
        Assert.assertNotNull(track);

        Movement newMovement = createMovement(10d, 10d, 3d, SegmentCategoryType.GAP, connectId, "user3");
        segmentBean.updateTrack(track,  newMovement, segment);
    }

    @Test
    @OperateOnDeployment("normal")
    public void addMovementBeforeFirst() throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {


        String connectId = UUID.randomUUID().toString();

        Movement firstMovement = createMovement(2d, 2d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE");
        Movement secondMovement = createMovement(3d, 3d, 0d, SegmentCategoryType.GAP, connectId, "TWO");
        segmentBean.createSegmentOnFirstMovement(firstMovement, secondMovement);
        em.flush();

        Assert.assertNotNull(secondMovement.getTrack());
        Assert.assertEquals(1, secondMovement.getTrack().getSegmentList().size());
        Assert.assertEquals(2, secondMovement.getTrack().getMovementList().size());



        Movement beforeFirstMovement = createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "BEFOREONE");


        segmentBean.addMovementBeforeFirst(firstMovement, beforeFirstMovement);







    }


    /*****************************************************************************************************************************************************
     *  helpers
     *****************************************************************************************************************************************************/

    // old version
    private Movement createMovement(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        return createMovement(longitude, latitude, altitude, segmentCategoryType, connectId, "TEST");
    }

    // added possibility to specify user for easier debug
    private Movement createMovement(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId, String userName) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        MovementType movementType = testUtil.createMovementType(longitude, latitude, altitude, segmentCategoryType, connectId);
        movementType = movementBatchModelBean.createMovement(movementType, userName);
        em.flush();
        Assert.assertNotNull(movementType.getConnectId());
        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getConnectId());
        List<Movement> movementList = movementConnect.getMovementList();
        Assert.assertNotNull(movementList);
        return movementList.get(movementList.size() - 1);
    }

    /* positiontime is imortant */
    private Movement createMovement(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId, String userName, Date positionTime) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        MovementType movementType = testUtil.createMovementType(longitude, latitude, altitude, segmentCategoryType, connectId);
        movementType.setPositionTime(positionTime);
        movementType = movementBatchModelBean.createMovement(movementType, userName);
        em.flush();
        Assert.assertNotNull(movementType.getConnectId());
        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getConnectId());
        List<Movement> movementList = movementConnect.getMovementList();
        Assert.assertNotNull(movementList);
        return movementList.get(movementList.size() - 1);
    }
}
