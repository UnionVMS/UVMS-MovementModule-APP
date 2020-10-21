package eu.europa.ec.fisheries.uvms.movement.service.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.group.MovementFilterGroup;

@RunWith(Arquillian.class)
public class MovementSearchGroupDaoIntTest extends TransactionalTests {

    private final static String TEST_USER_NAME = "MovementSearchGroupDaoIntTestUser";

    @EJB
    private MovementSearchGroupDao dao;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementFilterGroup()  {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup = dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementFilterGroupNoUpdateTime()  {
        expectedException.expect(ConstraintViolationException.class);

        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup.setUpdated(null);
        dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementFilterGroupNoUpdateBy()  {
        expectedException.expect(ConstraintViolationException.class);

        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup.setUpdatedBy(null);
        dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void deleteMovementFilterGroup()  {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup = dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        dao.deleteMovementFilterGroup(movementFilterGroup);
        em.flush();

        movementFilterGroup = dao.getMovementFilterGroupById(movementFilterGroup.getId());
        assertNull(movementFilterGroup);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementFilterGroupById()  {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup = dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();

        MovementFilterGroup movementFilterGroup2 = dao.getMovementFilterGroupById(movementFilterGroup.getId());
        assertNotNull(movementFilterGroup2);
        assertEquals(movementFilterGroup.getId(), movementFilterGroup2.getId());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void failGetMovementFilterGroupById()  {
        MovementFilterGroup movementFilterGroup = dao.getMovementFilterGroupById(UUID.randomUUID());
        assertNull(movementFilterGroup);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementFilterGroupsByUser()  {
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
    @OperateOnDeployment("movementservice")
    public void updateMovementFilterGroup()  {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup.setUpdatedBy("First Value");
        movementFilterGroup = dao.createMovementFilterGroup(movementFilterGroup);
        assertNotNull(movementFilterGroup.getId());
        em.flush();

        movementFilterGroup.setUpdatedBy("Second Value");
        movementFilterGroup = dao.updateMovementFilterGroup(movementFilterGroup);
        em.flush();

        MovementFilterGroup movementFilterGroup2 = dao.getMovementFilterGroupById(movementFilterGroup.getId());
        assertNotNull(movementFilterGroup2);
        assertEquals(movementFilterGroup.getId(), movementFilterGroup2.getId());
        assertEquals(movementFilterGroup.getUpdatedBy(), movementFilterGroup2.getUpdatedBy());
        assertNotEquals("First Value", movementFilterGroup2.getUpdatedBy());

        assertNotEquals(TEST_USER_NAME, movementFilterGroup2.getUpdatedBy());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void updateMovementFilterGroupFailedNoId()  {
        expectedException.expect(EJBTransactionRolledbackException.class);
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
