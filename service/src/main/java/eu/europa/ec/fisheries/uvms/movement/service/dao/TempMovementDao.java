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
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import eu.europa.ec.fisheries.uvms.movement.service.entity.temp.TempMovement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementDomainRuntimeException;

@Stateless
public class TempMovementDao {

    @PersistenceContext
    private EntityManager em;
    
    public TempMovement createTempMovementEntity(TempMovement tempMovement) {
        em.persist(tempMovement);
        return tempMovement;
    }

    public TempMovement getTempMovementByGuid(String guid) throws MovementDomainException {
        try {
            TypedQuery<TempMovement> query = em.createNamedQuery(TempMovement.FIND_BY_GUID, TempMovement.class);
            query.setParameter("guid", guid);
            return query.getSingleResult();
        } catch (NoResultException | NonUniqueResultException e) {
            throw new MovementDomainException("Error when fetching temp movement", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    public List<TempMovement> getTempMovementListPaginated(Integer page, Integer listSize) {
        try {
            TypedQuery<TempMovement> query = em.createNamedQuery(TempMovement.FIND_ALL_ORDERED, TempMovement.class);
            query.setFirstResult(listSize * (page - 1));
            query.setMaxResults(listSize);
            return query.getResultList();
        } catch (RuntimeException e) {
            throw new MovementDomainRuntimeException("Error when fetching temp movement list.", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    public Long getTempMovementListCount() {
        try {
            TypedQuery<Long> query = em.createNamedQuery(TempMovement.COUNT, Long.class);
            return query.getSingleResult();
        } catch (NoResultException | NonUniqueResultException e) {
            throw new MovementDomainRuntimeException("Error when fetching temp movement list count.", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }
}