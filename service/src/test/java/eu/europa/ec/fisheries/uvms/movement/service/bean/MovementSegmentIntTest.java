package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.uvms.movement.service.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Track;

@RunWith(Arquillian.class)
public class MovementSegmentIntTest extends TransactionalTests {

    private static final int ALL = -1;
    private static final int ORDER_NORMAL = 1;
    private static final int ORDER_REVERSED = 2;
    private static final int ORDER_RANDOM = 3;

    @EJB
    private MovementService movementService;

    @EJB
    private MovementDao movementDao;

    @EJB
    private IncomingMovementBean incomingMovementBean;

    @Test
    @OperateOnDeployment("movementservice")
    public void createThreeMovementTrackInOrder()  {
        MovementHelpers movementHelpers = new MovementHelpers(movementService);

        UUID connectId = UUID.randomUUID();

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
        Movement secondAfter = movementDao.getMovementById(secondMovement.getId());
        Movement thirdAfter = movementDao.getMovementById(thirdMovement.getId());

        Track track = firstAfter.getTrack();

        List<MicroMovement> movements = movementDao.getMicroMovementsDtoByTrack(track, 100);
        assertThat(movements.get(2).getGuid(), is(firstMovement.getId().toString()));
        assertThat(movements.get(1).getGuid(), is(secondMovement.getId().toString()));
        assertThat(movements.get(0).getGuid(), is(thirdMovement.getId().toString()));
        
        assertThat(firstAfter.getPreviousMovement(), is(CoreMatchers.nullValue()));
        assertThat(secondAfter.getPreviousMovement(), is(firstAfter));
        assertThat(thirdAfter.getPreviousMovement(), is(secondAfter));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createFourMovementTrackInOrder()  {
        MovementHelpers movementHelpers = new MovementHelpers(movementService);

        UUID connectId = UUID.randomUUID();

        Instant firstMovementDate = Instant.now();
        Instant secondMovementDate = firstMovementDate.plusMillis(300000);
        Instant thirdMovementDate  = firstMovementDate.plusMillis(600000);
        Instant fourthMovementDate  = firstMovementDate.plusMillis(900000);

        Movement firstMovement = movementHelpers.createMovement(0d, 0d, connectId, "ONE", firstMovementDate);
        Movement secondMovement = movementHelpers.createMovement(1d, 1d, connectId, "TWO", secondMovementDate);
        Movement thirdMovement = movementHelpers.createMovement(2d, 2d, connectId, "THREE", thirdMovementDate);
        Movement fourthMovement = movementHelpers.createMovement(3d, 3d, connectId, "FORTH", fourthMovementDate);

        incomingMovementBean.processMovement(firstMovement);
        incomingMovementBean.processMovement(secondMovement);
        incomingMovementBean.processMovement(thirdMovement);
        incomingMovementBean.processMovement(fourthMovement);
        em.flush();

        Movement firstAfter = movementDao.getMovementById(firstMovement.getId());
        Movement secondAfter = movementDao.getMovementById(secondMovement.getId());
        Movement thirdAfter = movementDao.getMovementById(thirdMovement.getId());
        Movement fourthAfter = movementDao.getMovementById(fourthMovement.getId());
        Track track = firstAfter.getTrack();

        List<MicroMovement> movements = movementDao.getMicroMovementsDtoByTrack(track, 100);
        assertThat(movements.get(3).getGuid(), is(firstMovement.getId().toString()));
        assertThat(movements.get(2).getGuid(), is(secondMovement.getId().toString()));
        assertThat(movements.get(1).getGuid(), is(thirdMovement.getId().toString()));
        assertThat(movements.get(0).getGuid(), is(fourthMovement.getId().toString()));
        
        assertThat(firstAfter.getPreviousMovement(), is(CoreMatchers.nullValue()));
        assertThat(secondAfter.getPreviousMovement(), is(firstAfter));
        assertThat(thirdAfter.getPreviousMovement(), is(secondAfter));
        assertThat(fourthAfter.getPreviousMovement(), is(thirdAfter));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createFourMovementTrackOutOfOrder()  {
        MovementHelpers movementHelpers = new MovementHelpers(movementService);

        UUID connectId = UUID.randomUUID();

        Instant firstMovementDate = Instant.now();
        Instant secondMovementDate = firstMovementDate.plusMillis(300000);
        Instant thirdMovementDate  = firstMovementDate.plusMillis(600000);
        Instant fourthMovementDate  = firstMovementDate.plusMillis(900000);

        Movement firstMovement = movementHelpers.createMovement(0d, 0d, connectId, "ONE", firstMovementDate);
        Movement thirdMovement = movementHelpers.createMovement(1d, 1d, connectId, "TWO", thirdMovementDate);
        Movement fourthMovement = movementHelpers.createMovement(3d, 3d, connectId, "FOURTH", fourthMovementDate);

        incomingMovementBean.processMovement(firstMovement);
        incomingMovementBean.processMovement(fourthMovement);
        incomingMovementBean.processMovement(thirdMovement);

        Movement secondMovement = movementHelpers.createMovement(2d, 2d, connectId, "THREE", secondMovementDate);
        incomingMovementBean.processMovement(secondMovement);
        
        Movement firstAfter = movementDao.getMovementById(firstMovement.getId());
        Movement secondAfter = movementDao.getMovementById(secondMovement.getId());
        Movement thirdAfter = movementDao.getMovementById(thirdMovement.getId());
        Movement fourthAfter = movementDao.getMovementById(fourthMovement.getId());
        Track track = firstAfter.getTrack();

        List<MicroMovement> movements = movementDao.getMicroMovementsDtoByTrack(track, 100);
        assertThat(movements.get(3).getGuid(), is(firstMovement.getId().toString()));
        assertThat(movements.get(2).getGuid(), is(secondMovement.getId().toString()));
        assertThat(movements.get(1).getGuid(), is(thirdMovement.getId().toString()));
        assertThat(movements.get(0).getGuid(), is(fourthMovement.getId().toString()));
        
        assertThat(firstAfter.getPreviousMovement(), is(CoreMatchers.nullValue()));
        assertThat(secondAfter.getPreviousMovement(), is(firstAfter));
        assertThat(thirdAfter.getPreviousMovement(), is(secondAfter));
        assertThat(fourthAfter.getPreviousMovement(), is(thirdAfter));
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void createFourMovementTrackOutOfOrder2()  {
        MovementHelpers movementHelpers = new MovementHelpers(movementService);

        UUID connectId = UUID.randomUUID();

        Instant firstMovementDate = Instant.now();
        Instant secondMovementDate = firstMovementDate.plusMillis(300000);
        Instant thirdMovementDate  = firstMovementDate.plusMillis(600000);
        Instant fourthMovementDate  = firstMovementDate.plusMillis(900000);

        Movement firstMovement = movementHelpers.createMovement(0d, 0d, connectId, "ONE", firstMovementDate);
        Movement secondMovement = movementHelpers.createMovement(2d, 2d, connectId, "THREE", secondMovementDate);
        Movement thirdMovement = movementHelpers.createMovement(1d, 1d, connectId, "TWO", thirdMovementDate);
        Movement fourthMovement = movementHelpers.createMovement(3d, 3d, connectId, "FOURTH", fourthMovementDate);

        incomingMovementBean.processMovement(secondMovement);
        incomingMovementBean.processMovement(firstMovement);
        incomingMovementBean.processMovement(fourthMovement);
        incomingMovementBean.processMovement(thirdMovement);
        
        Movement firstAfter = movementDao.getMovementById(firstMovement.getId());
        Movement secondAfter = movementDao.getMovementById(secondMovement.getId());
        Movement thirdAfter = movementDao.getMovementById(thirdMovement.getId());
        Movement fourthAfter = movementDao.getMovementById(fourthMovement.getId());
        Track track = firstAfter.getTrack();

        List<MicroMovement> movements = movementDao.getMicroMovementsDtoByTrack(track, 100);
        assertThat(movements.get(3).getGuid(), is(firstMovement.getId().toString()));
        assertThat(movements.get(2).getGuid(), is(secondMovement.getId().toString()));
        assertThat(movements.get(1).getGuid(), is(thirdMovement.getId().toString()));
        assertThat(movements.get(0).getGuid(), is(fourthMovement.getId().toString()));
        
        assertThat(firstAfter.getPreviousMovement(), is(CoreMatchers.nullValue()));
        assertThat(secondAfter.getPreviousMovement(), is(firstAfter));
        assertThat(thirdAfter.getPreviousMovement(), is(secondAfter));
        assertThat(fourthAfter.getPreviousMovement(), is(thirdAfter));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createVarbergGrenaNormal()  {
        MovementHelpers movementHelpers = new MovementHelpers(movementService);
        UUID connectId = UUID.randomUUID();

        List<Movement> movementList = movementHelpers.createVarbergGrenaMovements(ORDER_NORMAL, ALL, connectId);
        assertMovementIds(movementList);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createVarbergGrenaBasedOnReversedOrder()  {
        testVarbergGrenaBasedOnOrdering(ORDER_REVERSED);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createVarbergGrenaBasedOnRandomOrder()  {
        testVarbergGrenaBasedOnOrdering(ORDER_RANDOM);
    }

    private void testVarbergGrenaBasedOnOrdering(int order)  {
        MovementHelpers movementHelpers = new MovementHelpers(movementService);
        UUID connectId = UUID.randomUUID();

        List<Movement> movementList = movementHelpers.createVarbergGrenaMovements(order, ALL, connectId);

        Collections.sort(movementList, Comparator.comparing(m -> m.getTimestamp()));

        Movement previous = null; 
        for (Movement movement: movementList) {
            if (previous == null) {
                assertThat(movement.getPreviousMovement(), is(CoreMatchers.nullValue()));
            } else {
                assertThat(movement.getPreviousMovement(), is(previous));
            }
            previous = movement;
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createFishingTourVarberg()  {
        MovementHelpers movementHelpers = new MovementHelpers(movementService);
        UUID connectId = UUID.randomUUID();

        List<Movement> movementList = movementHelpers.createFishingTourVarberg(ORDER_NORMAL ,connectId);
        assertMovementIds(movementList);
    }

    private void assertMovementIds(List<Movement> movementList) {
        Collections.sort(movementList, Comparator.comparing(m -> m.getTimestamp()));

        Movement previous = null; 
        for (Movement movement: movementList) {
            if (previous == null) {
                assertThat(movement.getPreviousMovement(), is(CoreMatchers.nullValue()));
            } else {
                assertThat(movement.getPreviousMovement(), is(previous));
            }
            previous = movement;
        }
    }
}
