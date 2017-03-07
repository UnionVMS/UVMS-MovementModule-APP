package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.asset.v1.VesselType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementStateEnum;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.entity.temp.TempMovement;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.service.TempMovementService;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by andreasw on 2017-03-03.
 */
@RunWith(Arquillian.class)
public class TempMovementServiceBeanIntTest extends TransactionalTests {

    @EJB
    private TempMovementService tempMovementService;


    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createTempMovementDeployment();
    }


    @Test
    public void create() throws MovementServiceException, MovementDuplicateException {
        TempMovementType tempMovementType = createTempMovement();
        TempMovementType result = tempMovementService.createTempMovement(tempMovementType, "TEST");
        em.flush();
        Assert.assertNotNull(result.getGuid());
    }

    @Test
    public void createWithGivenId() throws MovementDuplicateException {
        String id = UUID.randomUUID().toString();
        TempMovementType tempMovementType = createTempMovement();
        tempMovementType.setGuid(id);
        try {
            tempMovementService.createTempMovement(tempMovementType, "TEST");
            em.flush();
            TempMovementType fetched = tempMovementService.getTempMovement(id);
            Assert.assertFalse("Should not reach this!", true);
        } catch (MovementServiceException e) {
            Assert.assertTrue(e.getMessage().contains("Error when getting temp movement"));
        }
    }

    @Test
    public void getTempMovement() throws MovementServiceException, MovementDuplicateException {
        TempMovementType tempMovementType = createTempMovement();
        TempMovementType result = tempMovementService.createTempMovement(tempMovementType, "TEST");
        em.flush();
        Assert.assertNotNull(result.getGuid());

        TempMovementType fetched = tempMovementService.getTempMovement(result.getGuid());
        Assert.assertNotNull(fetched);
        Assert.assertEquals(fetched.getGuid(), result.getGuid());
    }

    @Test
    public void getTempMovementWithBogusId() {
        TempMovementType tt = null;
        try {
            tt = tempMovementService.getTempMovement("TEST");
        } catch (MovementServiceException e) {
            Assert.assertTrue(e.getMessage().contains("Error when getting temp movement"));
        } catch (MovementDuplicateException e) {
            Assert.assertTrue("This should not be happening", false);
        }
        Assert.assertNull(tt);
    }

    @Test
    public void updateTempMovement() throws MovementServiceException, MovementDuplicateException {
        TempMovementType tempMovementType = createTempMovement();
        TempMovementType result = tempMovementService.createTempMovement(tempMovementType, "TEST");
        em.flush();
        String id = result.getGuid();
        Assert.assertNotNull(id);

        TempMovementType fetched = tempMovementService.getTempMovement(id);
        Assert.assertNotNull(fetched);
        Assert.assertEquals(id, fetched.getGuid());
        Assert.assertEquals(TempMovementStateEnum.SENT, fetched.getState());

        fetched.setState(TempMovementStateEnum.DELETED);
        tempMovementService.updateTempMovement(fetched, "TEST");
        em.flush();

        TempMovementType fetchedAgain = tempMovementService.getTempMovement(id);
        Assert.assertNotNull(fetched);
        Assert.assertEquals(id, fetchedAgain.getGuid());
        Assert.assertEquals(TempMovementStateEnum.DELETED, fetchedAgain.getState());
    }

    @Test
    public void archiveTempMovement() throws MovementServiceException, MovementDuplicateException {
        TempMovementType tempMovementType = createTempMovement();
        TempMovementType result = tempMovementService.createTempMovement(tempMovementType, "TEST");
        em.flush();
        String id = result.getGuid();
        Assert.assertNotNull(id);

        tempMovementService.archiveTempMovement(id, "TEST");

        TempMovementType fetched = tempMovementService.getTempMovement(id);
        Assert.assertNotNull(fetched);
        Assert.assertEquals(id, fetched.getGuid());
        Assert.assertEquals(TempMovementStateEnum.DELETED, fetched.getState());
    }

    @Test
    public void archiveTempMovementWithBogusId() throws MovementDuplicateException {
        String id = "BOGUS";

        try {
            tempMovementService.archiveTempMovement(id, "TEST");
        } catch (MovementServiceException e) {
            Assert.assertTrue(e.getMessage().contains("Error when updating temp movement status"));
        }
    }

    private TempMovementType createTempMovement() {
        VesselType vesselType = new VesselType();
        vesselType.setCfr("T");
        vesselType.setExtMarking("T");
        vesselType.setFlagState("T");
        vesselType.setIrcs("T");
        vesselType.setName("T");

        MovementPoint movementPoint = new MovementPoint();
        movementPoint.setAltitude(0.0);
        movementPoint.setLatitude(0.0);
        movementPoint.setLongitude(0.0);

        Date d = Calendar.getInstance().getTime();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));


        TempMovementType tempMovementType = new TempMovementType();
        tempMovementType.setAsset(vesselType);
        tempMovementType.setCourse(0.0);
        tempMovementType.setPosition(movementPoint);
        tempMovementType.setSpeed(0.0);
        tempMovementType.setState(TempMovementStateEnum.SENT);
        tempMovementType.setTime(sdf.format(d));
        //tempMovementType.setUpdatedTime();
        return tempMovementType;
    }

}
