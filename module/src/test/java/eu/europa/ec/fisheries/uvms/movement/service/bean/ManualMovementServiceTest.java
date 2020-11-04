package eu.europa.ec.fisheries.uvms.movement.service.bean;

import eu.europa.ec.fisheries.schema.movement.asset.v1.VesselType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.ManualMovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.message.JMSHelper;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.*;


/**
 * Created by andreasw on 2017-03-03.
 */
@RunWith(Arquillian.class)
public class ManualMovementServiceTest extends TransactionalTests {

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @EJB
    private ManualMovementService manualMovementService;

    @Inject
    MovementDao movementDao;

    private static String exchange = MessageConstants.QUEUE_EXCHANGE_EVENT_NAME;
    JMSHelper jmsHelper;

    @Before
    public void init() throws Exception {
        jmsHelper = new JMSHelper(connectionFactory);
        jmsHelper.clearQueue(exchange);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void sendManualMovementNullGuidCheckFailureTest() throws Exception {
        String username = ManualMovementServiceTest.class.getSimpleName() + UUID.randomUUID().toString();
        manualMovementService.sendManualMovement(null, username);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void sendManualMovementNullUsernameCheckFailureTest() throws Exception {
        manualMovementService.sendManualMovement(null, null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void sendManualMovementSuccessTest() throws Exception {
        UUID assetId = UUID.randomUUID();
        String username = "ManualMovementTests";

        ManualMovementDto draftMovement = createTempMovement();
        draftMovement.getAsset().setIrcs("TestIrcs:" + assetId);

        UUID reportId = manualMovementService.sendManualMovement(draftMovement, username);
        assertNull(reportId);

        Movement latestMovement = movementDao.getLatestMovement(assetId);
        assertNotNull(latestMovement);
        assertEquals(MovementSourceType.MANUAL, latestMovement.getSource());
        assertEquals(username, latestMovement.getUpdatedBy());
        assertEquals("10", latestMovement.getStatus());
        assertEquals(draftMovement.getMovement().getTimestamp(), latestMovement.getTimestamp());

    }

    private ManualMovementDto createTempMovement() {

        ManualMovementDto movement = new ManualMovementDto();
        VesselType asset = new VesselType();
        asset.setCfr("T");
        asset.setExtMarking("T");
        asset.setFlagState("T");
        asset.setIrcs("T");
        asset.setName("T");
        movement.setAsset(asset);

        MovementDto dto = new MovementDto();
        MovementPoint location = new MovementPoint();
        location.setLatitude(0.0);
        location.setLongitude(0.0);
        dto.setLocation(location);
        dto.setTimestamp(Instant.now());
        dto.setHeading(0.0f);
        dto.setSpeed(0.0f);
        dto.setSource(MovementSourceType.MANUAL);
        movement.setMovement(dto);

        return movement;
    }
}
