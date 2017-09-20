package eu.europa.ec.fisheries.uvms.movement.arquillian;

import eu.europa.ec.fisheries.uvms.movement.dao.TempMovementDao;
import eu.europa.ec.fisheries.uvms.movement.entity.temp.TempMovement;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.Date;
import java.util.List;

/**
 * Created by thofan on 2017-02-22.
 */

@RunWith(Arquillian.class)
public class TempMovementDaoIntTest extends BuildMovementTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(TempMovementDaoIntTest.class);

    @Inject
    UserTransaction userTransaction;

    @EJB
    private TempMovementDao tempMovementDao;


    @PersistenceContext
    private EntityManager em;


    /******************************************************************************************************************
     *   SETUP FUNCTIONS
     ******************************************************************************************************************/

    @Before
    public void before() throws SystemException, NotSupportedException {
        userTransaction.begin();
    }

    @After
    public void after() throws SystemException {
        userTransaction.rollback();
    }

    /******************************************************************************************************************
     *   TEST FUNCTIONS
     ******************************************************************************************************************/


    @Test
    @OperateOnDeployment("normal")
    public void createTempMovementEntity() {

        final double longitude = 9.140626D;
        final double latitude = 57.683805D;

        final TempMovement tempMovement = createTempMovementEntityHelper(longitude,latitude);
        Assert.assertTrue(tempMovement.getId() == null);
        final TempMovement createdTempMovement = tempMovementDao.createTempMovementEntity(tempMovement);
        em.flush();
        Assert.assertTrue(createdTempMovement.getId() != null);
        Assert.assertTrue(createdTempMovement.getGuid() != null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void createTempMovementEntity_NoState() {

        try {

            final double longitude = 9.140626D;
            final double latitude = 57.683805D;

            final TempMovement tempMovement = createTempMovementEntityHelper(longitude,latitude);
            tempMovement.setState(null);
            Assert.assertTrue(tempMovement.getId() == null);
            final TempMovement createdTempMovement = tempMovementDao.createTempMovementEntity(tempMovement);
            em.flush();
            Assert.fail("This is a db constraint violation and should not occur");
        } catch (final Exception e) {
            Assert.assertTrue(e != null);
        }
    }


    @Test
    @OperateOnDeployment("normal")
    public void getTempMovementByGuid() {
        try {
            final double longitude = 9.140626D;
            final double latitude = 57.683805D;

            // first create one
            final TempMovement tempMovement = createTempMovementEntityHelper(longitude, latitude);
            final TempMovement createdTempMovement = tempMovementDao.createTempMovementEntity(tempMovement);
            em.flush();
            final Long createdTempMovementId = createdTempMovement.getId();

            final String createdTempMovementGUID = createdTempMovement.getGuid();
            Assert.assertTrue(createdTempMovementGUID != null);


            // then fetch it

            final TempMovement fetchedTempMovement =  tempMovementDao.getTempMovementByGuid(createdTempMovementGUID);
            Assert.assertTrue(fetchedTempMovement != null);
            final Long fetchedTempMovementId = fetchedTempMovement.getId();
            Assert.assertTrue(createdTempMovementId.equals(fetchedTempMovementId));
        } catch (final MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getTempMovementByGuid_ZeroGuid() {
        try {

            // we assume that the probability for zeroguid exists in db is so low so we consider this safe
            final TempMovement fetchedTempMovement =  tempMovementDao.getTempMovementByGuid("00000000-0000-0000-0000-000000000000");
            Assert.fail("functions throws exception at nothing in result, so this should not happen");
        } catch (final MovementDaoException e) {
            Assert.assertTrue(e != null);
        }
    }


    @Test
    @OperateOnDeployment("normal")
    public void getTempMovementListPaginated() {

        try {
            // first create something that can be paginated  (a bunch of tempMovements)
            double longitude = 9.140626D;
            double latitude = 57.683805D;
            for (int i = 0; i < 100; i++) {
                final TempMovement tempMovement = createTempMovementEntityHelper(longitude, latitude);
                final TempMovement createdTempMovement = tempMovementDao.createTempMovementEntity(tempMovement);
                em.flush();
                longitude += 0.000010;
                latitude += 0.000010;
            }

            // now we can do some paginated retrieval

            List<TempMovement> list = tempMovementDao.getTempMovementListPaginated(1,25);
            Assert.assertTrue(list != null);
            Assert.assertTrue(list.size() == 25);

            list = tempMovementDao.getTempMovementListPaginated(2,25);
            Assert.assertTrue(list != null);
            Assert.assertTrue(list.size() == 25);


        }catch(final MovementDaoException e){
            Assert.fail(e.toString());
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getTempMovementListPaginated_Page2000() {

        try {
            // first create something that can be paginated  (a bunch of tempMovements)
            double longitude = 9.140626D;
            double latitude = 57.683805D;
            for (int i = 0; i < 100; i++) {
                final TempMovement tempMovement = createTempMovementEntityHelper(longitude, latitude);
                final TempMovement createdTempMovement = tempMovementDao.createTempMovementEntity(tempMovement);
                em.flush();
                longitude += 0.000010;
                latitude += 0.000010;
            }

            // now we can do some paginated retrieval

            List<TempMovement> list = tempMovementDao.getTempMovementListPaginated(1,25);
            Assert.assertTrue(list != null);
            Assert.assertTrue(list.size() == 25);

            list = tempMovementDao.getTempMovementListPaginated(2000,25);
            Assert.assertTrue(list != null);
            Assert.assertTrue(list.size() == 0);


        }catch(final MovementDaoException e){
            Assert.fail(e.toString());
        }
    }



    @Test
    @OperateOnDeployment("normal")
    public void getTempMovementListCount() {

        try {
            // first create something that can be paginated  (a bunch of tempMovements)
            double longitude = 9.140626D;
            double latitude = 57.683805D;
            for (int i = 0; i < 100; i++) {
                final TempMovement tempMovement = createTempMovementEntityHelper(longitude, latitude);
                final TempMovement createdTempMovement = tempMovementDao.createTempMovementEntity(tempMovement);
                em.flush();
                longitude += 0.000010;
                latitude += 0.000010;
            }


            final Long count = tempMovementDao.getTempMovementListCount();
            Assert.assertTrue(count != null);


        }catch(final MovementDaoException e){
            Assert.fail(e.toString());
        }



    }


    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/


    private TempMovement createTempMovementEntityHelper(final double longitude, final double latitude) {

        final Date now = DateUtil.nowUTC();

        final TempMovement tempMovement = new TempMovement();

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
