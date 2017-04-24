package eu.europa.fisheries.uvms.component.service.arquillian;


import org.junit.After;
import org.junit.Before;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.*;

public class TransactionalTests extends BuildMovementServiceTestDeployment {



    @Inject
    protected UserTransaction userTransaction;

    @PersistenceContext
    protected EntityManager em;

    @Before
    public void before() throws SystemException, NotSupportedException {
        userTransaction.begin();
    }

    @After
    public void after() throws SystemException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        userTransaction.rollback();
    }

}
