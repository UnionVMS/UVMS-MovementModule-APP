package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.search.v1.GroupListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
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

/**
 * Created by thofan on 2017-02-27.
 */

@RunWith(Arquillian.class)
public class MovementSearchGroupServiceIntTest extends TransactionalTests{

    private final static String TEST_USER_NAME = "MovementSearchGroupServiceIntTestUser";

    @EJB
    MovementSearchGroupService movementSearchGroupService;


    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createDeployment();
    }



    @Test
    public void createMovementSearchGroup() throws MovementServiceException, MovementDuplicateException {

        MovementSearchGroup movementSearchGroup = new MovementSearchGroup();
        movementSearchGroup.setName("TEST");
        movementSearchGroup.setDynamic(true);
        movementSearchGroup.setUser("TEST");
        GroupListCriteria criteria = new GroupListCriteria();
        criteria.setValue("TEST");
        criteria.setKey("MOVEMENT_ID");
        criteria.setType(SearchKeyType.MOVEMENT);
        movementSearchGroup.getSearchFields().add(criteria);
        movementSearchGroupService.createMovementSearchGroup(movementSearchGroup, TEST_USER_NAME);
        Assert.assertTrue(true);
    }







    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/

}
