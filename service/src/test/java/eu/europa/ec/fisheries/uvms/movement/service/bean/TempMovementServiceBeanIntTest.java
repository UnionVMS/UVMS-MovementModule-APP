package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetTempMovementListResponse;
import eu.europa.ec.fisheries.uvms.movement.model.constants.TempMovementStateEnum;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.temp.TempMovement;
//import eu.europa.ec.fisheries.uvms.movement.service.TempMovementService;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

/**
 * Created by andreasw on 2017-03-03.
 */
@RunWith(Arquillian.class)
public class TempMovementServiceBeanIntTest extends TransactionalTests {

    @EJB
    private TempMovementService tempMovementService;

    @Test
    @OperateOnDeployment("movementservice")
    public void create() throws MovementServiceException {
        TempMovement tempMovement = createTempMovement();
        TempMovement result = tempMovementService.createTempMovement(tempMovement, "TEST");
        em.flush();
        Assert.assertNotNull(result.getId());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createWithGivenId() {
        UUID id = UUID.randomUUID();
        TempMovement tempMovement = createTempMovement();
        try {
            tempMovementService.createTempMovement(tempMovement, "TEST");
            em.flush();
            TempMovement fetched = tempMovementService.getTempMovement(id);
            Assert.assertFalse("Should not reach this!", true);
        } catch (MovementServiceException e) {
            Assert.assertTrue(e.getMessage().contains("Error when getting temp movement"));
        }
    }
    
    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void createTempMovementNullTempMovementCheckFailureTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        tempMovementService.createTempMovement(null, username);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void createTempMovementNullUsernameCheckFailureTest() throws MovementServiceException {
        tempMovementService.createTempMovement(new TempMovement(), null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createTempMovementSuccessTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName();

        TempMovement tempMovement = createTempMovement();
        tempMovement.setState(TempMovementStateEnum.DRAFT);
        TempMovement createTempMovement = tempMovementService.createTempMovement(tempMovement, username);
        em.flush();

        assertNotNull(createTempMovement);
        assertNotNull(createTempMovement.getUpdated());
        assertNotNull(createTempMovement.getId());

        assertEquals(tempMovement.getSpeed(), createTempMovement.getSpeed());
        assertEquals(TempMovementStateEnum.DRAFT, createTempMovement.getState());
        assertEquals(tempMovement.getStatus(), createTempMovement.getStatus());

        assertEquals(tempMovement.getFlag(), createTempMovement.getFlag());
        assertEquals(tempMovement.getIrcs(), createTempMovement.getIrcs());
        assertEquals(tempMovement.getCfr(), createTempMovement.getCfr());
        assertEquals(tempMovement.getExternalMarkings(), createTempMovement.getExternalMarkings());
        assertEquals(tempMovement.getName(), createTempMovement.getName());

        assertEquals(tempMovement.getLongitude(), createTempMovement.getLongitude());
        assertEquals(tempMovement.getLatitude(), createTempMovement.getLatitude());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovement() throws MovementServiceException {
        TempMovement tempMovement = createTempMovement();
        TempMovement result = tempMovementService.createTempMovement(tempMovement, "TEST");
        em.flush();
        Assert.assertNotNull(result.getId());

        TempMovement fetched = tempMovementService.getTempMovement(result.getId());
        Assert.assertNotNull(fetched);
        Assert.assertEquals(fetched.getId(), result.getId());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovementWithBogusId() {
        TempMovement tt = null;
        try {
            tt = tempMovementService.getTempMovement(UUID.randomUUID());
        } catch (MovementServiceException e) {
            Assert.assertTrue(e.getMessage().contains("Error when getting temp movement"));
        } catch (Exception e) {
            Assert.assertTrue("This should not be happening", false);
        }
        Assert.assertNull(tt);
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovementSuccessTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName();

        TempMovement tempMovement = createTempMovement();
        TempMovement createTempMovement = tempMovementService.createTempMovement(tempMovement, username);
        em.flush();

        TempMovement getTempMovement = tempMovementService.getTempMovement(createTempMovement.getId());
        assertEquals(createTempMovement, getTempMovement);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void getTempMovementNullGuidCheckFailureTest() throws MovementServiceException {
        tempMovementService.getTempMovement(null);
    }

    @Test(expected = MovementServiceException.class)
    @OperateOnDeployment("movementservice")
    public void getTempMovementGuidDoNotExistCheckFailureTest() throws MovementServiceException {
        tempMovementService.getTempMovement(UUID.randomUUID());
    }

    @Test(expected = MovementServiceException.class)
    @OperateOnDeployment("movementservice")
    public void getTempMovementListNullCheckFailureTest() throws MovementServiceException {
        tempMovementService.getTempMovements(null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovementListSuccessTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName();

        TempMovement tempMovement = createTempMovement();
        TempMovement createTempMovement = tempMovementService.createTempMovement(tempMovement, username);
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
        TempMovement tempMovement = createTempMovement();
        TempMovement result = tempMovementService.createTempMovement(tempMovement, "TEST");
        em.flush();
        UUID id = result.getId();
        Assert.assertNotNull(id);

        TempMovement fetched = tempMovementService.getTempMovement(id);
        Assert.assertNotNull(fetched);
        Assert.assertEquals(id, fetched.getId());
        Assert.assertEquals(TempMovementStateEnum.SENT, fetched.getState());

        fetched.setState(TempMovementStateEnum.DELETED);
        tempMovementService.updateTempMovement(fetched, "TEST");
        em.flush();

        TempMovement fetchedAgain = tempMovementService.getTempMovement(id);
        Assert.assertNotNull(fetched);
        Assert.assertEquals(id, fetchedAgain.getId());
        Assert.assertEquals(TempMovementStateEnum.DELETED, fetchedAgain.getState());
    }
    
    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void updateTempMovementNullTempMovementCheckFailureTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        tempMovementService.updateTempMovement(null, username);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void updateTempMovementNullUsernameCheckFailureTest() throws MovementServiceException {
        tempMovementService.updateTempMovement(new TempMovement(), null);
    }

    @Test(expected = MovementServiceException.class)
    @OperateOnDeployment("movementservice")
    public void updateTempMovementNoValidGuidForTempMovementCheckFailureTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        TempMovement tempMovement = createTempMovement();
        em.flush();

        tempMovement.setId(UUID.randomUUID());
        tempMovementService.updateTempMovement(tempMovement, username);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void updateTempMovementSuccessTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName();

        TempMovement tempMovement = createTempMovement();
        TempMovement createTempMovement = tempMovementService.createTempMovement(tempMovement, username);
        em.flush();

        createTempMovement.setSpeed(25d);

        TempMovement updateTempMovement = tempMovementService.updateTempMovement(createTempMovement, username);

        assertNotNull(updateTempMovement);
        assertNotNull(updateTempMovement.getUpdated());
        assertNotNull(updateTempMovement.getId());

        assertEquals(createTempMovement.getSpeed(), updateTempMovement.getSpeed());
        assertEquals(createTempMovement.getState(), updateTempMovement.getState());
        assertEquals(createTempMovement.getStatus(), updateTempMovement.getStatus());

        assertEquals(createTempMovement.getFlag(), createTempMovement.getFlag());
        assertEquals(createTempMovement.getIrcs(), createTempMovement.getIrcs());
        assertEquals(createTempMovement.getCfr(), createTempMovement.getCfr());
        assertEquals(createTempMovement.getExternalMarkings(), createTempMovement.getExternalMarkings());
        assertEquals(createTempMovement.getName(), createTempMovement.getName());

        assertEquals(createTempMovement.getLongitude(), createTempMovement.getLongitude());
        assertEquals(createTempMovement.getLatitude(), createTempMovement.getLatitude());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void archiveTempMovement() throws MovementServiceException {
        TempMovement tempMovement = createTempMovement();
        TempMovement result = tempMovementService.createTempMovement(tempMovement, "TEST");
        em.flush();
        UUID id = result.getId();
        Assert.assertNotNull(id);

        tempMovementService.archiveTempMovement(id, "TEST");

        TempMovement fetched = tempMovementService.getTempMovement(id);
        Assert.assertNotNull(fetched);
        Assert.assertEquals(id, fetched.getId());
        Assert.assertEquals(TempMovementStateEnum.DELETED, fetched.getState());
    }

    @Test(expected = MovementServiceException.class)
    @OperateOnDeployment("movementservice")
    public void archiveTempMovementWithBogusId() throws MovementServiceException {
        UUID id = UUID.randomUUID();
        tempMovementService.archiveTempMovement(id, "TEST");
    }
    
    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void archiveTempMovementNullGuidCheckFailureTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        tempMovementService.archiveTempMovement(null, username);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void archiveTempMovementNullUsernameCheckFailureTest() throws MovementServiceException {
        tempMovementService.archiveTempMovement(UUID.randomUUID(), null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void archiveTempMovementSuccessTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName();

        TempMovement tempMovementType = createTempMovement();
        TempMovement createTempMovement = tempMovementService.createTempMovement(tempMovementType, username);
        em.flush();

        TempMovement archiveTempMovement = tempMovementService.archiveTempMovement(createTempMovement.getId(), username);
        em.flush();

        assertNotNull(archiveTempMovement);
        assertNotNull(archiveTempMovement.getUpdated());
        assertNotNull(archiveTempMovement.getId());
        assertEquals(TempMovementStateEnum.DELETED, archiveTempMovement.getState());
    }
    
    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void sendTempMovementNullGuidCheckFailureTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        tempMovementService.sendTempMovement(null, username);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void sendTempMovementNullUsernameCheckFailureTest() throws MovementServiceException {
        tempMovementService.sendTempMovement(UUID.randomUUID(), null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void sendTempMovementSuccessTest() throws MovementServiceException {
        String username = TempMovementServiceBeanIntTest.class.getSimpleName();

        TempMovement tempMovement = createTempMovement();
        TempMovement createTempMovement = tempMovementService.createTempMovement(tempMovement, username);
        em.flush();

        TempMovement sendTempMovement = tempMovementService.sendTempMovement(createTempMovement.getId(), username);
        em.flush();

        assertNotNull(sendTempMovement);
        assertNotNull(sendTempMovement.getUpdated());
        assertNotNull(sendTempMovement.getId());
        assertEquals(TempMovementStateEnum.SENT, sendTempMovement.getState());
    }

    private TempMovement createTempMovement() {

        TempMovement tempMovement = new TempMovement();
        tempMovement.setCfr("T");
        tempMovement.setExternalMarkings("T");
        tempMovement.setFlag("T");
        tempMovement.setIrcs("T");
        tempMovement.setName("T");
        tempMovement.setLatitude(0.0);
        tempMovement.setLongitude(0.0);
        tempMovement.setTimestamp(Instant.now());
        tempMovement.setCourse(0.0);
        tempMovement.setSpeed(0.0);
        tempMovement.setState(TempMovementStateEnum.SENT);
        tempMovement.setUpdated(Instant.now());
        tempMovement.setUpdatedBy("TEST");

        return tempMovement;
    }
}
