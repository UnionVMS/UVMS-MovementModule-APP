package eu.europa.ec.fisheries.uvms.movement.service.bean;

import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.uvms.movement.service.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;

/**
 * Created by roblar on 2017-03-08.
 */
@RunWith(Arquillian.class)
public class GetMapByQueryIntTest extends TransactionalTests {

    @Inject
    MovementService movementServiceBean;


    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void getMovementMapByQuery_settingPaginationOnAMovementMapQueryIsNotAllowed() {

        MovementQuery movementQuery = MovementTestHelper.createMovementQuery(true, false, false);
        movementServiceBean.getMapByQuery(movementQuery);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void getMovementMapByQuery_settingPaginationOnAMovementMapQueryThrowsMovementServiceException() {

        MovementQuery movementQuery = MovementTestHelper.createMovementQuery(true, false, false);
        movementServiceBean.getMapByQuery(movementQuery);
    }

    //ToDo: An arbitrary string value should not be allowed to be set for the ListCriteria field called 'value' by using a setter as the value *must* match only allowed enum values for the enum SearchKey.
    //ToDo: This enum is mapped by the SearchField enum toward the MovementTypeType enum. One solution could be to remove the setValue() method in the ListCriteria class.
    @Test(expected = IllegalArgumentException.class)
    @OperateOnDeployment("movementservice")
    public void getMovementMapByQuery_mustUseEnumValueFromMovementTypeTypeClassWhenSettingSearchKeyTypeValueTo_MOVEMENT_TYPE() {

        MovementQuery movementQuery = MovementTestHelper.createMovementQuery(false, true, false);
        movementServiceBean.getMapByQuery(movementQuery);
    }
}
