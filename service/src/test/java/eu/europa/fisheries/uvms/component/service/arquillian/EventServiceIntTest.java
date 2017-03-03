package eu.europa.fisheries.uvms.component.service.arquillian;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author roblar
 */
@RunWith(Arquillian.class)
public class EventServiceIntTest extends TransactionalTests {

    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createMovementSearchDeployment();
    }

    @Test
    public void dummy(){
        Assert.assertTrue(true);
    }

    /*
    @Test
    public void testCreateMovement(@Observes @CreateMovementEvent EventMessage message) {

    }

    @Test
    public void getMovementMapByQuery(@Observes @GetMovementMapByQueryEvent EventMessage message) {

    }

    @Test
    public void getMovementListByQuery(@Observes @GetMovementListByQueryEvent EventMessage message) {

    }

    @Test
    public void createMovementBatch(@Observes @CreateMovementBatchEvent EventMessage message) {

    }

    @Test
    public void ping(@Observes @PingEvent EventMessage message) {

    }

    @Test
    public void getMovementListByAreaAndTimeInterval(@Observes @GetMovementListByAreaAndTimeIntervalEvent EventMessage message) {

    }
    */
}
