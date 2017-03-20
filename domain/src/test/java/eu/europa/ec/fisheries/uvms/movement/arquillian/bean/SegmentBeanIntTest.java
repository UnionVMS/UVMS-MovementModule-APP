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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.util.List;

@RunWith(Arquillian.class)
@Ignore
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

        Movement fromMovement = createMovement(0d,0d,0d, SegmentCategoryType.EXIT_PORT);
        Movement toMovement = createMovement(1d,1d,0d, SegmentCategoryType.GAP);
        segmentBean.createSegmentOnFirstMovement(fromMovement, toMovement);
        em.flush();
        Assert.assertTrue(true);
    }

    @Test
    public void splitSegment() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException {
        Movement fromMovement = createMovement(0d,0d,0d, SegmentCategoryType.EXIT_PORT);
        Movement toMovement = createMovement(1d,1d,0d, SegmentCategoryType.GAP);
        segmentBean.createSegmentOnFirstMovement(fromMovement, toMovement);

        Movement newMovement = createMovement(.5d,.5d,0d, SegmentCategoryType.GAP);
        segmentBean.splitSegment(toMovement, newMovement);
        em.flush();
    }

    @Test
    public void fail_splitSegment() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException {
        Movement fromMovement = createMovement(0d,0d,0d, SegmentCategoryType.EXIT_PORT);
        Movement toMovement = createMovement(1d,1d,0d, SegmentCategoryType.GAP);
        segmentBean.createSegmentOnFirstMovement(fromMovement, toMovement);

        Movement newMovement = createMovement(1d,2d,0d, SegmentCategoryType.GAP);
        segmentBean.splitSegment(toMovement, newMovement);
        em.flush();
    }

    @Test
    public void createNewTrack() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException {

        Movement fromMovement = createMovement(0d,0d,0d, SegmentCategoryType.EXIT_PORT);
        Movement toMovement = createMovement(1d,1d,0d, SegmentCategoryType.GAP);

        Segment segment = MovementModelToEntityMapper.createSegment(fromMovement, toMovement);

        Movement newMovement = createMovement(1d,2d,0d, SegmentCategoryType.GAP);

        Track track = segmentBean.createNewTrack(segment, newMovement);
        em.flush();
        Assert.assertNotNull(track);
    }



    private Movement createMovement(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        MovementType movementType = testUtil.createMovementType(longitude, latitude, altitude, segmentCategoryType);
        movementType = movementBatchModelBean.createMovement(movementType, "TEST");
        em.flush();
        Assert.assertNotNull(movementType.getConnectId());
        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getConnectId());
        List<Movement> movementList = movementConnect.getMovementList();
        Assert.assertNotNull(movementList);
        Assert.assertTrue(movementList.size() == 1);
        Long id = movementList.get(0).getId();
        return movementDao.getMovementById(id);
    }


}
