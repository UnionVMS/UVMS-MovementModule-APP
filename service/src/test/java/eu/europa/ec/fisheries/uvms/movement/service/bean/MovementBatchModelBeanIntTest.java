package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import java.util.UUID;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
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
    @OperateOnDeployment("movementservice")
    public void getMovementConnect() {
        // Note getMovementConnectByConnectId CREATES one if it does not exists  (probably to force a batch import to succeed)
        UUID randomUUID = UUID.randomUUID();
        MovementConnect fetchedMovementConnect = movementBatchModelBean.getMovementConnectByConnectId(randomUUID);
        assertNotNull(fetchedMovementConnect);
        assertEquals(fetchedMovementConnect.getValue(), randomUUID);
    }


    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementConnect_ZEROISH_GUID() {
        UUID guid = UUID.fromString("100000-0000-0000-0000-000000000000");
        // Note getMovementConnectByConnectId CREATES one if it does not exists  (probably to force a batchimport to succeed)
        MovementConnect fetchedMovementConnect = movementBatchModelBean.getMovementConnectByConnectId(guid);
        assertNotNull(fetchedMovementConnect);
        assertEquals(fetchedMovementConnect.getValue(), guid);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementConnect_NULL_GUID() {
        assertNull(movementBatchModelBean.getMovementConnectByConnectId(null));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovement() {
        double longitude = rnd.nextDouble();
        double latitude = rnd.nextDouble();

        UUID randomUUID = UUID.randomUUID();
        Movement movement = MockData.createMovement(longitude, latitude, randomUUID);
        movement.getMovementConnect().setValue(randomUUID);

        Movement created = movementBatchModelBean.createMovement(movement);
        assertNotNull(created);
        assertEquals(randomUUID, created.getMovementConnect().getValue());

    }
}
