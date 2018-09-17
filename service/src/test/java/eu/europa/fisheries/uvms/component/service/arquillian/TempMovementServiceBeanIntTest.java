package eu.europa.fisheries.uvms.component.service.arquillian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.asset.v1.VesselType;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetTempMovementListResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementStateEnum;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.message.producer.bean.MessageProducerBean;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.TempMovementService;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

/**
 * Created by andreasw on 2017-03-03.
 */
@RunWith(Arquillian.class)
public class TempMovementServiceBeanIntTest extends TransactionalTests {

    @Before
    public void init() {
        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");
    }

    @EJB
    private TempMovementService tempMovementService;


    @Test
    @OperateOnDeployment("movementservice")
    public void create() throws MovementServiceException {
        TempMovementType tempMovementType = createTempMovement();
        TempMovementType result = tempMovementService.createTempMovement(tempMovementType, "TEST");
        em.flush();
        Assert.assertNotNull(result.getGuid());
    }

    @Test
    @ShouldThrowException(EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void createWithBrokenJMS() throws MovementServiceException {
        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "true");
        TempMovementType tempMovementType = createTempMovement();
        //This should still work because the only "dependency" that is broken is the AUDIT module.
        TempMovementType result = tempMovementService.createTempMovement(tempMovementType, "TEST");
        em.flush();
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createWithGivenId() {
        String id = UUID.randomUUID().toString();
        TempMovementType tempMovementType = createTempMovement();
        try {
            tempMovementService.createTempMovement(tempMovementType, "TEST");
            em.flush();
            TempMovementType fetched = tempMovementService.getTempMovement(id);
            Assert.assertFalse("Should not reach this!", true);
        } catch (MovementServiceException e) {
            Assert.assertTrue(e.getMessage().contains("Error when getting temp movement"));
        }
    }
    
    @Test(expected = EJBTransactionRolledbackException.class)
    public void createTempMovementNullTempMovementCheckFailureTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        tempMovementService.createTempMovement(null, username);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    public void createTempMovementNullUsernameCheckFailureTest() throws MovementServiceException {
        tempMovementService.createTempMovement(new TempMovementType(), null);
    }

    @Test
    public void createTempMovementSuccessTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName();

        TempMovementType tempMovementType = createTempMovement();
        tempMovementType.setState(TempMovementStateEnum.DRAFT);
        TempMovementType createTempMovement = tempMovementService.createTempMovement(tempMovementType, username);
        em.flush();

        assertNotNull(createTempMovement);
        assertNotNull(createTempMovement.getUpdatedTime());
        assertNotNull(createTempMovement.getGuid());

        assertEquals(tempMovementType.getSpeed(), createTempMovement.getSpeed());
        assertEquals(TempMovementStateEnum.DRAFT, createTempMovement.getState());
        assertEquals(tempMovementType.getStatus(), createTempMovement.getStatus());

        assertEquals(tempMovementType.getAsset(), createTempMovement.getAsset());

        assertEquals(tempMovementType.getPosition().getLongitude(), createTempMovement.getPosition().getLongitude());
        assertEquals(tempMovementType.getPosition().getLatitude(), createTempMovement.getPosition().getLatitude());
        assertNull(createTempMovement.getPosition().getAltitude());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovement() throws MovementServiceException {
        TempMovementType tempMovementType = createTempMovement();
        TempMovementType result = tempMovementService.createTempMovement(tempMovementType, "TEST");
        em.flush();
        Assert.assertNotNull(result.getGuid());

        TempMovementType fetched = tempMovementService.getTempMovement(result.getGuid());
        Assert.assertNotNull(fetched);
        Assert.assertEquals(fetched.getGuid(), result.getGuid());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovementWithBogusId() {
        TempMovementType tt = null;
        try {
            tt = tempMovementService.getTempMovement("TEST");
        } catch (MovementServiceException e) {
            Assert.assertTrue(e.getMessage().contains("Error when getting temp movement"));
        } catch (Exception e) {
            Assert.assertTrue("This should not be happening", false);
        }
        Assert.assertNull(tt);
    }
    
    @Test
    public void getTempMovementSuccessTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName();

        TempMovementType tempMovementType = createTempMovement();
        TempMovementType createTempMovement = tempMovementService.createTempMovement(tempMovementType, username);
        em.flush();

        TempMovementType getTempMovement = tempMovementService.getTempMovement(createTempMovement.getGuid());
        assertEquals(createTempMovement, getTempMovement);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    public void getTempMovementNullGuidCheckFailureTest() throws MovementServiceException {
        tempMovementService.getTempMovement(null);
    }

    @Test(expected = MovementServiceException.class)
    public void getTempMovementGuidDoNotExistCheckFailureTest() throws MovementServiceException {
        tempMovementService.getTempMovement(UUID.randomUUID().toString());
    }

    @Test(expected = MovementServiceException.class)
    public void getTempMovementListNullCheckFailureTest() throws MovementServiceException {
        tempMovementService.getTempMovements(null);
    }

    @Test
    public void getTempMovementListSuccessTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName();

        TempMovementType tempMovementType = createTempMovement();
        TempMovementType createTempMovement = tempMovementService.createTempMovement(tempMovementType, username);
        em.flush();

        MovementQuery query = new MovementQuery();
        ListPagination listPagination = new ListPagination();
        listPagination.setPage(BigInteger.valueOf(1));
        listPagination.setListSize(BigInteger.valueOf(1));
        query.setPagination(listPagination);
        GetTempMovementListResponse tempMovementList = tempMovementService.getTempMovements(query);
        assertNotNull(tempMovementList);
        assertEquals(1, tempMovementList.getMovement().size());
        //Assert.assertEquals(BigInteger.valueOf(1),tempMovementList.getTotalNumberOfPages());
        assertEquals(BigInteger.valueOf(1), tempMovementList.getCurrentPage());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void updateTempMovement() throws MovementServiceException {
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
    
    @Test(expected = EJBTransactionRolledbackException.class)
    public void updateTempMovementNullTempMovementCheckFailureTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        tempMovementService.updateTempMovement(null, username);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    public void updateTempMovementNullUsernameCheckFailureTest() throws MovementServiceException {
        tempMovementService.updateTempMovement(new TempMovementType(), null);
    }

    @Test(expected = MovementServiceException.class)
    public void updateTempMovementNoValidGuidForTempMovementCheckFailureTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        TempMovementType tempMovementType = createTempMovement();
        em.flush();

        tempMovementType.setGuid(UUID.randomUUID().toString());
        tempMovementService.updateTempMovement(tempMovementType, username);
    }

    @Test
    public void updateTempMovementSuccessTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName();

        TempMovementType tempMovementType = createTempMovement();
        TempMovementType createTempMovement = tempMovementService.createTempMovement(tempMovementType, username);
        em.flush();

        createTempMovement.setSpeed(25d);

        TempMovementType updateTempMovement = tempMovementService.updateTempMovement(createTempMovement, username);

        assertNotNull(updateTempMovement);
        assertNotNull(updateTempMovement.getUpdatedTime());
        assertNotNull(updateTempMovement.getGuid());

        assertEquals(createTempMovement.getSpeed(), updateTempMovement.getSpeed());
        assertEquals(createTempMovement.getState(), updateTempMovement.getState());
        assertEquals(createTempMovement.getStatus(), updateTempMovement.getStatus());

        assertEquals(createTempMovement.getAsset(), updateTempMovement.getAsset());

        assertEquals(createTempMovement.getPosition().getLongitude(), updateTempMovement.getPosition().getLongitude());
        assertEquals(createTempMovement.getPosition().getLatitude(), updateTempMovement.getPosition().getLatitude());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void archiveTempMovement() throws MovementServiceException {
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

    @Test(expected = MovementServiceException.class)
    @OperateOnDeployment("movementservice")
    public void archiveTempMovementWithBogusId() throws MovementServiceException {
        String id = "BOGUS";
        tempMovementService.archiveTempMovement(id, "TEST");
    }
    
    @Test(expected = EJBTransactionRolledbackException.class)
    public void archiveTempMovementNullGuidCheckFailureTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        tempMovementService.archiveTempMovement(null, username);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    public void archiveTempMovementNullUsernameCheckFailureTest() throws MovementServiceException {
        tempMovementService.archiveTempMovement("guid", null);
    }

    @Test
    public void archiveTempMovementSuccessTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName();

        TempMovementType tempMovementType = createTempMovement();
        TempMovementType createTempMovement = tempMovementService.createTempMovement(tempMovementType, username);
        em.flush();

        TempMovementType archiveTempMovement = tempMovementService.archiveTempMovement(createTempMovement.getGuid(), username);
        em.flush();

        assertNotNull(archiveTempMovement);
        assertNotNull(archiveTempMovement.getUpdatedTime());
        assertNotNull(archiveTempMovement.getGuid());
        assertEquals(TempMovementStateEnum.DELETED, archiveTempMovement.getState());
    }
    
    @Test(expected = EJBTransactionRolledbackException.class)
    public void sendTempMovementNullGuidCheckFailureTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        tempMovementService.sendTempMovement(null, username);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    public void sendTempMovementNullUsernameCheckFailureTest() throws MovementServiceException {
        tempMovementService.sendTempMovement("guid", null);
    }

    @Test
    public void sendTempMovementSuccessTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName();

        TempMovementType tempMovementType = createTempMovement();
        TempMovementType createTempMovement = tempMovementService.createTempMovement(tempMovementType, username);
        em.flush();

        TempMovementType sendTempMovement = tempMovementService.sendTempMovement(createTempMovement.getGuid(), username);
        em.flush();

        assertNotNull(sendTempMovement);
        assertNotNull(sendTempMovement.getUpdatedTime());
        assertNotNull(sendTempMovement.getGuid());
        assertEquals(TempMovementStateEnum.SENT, sendTempMovement.getState());
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

        Instant d = Instant.now();


        TempMovementType tempMovementType = new TempMovementType();
        tempMovementType.setAsset(vesselType);
        tempMovementType.setCourse(0.0);
        tempMovementType.setPosition(movementPoint);
        tempMovementType.setSpeed(0.0);
        tempMovementType.setState(TempMovementStateEnum.SENT);
        tempMovementType.setTime(DateUtil.parseDateToString(d, "yyyy-MM-dd HH:mm:ss Z"));
        //tempMovementType.setUpdatedTime();
        return tempMovementType;
    }
}
