package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import java.util.List;
import java.util.UUID;

import javax.ejb.EJB;
import javax.transaction.SystemException;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.TestUtil;
import eu.europa.ec.fisheries.uvms.movement.bean.IncomingMovementBean;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;

/**
 * Created by andreasw on 2017-03-09.
 */
@RunWith(Arquillian.class)
public class IncomingMovementBeanIntTest extends TransactionalTests {

    @EJB
    IncomingMovementBean incomingMovementBean;

    @EJB
    MovementBatchModelBean movementBatchModelBean;

    @EJB
    MovementDao movementDao;

    private TestUtil testUtil = new TestUtil();


    @Test
    @OperateOnDeployment("normal")
    public void create() throws MovementDaoMappingException, MovementModelException, SystemException, GeometryUtilException, MovementDaoException, MovementDuplicateException {
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(0d, 1d, 0d, SegmentCategoryType.EXIT_PORT, uuid);
        movementType = movementBatchModelBean.createMovement(movementType, "TEST");
        Assert.assertNotNull(movementType.getGuid());
        em.flush();
        MovementConnect movementConnent = movementDao.getMovementConnectByConnectId(movementType.getConnectId());
        List<Movement> movementList = movementConnent.getMovementList();
        Assert.assertNotNull(movementList);
        Assert.assertTrue(movementList.size() == 1);
        Long id = movementList.get(0).getId();
        incomingMovementBean.processMovement(id);
        Assert.assertTrue(true);

        Movement movement = movementDao.getMovementById(id);
        Assert.assertNotNull(movement);
        Assert.assertTrue(movement.getProcessed());
    }


}
