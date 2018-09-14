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
import eu.europa.ec.fisheries.uvms.movement.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.*;
import java.util.List;

@Stateless
public class TempMovementDaoBean extends Dao implements TempMovementDao {

    private static final Logger LOG = LoggerFactory.getLogger(TempMovementDaoBean.class);

    @Override
    public TempMovement createTempMovementEntity(TempMovement tempMovement) {
        em.persist(tempMovement);
        return tempMovement;
    }

    @Override
    public TempMovement getTempMovementByGuid(String guid) {
        try {
            TypedQuery<TempMovement> query = em.createNamedQuery(TempMovement.FIND_BY_GUID, TempMovement.class);
            query.setParameter("guid", guid);
            return query.getSingleResult();
        } catch (NoResultException | NonUniqueResultException e) {
            LOG.error("[ Error when fetching temp movement. ] {}", e.getMessage());
            throw new MovementDomainRuntimeException("Error when fetching temp movement", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    @Override
    public List<TempMovement> getTempMovementListPaginated(Integer page, Integer listSize) {
        try {
            TypedQuery<TempMovement> query = em.createNamedQuery(TempMovement.FIND_ALL_ORDERED, TempMovement.class);
            query.setFirstResult(listSize * (page - 1));
            query.setMaxResults(listSize);
            return query.getResultList();
        } catch (RuntimeException e) {
            LOG.error("[ Error when fetching temp movement list. ] {}", e.getMessage());
            throw new MovementDomainRuntimeException("[ Error when fetching temp movement list. ]", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    @Override
    public Long getTempMovementListCount() {
        try {
            TypedQuery<Long> query = em.createNamedQuery(TempMovement.COUNT, Long.class);
            return query.getSingleResult();
        } catch (NoResultException | NonUniqueResultException e) {
            LOG.error("[ Error when fetching temp movement list count. ] {}", e.getMessage());
            throw new MovementDomainRuntimeException("Error when fetching temp movement list count. ]", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }
}
