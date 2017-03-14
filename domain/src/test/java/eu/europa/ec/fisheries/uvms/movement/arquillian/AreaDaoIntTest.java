/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.movement.arquillian;

import eu.europa.ec.fisheries.uvms.movement.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.AreaDaoException;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 *
 * @author roblar
 */
@RunWith(Arquillian.class)
public class AreaDaoIntTest {

    // ToDo: Look at if:
    // ToDo: 1. The AreaDao interface should include a method to create/persist an AreaType entity.
    // ToDo: 2. AreaType related operations should be extracted into a separate interface, e.g. AreaTypeDao.
    // ToDo: 3. Neither 1. or 2.

    final static Logger LOG = LoggerFactory.getLogger(AreaDaoIntTest.class);

    @Inject
    private UserTransaction userTransaction;

    @PersistenceContext
    private EntityManager em;

    @EJB
    private AreaDao areaDao;

    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementTestDeployment.createDeployment();
    }

    @Before
    public void before() {

        try {
            userTransaction.begin();
        } catch (NotSupportedException e) {
            LOG.error(" [ Error when setting up user transaction. ] {}", e.getMessage());
            throw new RuntimeException(" [ Error when setting up user transaction. ]", e);
        } catch (SystemException e) {
            LOG.error(" [ The transaction manager encountered an unexpected error condition that prevents future transaction services from proceeding. ] ", e.getMessage());
            throw new RuntimeException(" [ The transaction manager encountered an unexpected error condition that prevents future transaction services from proceeding. ] ", e);
        }
    }

    @After
    public void after(){

        try {
            userTransaction.rollback();
        } catch (SystemException e) {
            LOG.error(" [ The transaction manager encountered an unexpected error condition that prevents future transaction services from proceeding. ] ", e.getMessage());
            throw new RuntimeException(" [ The transaction manager encountered an unexpected error condition that prevents future transaction services from proceeding. ] ", e);
        }
    }

    @Test
    public void testCreateArea() throws AreaDaoException {

        try {
            AreaType areaType = createAreaTypeHelper();
            areaType.setName("testCreateArea");
            em.persist(areaType);
            em.flush();

            Area area = createAreaHelper();
            area.setAreaCode("testCreateArea");
            area.setAreaType(areaType);

            // Persist the Area entity
            Area createdArea = areaDao.createMovementArea(area);
            areaDao.flushMovementAreas();

            assertNotNull(createdArea);

        } catch (AreaDaoException e) {
            fail("AreaDaoIntTests.testCreateArea(): Failed to create an area.");
            LOG.error(" [ AreaDaoIntTests.testCreateArea(): Failed to create an area. ] {}", e.getMessage());
        }
    }

    @Test
    public void testGetArea() throws AreaDaoException {

        AreaType areaType = createAreaTypeHelper();
        areaType.setName("testGetArea");
        em.persist(areaType);
        em.flush();

        Area area = createAreaHelper();
        area.setAreaCode("testGetArea");
        area.setAreaType(areaType);
        Area createdArea = areaDao.createMovementArea(area);
        areaDao.flushMovementAreas();

        Area readAreaFromDatabase = areaDao.readMovementAreaById(area.getAreaId());

        assertNotNull(readAreaFromDatabase);
        assertEquals(readAreaFromDatabase.getAreaId(), area.getAreaId());
    }

    @Test
    public void testGetAllAreas() throws AreaDaoException {

        List<Area> readAllAreasFromDatabase = null;

        try {

            for (int i = 0; i < 3; i++) {
                AreaType areaType = createAreaTypeHelper();
                areaType.setName("areaTypeName_testGetAllAreas_" + i);
                em.persist(areaType);
                em.flush();

                Area area = createAreaHelper();
                area.setAreaCode("areaCode_testGetAllAreas_" + i);
                area.setAreaType(areaType);
                Area createdArea = areaDao.createMovementArea(area);
                areaDao.flushMovementAreas();
            }

            readAllAreasFromDatabase = areaDao.getAreas();

            assertNotNull(readAllAreasFromDatabase);
            assertNotEquals(0, readAllAreasFromDatabase.size());

        } catch (AreaDaoException e) {
            fail("AreaDaoIntTests.testGetAllAreas(): Failed to get a list of all entries in the database table movement.areas.");
            LOG.error(" [ AreaDaoIntTests.testGetAllAreas(): Failed to get a list of all entries in the database table movement.areas. ] {}", e.getMessage());
        }
    }

    @Test
    public void failCreateArea_remoteId_erroneousFieldSize() throws AreaDaoException {

        try {
            Area failingArea = new Area();
            failingArea.setRemoteId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"); //61 characters.
            em.persist(failingArea);
            em.flush();

            fail("Negative test: Field size constraint violation for column remoteId in table movement.area. Setting a string larger than 60 characters is expected to fail.");

        } catch (RuntimeException e) {
            assertTrue(true);
            LOG.error(" [ Negative test: Field size constraint violation for column remoteId in table movement.area. Setting a string larger than 60 characters is expected to fail. ] {}", e.getMessage());
        }
    }

    // Note: This test is testing an AreaType entity. The Area entity has a many-to-one relationship to the AreaType entity.
    @Test
    public void failCreateAreaType_areaTypeName_uniqueNameConstraint() throws AreaDaoException {

        try {
            AreaType originalAreaType = createAreaTypeHelper();
            originalAreaType.setName("failCreateAreaType_areaTypeName_uniqueNameConstraint");
            em.persist(originalAreaType);
            em.flush();

            AreaType conflictingAreaType = createAreaTypeHelper();
            conflictingAreaType.setName("failCreateAreaType_areaTypeName_uniqueNameConstraint");
            em.persist(conflictingAreaType);
            em.flush();

            fail("Negative test: Unique name constraint violation for column areatype_name in table movement.areatype. Attempting to set an already existing name is expected to fail.");

        } catch (RuntimeException e) {
            assertTrue(true);
            LOG.error(" [ Negative test: Unique name constraint violation for column areatype_name in table movement.areatype. Attempting to set an already existing name is expected to fail. ] {}", e.getMessage());
        }
    }

    @Test
    public void failCreateArea_areaCode_uniqueNameConstraint() {

        try {

            AreaType originalAreaType = createAreaTypeHelper();
            originalAreaType.setName("failCreateArea_areaCode_uniqueNameConstraint_original");
            em.persist(originalAreaType);
            em.flush();

            Area originalArea = createAreaHelper();
            originalArea.setAreaCode("failCreateArea_areaCode_uniqueNameConstraint");
            originalArea.setAreaType(originalAreaType);
            em.persist(originalArea);
            em.flush();

            AreaType conflictingAreaType = createAreaTypeHelper();
            conflictingAreaType.setName("failCreateArea_areaCode_uniqueNameConstraint_conflicting");
            em.persist(conflictingAreaType);
            em.flush();

            Area conflictingArea = createAreaHelper();
            conflictingArea.setAreaCode("failCreateArea_areaCode_uniqueNameConstraint"); //Introducing name constraint violation here.
            conflictingArea.setAreaType(conflictingAreaType);
            em.persist(conflictingArea);
            em.flush();

            fail("Negative test: Unique name constraint violation for column area_code in table movement.area. Attempting to set an already existing name is expected to fail.");

        } catch (RuntimeException e) {
            assertTrue(true);
            LOG.error(" [ Negative test: Unique name constraint violation for column area_code in table movement.area. Attempting to set an already existing name is expected to fail. ] {}", e.getMessage());
        }
    }

    @Test
    public void failCreateArea_areaUpdattim_setToNull() throws AreaDaoException {

        try {
            AreaType areaType = createAreaTypeHelper();
            areaType.setName("failCreateArea_areaUpdattim_setToNull");
            em.persist(areaType);
            em.flush();

            Area area = createAreaHelper();
            area.setAreaCode("failCreateArea_areaUpdattim_setToNull");
            area.setAreaType(areaType);
            em.persist(area);
            em.flush();

            area.setAreaUpdattim(null); //Introducing null constraint violation here.
            em.persist(area);
            em.flush();

            fail("Negative test: Not null constraint violation for column area_updattim in table movement.area. Setting a null value is expected to fail.");

        } catch (RuntimeException e) {
            assertTrue(true);
            LOG.error(" [ Negative test: Not null constraint violation for column area_updattim in table movement.area. Setting a null value is expected to fail. ] {}", e.getMessage());
        }
    }

    @Test
    public void failCreateArea_areaUpuser_setToNull() {

        try {
            AreaType areaType = createAreaTypeHelper();
            areaType.setName("failCreateArea_areaUpuser_setToNull");
            em.persist(areaType);
            em.flush();

            Area area = new Area();
            area.setAreaName("areaName");
            area.setRemoteId("remoteId");
            area.setAreaUpdattim(DateUtil.nowUTC());
            area.setAreaCode("failCreateArea_areaUpuser_setToNull");
            area.setAreaType(areaType);

            area.setAreaUpuser(null); //Introducing null constraint violation here.

            em.persist(area);
            em.flush();

            fail("Negative test: Not null constraint violation for column area_upuser in table movement.area. Setting a null value is expected to fail.");

        } catch (RuntimeException e) {
            assertTrue(true);
            LOG.error(" [ Negative test: Not null constraint violation for column area_upuser in table movement.area. Setting a null value is expected to fail. ] {}", e.getMessage());
        }
    }

    @Test
    public void failCreateArea_areaUpuser_erroneousFieldSize() {

        try {
            AreaType areaType = createAreaTypeHelper();
            areaType.setName("failCreateArea_areaUpuser_erroneousFieldSize");
            em.persist(areaType);
            em.flush();

            Area failingArea = createAreaHelper();
            failingArea.setAreaUpuser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"); //61 characters.
            em.persist(failingArea);
            em.flush();

            fail("Negative test: Field size constraint violation for column area_upuser in table movement.area. Setting a string larger than 60 characters is expected to fail.");

        } catch (RuntimeException e) {
            assertTrue(true);
            LOG.error(" [ Negative test: Field size constraint violation for column area_upuser in table movement.area. Setting a string larger than 60 characters is expected to fail. ] {}", e.getMessage());
        }
    }

    @Test
    public void failGetArea() throws AreaDaoException {

        Area failingAreaRead = areaDao.readMovementAreaById(-42L);
        assertNull(failingAreaRead);
    }

    private Area createAreaHelper() {

        Area area = new Area();
        area.setAreaName("areaName");
        area.setRemoteId("remoteId");
        area.setAreaUpdattim(DateUtil.nowUTC());
        area.setAreaUpuser("areaUpuser");

        return area;
    }

    private AreaType createAreaTypeHelper() {

        AreaType areaType = new AreaType();
        areaType.setUpdatedTime(DateUtil.nowUTC());
        areaType.setUpdatedUser("areaTypeUpdatedUser");

        return areaType;
    }
}