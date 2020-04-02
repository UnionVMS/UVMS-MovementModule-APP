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
package eu.europa.ec.fisheries.uvms.movement.service.dao;

import java.util.List;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import eu.europa.ec.fisheries.uvms.movement.service.entity.temp.TempMovement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceRuntimeException;

@Stateless
public class TempMovementDao {

    @PersistenceContext
    private EntityManager em;
    
    public TempMovement createTempMovementEntity(TempMovement tempMovement) {
        em.persist(tempMovement);
        return tempMovement;
    }

    public TempMovement getTempMovementById(String id) throws MovementServiceException {
        TempMovement tempMovement = em.find(TempMovement.class, id);
        if (tempMovement == null) {
            throw new MovementServiceException("Error when fetching temp movement", ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
        return tempMovement;
    }

    public List<TempMovement> getTempMovementListPaginated(Integer page, Integer listSize) {
        try {
            TypedQuery<TempMovement> query = em.createNamedQuery(TempMovement.FIND_ALL_ORDERED, TempMovement.class);
            query.setFirstResult(listSize * (page - 1));
            query.setMaxResults(listSize);
            return query.getResultList();
        } catch (RuntimeException e) {
            throw new MovementServiceRuntimeException("Error when fetching temp movement list.", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    public Long getTempMovementListCount() {
        try {
            TypedQuery<Long> query = em.createNamedQuery(TempMovement.COUNT, Long.class);
            return query.getSingleResult();
        } catch (NoResultException | NonUniqueResultException e) {
            throw new MovementServiceRuntimeException("Error when fetching temp movement list count.", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }
}
