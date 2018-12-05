package eu.europa.ec.fisheries.uvms.movement.service.bean;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;

import eu.europa.ec.fisheries.uvms.movement.service.entity.group.MovementFilterGroup;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementGroupMapper;
import eu.europa.ec.fisheries.uvms.movement.service.util.CalculationUtil;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.search.v1.GroupListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKeyType;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;

import static org.junit.Assert.*;

/**
 * Created by thofan on 2017-02-27.
 */

@RunWith(Arquillian.class)
public class MovementSearchGroupServiceIntTest extends TransactionalTests {

    /** TODO TODO TODO
     *   OBS in this artifact , there is confusion in the datatype of the Id
     *   It returns BigInteger  , as input the methods takes Long
     *
     *   This is an error an should be corrected to Long as in the rest of the application
     */
    
    private final static String TEST_USER_NAME = "MovementSearchGroupServiceIntTestUser";

    @EJB
    MovementSearchGroupService movementSearchGroupService;


    /******************************************************************************************************************
     *   TEST FUNCTIONS
     ******************************************************************************************************************/

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Movement_Dynamic() {
        for (SearchKey searchKey : SearchKey.values()) {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, searchKey.value());
            MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, "TEST");
            assertNotNull(created);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Movement_NONDynamic() {
        for (SearchKey searchKey : SearchKey.values()) {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.MOVEMENT, searchKey.value());
            MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, "TEST");
            assertNotNull(created);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Movement_Dynamic_FAIL() {
        try {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, "FAIL");
            movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, "TEST");
            fail();
        } catch (EJBTransactionRolledbackException e) {
            assertNotNull(e);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Movement_Dynamic_NULLFAIL() {
        try {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, "null");
            movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, "TEST");
            fail();
        } catch (EJBTransactionRolledbackException e) {
            assertNotNull(e);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Movement_NONDynamic_FAIL() {
        try {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.MOVEMENT, "FAIL");
            movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, "TEST");
            fail();
        } catch (EJBTransactionRolledbackException e) {
            assertNotNull(e);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Asset_Dynamic()  {
        for (SearchKey searchKey : SearchKey.values()) {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.ASSET, searchKey.value());
            MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, "TEST");
            assertNotNull(created);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Asset_NONDynamic()  {
        for (SearchKey searchKey : SearchKey.values()) {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.ASSET, searchKey.value());
            MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, "TEST");
            assertNotNull(created);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Asset_DynamicCrapData()  {
        MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.ASSET, UUID.randomUUID().toString());
        MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, "TEST");
        assertNotNull(created);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Asset_NONDynamicCrapData()  {
        MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.ASSET, UUID.randomUUID().toString());
        MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, "TEST");
        assertNotNull(created);
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup()  {
        MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.ASSET, UUID.randomUUID().toString());
        MovementFilterGroup filterGroupAfterPersist = movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(filterGroupAfterPersist.getId());
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void failCreateMovementSearchGroupNoUserName()  {
        MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.ASSET, UUID.randomUUID().toString());
        movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, null);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void failCreateMovementSearchGroupNoName()  {
        MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.ASSET, UUID.randomUUID().toString());
        movementSearchGroup.setName(null);
        movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, TEST_USER_NAME);
        em.flush();
    }


    @Test
    @OperateOnDeployment("movementservice")
    public void deleteMovementSearchGroup()  {
        MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, "TEST");
        assertNotNull(created);

        UUID createdMovementSearchGroupID = created.getId();
        assertNotNull(createdMovementSearchGroupID);
        try {
            MovementFilterGroup deletedMovementFilterGroup = movementSearchGroupService.deleteMovementFilterGroup(createdMovementSearchGroupID);
            assertNotNull(deletedMovementFilterGroup);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void deleteMovementSearchGroup_MIN_VALUE_AS_ID() {
        try {
            movementSearchGroupService.deleteMovementFilterGroup(UUID.randomUUID());
            fail();
        } catch (RuntimeException e) {
            assertNotNull(e);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void deleteMovementSearchGroup_NULL_AS_ID() {
        try {
            movementSearchGroupService.deleteMovementFilterGroup(null);
            fail();
        } catch (Exception e) {
            assertNotNull(e);
        }
    }
    
    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void deleteMovementSearchGroup_then_getById_Exception_Thrown()  {
        MovementSearchGroup movementGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(created.getId());

        movementSearchGroupService.deleteMovementFilterGroup(created.getId());
        em.flush();

        movementSearchGroupService.getMovementFilterGroup(created.getId());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroup() {
        try {
            // first create one
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
            MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, "TEST");
            assertNotNull(created);
            assertNotNull(created.getId());

            UUID createdMovementSearchGroupId = created.getId();
            MovementFilterGroup fetched = movementSearchGroupService.getMovementFilterGroup(createdMovementSearchGroupId);
            assertNotNull(fetched);
            assertNotNull(fetched.getId());
            UUID fetchedMovementSearchGroupId = fetched.getId();
            assertEquals(createdMovementSearchGroupId, fetchedMovementSearchGroupId);
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroupNormal()  {
        MovementSearchGroup movementGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(created.getId());

        MovementFilterGroup tryToFindIt = movementSearchGroupService.getMovementFilterGroup(created.getId());
        assertNotNull(tryToFindIt);

        assertEquals(created.getId(), tryToFindIt.getId());
        assertEquals(created.getName(), tryToFindIt.getName());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroup_NULL_IN_KEY() {
        try {
            movementSearchGroupService.getMovementFilterGroup(null);
            fail();
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroup_MINVAL_IN_KEY() {
        try {
            movementSearchGroupService.getMovementFilterGroup(UUID.randomUUID());
            fail();
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void updateMovementSearchGroup() {

        // TODO changed_by   not visible to client ??????

        try {
            // create one
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
            MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, "TEST");
            assertNotNull(created);

            UUID createdMovementSearchGroupID = created.getId();
            assertNotNull(createdMovementSearchGroupID);

            // fix a new one
            MovementSearchGroup aNewMovementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
            // put id in it from the created so it can be used as update info
            aNewMovementSearchGroup.setId(CalculationUtil.convertToBigInteger(createdMovementSearchGroupID));
            aNewMovementSearchGroup.setName("CHANGED_NAME");

            MovementFilterGroup updated = movementSearchGroupService.updateMovementFilterGroup(aNewMovementSearchGroup, "TEST_UPD");
            assertNotNull(updated);
            assertNotNull(updated.getId());
            UUID updatedMovementSearchGroupID = updated.getId();

            assertEquals(updatedMovementSearchGroupID, createdMovementSearchGroupID);

            // now ensure the update actually were persisted
            MovementFilterGroup fetched = movementSearchGroupService.getMovementFilterGroup(updatedMovementSearchGroupID);
            assertNotNull(fetched);
            assertEquals("CHANGED_NAME", fetched.getName());
        } catch (Exception e) {
            fail();
        }
    }


    @Test
    @OperateOnDeployment("movementservice")
    public void updateMovementNON_EXISTING_SearchGroup() {
        try {
            // fix a new one
            MovementSearchGroup aNewMovementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
            // put id in it from the created so it can be used as update info
            aNewMovementSearchGroup.setId(BigInteger.valueOf(Long.MIN_VALUE));
            aNewMovementSearchGroup.setName("CHANGED_NAME");
            movementSearchGroupService.updateMovementFilterGroup(aNewMovementSearchGroup, "TEST_UPD");
            fail();
        } catch (Exception e) {
            assertNotNull(e);
        }
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void updateMovementSearchGroupNormal()  {
        MovementSearchGroup movementGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(created.getId());

        MovementFilterGroup tryToFindIt = movementSearchGroupService.getMovementFilterGroup(created.getId());
        assertNotNull(tryToFindIt);
        assertEquals(created.getName(), tryToFindIt.getName());

        created.setName("CHANGED_IT");
        MovementSearchGroup movementSearchGroup = MovementGroupMapper.toMovementSearchGroup(created);
        tryToFindIt = movementSearchGroupService.updateMovementFilterGroup(movementSearchGroup, TEST_USER_NAME);

        assertNotNull(tryToFindIt);
        assertEquals("CHANGED_IT", tryToFindIt.getName());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void updateMovementSearchGroupWithExtraCriteria()  {
        MovementSearchGroup movementGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(created.getId());

        MovementFilterGroup tryToFindIt = movementSearchGroupService.getMovementFilterGroup(created.getId());
        assertNotNull(tryToFindIt);
        assertEquals(created.getName(), tryToFindIt.getName());

        created.setName("CHANGED_IT");
        GroupListCriteria criteria = new GroupListCriteria();
        criteria.setType(SearchKeyType.ASSET);
        criteria.setKey("IRCS");
        criteria.setValue("SLEA2");

        MovementSearchGroup movementSearchGroup = MovementGroupMapper.toMovementSearchGroup(created);
        movementSearchGroup.getSearchFields().add(criteria);
        tryToFindIt = movementSearchGroupService.updateMovementFilterGroup(movementSearchGroup, TEST_USER_NAME);

        assertNotNull(tryToFindIt);
        assertEquals("CHANGED_IT", tryToFindIt.getName());
        MovementSearchGroup updated = MovementGroupMapper.toMovementSearchGroup(created);
        assertEquals(2, updated.getSearchFields().size());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void updateMovementSearchGroupRemoveCriterias()  {
        MovementSearchGroup movementGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(created.getId());

        created.setName("CHANGED_IT");
        MovementSearchGroup movementSearchGroup = MovementGroupMapper.toMovementSearchGroup(created);
        movementSearchGroup.getSearchFields().clear();
        MovementFilterGroup tryToFindIt = movementSearchGroupService.updateMovementFilterGroup(movementSearchGroup, TEST_USER_NAME);

        assertNotNull(tryToFindIt);
        assertEquals("CHANGED_IT", tryToFindIt.getName());
        MovementSearchGroup updated = MovementGroupMapper.toMovementSearchGroup(created);
        assertEquals(0, updated.getSearchFields().size());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void updateMovementSearchGroupRemoveCriteriasAddOne()  {
        MovementSearchGroup movementGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(created.getId());

        created.setName("CHANGED_IT");
        GroupListCriteria crit = new GroupListCriteria();
        crit.setType(SearchKeyType.ASSET);
        crit.setKey("IRCS");
        crit.setValue("SLEA2");
        MovementSearchGroup movementSearchGroup = MovementGroupMapper.toMovementSearchGroup(created);
        movementSearchGroup.getSearchFields().clear();
        movementSearchGroup.getSearchFields().add(crit);
        MovementFilterGroup tryToFindIt = movementSearchGroupService.updateMovementFilterGroup(movementSearchGroup, TEST_USER_NAME);

        assertNotNull(tryToFindIt);
        assertEquals("CHANGED_IT", tryToFindIt.getName());
        MovementSearchGroup updated = MovementGroupMapper.toMovementSearchGroup(created);
        assertEquals(1, updated.getSearchFields().size());
        GroupListCriteria criteria = updated.getSearchFields().get(0);
        assertEquals("SLEA2", criteria.getValue());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroupsByUser() {
        try {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
            MovementFilterGroup createdMovementSearchGroup = movementSearchGroupService.createMovementFilterGroup(movementSearchGroup, "TEST");
            List<MovementFilterGroup> rs = movementSearchGroupService.getMovementFilterGroupsByUser("TEST");
            assertNotNull(rs);
            assertTrue(rs.size() != 0);
        } catch (Exception e) {
            fail(e.toString());
        }
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroupsByUserNormal()  {
        int searchGroupsBefore = movementSearchGroupService.getMovementFilterGroupsByUser(TEST_USER_NAME).size();
        
        MovementSearchGroup movementGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        movementGroup.setUser(TEST_USER_NAME);
        MovementFilterGroup created = movementSearchGroupService.createMovementFilterGroup(movementGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(created.getId());

        List<MovementFilterGroup> movementSearchGroupsByUser = movementSearchGroupService.getMovementFilterGroupsByUser(TEST_USER_NAME);
        assertNotNull(movementSearchGroupsByUser);
        assertEquals(searchGroupsBefore + 1, movementSearchGroupsByUser.size());

        MovementFilterGroup tryToFindIt = movementSearchGroupsByUser.get(0);

        assertEquals(created.getId(), tryToFindIt.getId());
        assertEquals(created.getName(), tryToFindIt.getName());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroupsBy_NON_EXISTING_User() {
        try {
            List<MovementFilterGroup> rs = movementSearchGroupService.getMovementFilterGroupsByUser("UUID.randomUUID.toString()");
            assertNotNull(rs);
            assertEquals(0, rs.size());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroupsBy_NULL_User() {
        try {
            List<MovementFilterGroup> rs = movementSearchGroupService.getMovementFilterGroupsByUser(null);
            assertNotNull(rs);
            assertEquals(0, rs.size());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/
    private MovementSearchGroup createMovementSearchGroupHelper(String name, boolean dynamic, SearchKeyType criteriaType, String searchKey) {
        /*      FOR Movement these are allowed
                allowed values
                MOVEMENT_ID,
                SEGMENT_ID,
                TRACK_ID,
                CONNECT_ID,
                MOVEMENT_TYPE,
                ACTIVITY_TYPE,
                DATE,
                AREA,
                AREA_ID,
                STATUS,
                SOURCE,
                CATEGORY,
                NR_OF_LATEST_REPORTS;

                for Asset and Other   . . .  Anything is Ok
        */
        MovementSearchGroup movementSearchGroup = new MovementSearchGroup();
        movementSearchGroup.setName(name);
        movementSearchGroup.setDynamic(dynamic);
        movementSearchGroup.setUser("TEST");
        GroupListCriteria criteria = new GroupListCriteria();
        criteria.setValue("TEST");
        criteria.setKey(searchKey);
        criteria.setType(criteriaType);
        movementSearchGroup.getSearchFields().add(criteria);
        return movementSearchGroup;
    }
}
