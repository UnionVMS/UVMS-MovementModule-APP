package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.search.v1.GroupListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKeyType;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.service.MovementSearchGroupService;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

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


    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createMovmentSearchDeployment();
    }



    /******************************************************************************************************************
     *   TEST FUNCTIONS
     ******************************************************************************************************************/

    @Test
    public void createMovementSearchGroup_Movement_Dynamic() throws MovementServiceException, MovementDuplicateException {

        for (SearchKey searchKey : SearchKey.values()) {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, searchKey.value());
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.assertTrue(createdMovementSearchGroup != null);
        }
    }

    @Test
    public void createMovementSearchGroup_Movement_NONDynamic() throws MovementServiceException, MovementDuplicateException {

        for (SearchKey searchKey : SearchKey.values()) {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.MOVEMENT, searchKey.value());
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.assertTrue(createdMovementSearchGroup != null);
        }
    }

    @Test
    public void createMovementSearchGroup_Movement_Dynamic_FAIL() throws MovementServiceException, MovementDuplicateException {

        try {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, "FAIL");
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.fail();
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    public void createMovementSearchGroup_Movement_Dynamic_NULLFAIL() throws MovementServiceException, MovementDuplicateException {

        try {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.MOVEMENT, "null");
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.fail();
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    public void createMovementSearchGroup_Movement_NONDynamic_FAIL() throws MovementServiceException, MovementDuplicateException {

        try {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.MOVEMENT, "FAIL");
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.fail();
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    public void createMovementSearchGroup_Asset_Dynamic() throws MovementServiceException, MovementDuplicateException {

        for (SearchKey searchKey : SearchKey.values()) {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.ASSET, searchKey.value());
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.assertTrue(createdMovementSearchGroup != null);
        }
    }

    @Test
    public void createMovementSearchGroup_Asset_NONDynamic() throws MovementServiceException, MovementDuplicateException {

        for (SearchKey searchKey : SearchKey.values()) {
            MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.ASSET, searchKey.value());
            MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
            Assert.assertTrue(createdMovementSearchGroup != null);
        }
    }

    @Test
    public void createMovementSearchGroup_Asset_DynamicCrapData() throws MovementServiceException, MovementDuplicateException {

        MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", true, SearchKeyType.ASSET, UUID.randomUUID().toString());
        MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
        Assert.assertTrue(createdMovementSearchGroup != null);
    }

    @Test
    public void createMovementSearchGroup_Asset_NONDynamicCrapData() throws MovementServiceException, MovementDuplicateException {
        MovementSearchGroup movementSearchGroup = createMovementSearchGroupHelper("TEST", false, SearchKeyType.ASSET, UUID.randomUUID().toString());
        MovementSearchGroup createdMovementSearchGroup = movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, "TEST");
        Assert.assertTrue(createdMovementSearchGroup != null);
    }


    @Test
    public void deleteMovementSearchGroup() throws MovementServiceException, MovementDuplicateException {
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
    public void deleteMovementSearchGroup_MIN_VALUE_AS_ID() throws MovementServiceException, MovementDuplicateException {
        try {
            MovementSearchGroup deletedMovementSearchGroup = movementSearchGroupService.deleteMovementSearchGroup(Long.MIN_VALUE);
            Assert.fail();
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    public void deleteMovementSearchGroup_NULL_AS_ID() throws MovementServiceException, MovementDuplicateException {
        try {
            MovementSearchGroup deletedMovementSearchGroup = movementSearchGroupService.deleteMovementSearchGroup(null);
            Assert.fail();
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    public void getMovementSearchGroup() throws MovementServiceException, MovementDuplicateException {
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
    public void getMovementSearchGroup_NULL_IN_KEY() throws MovementServiceException, MovementDuplicateException {
        try {
            MovementSearchGroup fetchedMovementSearchGroup = movementSearchGroupService.getMovementSearchGroup(null);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    public void getMovementSearchGroup_MINVAL_IN_KEY() throws MovementServiceException, MovementDuplicateException {
        try {
            MovementSearchGroup fetchedMovementSearchGroup = movementSearchGroupService.getMovementSearchGroup(Long.MIN_VALUE);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }
    }


    @Test
    public void updateMovementSearchGroup() throws MovementServiceException, MovementDuplicateException {

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
    public void updateMovementNON_EXISTING_SearchGroup() throws MovementServiceException, MovementDuplicateException {

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
    public void getMovementSearchGroupsByUser() throws MovementServiceException, MovementDuplicateException {

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
    public void getMovementSearchGroupsBy_NON_EXISTING_User() throws MovementServiceException, MovementDuplicateException {

        try {
            List<MovementSearchGroup> rs = movementSearchGroupService.getMovementSearchGroupsByUser("UUID.randomUUID.toString()");
            Assert.assertTrue(rs != null);
            Assert.assertTrue(rs.size() == 0);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void getMovementSearchGroupsBy_NULL_User() throws MovementServiceException, MovementDuplicateException {

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
