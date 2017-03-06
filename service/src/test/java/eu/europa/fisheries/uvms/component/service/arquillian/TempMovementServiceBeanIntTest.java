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
