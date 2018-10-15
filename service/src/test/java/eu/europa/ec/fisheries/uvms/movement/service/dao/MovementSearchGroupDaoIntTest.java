package eu.europa.ec.fisheries.uvms.movement.service.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.persistence.PersistenceException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.group.MovementFilterGroup;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

@RunWith(Arquillian.class)
public class MovementSearchGroupDaoIntTest extends TransactionalTests {

    private final static String TEST_USER_NAME = "MovementSearchGroupDaoIntTestUser";

    @EJB
    private MovementSearchGroupDao dao;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void createMovementFilterGroup() throws MovementServiceException {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup = dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();
    }

    @Test
    public void createMovementFilterGroupNoUpdateTime() throws MovementServiceException {
        expectedException.expect(PersistenceException.class);

        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup.setUpdated(null);
        dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();
    }

    @Test
    public void createMovementFilterGroupNoUpdateBy() throws MovementServiceException {
        expectedException.expect(PersistenceException.class);

        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup.setUpdatedBy(null);
        dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();
    }

    @Test
    public void deleteMovementFilterGroup() throws MovementServiceException {
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
    public void getMovementFilterGroupById() throws MovementServiceException {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup = dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();

        MovementFilterGroup movementFilterGroup2 = dao.getMovementFilterGroupById(movementFilterGroup.getId().intValue());
        assertNotNull(movementFilterGroup2);
        assertEquals(movementFilterGroup.getId(), movementFilterGroup2.getId());
    }

    @Test
    public void failGetMovementFilterGroupById() throws MovementServiceException {
        MovementFilterGroup movementFilterGroup = dao.getMovementFilterGroupById(-1337);
        assertNull(movementFilterGroup);
    }

    @Test
    public void getMovementFilterGroupsByUser() throws MovementServiceException {
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
    public void updateMovementFilterGroup() throws MovementServiceException {
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
    public void updateMovementFilterGroupFailedNoId() throws MovementServiceException {
        expectedException.expect(MovementServiceException.class);
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        dao.updateMovementFilterGroup(movementFilterGroup);
        em.flush();
    }

    private MovementFilterGroup newFilterGroup() {
        MovementFilterGroup movementFilterGroup = new MovementFilterGroup();
        movementFilterGroup.setName("Test");
        movementFilterGroup.setActive("true");
        movementFilterGroup.setDynamic("true");
        movementFilterGroup.setFilters(new ArrayList<>());
        movementFilterGroup.setGlobal("true");
        movementFilterGroup.setUser(TEST_USER_NAME);
        movementFilterGroup.setUpdated(Instant.now());
        movementFilterGroup.setUpdatedBy(TEST_USER_NAME);

        return movementFilterGroup;
    }
}
