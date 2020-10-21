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

import eu.europa.ec.fisheries.uvms.movement.service.entity.group.MovementFilterGroup;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;

@Stateless
public class MovementSearchGroupDao {

    @PersistenceContext
    private EntityManager em;
    
    public MovementFilterGroup createMovementFilterGroup(MovementFilterGroup filterGroup) {
        em.persist(filterGroup);
        return filterGroup;
    }

    public MovementFilterGroup getMovementFilterGroupById(UUID groupId) {
        return em.find(MovementFilterGroup.class, groupId);
    }

    public List<MovementFilterGroup> getMovementFilterGroupsByUser(String user) {
        TypedQuery<MovementFilterGroup> query = em.createNamedQuery(MovementFilterGroup.GROUP_VESSEL_BY_USER, MovementFilterGroup.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    public MovementFilterGroup updateMovementFilterGroup(MovementFilterGroup filterGroup){
        //Sanity check on id to prevent create operation instead of update operation.
        if(filterGroup.getId() != null && getMovementFilterGroupById(filterGroup.getId()) != null) {
            filterGroup = em.merge(filterGroup);
            em.flush();
        } else {
            throw new IllegalArgumentException("Missing ID or filterGroup with matching ID.");
        }
        return filterGroup;
    }

    public MovementFilterGroup deleteMovementFilterGroup(MovementFilterGroup filterGroup) {
        em.remove(filterGroup);
        return filterGroup;
    }
}
