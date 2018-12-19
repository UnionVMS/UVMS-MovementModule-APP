package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;

import eu.europa.ec.fisheries.uvms.movement.service.entity.temp.DraftMovement;
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
//import eu.europa.ec.fisheries.uvms.movement.service.DraftMovementService;


/**
 * Created by andreasw on 2017-03-03.
 */
@RunWith(Arquillian.class)
public class DraftMovementServiceBeanIntTest extends TransactionalTests {

    @EJB
    private DraftMovementService draftMovementService;

    @Test
    @OperateOnDeployment("movementservice")
    public void create() throws Exception {
        DraftMovement draftMovement = createTempMovement();
        DraftMovement result = draftMovementService.createDraftMovement(draftMovement, "TEST");
        em.flush();
        Assert.assertNotNull(result.getId());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createWithGivenId() {
        UUID id = UUID.randomUUID();
        DraftMovement draftMovement = createTempMovement();
        try {
            draftMovementService.createDraftMovement(draftMovement, "TEST");
            em.flush();
            DraftMovement fetched = draftMovementService.getDraftMovement(id);
            Assert.assertFalse("Should not reach this!", true);
        } catch (EJBTransactionRolledbackException e) {
            Assert.assertTrue(e.getMessage().contains("Error when fetching temp movement"));
        }
    }
    
    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void createTempMovementNullTempMovementCheckFailureTest() throws Exception {
        String username = DraftMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        draftMovementService.createDraftMovement(null, username);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void createTempMovementNullUsernameCheckFailureTest() throws Exception {
        draftMovementService.createDraftMovement(new DraftMovement(), null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createTempMovementSuccessTest() throws Exception {
        String username = DraftMovementServiceBeanIntTest.class.getSimpleName();

        DraftMovement draftMovement = createTempMovement();
        draftMovement.setState(TempMovementStateEnum.DRAFT);
        DraftMovement createDraftMovement = draftMovementService.createDraftMovement(draftMovement, username);
        em.flush();

        assertNotNull(createDraftMovement);
        assertNotNull(createDraftMovement.getUpdated());
        assertNotNull(createDraftMovement.getId());

        assertEquals(draftMovement.getSpeed(), createDraftMovement.getSpeed());
        assertEquals(TempMovementStateEnum.DRAFT, createDraftMovement.getState());
        assertEquals(draftMovement.getStatus(), createDraftMovement.getStatus());

        assertEquals(draftMovement.getFlag(), createDraftMovement.getFlag());
        assertEquals(draftMovement.getIrcs(), createDraftMovement.getIrcs());
        assertEquals(draftMovement.getCfr(), createDraftMovement.getCfr());
        assertEquals(draftMovement.getExternalMarkings(), createDraftMovement.getExternalMarkings());
        assertEquals(draftMovement.getName(), createDraftMovement.getName());

        assertEquals(draftMovement.getLongitude(), createDraftMovement.getLongitude());
        assertEquals(draftMovement.getLatitude(), createDraftMovement.getLatitude());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovement() throws Exception {
        DraftMovement draftMovement = createTempMovement();
        DraftMovement result = draftMovementService.createDraftMovement(draftMovement, "TEST");
        em.flush();
        Assert.assertNotNull(result.getId());

        DraftMovement fetched = draftMovementService.getDraftMovement(result.getId());
        Assert.assertNotNull(fetched);
        Assert.assertEquals(fetched.getId(), result.getId());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovementWithBogusId() {
        DraftMovement tt = null;
        try {
            tt = draftMovementService.getDraftMovement(UUID.randomUUID());
        } catch (EJBTransactionRolledbackException e) {
            Assert.assertTrue(e.getMessage().contains("Error when fetching temp movement"));
        } catch (Exception e) {
            Assert.assertTrue("This should not be happening", false);
        }
        Assert.assertNull(tt);
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovementSuccessTest() throws Exception {
        String username = DraftMovementServiceBeanIntTest.class.getSimpleName();

        DraftMovement draftMovement = createTempMovement();
        DraftMovement createDraftMovement = draftMovementService.createDraftMovement(draftMovement, username);
        em.flush();

        DraftMovement getDraftMovement = draftMovementService.getDraftMovement(createDraftMovement.getId());
        assertEquals(createDraftMovement, getDraftMovement);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void getTempMovementNullGuidCheckFailureTest() {
        draftMovementService.getDraftMovement(null);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void getTempMovementGuidDoNotExistCheckFailureTest() {
        draftMovementService.getDraftMovement(UUID.randomUUID());
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void getTempMovementListNullCheckFailureTest()  {
        draftMovementService.getDraftMovements(null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovementListSuccessTest() throws Exception {
        String username = DraftMovementServiceBeanIntTest.class.getSimpleName();

        DraftMovement draftMovement = createTempMovement();
        DraftMovement createDraftMovement = draftMovementService.createDraftMovement(draftMovement, username);
        em.flush();

        MovementQuery query = new MovementQuery();
        ListPagination listPagination = new ListPagination();
        listPagination.setPage(BigInteger.valueOf(1));
        listPagination.setListSize(BigInteger.valueOf(1));
        query.setPagination(listPagination);
        GetTempMovementListResponse tempMovementList = draftMovementService.getDraftMovements(query);
        assertNotNull(tempMovementList);
        assertEquals(1, tempMovementList.getMovement().size());
        //Assert.assertEquals(BigInteger.valueOf(1),tempMovementList.getTotalNumberOfPages());
        assertEquals(BigInteger.valueOf(1), tempMovementList.getCurrentPage());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void updateTempMovement() throws Exception {
        DraftMovement draftMovement = createTempMovement();
        DraftMovement result = draftMovementService.createDraftMovement(draftMovement, "TEST");
        em.flush();
        UUID id = result.getId();
        Assert.assertNotNull(id);

        DraftMovement fetched = draftMovementService.getDraftMovement(id);
        Assert.assertNotNull(fetched);
        Assert.assertEquals(id, fetched.getId());
        Assert.assertEquals(TempMovementStateEnum.SENT, fetched.getState());

        fetched.setState(TempMovementStateEnum.DELETED);
        draftMovementService.updateDraftMovement(fetched, "TEST");
        em.flush();

        DraftMovement fetchedAgain = draftMovementService.getDraftMovement(id);
        Assert.assertNotNull(fetched);
        Assert.assertEquals(id, fetchedAgain.getId());
        Assert.assertEquals(TempMovementStateEnum.DELETED, fetchedAgain.getState());
    }
    
    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void updateTempMovementNullTempMovementCheckFailureTest()  {
        String username = DraftMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        draftMovementService.updateDraftMovement(null, username);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void updateTempMovementNullUsernameCheckFailureTest()  {
        draftMovementService.updateDraftMovement(new DraftMovement(), null);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void updateTempMovementNoValidGuidForTempMovementCheckFailureTest() {
        String username = DraftMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        DraftMovement draftMovement = createTempMovement();
        em.flush();

        draftMovement.setId(UUID.randomUUID());
        draftMovementService.updateDraftMovement(draftMovement, username);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void updateTempMovementSuccessTest() throws Exception {
        String username = DraftMovementServiceBeanIntTest.class.getSimpleName();

        DraftMovement draftMovement = createTempMovement();
        DraftMovement createDraftMovement = draftMovementService.createDraftMovement(draftMovement, username);
        em.flush();

        createDraftMovement.setSpeed(25d);

        DraftMovement updateDraftMovement = draftMovementService.updateDraftMovement(createDraftMovement, username);

        assertNotNull(updateDraftMovement);
        assertNotNull(updateDraftMovement.getUpdated());
        assertNotNull(updateDraftMovement.getId());

        assertEquals(createDraftMovement.getSpeed(), updateDraftMovement.getSpeed());
        assertEquals(createDraftMovement.getState(), updateDraftMovement.getState());
        assertEquals(createDraftMovement.getStatus(), updateDraftMovement.getStatus());

        assertEquals(createDraftMovement.getFlag(), createDraftMovement.getFlag());
        assertEquals(createDraftMovement.getIrcs(), createDraftMovement.getIrcs());
        assertEquals(createDraftMovement.getCfr(), createDraftMovement.getCfr());
        assertEquals(createDraftMovement.getExternalMarkings(), createDraftMovement.getExternalMarkings());
        assertEquals(createDraftMovement.getName(), createDraftMovement.getName());

        assertEquals(createDraftMovement.getLongitude(), createDraftMovement.getLongitude());
        assertEquals(createDraftMovement.getLatitude(), createDraftMovement.getLatitude());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void archiveTempMovement() throws Exception {
        DraftMovement draftMovement = createTempMovement();
        DraftMovement result = draftMovementService.createDraftMovement(draftMovement, "TEST");
        em.flush();
        UUID id = result.getId();
        Assert.assertNotNull(id);

        draftMovementService.archiveDraftMovement(id, "TEST");

        DraftMovement fetched = draftMovementService.getDraftMovement(id);
        Assert.assertNotNull(fetched);
        Assert.assertEquals(id, fetched.getId());
        Assert.assertEquals(TempMovementStateEnum.DELETED, fetched.getState());
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void archiveTempMovementWithBogusId() throws Exception {
        UUID id = UUID.randomUUID();
        draftMovementService.archiveDraftMovement(id, "TEST");
    }
    
    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void archiveTempMovementNullGuidCheckFailureTest() throws Exception {
        String username = DraftMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        draftMovementService.archiveDraftMovement(null, username);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void archiveTempMovementNullUsernameCheckFailureTest() throws Exception {
        draftMovementService.archiveDraftMovement(UUID.randomUUID(), null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void archiveTempMovementSuccessTest() throws Exception {
        String username = DraftMovementServiceBeanIntTest.class.getSimpleName();

        DraftMovement draftMovementType = createTempMovement();
        DraftMovement createDraftMovement = draftMovementService.createDraftMovement(draftMovementType, username);
        em.flush();

        DraftMovement archiveDraftMovement = draftMovementService.archiveDraftMovement(createDraftMovement.getId(), username);
        em.flush();

        assertNotNull(archiveDraftMovement);
        assertNotNull(archiveDraftMovement.getUpdated());
        assertNotNull(archiveDraftMovement.getId());
        assertEquals(TempMovementStateEnum.DELETED, archiveDraftMovement.getState());
    }
    
    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void sendTempMovementNullGuidCheckFailureTest() throws Exception {
        String username = DraftMovementServiceBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
        draftMovementService.sendDraftMovement(null, username);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void sendTempMovementNullUsernameCheckFailureTest() throws Exception {
        draftMovementService.sendDraftMovement(UUID.randomUUID(), null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void sendTempMovementSuccessTest() throws Exception {
        String username = DraftMovementServiceBeanIntTest.class.getSimpleName();

        DraftMovement draftMovement = createTempMovement();
        DraftMovement createDraftMovement = draftMovementService.createDraftMovement(draftMovement, username);
        em.flush();

        DraftMovement sendDraftMovement = draftMovementService.sendDraftMovement(createDraftMovement.getId(), username);
        em.flush();

        assertNotNull(sendDraftMovement);
        assertNotNull(sendDraftMovement.getUpdated());
        assertNotNull(sendDraftMovement.getId());
        assertEquals(TempMovementStateEnum.SENT, sendDraftMovement.getState());
    }

    private DraftMovement createTempMovement() {

        DraftMovement draftMovement = new DraftMovement();
        draftMovement.setCfr("T");
        draftMovement.setExternalMarkings("T");
        draftMovement.setFlag("T");
        draftMovement.setIrcs("T");
        draftMovement.setName("T");
        draftMovement.setLatitude(0.0);
        draftMovement.setLongitude(0.0);
        draftMovement.setTimestamp(Instant.now());
        draftMovement.setCourse(0.0);
        draftMovement.setSpeed(0.0);
        draftMovement.setState(TempMovementStateEnum.SENT);
        draftMovement.setUpdated(Instant.now());
        draftMovement.setUpdatedBy("TEST");

        return draftMovement;
    }
}
