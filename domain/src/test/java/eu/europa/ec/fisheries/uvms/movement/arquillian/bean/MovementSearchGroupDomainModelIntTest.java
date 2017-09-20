package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;


import java.util.List;

import javax.ejb.EJB;
import javax.persistence.PersistenceException;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementSearchGroupDomainModelBean;

@RunWith(Arquillian.class)
public class MovementSearchGroupDomainModelIntTest extends TransactionalTests {

    private final static String TEST_USER_NAME = "MovementSearchGroupDomainModelIntTestsUser";

    @EJB
    private MovementSearchGroupDomainModelBean movementSearchGroupDomainModelBean;

    @Test
    @OperateOnDeployment("normal")
    public void createMovementSearchGroup() throws MovementModelException {
        final MovementSearchGroup movementSearchGroup = createSearchGroup();
        final MovementSearchGroup movementSearchGroupAfterPersist = movementSearchGroupDomainModelBean.createMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);
        em.flush();
        Assert.assertNotNull(movementSearchGroupAfterPersist.getId());
        // TODO: This is actually wierd... We are marshalling and unmarshalling back and forth, watch out for this when merging APP and DB
        Assert.assertNull(movementSearchGroup.getId());
    }

    @Test
    @OperateOnDeployment("normal")
    public void failCreateMovementSearchGroupNoUserName() throws MovementModelException {
        final MovementSearchGroup movementSearchGroup = createSearchGroup();
        try {
            movementSearchGroupDomainModelBean.createMovementSearchGroup(movementSearchGroup, null);
            em.flush();
            Assert.assertFalse("This should fail, username is null", false);
        } catch (final PersistenceException ex) {
            Assert.assertTrue(ex.getMessage().contains("ConstraintViolation"));
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void failCreateMovementSearchGroupNoName() {
        final MovementSearchGroup movementSearchGroup = createSearchGroup();
        try {
            movementSearchGroup.setName(null);
            movementSearchGroupDomainModelBean.createMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);
            em.flush();
            Assert.assertFalse("This should fail, name is null", false);
        } catch (final MovementModelException ex) {
            Assert.assertTrue(ex.getMessage().contains("Could not create movement search group"));
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementSearchGroup() throws MovementModelException {
        final MovementSearchGroup movementSearchGroup = movementSearchGroupDomainModelBean.createMovementSearchGroup(createSearchGroup(), TEST_USER_NAME);
        em.flush();
        Assert.assertNotNull(movementSearchGroup.getId());

        final MovementSearchGroup tryToFindIt = movementSearchGroupDomainModelBean.getMovementSearchGroup(movementSearchGroup.getId());
        Assert.assertNotNull(tryToFindIt);

        Assert.assertEquals(movementSearchGroup.getId(), tryToFindIt.getId());
        Assert.assertEquals(movementSearchGroup.getName(), tryToFindIt.getName());
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementSearchGroupsByUser() throws MovementModelException {
        final MovementSearchGroup movementSearchGroup = movementSearchGroupDomainModelBean.createMovementSearchGroup(createSearchGroup(), TEST_USER_NAME);
        em.flush();
        Assert.assertNotNull(movementSearchGroup.getId());

        final List<MovementSearchGroup> movementSearchGroupsByUser = movementSearchGroupDomainModelBean.getMovementSearchGroupsByUser(TEST_USER_NAME);
        Assert.assertNotNull(movementSearchGroupsByUser);
        Assert.assertTrue(movementSearchGroupsByUser.size() == 1);

        final MovementSearchGroup tryToFindIt = movementSearchGroupsByUser.get(0);

        Assert.assertEquals(movementSearchGroup.getId(), tryToFindIt.getId());
        Assert.assertEquals(movementSearchGroup.getName(), tryToFindIt.getName());
    }

    @Test
    @OperateOnDeployment("normal")
    public void deleteMovementSearchGroup() throws MovementModelException {
        final MovementSearchGroup movementSearchGroup = movementSearchGroupDomainModelBean.createMovementSearchGroup(createSearchGroup(), TEST_USER_NAME);
        em.flush();
        Assert.assertNotNull(movementSearchGroup.getId());

        movementSearchGroupDomainModelBean.deleteMovementSearchGroup(movementSearchGroup.getId());
        em.flush();

        try {
            final MovementSearchGroup tryToFindIt = movementSearchGroupDomainModelBean.getMovementSearchGroup(movementSearchGroup.getId());
            Assert.assertNull(tryToFindIt);
        } catch(final MovementModelException ex) {
            Assert.assertTrue(ex.getMessage().contains("ID:" + movementSearchGroup.getId()));
        }
    }



    @Test
    @OperateOnDeployment("normal")
    public void updateMovementSearchGroup() throws MovementModelException {
        final MovementSearchGroup movementSearchGroup = movementSearchGroupDomainModelBean.createMovementSearchGroup(createSearchGroup(), TEST_USER_NAME);
        em.flush();
        Assert.assertNotNull(movementSearchGroup.getId());

        MovementSearchGroup tryToFindIt = movementSearchGroupDomainModelBean.getMovementSearchGroup(movementSearchGroup.getId());
        Assert.assertNotNull(tryToFindIt);
        Assert.assertEquals(movementSearchGroup.getName(), tryToFindIt.getName());

        movementSearchGroup.setName("CHANGED_IT");
        movementSearchGroupDomainModelBean.updateMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);

        tryToFindIt = movementSearchGroupDomainModelBean.getMovementSearchGroup(movementSearchGroup.getId());
        Assert.assertNotNull(tryToFindIt);
        Assert.assertEquals("CHANGED_IT", tryToFindIt.getName());
    }


    @Test
    @OperateOnDeployment("normal")
    public void updateMovementSearchGroupWithExtraCriteria() throws MovementModelException {
        final MovementSearchGroup movementSearchGroup = movementSearchGroupDomainModelBean.createMovementSearchGroup(createSearchGroup(), TEST_USER_NAME);
        em.flush();
        Assert.assertNotNull(movementSearchGroup.getId());

        MovementSearchGroup tryToFindIt = movementSearchGroupDomainModelBean.getMovementSearchGroup(movementSearchGroup.getId());
        Assert.assertNotNull(tryToFindIt);
        Assert.assertEquals(movementSearchGroup.getName(), tryToFindIt.getName());

        movementSearchGroup.setName("CHANGED_IT");
        movementSearchGroup.getSearchFields().add(createCriteria(SearchKeyType.ASSET, "IRCS", "SLEA2"));
        movementSearchGroupDomainModelBean.updateMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);

        tryToFindIt = movementSearchGroupDomainModelBean.getMovementSearchGroup(movementSearchGroup.getId());
        Assert.assertNotNull(tryToFindIt);
        Assert.assertEquals("CHANGED_IT", tryToFindIt.getName());
        Assert.assertTrue(tryToFindIt.getSearchFields().size() == 4);
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateMovementSearchGroupRemoveCriterias() throws MovementModelException {
        final MovementSearchGroup movementSearchGroup = movementSearchGroupDomainModelBean.createMovementSearchGroup(createSearchGroup(), TEST_USER_NAME);
        em.flush();
        Assert.assertNotNull(movementSearchGroup.getId());

        movementSearchGroup.setName("CHANGED_IT");
        movementSearchGroup.getSearchFields().clear();
        movementSearchGroupDomainModelBean.updateMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);

        final MovementSearchGroup tryToFindIt = movementSearchGroupDomainModelBean.getMovementSearchGroup(movementSearchGroup.getId());
        Assert.assertNotNull(tryToFindIt);
        Assert.assertEquals("CHANGED_IT", tryToFindIt.getName());
        Assert.assertTrue(tryToFindIt.getSearchFields().size() == 0);
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateMovementSearchGroupRemoveCriteriasAddOne() throws MovementModelException {
        final MovementSearchGroup movementSearchGroup = movementSearchGroupDomainModelBean.createMovementSearchGroup(createSearchGroup(), TEST_USER_NAME);
        em.flush();
        Assert.assertNotNull(movementSearchGroup.getId());

        movementSearchGroup.setName("CHANGED_IT");
        movementSearchGroup.getSearchFields().clear();
        movementSearchGroup.getSearchFields().add(createCriteria(SearchKeyType.ASSET, "IRCS", "SLEA2"));
        movementSearchGroupDomainModelBean.updateMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);

        final MovementSearchGroup tryToFindIt = movementSearchGroupDomainModelBean.getMovementSearchGroup(movementSearchGroup.getId());
        Assert.assertNotNull(tryToFindIt);
        Assert.assertEquals("CHANGED_IT", tryToFindIt.getName());
        Assert.assertTrue(tryToFindIt.getSearchFields().size() == 1);
        final GroupListCriteria criteria = tryToFindIt.getSearchFields().get(0);
        Assert.assertEquals("SLEA2", criteria.getValue());
    }



    private MovementSearchGroup createSearchGroup() {
        final MovementSearchGroup movementSearchGroup = new MovementSearchGroup();
        movementSearchGroup.setUser(TEST_USER_NAME);
        movementSearchGroup.setName("MOVEMENT_SEARCH_GROUP_TEST");
        movementSearchGroup.setDynamic(true);
        final GroupListCriteria criteria = new GroupListCriteria();
        criteria.setType(SearchKeyType.MOVEMENT);
        criteria.setKey("WHAT");
        criteria.setValue("IS THIS?");
        movementSearchGroup.getSearchFields().add(createCriteria(SearchKeyType.ASSET, "FLAG_STATE", "SWE"));
        movementSearchGroup.getSearchFields().add(createCriteria(SearchKeyType.OTHER, "TIME_SPAN", "TODAY"));
        movementSearchGroup.getSearchFields().add(createCriteria(SearchKeyType.ASSET, "IRCS", "SLEA"));

        return movementSearchGroup;
    }

    private GroupListCriteria createCriteria(final SearchKeyType type, final String key, final String value) {
        final GroupListCriteria criteria = new GroupListCriteria();
        criteria.setType(type);
        criteria.setKey(key);
        criteria.setValue(value);
        return criteria;
    }

}
