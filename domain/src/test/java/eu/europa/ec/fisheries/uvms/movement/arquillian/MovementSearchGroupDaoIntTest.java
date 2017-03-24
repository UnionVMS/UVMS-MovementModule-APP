package eu.europa.ec.fisheries.uvms.movement.arquillian;

import eu.europa.ec.fisheries.uvms.movement.dao.MovementSearchGroupDao;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementSearchGroupDaoException;
import eu.europa.ec.fisheries.uvms.movement.entity.group.MovementFilter;
import eu.europa.ec.fisheries.uvms.movement.entity.group.MovementFilterGroup;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
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
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup.setUpdated(null);
        try {
            dao.createMovementFilterGroup(movementFilterGroup);
            Assert.assertNotNull(movementFilterGroup.getId());
            em.flush();
            Assert.fail("Constraint on updated (time) should generate error");
        } catch (PersistenceException ignore) {
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void createMovementFilterGroupNoUpdateBy() throws MovementSearchGroupDaoException {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        movementFilterGroup.setUpdatedBy(null);
        try {
            dao.createMovementFilterGroup(movementFilterGroup);
            Assert.assertNotNull(movementFilterGroup.getId());
            em.flush();
            Assert.fail("Constraint on updateby should generate error");
        } catch (PersistenceException ignore) {
        }
    }


    @Test
    @OperateOnDeployment("normal")
    public void deleteMovementFilterGroup() throws MovementSearchGroupDaoException {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        dao.createMovementFilterGroup(movementFilterGroup);
        Assert.assertNotNull(movementFilterGroup.getId());
        Long filterGroupId = movementFilterGroup.getId();
        dao.deleteMovementFilterGroup(movementFilterGroup);
        em.flush();

        // TODO: Wrong id type
        movementFilterGroup = dao.getMovementFilterGroupById(filterGroupId.intValue());
        Assert.assertNull(movementFilterGroup);
    }


    @Test
    @OperateOnDeployment("normal")
    public void getMovementFilterGroupById() throws MovementSearchGroupDaoException {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        dao.createMovementFilterGroup(movementFilterGroup);
        Assert.assertNotNull(movementFilterGroup.getId());
        Long filterGroupId = movementFilterGroup.getId();
        em.flush();

        MovementFilterGroup movementFilterGroup2 = dao.getMovementFilterGroupById(filterGroupId.intValue());
        Assert.assertNotNull(movementFilterGroup2);
        Assert.assertEquals(movementFilterGroup.getId(), movementFilterGroup2.getId());
    }


    @Test
    @OperateOnDeployment("normal")
    public void failGetMovementFilterGroupById() throws MovementSearchGroupDaoException {
        MovementFilterGroup movementFilterGroup2 = dao.getMovementFilterGroupById(-1337);
        Assert.assertNull(movementFilterGroup2);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementFilterGroupsByUser() throws MovementSearchGroupDaoException {
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        dao.createMovementFilterGroup(movementFilterGroup);
        Assert.assertNotNull(movementFilterGroup.getId());
        Long filterGroupId = movementFilterGroup.getId();
        em.flush();

        List<MovementFilterGroup> movementFilterGroupsByUser = dao.getMovementFilterGroupsByUser(TEST_USER_NAME);
        Assert.assertNotNull(movementFilterGroupsByUser);
        Assert.assertTrue(movementFilterGroupsByUser.size() > 0);

        MovementFilterGroup foundIt = null;
        for(MovementFilterGroup each : movementFilterGroupsByUser) {
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
        MovementFilterGroup movementFilterGroup = newFilterGroup();
        dao.createMovementFilterGroup(movementFilterGroup);
        Assert.assertNotNull(movementFilterGroup.getId());
        Long filterGroupId = movementFilterGroup.getId();
        em.flush();

        movementFilterGroup.setUpdatedBy("Test Ghost");
        //TODO: Uses MERGE, fix this!!!
        dao.updateMovementFilterGroup(movementFilterGroup);
        em.flush();

        MovementFilterGroup movementFilterGroup2 = dao.getMovementFilterGroupById(filterGroupId.intValue());
        Assert.assertNotNull(movementFilterGroup2);
        Assert.assertEquals(movementFilterGroup.getId(), movementFilterGroup2.getId());
        Assert.assertNotEquals(TEST_USER_NAME, movementFilterGroup2.getUpdatedBy());
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