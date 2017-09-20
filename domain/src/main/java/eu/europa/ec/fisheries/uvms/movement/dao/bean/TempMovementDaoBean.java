/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.
This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.movement.dao.bean;

import eu.europa.ec.fisheries.uvms.movement.dao.Dao;
import eu.europa.ec.fisheries.uvms.movement.dao.TempMovementDao;
import eu.europa.ec.fisheries.uvms.movement.entity.temp.TempMovement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.*;
import java.util.List;

@Stateless
public class TempMovementDaoBean extends Dao implements TempMovementDao {

    private final static Logger LOG = LoggerFactory.getLogger(TempMovementDaoBean.class);

    @Override
    public TempMovement createTempMovementEntity(final TempMovement tempMovement) {
        em.persist(tempMovement);
        return tempMovement;
    }

    @Override
    public TempMovement getTempMovementByGuid(final String guid) throws MovementDaoException {
        try {
            final TypedQuery<TempMovement> query = em.createNamedQuery("TempMovement.findByGuidId", TempMovement.class);
            query.setParameter("guid", guid);
            final TempMovement tempMovement = query.getSingleResult();
            return tempMovement;
        } catch (NoResultException | NonUniqueResultException e) {
            LOG.error("[ Error when fetching temp movement. ] {}", e.getMessage());
            throw new MovementDaoException(13, "[ Error when fetching temp movement. ]", e);
        }
    }

    @Override
    public List<TempMovement> getTempMovementListPaginated(final Integer page, final Integer listSize) throws MovementDaoException {
        try {
            final TypedQuery<TempMovement> query = em.createNamedQuery("TempMovement.findAllOrdered", TempMovement.class);
            query.setFirstResult(listSize * (page - 1));
            query.setMaxResults(listSize);
            final List<TempMovement> resultList = query.getResultList();
            return resultList;
        } catch (NoResultException | NonUniqueResultException e) {
            LOG.error("[ Error when fetching temp movment list. ] {}", e.getMessage());
            throw new MovementDaoException(13, "[ Error when fetching temp movement list. ]", e);
        }
    }

    @Override
    public Long getTempMovementListCount() throws MovementDaoException {
        try {
            final TypedQuery<Long> query = em.createNamedQuery("TempMovement.count", Long.class);
            final Long singleResult = query.getSingleResult();
            return singleResult;
        } catch (NoResultException | NonUniqueResultException e) {
            LOG.error("[ Error when fetching temp movment list count. ] {}", e.getMessage());
            throw new MovementDaoException(13, "[ Error when fetching temp movement list count. ]", e);
        }
    }

}