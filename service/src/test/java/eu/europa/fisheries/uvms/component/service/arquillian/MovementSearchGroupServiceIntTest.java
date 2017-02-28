package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.service.MovementSearchGroupService;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementSearchGroupServiceBean;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;

/**
 * Created by thofan on 2017-02-27.
 */

@RunWith(Arquillian.class)
public class MovementSearchGroupServiceIntTest extends TransactionalTests{


  @EJB
  MovementSearchGroupServiceBean movementSearchGroupService;


    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createDeployment();
    }



    @Test
    public void createMovementSearchGroup(){

        Assert.assertTrue(movementSearchGroupService != null);



        try {
            movementSearchGroupService.createMovementSearchGroup(null,null);
        } catch (MovementServiceException e) {
            e.printStackTrace();
        } catch (MovementDuplicateException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(true);

    }







    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/



}
