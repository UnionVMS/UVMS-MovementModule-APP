package eu.europa.ec.fisheries.uvms.movement.service.dao;


import java.util.UUID;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;

/**
 * Created by thofan on 2017-02-14.
 */

@RunWith(Arquillian.class)
public class MovementConnectIntTest extends TransactionalTests {

    final static Logger LOG = LoggerFactory.getLogger(MovementConnectIntTest.class);

    @Test
    public void createMovementConnect() {

        for(int i = 0 ; i < 10 ; i++) {
            MovementConnect movementConnect = new MovementConnect();
            movementConnect.setValue(UUID.randomUUID());
            movementConnect.setUpdatedBy("arquillian");
            movementConnect.setUpdated(DateUtil.nowUTC());
            em.persist(movementConnect);
            em.flush();
        }
    }
}
