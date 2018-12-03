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
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import eu.europa.ec.fisheries.uvms.movement.service.entity.temp.DraftMovement;
import org.hibernate.HibernateException;

@Stateless
public class DraftMovementDao {

    @PersistenceContext
    private EntityManager em;
    
    public DraftMovement createDraftMovementEntity(DraftMovement draftMovement) {
        em.persist(draftMovement);
        return draftMovement;
    }

    public DraftMovement getDraftMovementById(UUID id) {
        DraftMovement draftMovement = em.find(DraftMovement.class, id);
        if (draftMovement == null) {
            throw new HibernateException("Error when fetching temp movement");
        }
        return draftMovement;
    }

    public List<DraftMovement> getDraftMovementListPaginated(Integer page, Integer listSize) {
            TypedQuery<DraftMovement> query = em.createNamedQuery(DraftMovement.FIND_ALL_ORDERED, DraftMovement.class);
            query.setFirstResult(listSize * (page - 1));
            query.setMaxResults(listSize);
            return query.getResultList();
    }

    public Long getDraftMovementListCount() {
            TypedQuery<Long> query = em.createNamedQuery(DraftMovement.COUNT, Long.class);
            return query.getSingleResult();
    }
}
