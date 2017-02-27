package eu.europa.fisheries.uvms.component.service.arquillian;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by thofan on 2017-02-27.
 */

@RunWith(Arquillian.class)
public class MovementSearchGroupServiceIntTest extends TransactionalTests{


    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementTestDeployment.createDeployment();
    }



    @Test
    public void tst(){

        Assert.assertTrue(true);

    }







    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/



}
