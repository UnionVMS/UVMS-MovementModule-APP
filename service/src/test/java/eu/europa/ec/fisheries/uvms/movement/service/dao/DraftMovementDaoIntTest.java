package eu.europa.ec.fisheries.uvms.movement.service.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;

import eu.europa.ec.fisheries.uvms.movement.service.entity.temp.DraftMovement;
import org.hamcrest.core.StringContains;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.ThrowableMessageMatcher;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.movement.model.constants.TempMovementStateEnum;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;

/**
 * Created by thofan on 2017-02-22.
 */

@RunWith(Arquillian.class)
public class DraftMovementDaoIntTest extends TransactionalTests {

    private static final Logger LOG = LoggerFactory.getLogger(DraftMovementDaoIntTest.class);

    @EJB
    private DraftMovementDao draftMovementDao;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final double LONGITUDE = 9.140626D;
    private static final double LATITUDE = 57.683805D;

    /******************************************************************************************************************
     *   TEST FUNCTIONS
     ******************************************************************************************************************/

    @Test
    @OperateOnDeployment("movementservice")
    public void createTempMovementEntity() {

        DraftMovement draftMovement = createTempMovementEntityHelper(LONGITUDE, LATITUDE);
        assertNull(draftMovement.getId());
        DraftMovement createdDraftMovement = draftMovementDao.createDraftMovementEntity(draftMovement);
        em.flush();

        assertNotNull(createdDraftMovement.getId());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createTempMovementEntity_NoState_Exception_Thrown() {

        thrown.expect(Exception.class);

        DraftMovement draftMovement = createTempMovementEntityHelper(LONGITUDE, LATITUDE);
        draftMovement.setState(null);
        assertNull(draftMovement.getId());
        draftMovementDao.createDraftMovementEntity(draftMovement);
        em.flush();
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovementByGuid()  {

        // first create one
        DraftMovement draftMovement = createTempMovementEntityHelper(LONGITUDE, LATITUDE);
        DraftMovement createdDraftMovement = draftMovementDao.createDraftMovementEntity(draftMovement);
        em.flush();
        UUID createdTempMovementId = createdDraftMovement.getId();

        UUID createdTempMovementGUID = createdDraftMovement.getId();
        assertNotNull(createdTempMovementGUID);

        // then fetch it
        DraftMovement fetchedDraftMovement =  draftMovementDao.getDraftMovementById(createdTempMovementGUID);
        assertNotNull(fetchedDraftMovement);
        UUID fetchedTempMovementId = fetchedDraftMovement.getId();
        assertEquals(createdTempMovementId, fetchedTempMovementId);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovementByGuid_ZeroGuid_Exception_Thrown()  {

        thrown.expect(EJBTransactionRolledbackException.class);
        expectedMessage("Error when fetching temp movement");

        // we assume that the probability for zero guid exists in db is so low so we consider this safe
        draftMovementDao.getDraftMovementById(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovementListPaginated() {

        // first create something that can be paginated  (a bunch of tempMovements)
        for (int i = 0; i < 100; i++) {
            DraftMovement draftMovement = createTempMovementEntityHelper(LONGITUDE, LATITUDE);
            draftMovementDao.createDraftMovementEntity(draftMovement);
            em.flush();
        }

        // now we can do some paginated retrieval
        List<DraftMovement> draftMovementList = draftMovementDao.getDraftMovementListPaginated(1,25);
        assertNotNull(draftMovementList);
        assertEquals(25, draftMovementList.size());

        draftMovementList = draftMovementDao.getDraftMovementListPaginated(2,25);
        assertNotNull(draftMovementList);
        assertEquals(25, draftMovementList.size());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovementListPaginated_Page2000() {

        // first create something that can be paginated  (a bunch of tempMovements)
        for (int i = 0; i < 100; i++) {
            DraftMovement draftMovement = createTempMovementEntityHelper(LONGITUDE, LATITUDE);
            draftMovementDao.createDraftMovementEntity(draftMovement);
            em.flush();
        }

        // now we can do some paginated retrieval
        List<DraftMovement> draftMovementList = draftMovementDao.getDraftMovementListPaginated(1,25);
        assertNotNull(draftMovementList);
        assertEquals(25, draftMovementList.size());

        draftMovementList = draftMovementDao.getDraftMovementListPaginated(2000,25);
        assertNotNull(draftMovementList);
        assertEquals(0, draftMovementList.size());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getTempMovementListCount() {

        // first create something that can be paginated  (a bunch of tempMovements)
        for (int i = 0; i < 100; i++) {
            DraftMovement draftMovement = createTempMovementEntityHelper(LONGITUDE, LATITUDE);
            draftMovementDao.createDraftMovementEntity(draftMovement);
            em.flush();
        }

        Long count = draftMovementDao.getDraftMovementListCount();
        assertNotNull(count);
    }

    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/

    private DraftMovement createTempMovementEntityHelper(double longitude, double latitude) {

        Instant now = DateUtil.nowUTC();

        DraftMovement draftMovement = new DraftMovement();

        draftMovement.setTimestamp(now);
        draftMovement.setUpdated(now);
        draftMovement.setUpdatedBy("Arquillian");

        draftMovement.setLongitude(longitude);
        draftMovement.setLatitude(latitude);
        draftMovement.setState(TempMovementStateEnum.DRAFT);
        draftMovement.setSpeed(12D);

        return draftMovement;
    }

    private void expectedMessage(String message) {
        thrown.expect(new ThrowableMessageMatcher(new StringContains(message)));
    }
}
