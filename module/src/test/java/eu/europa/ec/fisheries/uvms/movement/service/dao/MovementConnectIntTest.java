package eu.europa.ec.fisheries.uvms.movement.service.dao;


import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by thofan on 2017-02-14.
 */

@RunWith(Arquillian.class)
public class MovementConnectIntTest extends TransactionalTests {

    final static Logger LOG = LoggerFactory.getLogger(MovementConnectIntTest.class);

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementConnect() {

        for(int i = 0 ; i < 10 ; i++) {
            MovementConnect movementConnect = new MovementConnect();
            movementConnect.setId(UUID.randomUUID());
            movementConnect.setUpdatedBy("arquillian");
            movementConnect.setUpdated(Instant.now());
            em.persist(movementConnect);
            em.flush();
        }
    }
}
