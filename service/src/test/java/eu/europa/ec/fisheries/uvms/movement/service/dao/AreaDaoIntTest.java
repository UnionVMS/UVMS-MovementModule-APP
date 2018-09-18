/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.movement.service.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.List;
import javax.ejb.EJB;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementDomainException;

/**
 * @author roblar
 */
@RunWith(Arquillian.class)
public class AreaDaoIntTest extends TransactionalTests {

    // ToDo: Look at if:
    // ToDo: 1. The AreaDao interface should include a method to create/persist an AreaType entity.
    // ToDo: 2. AreaType related operations should be extracted into a separate interface, e.g. AreaTypeDao.
    // ToDo: 3. Neither 1. or 2.

    private static final Logger LOG = LoggerFactory.getLogger(AreaDaoIntTest.class);

    @EJB
    private AreaDao areaDao;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testCreateArea() throws MovementDomainException {

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
    }

    @Test
    public void testGetArea() throws MovementDomainException {

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
    public void testGetAllAreas() throws MovementDomainException {

        List<Area> readAllAreasFromDatabase;

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
    }

    @Test
    public void failCreateArea_remoteId_erroneousFieldSize() {

        expectedException.expect(RuntimeException.class);

        Area failingArea = new Area();
        failingArea.setRemoteId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"); //61 characters. Constraint violation
        em.persist(failingArea);
        em.flush();
    }

    // Note: This test is testing an AreaType entity. The Area entity has a many-to-one relationship to the AreaType entity.
    // Unique name constraint violation
    @Test
    public void failCreateAreaType_areaTypeName_uniqueNameConstraint() {

        expectedException.expect(RuntimeException.class);

        AreaType originalAreaType = createAreaTypeHelper();
        originalAreaType.setName("failCreateAreaType_areaTypeName_uniqueNameConstraint");
        em.persist(originalAreaType);
        em.flush();

        AreaType conflictingAreaType = createAreaTypeHelper();
        conflictingAreaType.setName("failCreateAreaType_areaTypeName_uniqueNameConstraint");
        em.persist(conflictingAreaType);
        em.flush();
    }

    // Unique name constraint violation for column area_code in table movement.area.
    @Test
    public void failCreateArea_areaCode_uniqueNameConstraint() {

        expectedException.expect(RuntimeException.class);

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
    }

    // Not null constraint violation for column area_updattim in table movement.area.
    @Test
    public void failCreateArea_areaUpdattim_setToNull() {

        expectedException.expect(RuntimeException.class);

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
    }

    // Not null constraint violation for column area_upuser in table movement.area.
    @Test
    public void failCreateArea_areaUpuser_setToNull() {

        expectedException.expect(RuntimeException.class);

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
    }

    // Field size constraint violation for column area_upuser in table movement.area.
    @Test
    public void failCreateArea_areaUpuser_erroneousFieldSize() {

        expectedException.expect(RuntimeException.class);

        AreaType areaType = createAreaTypeHelper();
        areaType.setName("failCreateArea_areaUpuser_erroneousFieldSize");
        em.persist(areaType);
        em.flush();

        Area failingArea = createAreaHelper();
        failingArea.setAreaUpuser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"); //61 characters.
        em.persist(failingArea);
        em.flush();
    }

    @Test
    public void failGetArea() throws MovementDomainException {
        Area failingAreaRead = areaDao.readMovementAreaById(-42L);
        assertNull(failingAreaRead);
    }

    private Area createAreaHelper() {
        Area area = new Area();
        area.setAreaName("areaName");
        area.setAreaCode("AreaCode" + MovementHelpers.getRandomIntegers(10));
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
