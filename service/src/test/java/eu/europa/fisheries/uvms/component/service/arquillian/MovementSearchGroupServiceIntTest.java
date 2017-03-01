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
import java.util.UUID;

/**
 * Created by thofan on 2017-02-27.
 */

@RunWith(Arquillian.class)
public class MovementSearchGroupServiceIntTest extends TransactionalTests {

    private final static String TEST_USER_NAME = "MovementSearchGroupServiceIntTestUser";

    @EJB
    MovementSearchGroupService movementSearchGroupService;


    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createDeployment();
    }


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
