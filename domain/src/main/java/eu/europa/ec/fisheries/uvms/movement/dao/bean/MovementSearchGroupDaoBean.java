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

import eu.europa.ec.fisheries.uvms.movement.constant.UvmsConstants;
import eu.europa.ec.fisheries.uvms.movement.dao.Dao;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementSearchGroupDao;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementSearchGroupDaoException;
import eu.europa.ec.fisheries.uvms.movement.entity.group.MovementFilterGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless
public class MovementSearchGroupDaoBean extends Dao implements MovementSearchGroupDao {

    private static final Logger LOG = LoggerFactory.getLogger(MovementSearchGroupDaoBean.class);

    @Override
    public MovementFilterGroup createMovementFilterGroup(final MovementFilterGroup filterGroup) throws MovementSearchGroupDaoException {
        try {
            em.persist(filterGroup);
            return filterGroup;
        }
        catch (final Exception e) {
            LOG.error("[ Error when creating movement search filter group. ] {}", e.getMessage());
            throw new MovementSearchGroupDaoException("Could not persist movement search filter group.", e);
        }
    }

    @Override
    public MovementFilterGroup getMovementFilterGroupById(final Integer groupId) {
        LOG.debug("Get movement search group by ID.");
        return em.find(MovementFilterGroup.class, groupId.longValue());
    }

    @Override
    public List<MovementFilterGroup> getMovementFilterGroupsByUser(final String user) throws MovementSearchGroupDaoException {
        try {
            LOG.debug("Get movement groups by user.");
            final TypedQuery<MovementFilterGroup> query = em.createNamedQuery(UvmsConstants.GROUP_VESSEL_BY_USER, MovementFilterGroup.class);
            query.setParameter("user", user);
            final List<MovementFilterGroup> resultList = query.getResultList();
            return resultList;
        }
        catch (final Exception e) {
            LOG.error("[ Error when getting movement filter groups by user. ] {}", e.getMessage());
            throw new MovementSearchGroupDaoException("Could not get movement filter groups by user.", e);
        }
    }

    @Override
	public MovementFilterGroup updateMovementFilterGroup(final MovementFilterGroup filterGroup) throws MovementSearchGroupDaoException {
        try {
            //Sanity check on id to prevent create operation instead of update operation.
            if(filterGroup.getId() != null && getMovementFilterGroupById(filterGroup.getId().intValue()) != null) {
             em.merge(filterGroup);
             em.flush();
            } else {
                throw new MovementSearchGroupDaoException("Missing id or filtergroup with matching id.");
            }
            return filterGroup;
        } catch (final Exception e) {
            LOG.error("[ Error when updating entity ] {}", e.getMessage());
            throw new MovementSearchGroupDaoException("[ Error when updating entity ]", e);
        }
    }

    @Override
    public MovementFilterGroup deleteMovementFilterGroup(final MovementFilterGroup filterGroup) throws MovementSearchGroupDaoException {
        try {
            em.remove(filterGroup);
            return filterGroup;
        }
        catch (final Exception e) {
            LOG.error("[ Error when deleting filter group. ] {}", e.getMessage());
            throw new MovementSearchGroupDaoException("Could not delete movement filter group.", e);
        }
    }
}