package eu.europa.ec.fisheries.uvms.movement.arquillian;


import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.*;
import java.util.UUID;

/**
 * Created by thofan on 2017-02-14.
 */

@RunWith(Arquillian.class)
public class MovementConnectIntTest extends TransactionalTests {

    final static Logger LOG = LoggerFactory.getLogger(MovementConnectIntTest.class);



    @Test
    @OperateOnDeployment("normal")
    public void createMovementConnect() {

        try {
            userTransaction.begin();
            for(int i = 0 ; i < 10 ; i++) {
                MovementConnect movementConnect = new MovementConnect();
                movementConnect.setValue(UUID.randomUUID().toString());
                movementConnect.setUpdatedBy("arquillian");
                movementConnect.setUpdated(DateUtil.nowUTC());
                em.persist(movementConnect);
                em.flush();
            }
            userTransaction.rollback();

      } catch (NotSupportedException e) {
            LOG.error(e.toString());
        } catch (SystemException e) {
            LOG.error(e.toString());
        }



    }




}
