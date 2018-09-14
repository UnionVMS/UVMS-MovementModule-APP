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

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.movement.dao.Dao;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementSearchGroupDao;
import eu.europa.ec.fisheries.uvms.movement.entity.group.MovementFilterGroup;
import eu.europa.ec.fisheries.uvms.movement.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;

@Stateless
public class MovementSearchGroupDaoBean extends Dao implements MovementSearchGroupDao {

    private static final Logger LOG = LoggerFactory.getLogger(MovementSearchGroupDaoBean.class);

    @Override
    public MovementFilterGroup createMovementFilterGroup(MovementFilterGroup filterGroup) {
        em.persist(filterGroup);
        return filterGroup;
    }

    @Override
    public MovementFilterGroup getMovementFilterGroupById(Integer groupId) {
        LOG.debug("Get movement search group by ID.");
        return em.find(MovementFilterGroup.class, groupId.longValue());
    }

    @Override
    public List<MovementFilterGroup> getMovementFilterGroupsByUser(String user) {
        LOG.debug("Get movement groups by user.");
        TypedQuery<MovementFilterGroup> query = em.createNamedQuery(MovementFilterGroup.GROUP_VESSEL_BY_USER, MovementFilterGroup.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    @Override
    public MovementFilterGroup updateMovementFilterGroup(MovementFilterGroup filterGroup) throws MovementDomainException {
        //Sanity check on id to prevent create operation instead of update operation.
        if(filterGroup.getId() != null && getMovementFilterGroupById(filterGroup.getId().intValue()) != null) {
            filterGroup = em.merge(filterGroup);
            em.flush();
        } else {
            throw new MovementDomainException("Missing ID or filterGroup with matching ID.", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        return filterGroup;
    }

    @Override
    public MovementFilterGroup deleteMovementFilterGroup(MovementFilterGroup filterGroup) {
        em.remove(filterGroup);
        return filterGroup;
    }
}
