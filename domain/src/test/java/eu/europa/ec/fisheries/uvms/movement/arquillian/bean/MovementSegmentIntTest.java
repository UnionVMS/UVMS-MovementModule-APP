package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import com.vividsolutions.jts.geom.LineString;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.bean.IncomingMovementBean;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.transaction.SystemException;
import java.util.*;

@RunWith(Arquillian.class)
public class MovementSegmentIntTest extends TransactionalTests {

    private static int ALL = -1;
    private static int ORDER_NORMAL = 1;
    private static int ORDER_REVERSED = 2;
    private static int ORDER_RANDOM = 3;

    private static Logger LOGGER = LoggerFactory.getLogger(MovementSegmentIntTest.class);

    @EJB
    MovementBatchModelBean movementBatchModelBean;

    @EJB
    MovementDao movementDao;

    @EJB
    IncomingMovementBean incomingMovementBean;


    @Test
    @OperateOnDeployment("normal")
    public void createThreeMovementTrackInOrder() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException, SystemException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        String connectId = UUID.randomUUID().toString();

        Date dateFirstMovement = Calendar.getInstance().getTime();
        Date dateSecondMovement = new Date(dateFirstMovement.getTime() + 300000);
        Date dateThirdMovement = new Date(dateSecondMovement.getTime() + 300000);

        Movement firstMovement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", dateFirstMovement);
        Movement secondMovement = movementHelpers.createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", dateSecondMovement);
        Movement thirdMovement = movementHelpers.createMovement(2d, 2d, 0d, SegmentCategoryType.GAP, connectId, "THREE", dateThirdMovement);

        incomingMovementBean.processMovement(firstMovement.getId());
        incomingMovementBean.processMovement(secondMovement.getId());
        incomingMovementBean.processMovement(thirdMovement.getId());

        em.flush();

        Movement firstAfter = movementDao.getMovementById(firstMovement.getId());

        Assert.assertEquals(2, firstAfter.getTrack().getSegmentList().size());

        Track track = firstAfter.getTrack();
        Segment s1 = track.getSegmentList().get(0);
        Assert.assertEquals(s1.getFromMovement().getId(), firstMovement.getId());
        Assert.assertEquals(s1.getToMovement().getId(), secondMovement.getId());

        Segment s2 = track.getSegmentList().get(1);
        Assert.assertEquals(s2.getFromMovement().getId(), secondMovement.getId());
        Assert.assertEquals(s2.getToMovement().getId(), thirdMovement.getId());


    }

    @Test
    @OperateOnDeployment("normal")
    public void createFourMovementTrackInOrder() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException, SystemException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        String connectId = UUID.randomUUID().toString();

        Date dateFirstMovement = Calendar.getInstance().getTime();
        Date dateSecondMovement = new Date(dateFirstMovement.getTime() + 300000);
        Date dateThirdMovement = new Date(dateSecondMovement.getTime() + 300000);
        Date dateForthMovement = new Date(dateThirdMovement.getTime() + 300000);

        Movement firstMovement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", dateFirstMovement);
        Movement secondMovement = movementHelpers.createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", dateSecondMovement);
        Movement thirdMovement = movementHelpers.createMovement(2d, 2d, 0d, SegmentCategoryType.GAP, connectId, "THREE", dateThirdMovement);
        Movement forthMovement = movementHelpers.createMovement(3d, 3d, 0d, SegmentCategoryType.GAP, connectId, "FORTH", dateForthMovement);

        incomingMovementBean.processMovement(firstMovement.getId());
        incomingMovementBean.processMovement(secondMovement.getId());
        incomingMovementBean.processMovement(thirdMovement.getId());
        incomingMovementBean.processMovement(forthMovement.getId());

        em.flush();

        Movement firstAfter = movementDao.getMovementById(firstMovement.getId());

        Assert.assertEquals(3, firstAfter.getTrack().getSegmentList().size());

        Track track = firstAfter.getTrack();
        Segment s1 = track.getSegmentList().get(0);
        Assert.assertEquals(s1.getFromMovement().getId(), firstMovement.getId());
        Assert.assertEquals(s1.getToMovement().getId(), secondMovement.getId());

        Segment s2 = track.getSegmentList().get(1);
        Assert.assertEquals(s2.getFromMovement().getId(), secondMovement.getId());
        Assert.assertEquals(s2.getToMovement().getId(), thirdMovement.getId());

        Segment s3 = track.getSegmentList().get(2);
        Assert.assertEquals(s3.getFromMovement().getId(), thirdMovement.getId());
        Assert.assertEquals(s3.getToMovement().getId(), forthMovement.getId());

    }


    @Test
    @OperateOnDeployment("normal")
    public void createFourMovementTrackOutOfOrder() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException, SystemException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);

        String connectId = UUID.randomUUID().toString();

        Date dateFirstMovement = Calendar.getInstance().getTime();
        Date dateSecondMovement = new Date(dateFirstMovement.getTime() + 300000);
        Date dateThirdMovement = new Date(dateSecondMovement.getTime() + 300000);
        Date dateForthMovement = new Date(dateThirdMovement.getTime() + 300000);

        Movement firstMovement = movementHelpers.createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", dateFirstMovement);
        Movement secondMovement = movementHelpers.createMovement(2d, 2d, 0d, SegmentCategoryType.GAP, connectId, "THREE", dateThirdMovement);
        Movement thirdMovement = movementHelpers.createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", dateSecondMovement);
        Movement forthMovement = movementHelpers.createMovement(3d, 3d, 0d, SegmentCategoryType.GAP, connectId, "FORTH", dateForthMovement);

        incomingMovementBean.processMovement(firstMovement.getId());
        em.flush();
        incomingMovementBean.processMovement(secondMovement.getId());
        em.flush();
        incomingMovementBean.processMovement(thirdMovement.getId());
        em.flush();
        incomingMovementBean.processMovement(forthMovement.getId());
        em.flush();


        Movement firstAfter = movementDao.getMovementById(firstMovement.getId());

        Assert.assertEquals(3, firstAfter.getTrack().getSegmentList().size());

        Track track = firstAfter.getTrack();
        Segment s1 = track.getSegmentList().get(0);
        Assert.assertEquals(s1.getFromMovement().getId(), firstMovement.getId());
        Assert.assertEquals(s1.getToMovement().getId(), thirdMovement.getId());

        Segment s2 = track.getSegmentList().get(1);
        Assert.assertEquals(s2.getFromMovement().getId(), thirdMovement.getId());
        Assert.assertEquals(s2.getToMovement().getId(), secondMovement.getId());

        Segment s3 = track.getSegmentList().get(2);
        Assert.assertEquals(s3.getFromMovement().getId(), secondMovement.getId());
        Assert.assertEquals(s3.getToMovement().getId(), forthMovement.getId());

    }

    @Test
    @OperateOnDeployment("normal")
    public void createVarbergGrenaNormal() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException, SystemException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
        String connectId = UUID.randomUUID().toString();

        List<Movement> rs = movementHelpers.createVarbergGrenaMovements(ORDER_NORMAL, ALL ,connectId);
        for(Movement movement : rs){
            incomingMovementBean.processMovement(movement.getId());
            em.flush();
        }

        int n = rs.size();
        int i = 0;

        Movement previousMovement = null;
        while(i < n){
            Movement currentMovement = rs.get(i);
            if(i == 0){
                previousMovement = currentMovement;
                i++;
                continue;
            }
            Track track = currentMovement.getTrack();
            Segment  segment = track.getSegmentList().get(i - 1);
            Assert.assertEquals(segment.getFromMovement().getId(), previousMovement.getId());
            Assert.assertEquals(segment.getToMovement().getId(), currentMovement.getId());
            i++;
            if(i < n){
                previousMovement = currentMovement;
            }
        }
        Assert.assertEquals(rs.size(), i);
    }

    @Test
    @OperateOnDeployment("normal")
    public void createVarbergGrenaBasedOnSegmentList() throws MovementDaoMappingException, MovementDaoException, GeometryUtilException, MovementModelException, MovementDuplicateException, SystemException {
        MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
        String connectId = UUID.randomUUID().toString();

        List<Movement> rs = movementHelpers.createVarbergGrenaMovements(ORDER_REVERSED, ALL ,connectId);
        for(Movement movement : rs){
            incomingMovementBean.processMovement(movement);
            em.flush();
        }


        Movement aMovement = rs.get(rs.size() - 1);
        Track track = aMovement.getTrack();
        List<Segment> segmentList = track.getSegmentList();
        int n = segmentList.size();
        int i = 0;
        Assert.assertEquals(segmentList.size(), rs.size() - 1);



        Collections.sort(segmentList, new Comparator<Segment>(){
            public int compare(Segment s1, Segment s2) {
                return s1.getFromMovement().getTimestamp().compareTo(s2.getFromMovement().getTimestamp());
            }
        });




        Segment previousSegment = null;
        while(i < n){
            Segment currentSegment = segmentList.get(i);
            if(i == 0){
                previousSegment = currentSegment;
                i++;
                continue;
            }
            Assert.assertEquals(currentSegment.getFromMovement().getId(), previousSegment.getToMovement().getId());
            i++;
            if(i < n){
                previousSegment = currentSegment;
            }
        }
        Assert.assertEquals(segmentList.size(), i);
    }



}

