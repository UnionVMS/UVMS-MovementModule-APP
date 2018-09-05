/*
 Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
 © European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
 redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
 the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
 copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.movement.bean;

import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDaoBean;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by osdjup on 2016-12-19.
 */
@Singleton
@Startup
@TransactionManagement( TransactionManagementType.BEAN )
public class MovementProcessorBean {

    final static Logger LOG = LoggerFactory.getLogger(MovementProcessorBean.class);

    private ScheduledExecutorService executor;

    @EJB
    private MovementDaoBean dao;

    @EJB
    private IncomingMovementBean incomingMovementBean;

    @Resource
    private EJBContext context;

    /*@PostConstruct
    public void init() {
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    processMovements();
                } catch (SystemException e) {
                    LOG.error("", e);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }*/

    /*@PreDestroy
    public void destroy() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }*/

    public void processMovements() throws SystemException {
        UserTransaction utx = context.getUserTransaction();
        LOG.debug("------------------- Processing started ------------------------");
        long start = System.currentTimeMillis();
        try {
            utx.begin();
            List<Long> movements = dao.getUnprocessedMovementIds();
            LOG.debug("Movement processing time for {} movements: {} ms", movements.size(), (System.currentTimeMillis() - start));
            utx.commit();

            for (Long id : movements) {
                incomingMovementBean.processMovement(id);
            }

        } catch (Exception e) {
            LOG.error("Error while processing movement", e);
            utx.rollback();
        }
    }

}
