package eu.europa.ec.fisheries.uvms.movement.arquillian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.time.Instant;
import java.util.List;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.movement.dao.bean.TempMovementDao;
import eu.europa.ec.fisheries.uvms.movement.entity.temp.TempMovement;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.model.constants.TempMovementStateEnum;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;

/**
 * Created by thofan on 2017-02-22.
 */

@RunWith(Arquillian.class)
public class TempMovementDaoIntTest extends TransactionalTests {

    private static final Logger LOG = LoggerFactory.getLogger(TempMovementDaoIntTest.class);

    @EJB
    private TempMovementDao tempMovementDao;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final double LONGITUDE = 9.140626D;
    private static final double LATITUDE = 57.683805D;

    /******************************************************************************************************************
     *   TEST FUNCTIONS
     ******************************************************************************************************************/

    @Test
    @OperateOnDeployment("normal")
    public void createTempMovementEntity() throws MovementDomainException {

        TempMovement tempMovement = createTempMovementEntityHelper(LONGITUDE, LATITUDE);
        assertNull(tempMovement.getId());
        TempMovement createdTempMovement = tempMovementDao.createTempMovementEntity(tempMovement);
        em.flush();

        assertNotNull(createdTempMovement.getId());
        assertNotNull(createdTempMovement.getGuid());
    }

    @Test
    @OperateOnDeployment("normal")
    public void createTempMovementEntity_NoState_Exception_Thrown() throws MovementDomainException {

        expectedException.expect(Exception.class);

        TempMovement tempMovement = createTempMovementEntityHelper(LONGITUDE, LATITUDE);
        tempMovement.setState(null);
        assertNull(tempMovement.getId());
        tempMovementDao.createTempMovementEntity(tempMovement);
        em.flush();
    }

    @Test
    @OperateOnDeployment("normal")
    public void getTempMovementByGuid() throws MovementDomainException {

        // first create one
        TempMovement tempMovement = createTempMovementEntityHelper(LONGITUDE, LATITUDE);
        TempMovement createdTempMovement = tempMovementDao.createTempMovementEntity(tempMovement);
        em.flush();
        Long createdTempMovementId = createdTempMovement.getId();

        String createdTempMovementGUID = createdTempMovement.getGuid();
        assertNotNull(createdTempMovementGUID);

        // then fetch it
        TempMovement fetchedTempMovement =  tempMovementDao.getTempMovementByGuid(createdTempMovementGUID);
        assertNotNull(fetchedTempMovement);
        Long fetchedTempMovementId = fetchedTempMovement.getId();
        assertEquals(createdTempMovementId, fetchedTempMovementId);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getTempMovementByGuid_ZeroGuid_Exception_Thrown() throws MovementDomainException {

        expectedException.expect(MovementDomainException.class);
        expectedException.expectMessage("Error when fetching temp movement");

        // we assume that the probability for zero guid exists in db is so low so we consider this safe
        tempMovementDao.getTempMovementByGuid("00000000-0000-0000-0000-000000000000");
    }

    @Test
    @OperateOnDeployment("normal")
    public void getTempMovementListPaginated() throws MovementDomainException {

        // first create something that can be paginated  (a bunch of tempMovements)
        for (int i = 0; i < 100; i++) {
            TempMovement tempMovement = createTempMovementEntityHelper(LONGITUDE, LATITUDE);
            tempMovementDao.createTempMovementEntity(tempMovement);
            em.flush();
        }

        // now we can do some paginated retrieval
        List<TempMovement> tempMovementList = tempMovementDao.getTempMovementListPaginated(1,25);
        assertNotNull(tempMovementList);
        assertEquals(25, tempMovementList.size());

        tempMovementList = tempMovementDao.getTempMovementListPaginated(2,25);
        assertNotNull(tempMovementList);
        assertEquals(25, tempMovementList.size());
    }

    @Test
    @OperateOnDeployment("normal")
    public void getTempMovementListPaginated_Page2000() throws MovementDomainException {

        // first create something that can be paginated  (a bunch of tempMovements)
        for (int i = 0; i < 100; i++) {
            TempMovement tempMovement = createTempMovementEntityHelper(LONGITUDE, LATITUDE);
            tempMovementDao.createTempMovementEntity(tempMovement);
            em.flush();
        }

        // now we can do some paginated retrieval
        List<TempMovement> tempMovementList = tempMovementDao.getTempMovementListPaginated(1,25);
        assertNotNull(tempMovementList);
        assertEquals(25, tempMovementList.size());

        tempMovementList = tempMovementDao.getTempMovementListPaginated(2000,25);
        assertNotNull(tempMovementList);
        assertEquals(0, tempMovementList.size());
    }

    @Test
    @OperateOnDeployment("normal")
    public void getTempMovementListCount() throws MovementDomainException {

        // first create something that can be paginated  (a bunch of tempMovements)
        for (int i = 0; i < 100; i++) {
            TempMovement tempMovement = createTempMovementEntityHelper(LONGITUDE, LATITUDE);
            tempMovementDao.createTempMovementEntity(tempMovement);
            em.flush();
        }

        Long count = tempMovementDao.getTempMovementListCount();
        assertNotNull(count);
    }

    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/

    private TempMovement createTempMovementEntityHelper(double longitude, double latitude) {

        Instant now = DateUtil.nowUTC();

        TempMovement tempMovement = new TempMovement();

        tempMovement.setTimestamp(now);
        tempMovement.setUpdated(now);
        tempMovement.setUpdatedBy("Arquillian");

        tempMovement.setLongitude(longitude);
        tempMovement.setLatitude(latitude);
        tempMovement.setState(TempMovementStateEnum.DRAFT);
        tempMovement.setSpeed(12D);

        return tempMovement;
    }
}
