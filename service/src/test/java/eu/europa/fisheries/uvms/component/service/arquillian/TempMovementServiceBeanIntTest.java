package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.uvms.movement.service.TempMovementService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;

/**
 * Created by andreasw on 2017-03-03.
 */
@RunWith(Arquillian.class)
public class TempMovementServiceBeanIntTest extends TransactionalTests {

    @EJB
    private TempMovementService tempMovementService;


    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createTempMovementDeployment();
    }


    @Test
    public void testMe() {

        Assert.assertTrue(true);
    }

}
