package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.uvms.movement.service.MovementSearchGroupService;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;

/**
 * Created by thofan on 2017-03-02.
 */

@RunWith(Arquillian.class)
public class MovementServiceIntTest   extends TransactionalTests {


    private final static String TEST_USER_NAME = "MovementServiceIntTestTestUser";

    @EJB
    MovementService movementService;


    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createDeployment();
    }

    @Test
    public void xxx(){

    }





}
