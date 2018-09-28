package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import java.util.UUID;
import javax.ejb.EJB;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

/**
 * Created by thofan on 2017-02-23.
 */

@RunWith(Arquillian.class)
public class MovementBatchModelBeanIntTest extends TransactionalTests {

    private Random rnd = new Random();

    @EJB
    private MovementBatchModelBean movementBatchModelBean;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    /******************************************************************************************************************
     *   TEST FUNCTIONS
     ******************************************************************************************************************/

    @Test
    public void getMovementConnect() {
        // Note getMovementConnectByConnectId CREATES one if it does not exists  (probably to force a batchimport to succeed)
        String randomUUID = UUID.randomUUID().toString();
        MovementConnect fetchedMovementConnect = movementBatchModelBean.getMovementConnectByConnectId(randomUUID);
        assertTrue(fetchedMovementConnect != null);
        assertTrue(fetchedMovementConnect.getValue().equals(randomUUID));
    }


    @Test
    public void getMovementConnect_ZEROISH_GUID() {
        String guid = "100000-0000-0000-0000-000000000000";
        // Note getMovementConnectByConnectId CREATES one if it does not exists  (probably to force a batchimport to succeed)
        MovementConnect fetchedMovementConnect = movementBatchModelBean.getMovementConnectByConnectId(guid);
        assertTrue(fetchedMovementConnect != null);
        assertTrue(fetchedMovementConnect.getValue().equals(guid));
    }

    @Test
    public void getMovementConnect_NULL_GUID() {
        assertNull(movementBatchModelBean.getMovementConnectByConnectId(null));
    }

    @Test
    public void createMovement() throws MovementServiceException {
        double longitude = rnd.nextDouble();
        double latitude = rnd.nextDouble();

        String randomUUID = UUID.randomUUID().toString();
        Movement movement = MockData.createMovement(longitude, latitude, randomUUID);
        movement.getMovementConnect().setValue(randomUUID);

        movementBatchModelBean.createMovement(movement);
    }
}
