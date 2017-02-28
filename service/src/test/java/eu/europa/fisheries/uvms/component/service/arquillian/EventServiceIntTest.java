package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.uvms.movement.message.event.*;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 *
 * @author roblar
 */
@RunWith(Arquillian.class)
public class EventServiceIntTest extends TransactionalTests {

    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createDeployment();
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
