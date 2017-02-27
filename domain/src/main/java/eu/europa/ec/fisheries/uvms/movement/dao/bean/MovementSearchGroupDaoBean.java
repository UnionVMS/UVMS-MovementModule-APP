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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import eu.europa.ec.fisheries.uvms.movement.dao.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.movement.constant.UvmsConstants;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementSearchGroupDao;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementSearchGroupDaoException;
import eu.europa.ec.fisheries.uvms.movement.entity.group.MovementFilterGroup;

@Stateless
public class MovementSearchGroupDaoBean extends Dao implements MovementSearchGroupDao {

    private static final Logger LOG = LoggerFactory.getLogger(MovementSearchGroupDaoBean.class);

    @Override
    public MovementFilterGroup createMovementFilterGroup(MovementFilterGroup filterGroup) throws MovementSearchGroupDaoException {
        try {
            em.persist(filterGroup);
            return filterGroup;
        }
        catch (Exception e) {
            LOG.error("[ Error when creating movement search filter group. ] {}", e.getMessage());
            throw new MovementSearchGroupDaoException("Could not persist movement search filter group.", e);
        }
    }

    @Override
    public MovementFilterGroup getMovementFilterGroupById(Integer groupId) {
        LOG.info("Get movement group by ID.");
        return em.find(MovementFilterGroup.class, groupId.longValue());
    }

    @Override
    public List<MovementFilterGroup> getMovementFilterGroupsByUser(String user) throws MovementSearchGroupDaoException {
        try {
            LOG.info("Get movement groups by user.");
            TypedQuery<MovementFilterGroup> query = em.createNamedQuery(UvmsConstants.GROUP_VESSEL_BY_USER, MovementFilterGroup.class);
            query.setParameter("user", user);
            List<MovementFilterGroup> resultList = query.getResultList();
            return resultList;
        }
        catch (Exception e) {
            LOG.error("[ Error when getting movement filter groups by user. ] {}", e.getMessage());
            throw new MovementSearchGroupDaoException("Could not get movement filter groups by user.", e);
        }
    }

    public MovementFilterGroup updateMovementFilterGroup(MovementFilterGroup filterGroup) throws MovementSearchGroupDaoException {
        try {
            em.merge(filterGroup);
            em.flush();
            return filterGroup;
        } catch (Exception e) {
            LOG.error("[ Error when updating entity ] {}", e.getMessage());
            throw new MovementSearchGroupDaoException("[ Error when updating entity ]", e);
        }
    }

    @Override
    public MovementFilterGroup deleteMovementFilterGroup(MovementFilterGroup filterGroup) throws MovementSearchGroupDaoException {
        try {
            em.remove(filterGroup);
            return filterGroup;
        }
        catch (Exception e) {
            LOG.error("[ Error when deleting filter group. ] {}", e.getMessage());
            throw new MovementSearchGroupDaoException("Could not delete movement filter group.", e);
        }
    }
}