package eu.europa.fisheries.uvms.component.service.arquillian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.search.v1.GroupListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKeyType;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.service.MovementSearchGroupService;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

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
    public void createMovementSearchGroup_Movement_Dynamic() throws MovementServiceException {

        for (SearchKey searchKey : SearchKey.values()) {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, searchKey.value());
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.assertTrue(createdMovementSearchGroup != null);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Movement_NONDynamic() throws MovementServiceException {

        for (SearchKey searchKey : SearchKey.values()) {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.MOVEMENT, searchKey.value());
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.assertTrue(createdMovementSearchGroup != null);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Movement_Dynamic_FAIL() {

        try {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, "FAIL");
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.fail();
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Movement_Dynamic_NULLFAIL() {

        try {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, "null");
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.fail();
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Movement_NONDynamic_FAIL() {

        try {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.MOVEMENT, "FAIL");
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.fail();
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Asset_Dynamic() throws MovementServiceException {

        for (SearchKey searchKey : SearchKey.values()) {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.ASSET, searchKey.value());
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.assertTrue(createdMovementSearchGroup != null);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Asset_NONDynamic() throws MovementServiceException {

        for (SearchKey searchKey : SearchKey.values()) {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.ASSET, searchKey.value());
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.assertTrue(createdMovementSearchGroup != null);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Asset_DynamicCrapData() throws MovementServiceException {

        MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.ASSET, UUID.randomUUID().toString());
        MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
        Assert.assertTrue(createdMovementSearchGroup != null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup_Asset_NONDynamicCrapData() throws MovementServiceException {
        MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.ASSET, UUID.randomUUID().toString());
        MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
        Assert.assertTrue(createdMovementSearchGroup != null);
    }
    
    @Test
    public void createMovementSearchGroup() throws MovementServiceException {
        MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.ASSET, UUID.randomUUID().toString());
        MovementSearchGroup movementSearchGroupAfterPersist = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroupAfterPersist.getId());
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    public void failCreateMovementSearchGroupNoUserName() throws MovementServiceException {
        MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.ASSET, UUID.randomUUID().toString());
        movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, null);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    public void failCreateMovementSearchGroupNoName() throws MovementServiceException {
        MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.ASSET, UUID.randomUUID().toString());
        movementSearchGroup.setName(null);
        movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);
        em.flush();
    }


    @Test
    @OperateOnDeployment("movementservice")
    public void deleteMovementSearchGroup() throws MovementServiceException {
        MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
        Assert.assertTrue(createdMovementSearchGroup != null);

        BigInteger createdMovementSearchGroupID = createdMovementSearchGroup.getId();
        Assert.assertTrue(createdMovementSearchGroupID != null);

        Long key = createdMovementSearchGroupID.longValue();
        try {
            MovementSearchGroup deletedMovementSearchGroup = movementSearchGroupService.deleteMovementSearchGroup(key);
            Assert.assertTrue(deletedMovementSearchGroup != null);
        } catch (MovementServiceException e) {
            Assert.fail();
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void deleteMovementSearchGroup_MIN_VALUE_AS_ID() throws MovementServiceException {
        try {
            MovementSearchGroup deletedMovementSearchGroup = movementSearchGroupService.deleteMovementSearchGroup(Long.MIN_VALUE);
            Assert.fail();
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void deleteMovementSearchGroup_NULL_AS_ID() {
        try {
            MovementSearchGroup deletedMovementSearchGroup = movementSearchGroupService.deleteMovementSearchGroup(null);
            Assert.fail();
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }
    }
    
    @Test(expected = MovementServiceException.class)
    public void deleteMovementSearchGroup_then_getById_Exception_Thrown() throws MovementDomainException, MovementServiceException {
        MovementSearchGroup movementGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        MovementSearchGroup movementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroup.getId());

        movementSearchGroupService.deleteMovementSearchGroup(movementSearchGroup.getId().longValue());
        em.flush();

        movementSearchGroupService.getMovementSearchGroup(movementSearchGroup.getId().longValue());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroup() {
        try {

            // first create one
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.assertTrue(createdMovementSearchGroup != null);
            Assert.assertTrue(createdMovementSearchGroup.getId() != null);

            BigInteger createdMovementSearchGroupId = createdMovementSearchGroup.getId();
            MovementSearchGroup fetchedMovementSearchGroup = movementSearchGroupService.getMovementSearchGroup(createdMovementSearchGroupId.longValue());
            Assert.assertTrue(fetchedMovementSearchGroup != null);
            Assert.assertTrue(fetchedMovementSearchGroup.getId() != null);
            BigInteger fetchedMovementSearchGroupId = fetchedMovementSearchGroup.getId();
            Assert.assertTrue(createdMovementSearchGroupId.equals(fetchedMovementSearchGroupId));
        } catch (MovementServiceException e) {
            Assert.fail();
        } catch (Exception e) {
            Assert.fail();
        }
    }
    
    @Test
    public void getMovementSearchGroupNormal() throws MovementServiceException {
        MovementSearchGroup movementGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        MovementSearchGroup movementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroup.getId());

        MovementSearchGroup tryToFindIt = movementSearchGroupService.getMovementSearchGroup(movementSearchGroup.getId().longValue());
        assertNotNull(tryToFindIt);

        assertEquals(movementSearchGroup.getId(), tryToFindIt.getId());
        assertEquals(movementSearchGroup.getName(), tryToFindIt.getName());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroup_NULL_IN_KEY() {
        try {
            MovementSearchGroup fetchedMovementSearchGroup = movementSearchGroupService.getMovementSearchGroup(null);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroup_MINVAL_IN_KEY() {
        try {
            MovementSearchGroup fetchedMovementSearchGroup = movementSearchGroupService.getMovementSearchGroup(Long.MIN_VALUE);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }
    }


    @Test
    @OperateOnDeployment("movementservice")
    public void updateMovementSearchGroup() {

        // TODO changed_by   not visible to client ??????

        try {

            // create one
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.assertTrue(createdMovementSearchGroup != null);

            BigInteger createdMovementSearchGroupID = createdMovementSearchGroup.getId();
            Assert.assertTrue(createdMovementSearchGroupID != null);
            Long createdKey = createdMovementSearchGroupID.longValue();

            // fix a new one
            MovementSearchGroup aNewMovementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
            // put id in it from the created so it can be used as update info
            aNewMovementSearchGroup.setId(BigInteger.valueOf(createdKey));
            aNewMovementSearchGroup.setName("CHANGED_NAME");

            MovementSearchGroup updatedMovementSearchGroup = movementSearchGroupService.updateMovementSearchGroup(aNewMovementSearchGroup, "TEST_UPD");
            Assert.assertTrue(updatedMovementSearchGroup != null);
            Assert.assertTrue(updatedMovementSearchGroup.getId() != null);
            BigInteger updatedMovementSearchGroupID = updatedMovementSearchGroup.getId();

            Assert.assertTrue(updatedMovementSearchGroupID.equals(createdMovementSearchGroupID));

            // now ensure the update actually were persisted
            MovementSearchGroup fetchedMovementSearchGroup = movementSearchGroupService.getMovementSearchGroup(updatedMovementSearchGroupID.longValue());
            Assert.assertTrue(fetchedMovementSearchGroup != null);
            Assert.assertTrue(fetchedMovementSearchGroup.getName().equals("CHANGED_NAME"));
        } catch (Exception e) {
            Assert.fail();
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
            MovementSearchGroup updatedMovementSearchGroup = movementSearchGroupService.updateMovementSearchGroup(aNewMovementSearchGroup, "TEST_UPD");
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }
    }
    
    @Test
    public void updateMovementSearchGroupNormal() throws MovementServiceException {
        MovementSearchGroup movementGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        MovementSearchGroup movementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroup.getId());

        MovementSearchGroup tryToFindIt = movementSearchGroupService.getMovementSearchGroup(movementSearchGroup.getId().longValue());
        assertNotNull(tryToFindIt);
        assertEquals(movementSearchGroup.getName(), tryToFindIt.getName());

        movementSearchGroup.setName("CHANGED_IT");
        tryToFindIt = movementSearchGroupService.updateMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);

        assertNotNull(tryToFindIt);
        assertEquals("CHANGED_IT", tryToFindIt.getName());
    }

    @Test
    public void updateMovementSearchGroupWithExtraCriteria() throws MovementServiceException {
        MovementSearchGroup movementGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        MovementSearchGroup movementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroup.getId());

        MovementSearchGroup tryToFindIt = movementSearchGroupService.getMovementSearchGroup(movementSearchGroup.getId().longValue());
        assertNotNull(tryToFindIt);
        assertEquals(movementSearchGroup.getName(), tryToFindIt.getName());

        movementSearchGroup.setName("CHANGED_IT");
        GroupListCriteria criteria = new GroupListCriteria();
        criteria.setType(SearchKeyType.ASSET);
        criteria.setKey("IRCS");
        criteria.setValue("SLEA2");
        movementSearchGroup.getSearchFields().add(criteria);
        tryToFindIt = movementSearchGroupService.updateMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);

        assertNotNull(tryToFindIt);
        assertEquals("CHANGED_IT", tryToFindIt.getName());
        assertEquals(2, tryToFindIt.getSearchFields().size());
    }

    @Test
    public void updateMovementSearchGroupRemoveCriterias() throws MovementServiceException {
        MovementSearchGroup movementGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        MovementSearchGroup movementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroup.getId());

        movementSearchGroup.setName("CHANGED_IT");
        movementSearchGroup.getSearchFields().clear();
        MovementSearchGroup tryToFindIt = movementSearchGroupService.updateMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);

        assertNotNull(tryToFindIt);
        assertEquals("CHANGED_IT", tryToFindIt.getName());
        assertEquals(0, tryToFindIt.getSearchFields().size());
    }

    @Test
    public void updateMovementSearchGroupRemoveCriteriasAddOne() throws MovementServiceException {
        MovementSearchGroup movementGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        MovementSearchGroup movementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroup.getId());

        movementSearchGroup.setName("CHANGED_IT");
        movementSearchGroup.getSearchFields().clear();
        GroupListCriteria crit = new GroupListCriteria();
        crit.setType(SearchKeyType.ASSET);
        crit.setKey("IRCS");
        crit.setValue("SLEA2");
        movementSearchGroup.getSearchFields().add(crit);
        MovementSearchGroup tryToFindIt = movementSearchGroupService.updateMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);

        assertNotNull(tryToFindIt);
        assertEquals("CHANGED_IT", tryToFindIt.getName());
        assertEquals(1, tryToFindIt.getSearchFields().size());
        GroupListCriteria criteria = tryToFindIt.getSearchFields().get(0);
        assertEquals("SLEA2", criteria.getValue());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroupsByUser() {

        try {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            List<MovementSearchGroup> rs = movementSearchGroupService.getMovementSearchGroupsByUser("TEST");
            Assert.assertTrue(rs != null);
            Assert.assertTrue(rs.size() != 0);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }
    
    @Test
    public void getMovementSearchGroupsByUserNormal() throws MovementServiceException {
        int searchGroupsBefore = movementSearchGroupService.getMovementSearchGroupsByUser(TEST_USER_NAME).size();
        
        MovementSearchGroup movementGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, SearchKey.MOVEMENT_ID.value());
        movementGroup.setUser(TEST_USER_NAME);
        MovementSearchGroup movementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementGroup, TEST_USER_NAME);
        em.flush();
        assertNotNull(movementSearchGroup.getId());

        List<MovementSearchGroup> movementSearchGroupsByUser = movementSearchGroupService.getMovementSearchGroupsByUser(TEST_USER_NAME);
        assertNotNull(movementSearchGroupsByUser);
        assertEquals(searchGroupsBefore + 1, movementSearchGroupsByUser.size());

        MovementSearchGroup tryToFindIt = movementSearchGroupsByUser.get(0);

        assertEquals(movementSearchGroup.getId(), tryToFindIt.getId());
        assertEquals(movementSearchGroup.getName(), tryToFindIt.getName());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroupsBy_NON_EXISTING_User() {

        try {
            List<MovementSearchGroup> rs = movementSearchGroupService.getMovementSearchGroupsByUser("UUID.randomUUID.toString()");
            Assert.assertTrue(rs != null);
            Assert.assertTrue(rs.size() == 0);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroupsBy_NULL_User() {

        try {
            List<MovementSearchGroup> rs = movementSearchGroupService.getMovementSearchGroupsByUser(null);
            Assert.assertTrue(rs != null);
            Assert.assertTrue(rs.size() == 0);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }


    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/
    private MovementSearchGroup createMovementSearchGroupHelper(String name, boolean dynamic, SearchKeyType criteriatype, String searchKey) {


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
        criteria.setType(criteriatype);
        movementSearchGroup.getSearchFields().add(criteria);
        return movementSearchGroup;
    }
}
