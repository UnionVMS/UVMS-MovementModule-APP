package eu.europa.ec.fisheries.uvms.movement.service.bean;

import eu.europa.ec.fisheries.schema.exchange.module.v1.SetMovementReportRequest;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.movement.asset.v1.VesselType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dto.ManualMovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovement;
import eu.europa.ec.fisheries.uvms.movement.service.message.JMSHelper;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.jms.ConnectionFactory;
import javax.jms.TextMessage;
import java.time.Instant;
import java.util.Date;
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
        String username = ManualMovementServiceTest.class.getSimpleName() + UUID.randomUUID().toString();

        ManualMovementDto draftMovement = createTempMovement();

        manualMovementService.sendManualMovement(draftMovement, username);

        userTransaction.commit();
        userTransaction.begin();

        TextMessage message = (TextMessage) jmsHelper.listenOnQueue(exchange);
        assertNotNull(message);

        SetMovementReportRequest reportRequest = JAXBMarshaller.unmarshallTextMessage(message, SetMovementReportRequest.class);
        assertNotNull(reportRequest);

        assertEquals(reportRequest.getUsername(), username);
        assertEquals(eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementSourceType.MANUAL, reportRequest.getRequest().getMovement().getSource());
        assertEquals("10", reportRequest.getRequest().getMovement().getStatus());
        assertTrue(reportRequest.getRequest().getMovement().getAssetId().getAssetIdList().stream().anyMatch(id -> id.getIdType().equals(AssetIdType.IRCS)));
        assertTrue(reportRequest.getRequest().getMovement().getAssetId().getAssetIdList().stream().anyMatch(id -> id.getValue().equals(draftMovement.getAsset().getIrcs())));
        assertTrue(reportRequest.getRequest().getMovement().getAssetId().getAssetIdList().stream().anyMatch(id -> id.getIdType().equals(AssetIdType.CFR)));
        assertTrue(reportRequest.getRequest().getMovement().getAssetId().getAssetIdList().stream().anyMatch(id -> id.getValue().equals(draftMovement.getAsset().getCfr())));
        assertEquals(Date.from(draftMovement.getMovement().getTimestamp()), reportRequest.getRequest().getMovement().getPositionTime());
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

        MicroMovement micro = new MicroMovement();
        MovementPoint location = new MovementPoint();
        location.setLatitude(0.0);
        location.setLongitude(0.0);
        micro.setLocation(location);
        micro.setTimestamp(Instant.now());
        micro.setHeading(0.0);
        micro.setSpeed(0.0);
        micro.setSource(MovementSourceType.MANUAL);
        movement.setMovement(micro);

        return movement;
    }
}
