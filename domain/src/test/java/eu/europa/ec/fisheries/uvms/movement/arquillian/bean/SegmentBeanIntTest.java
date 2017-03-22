package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import eu.europa.ec.fisheries.schema.movement.v1.*;
import eu.europa.ec.fisheries.uvms.movement.arquillian.BuildMovementTestDeployment;
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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
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

    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementTestDeployment.createDeployment();
    }


    @Test
    public void createSegmentOnFirstMovement() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException {
        String uuid = UUID.randomUUID().toString();

        Movement fromMovement = createMovement(0d,0d,0d, SegmentCategoryType.EXIT_PORT, uuid);
        Movement toMovement = createMovement(1d,1d,0d, SegmentCategoryType.GAP, uuid);
        segmentBean.createSegmentOnFirstMovement(fromMovement, toMovement);
        em.flush();
        Assert.assertNotNull(toMovement.getTrack());
        Assert.assertEquals(1, toMovement.getTrack().getSegmentList().size());
        Assert.assertEquals(2, toMovement.getTrack().getMovementList().size());
    }

    @Test
    public void splitSegment() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException {
        String uuid = UUID.randomUUID().toString();

        Movement fromMovement = createMovement(0d,0d,0d, SegmentCategoryType.EXIT_PORT, uuid);
        movementDao.upsertLatestMovement(fromMovement, fromMovement.getMovementConnect());
        Movement toMovement = createMovement(1d,1d,0d, SegmentCategoryType.GAP, uuid);
        movementDao.upsertLatestMovement(toMovement, toMovement.getMovementConnect());

        segmentBean.createSegmentOnFirstMovement(fromMovement, toMovement);

        Movement newMovement = createMovement(.5d,.5d,0d, SegmentCategoryType.GAP, uuid);
        movementDao.upsertLatestMovement(toMovement, toMovement.getMovementConnect());
        segmentBean.splitSegment(toMovement, newMovement);
        em.flush();

        Track track = newMovement.getTrack();
        Assert.assertNotNull(track);
        Assert.assertEquals(2, track.getSegmentList().size());
        Assert.assertEquals(3, track.getMovementList().size());
    }

    @Test
    public void createNewTrack() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException {

        String uuid = UUID.randomUUID().toString();

        Movement fromMovement = createMovement(0d,0d,0d, SegmentCategoryType.EXIT_PORT, uuid);
        Movement toMovement = createMovement(1d,1d,0d, SegmentCategoryType.GAP, uuid);

        Segment segment = MovementModelToEntityMapper.createSegment(fromMovement, toMovement);

        Track track = segmentBean.createNewTrack(segment);
        em.flush();
        Assert.assertNotNull(track);
        Assert.assertEquals(1, track.getSegmentList().size());
        Assert.assertEquals(2, track.getMovementList().size());
    }



    private Movement createMovement(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        MovementType movementType = testUtil.createMovementType(longitude, latitude, altitude, segmentCategoryType, connectId);
        movementType = movementBatchModelBean.createMovement(movementType, "TEST");
        em.flush();
        Assert.assertNotNull(movementType.getConnectId());
        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getConnectId());
        List<Movement> movementList = movementConnect.getMovementList();
        Assert.assertNotNull(movementList);
        //Assert.assertTrue(movementList.size() == 1);
        int size = movementList.size();
        Long id = movementList.get(size-1).getId();
        return movementDao.getMovementById(id);
    }


}
