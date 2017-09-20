package eu.europa.ec.fisheries.uvms.movement.arquillian;

import eu.europa.ec.fisheries.uvms.movement.dao.MovementSearchGroupDao;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementSearchGroupDaoException;
import eu.europa.ec.fisheries.uvms.movement.entity.group.MovementFilter;
import eu.europa.ec.fisheries.uvms.movement.entity.group.MovementFilterGroup;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


@RunWith(Arquillian.class)
public class MovementSearchGroupDaoIntTest extends TransactionalTests {

    private final static String TEST_USER_NAME = "MovementSearchGroupDaoIntTestUser";

    @EJB
    private MovementSearchGroupDao dao;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    @OperateOnDeployment("normal")
    public void createMovementFilterGroup() throws MovementSearchGroupDaoException {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup = dao.createMovementFilterGroup(movementFilterGroup);
        Assert.assertNotNull(movementFilterGroup.getId());
        em.flush();
    }


    @Test
    @OperateOnDeployment("normal")
    public void createMovementFilterGroupNoUpdateTime() throws MovementSearchGroupDaoException {
        final MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup.setUpdated(null);
        try {
            dao.createMovementFilterGroup(movementFilterGroup);
            Assert.assertNotNull(movementFilterGroup.getId());
            em.flush();
            Assert.fail("Constraint on updated (time) should generate error");
        } catch (final PersistenceException ignore) {
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void createMovementFilterGroupNoUpdateBy() throws MovementSearchGroupDaoException {
        final MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup.setUpdatedBy(null);
        try {
            dao.createMovementFilterGroup(movementFilterGroup);
            Assert.assertNotNull(movementFilterGroup.getId());
            em.flush();
            Assert.fail("Constraint on updateby should generate error");
        } catch (final PersistenceException ignore) {
        }
    }


    @Test
    @OperateOnDeployment("normal")
    public void deleteMovementFilterGroup() throws MovementSearchGroupDaoException {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        dao.createMovementFilterGroup(movementFilterGroup);
        Assert.assertNotNull(movementFilterGroup.getId());
        final Long filterGroupId = movementFilterGroup.getId();
        dao.deleteMovementFilterGroup(movementFilterGroup);
        em.flush();

        // TODO: Wrong id type
        movementFilterGroup = dao.getMovementFilterGroupById(filterGroupId.intValue());
        Assert.assertNull(movementFilterGroup);
    }


    @Test
    @OperateOnDeployment("normal")
    public void getMovementFilterGroupById() throws MovementSearchGroupDaoException {
        final MovementFilterGroup movementFilterGroup = newFilterGroup();
        dao.createMovementFilterGroup(movementFilterGroup);
        Assert.assertNotNull(movementFilterGroup.getId());
        final Long filterGroupId = movementFilterGroup.getId();
        em.flush();

        final MovementFilterGroup movementFilterGroup2 = dao.getMovementFilterGroupById(filterGroupId.intValue());
        Assert.assertNotNull(movementFilterGroup2);
        Assert.assertEquals(movementFilterGroup.getId(), movementFilterGroup2.getId());
    }


    @Test
    @OperateOnDeployment("normal")
    public void failGetMovementFilterGroupById() throws MovementSearchGroupDaoException {
        final MovementFilterGroup movementFilterGroup2 = dao.getMovementFilterGroupById(-1337);
        Assert.assertNull(movementFilterGroup2);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementFilterGroupsByUser() throws MovementSearchGroupDaoException {
        final MovementFilterGroup movementFilterGroup = newFilterGroup();
        dao.createMovementFilterGroup(movementFilterGroup);
        Assert.assertNotNull(movementFilterGroup.getId());
        final Long filterGroupId = movementFilterGroup.getId();
        em.flush();

        final List<MovementFilterGroup> movementFilterGroupsByUser = dao.getMovementFilterGroupsByUser(TEST_USER_NAME);
        Assert.assertNotNull(movementFilterGroupsByUser);
        Assert.assertTrue(movementFilterGroupsByUser.size() > 0);

        MovementFilterGroup foundIt = null;
        for(final MovementFilterGroup each : movementFilterGroupsByUser) {
            if(each.getId().equals(filterGroupId)) {
                foundIt = each;
                break;
            }
        }
        Assert.assertNotNull(foundIt);
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateMovementFilterGroup() throws MovementSearchGroupDaoException {
        final MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup.setUpdatedBy("First Value");
        dao.createMovementFilterGroup(movementFilterGroup);
        Assert.assertNotNull(movementFilterGroup.getId());
        final Long filterGroupId = movementFilterGroup.getId();
        em.flush();

        movementFilterGroup.setUpdatedBy("Second Value");
        //TODO: Uses MERGE, fix this!!!
        dao.updateMovementFilterGroup(movementFilterGroup);
        em.flush();

        final MovementFilterGroup movementFilterGroup2 = dao.getMovementFilterGroupById(filterGroupId.intValue());
        Assert.assertNotNull(movementFilterGroup2);
        Assert.assertEquals(movementFilterGroup.getId(), movementFilterGroup2.getId());
        Assert.assertEquals(movementFilterGroup.getUpdatedBy(), movementFilterGroup2.getUpdatedBy());
        Assert.assertNotEquals("First Value", movementFilterGroup2.getUpdatedBy());

        Assert.assertNotEquals(TEST_USER_NAME, movementFilterGroup2.getUpdatedBy());
    }

    @Test(expected = MovementSearchGroupDaoException.class)
    @OperateOnDeployment("normal")
    public void updateMovementFilterGroupFailedNoId() throws MovementSearchGroupDaoException {
        final MovementFilterGroup movementFilterGroup = newFilterGroup();
        dao.updateMovementFilterGroup(movementFilterGroup);
        em.flush();
    }

    private MovementFilterGroup newFilterGroup() {
        final MovementFilterGroup movementFilterGroup = new MovementFilterGroup();
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