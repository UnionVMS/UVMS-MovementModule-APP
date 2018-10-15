package eu.europa.ec.fisheries.uvms.movement.service.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import org.hamcrest.core.StringContains;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.ThrowableMessageMatcher;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;

@RunWith(Arquillian.class)
public class AreaDaoTest extends TransactionalTests {

    @EJB
    private AreaDao areaDao;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetAreaTypeByCode() {
        AreaType areaType = MockData.createAreaType();
        em.persist(areaType);
        em.flush();
        
        AreaType output = areaDao.getAreaTypeByCode("TestAreaType");
        assertEquals(areaType, output);
        
        output = areaDao.getAreaTypeByCode("TestAreaType2");// should result in a null return
        assertNull(output);
        
        try {
            //trying to create a duplicate
            AreaType areaTypeDuplicate = MockData.createAreaType();
            em.persist(areaTypeDuplicate);
            em.flush();
            fail("duplicate namnes should not be allowed"); //thus the catch clause for multiple areas in the method is invalid
        } catch (Exception e) {
            assertTrue(true);
        }
    }
    
    
    @Test
    public void testGetAreaByRemoteIDAndCode() {
        AreaType areaType = MockData.createAreaType();
        em.persist(areaType);
        em.flush();

        Area area = MockData.createArea(areaType);
        
        Area createdArea = areaDao.createMovementArea(area);

        Area output = areaDao.getAreaByCode(area.getAreaCode()); //remoteId is not used at all  TestAreaCode
        assertEquals(createdArea.getAreaId(), output.getAreaId());
        
        output = areaDao.getAreaByCode("ShouldNotExist");
        assertNull(output);
    }

    @Test
    public void testGetAreaByRemoteIDAndCode_willFail() {

        thrown.expect(EJBTransactionRolledbackException.class);
        expectedMessage("No valid input parameters to method getAreaByRemoteIdAndCode");

        AreaType areaType = MockData.createAreaType();

        em.persist(areaType);
        em.flush();

        Area area = MockData.createArea(areaType);

        areaDao.createMovementArea(area);

        areaDao.getAreaByCode(null);
    }
    
    private void expectedMessage(String message) {
        thrown.expect(new ThrowableMessageMatcher(new StringContains(message)));
    }
    
}
