package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
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
        // Note getOrCreateMovementConnectByConnectId CREATES one if it does not exists  (probably to force a batch import to succeed)
        MovementConnect mc = new MovementConnect();
        mc.setId(UUID.randomUUID());
        mc.setUpdated(Instant.now());
        mc.setUpdatedBy("Test Connector");
        MovementConnect fetchedMovementConnect = movementBatchModelBean.getOrCreateMovementConnectByConnectId(mc);
        assertNotNull(fetchedMovementConnect);
        assertEquals(fetchedMovementConnect.getId(), mc.getId());
    }


    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementConnect_ZEROISH_GUID() {
        MovementConnect mc = new MovementConnect();
        mc.setId(UUID.fromString("100000-0000-0000-0000-000000000000"));
        mc.setUpdated(Instant.now());
        mc.setUpdatedBy("Test Connector");
        // Note getOrCreateMovementConnectByConnectId CREATES one if it does not exists  (probably to force a batchimport to succeed)
        MovementConnect fetchedMovementConnect = movementBatchModelBean.getOrCreateMovementConnectByConnectId(mc);
        assertNotNull(fetchedMovementConnect);
        assertEquals(fetchedMovementConnect.getId(), mc.getId());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementConnect_NULL_GUID() {
        assertNull(movementBatchModelBean.getOrCreateMovementConnectByConnectId(null));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovement() {
        double longitude = rnd.nextDouble();
        double latitude = rnd.nextDouble();

        UUID randomUUID = UUID.randomUUID();
        Movement movement = MockData.createMovement(longitude, latitude, randomUUID);
        movement.getMovementConnect().setId(randomUUID);

        Movement created = movementBatchModelBean.createMovement(movement);
        assertNotNull(created);
        assertEquals(randomUUID, created.getMovementConnect().getId());

    }
}
