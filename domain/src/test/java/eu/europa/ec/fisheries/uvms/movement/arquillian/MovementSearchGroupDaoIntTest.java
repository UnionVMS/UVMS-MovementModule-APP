package eu.europa.ec.fisheries.uvms.movement.arquillian;

import eu.europa.ec.fisheries.uvms.movement.dao.MovementSearchGroupDao;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementSearchGroupDaoException;
import eu.europa.ec.fisheries.uvms.movement.entity.group.MovementFilter;
import eu.europa.ec.fisheries.uvms.movement.entity.group.MovementFilterGroup;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class MovementSearchGroupDaoIntTest extends TransactionalTests {

    private final static String TEST_USER_NAME = "MovementSearchGroupDaoIntTestUser";

    @EJB
    private MovementSearchGroupDao dao;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    @OperateOnDeployment("normal")
    public void createMovementFilterGroup() throws MovementSearchGroupDaoException {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup = dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();
    }

    @Test
    @OperateOnDeployment("normal")
    public void createMovementFilterGroupNoUpdateTime() throws MovementSearchGroupDaoException {
        expectedException.expect(PersistenceException.class);

        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup.setUpdated(null);
        dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();
    }

    @Test
    @OperateOnDeployment("normal")
    public void createMovementFilterGroupNoUpdateBy() throws MovementSearchGroupDaoException {
        expectedException.expect(PersistenceException.class);

        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup.setUpdatedBy(null);
        dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();
    }

    @Test
    @OperateOnDeployment("normal")
    public void deleteMovementFilterGroup() throws MovementSearchGroupDaoException {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup = dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        dao.deleteMovementFilterGroup(movementFilterGroup);
        em.flush();

        // TODO: Wrong id type. Will be fixed later. /ksm
        // TODO: This change will require updates in  some DAOs and in a xsd schema(MovementSearch.xsd) as well.
        movementFilterGroup = dao.getMovementFilterGroupById(movementFilterGroup.getId().intValue());
        assertNull(movementFilterGroup);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementFilterGroupById() throws MovementSearchGroupDaoException {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup = dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();

        MovementFilterGroup movementFilterGroup2 = dao.getMovementFilterGroupById(movementFilterGroup.getId().intValue());
        assertNotNull(movementFilterGroup2);
        assertEquals(movementFilterGroup.getId(), movementFilterGroup2.getId());
    }

    @Test
    @OperateOnDeployment("normal")
    public void failGetMovementFilterGroupById() throws MovementSearchGroupDaoException {
        MovementFilterGroup movementFilterGroup = dao.getMovementFilterGroupById(-1337);
        assertNull(movementFilterGroup);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementFilterGroupsByUser() throws MovementSearchGroupDaoException {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup = dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();

        List<MovementFilterGroup> movementFilterGroupsByUser = dao.getMovementFilterGroupsByUser(TEST_USER_NAME);
        assertNotNull(movementFilterGroupsByUser);
        assertTrue(movementFilterGroupsByUser.size() > 0);

        MovementFilterGroup foundIt = null;
        for(MovementFilterGroup mfg : movementFilterGroupsByUser) {
            if(mfg.getId().equals(movementFilterGroup.getId())) {
                foundIt = mfg;
                break;
            }
        }
        assertNotNull(foundIt);
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateMovementFilterGroup() throws MovementSearchGroupDaoException {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup.setUpdatedBy("First Value");
        movementFilterGroup = dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();

        movementFilterGroup.setUpdatedBy("Second Value");
        movementFilterGroup = dao.updateMovementFilterGroup(movementFilterGroup);
        em.flush();

        MovementFilterGroup movementFilterGroup2 = dao.getMovementFilterGroupById(movementFilterGroup.getId().intValue());
        assertNotNull(movementFilterGroup2);
        assertEquals(movementFilterGroup.getId(), movementFilterGroup2.getId());
        assertEquals(movementFilterGroup.getUpdatedBy(), movementFilterGroup2.getUpdatedBy());
        assertNotEquals("First Value", movementFilterGroup2.getUpdatedBy());

        assertNotEquals(TEST_USER_NAME, movementFilterGroup2.getUpdatedBy());
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateMovementFilterGroupFailedNoId() throws MovementSearchGroupDaoException {
        expectedException.expect(MovementSearchGroupDaoException.class);
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        dao.updateMovementFilterGroup(movementFilterGroup);
        em.flush();
    }

    private MovementFilterGroup newFilterGroup() {
        MovementFilterGroup movementFilterGroup = new MovementFilterGroup();
        movementFilterGroup.setName("Test");
        movementFilterGroup.setActive("true");
        movementFilterGroup.setDynamic("true");
        movementFilterGroup.setFilters(new ArrayList<MovementFilter>());
        movementFilterGroup.setGlobal("true");
        movementFilterGroup.setUser(TEST_USER_NAME);
        movementFilterGroup.setUpdated(Calendar.getInstance().getTime());
        movementFilterGroup.setUpdatedBy(TEST_USER_NAME);

        return movementFilterGroup;
    }
}
