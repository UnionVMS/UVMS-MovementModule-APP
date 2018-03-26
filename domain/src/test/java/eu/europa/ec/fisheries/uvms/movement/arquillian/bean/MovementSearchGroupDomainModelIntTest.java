package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import eu.europa.ec.fisheries.schema.movement.search.v1.GroupListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKeyType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementSearchGroupDomainModelBean;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.persistence.PersistenceException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class MovementSearchGroupDomainModelIntTest extends TransactionalTests {

    private final static String TEST_USER_NAME = "MovementSearchGroupDomainModelIntTestsUser";

    @EJB
    private MovementSearchGroupDomainModelBean movementSearchGroupDomainModelBean;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    @OperateOnDeployment("normal")
    public void createMovementSearchGroup() throws MovementModelException {
        MovementSearchGroup movementSearchGroup = createSearchGroup();
        MovementSearchGroup movementSearchGroupAfterPersist = movementSearchGroupDomainModelBean.createMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroupAfterPersist.getId());
    }

    @Test
    @OperateOnDeployment("normal")
    public void failCreateMovementSearchGroupNoUserName() throws MovementModelException {
        expectedException.expect(PersistenceException.class);
        expectedException.expectMessage("could not execute statement");

        MovementSearchGroup movementSearchGroup = createSearchGroup();
        movementSearchGroupDomainModelBean.createMovementSearchGroup(movementSearchGroup, null);
        em.flush();
    }

    @Test
    @OperateOnDeployment("normal")
    public void failCreateMovementSearchGroupNoName() throws MovementModelException {
        expectedException.expect(MovementModelException.class);
        expectedException.expectMessage("Could not create movement search group.");

        MovementSearchGroup movementSearchGroup = createSearchGroup();
        movementSearchGroup.setName(null);
        movementSearchGroupDomainModelBean.createMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);
        em.flush();
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementSearchGroup() throws MovementModelException {
        MovementSearchGroup movementSearchGroup = movementSearchGroupDomainModelBean.createMovementSearchGroup(createSearchGroup(), TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroup.getId());

        MovementSearchGroup tryToFindIt = movementSearchGroupDomainModelBean.getMovementSearchGroup(movementSearchGroup.getId());
        assertNotNull(tryToFindIt);

        assertEquals(movementSearchGroup.getId(), tryToFindIt.getId());
        assertEquals(movementSearchGroup.getName(), tryToFindIt.getName());
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementSearchGroupsByUser() throws MovementModelException {
        MovementSearchGroup movementSearchGroup = movementSearchGroupDomainModelBean.createMovementSearchGroup(createSearchGroup(), TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroup.getId());

        List<MovementSearchGroup> movementSearchGroupsByUser = movementSearchGroupDomainModelBean.getMovementSearchGroupsByUser(TEST_USER_NAME);
        assertNotNull(movementSearchGroupsByUser);
        assertTrue(movementSearchGroupsByUser.size() == 1);

        MovementSearchGroup tryToFindIt = movementSearchGroupsByUser.get(0);

        assertEquals(movementSearchGroup.getId(), tryToFindIt.getId());
        assertEquals(movementSearchGroup.getName(), tryToFindIt.getName());
    }

    @Test
    @OperateOnDeployment("normal")
    public void deleteMovementSearchGroup_then_getById_Exception_Thrown() throws MovementModelException {

        MovementSearchGroup movementSearchGroup = movementSearchGroupDomainModelBean.createMovementSearchGroup(createSearchGroup(), TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroup.getId());

        expectedException.expect(MovementModelException.class);
        expectedException.expectMessage("Could not get movement search group by group ID:" + movementSearchGroup.getId());

        movementSearchGroupDomainModelBean.deleteMovementSearchGroup(movementSearchGroup.getId());
        em.flush();

        movementSearchGroupDomainModelBean.getMovementSearchGroup(movementSearchGroup.getId());
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateMovementSearchGroup() throws MovementModelException {
        MovementSearchGroup movementSearchGroup = movementSearchGroupDomainModelBean.createMovementSearchGroup(createSearchGroup(), TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroup.getId());

        MovementSearchGroup tryToFindIt = movementSearchGroupDomainModelBean.getMovementSearchGroup(movementSearchGroup.getId());
        assertNotNull(tryToFindIt);
        assertEquals(movementSearchGroup.getName(), tryToFindIt.getName());

        movementSearchGroup.setName("CHANGED_IT");
        tryToFindIt = movementSearchGroupDomainModelBean.updateMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);

        assertNotNull(tryToFindIt);
        assertEquals("CHANGED_IT", tryToFindIt.getName());
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateMovementSearchGroupWithExtraCriteria() throws MovementModelException {
        MovementSearchGroup movementSearchGroup = movementSearchGroupDomainModelBean.createMovementSearchGroup(createSearchGroup(), TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroup.getId());

        MovementSearchGroup tryToFindIt = movementSearchGroupDomainModelBean.getMovementSearchGroup(movementSearchGroup.getId());
        assertNotNull(tryToFindIt);
        assertEquals(movementSearchGroup.getName(), tryToFindIt.getName());

        movementSearchGroup.setName("CHANGED_IT");
        movementSearchGroup.getSearchFields().add(createCriteria(SearchKeyType.ASSET, "IRCS", "SLEA2"));
        tryToFindIt = movementSearchGroupDomainModelBean.updateMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);

        assertNotNull(tryToFindIt);
        assertEquals("CHANGED_IT", tryToFindIt.getName());
        assertEquals(4, tryToFindIt.getSearchFields().size());
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateMovementSearchGroupRemoveCriterias() throws MovementModelException {
        MovementSearchGroup movementSearchGroup = movementSearchGroupDomainModelBean.createMovementSearchGroup(createSearchGroup(), TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroup.getId());

        movementSearchGroup.setName("CHANGED_IT");
        movementSearchGroup.getSearchFields().clear();
        MovementSearchGroup tryToFindIt = movementSearchGroupDomainModelBean.updateMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);

        assertNotNull(tryToFindIt);
        assertEquals("CHANGED_IT", tryToFindIt.getName());
        assertEquals(0, tryToFindIt.getSearchFields().size());
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateMovementSearchGroupRemoveCriteriasAddOne() throws MovementModelException {
        MovementSearchGroup movementSearchGroup = movementSearchGroupDomainModelBean.createMovementSearchGroup(createSearchGroup(), TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroup.getId());

        movementSearchGroup.setName("CHANGED_IT");
        movementSearchGroup.getSearchFields().clear();
        movementSearchGroup.getSearchFields().add(createCriteria(SearchKeyType.ASSET, "IRCS", "SLEA2"));
        MovementSearchGroup tryToFindIt = movementSearchGroupDomainModelBean.updateMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);

        assertNotNull(tryToFindIt);
        assertEquals("CHANGED_IT", tryToFindIt.getName());
        assertEquals(1, tryToFindIt.getSearchFields().size());
        GroupListCriteria criteria = tryToFindIt.getSearchFields().get(0);
        assertEquals("SLEA2", criteria.getValue());
    }

    private MovementSearchGroup createSearchGroup() {
        MovementSearchGroup movementSearchGroup = new MovementSearchGroup();
        movementSearchGroup.setUser(TEST_USER_NAME);
        movementSearchGroup.setName("MOVEMENT_SEARCH_GROUP_TEST");
        movementSearchGroup.setDynamic(true);
        GroupListCriteria criteria = new GroupListCriteria();
        criteria.setType(SearchKeyType.MOVEMENT);
        criteria.setKey("WHAT");
        criteria.setValue("IS THIS?");
        movementSearchGroup.getSearchFields().add(createCriteria(SearchKeyType.ASSET, "FLAG_STATE", "SWE"));
        movementSearchGroup.getSearchFields().add(createCriteria(SearchKeyType.OTHER, "TIME_SPAN", "TODAY"));
        movementSearchGroup.getSearchFields().add(createCriteria(SearchKeyType.ASSET, "IRCS", "SLEA"));

        return movementSearchGroup;
    }

    private GroupListCriteria createCriteria(SearchKeyType type, String key, String value) {
        GroupListCriteria criteria = new GroupListCriteria();
        criteria.setType(type);
        criteria.setKey(key);
        criteria.setValue(value);
        return criteria;
    }

}
