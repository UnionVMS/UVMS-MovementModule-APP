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
package eu.europa.ec.fisheries.uvms.movement.bean;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementSearchGroupDao;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementSearchGroupDaoException;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementSearchMapperException;
import eu.europa.ec.fisheries.uvms.movement.entity.group.MovementFilterGroup;
import eu.europa.ec.fisheries.uvms.movement.mapper.MovementGroupMapper;
import eu.europa.ec.fisheries.uvms.movement.model.MovementSearchGroupDomainModel;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Stateless
@LocalBean
public class MovementSearchGroupDomainModelBean implements MovementSearchGroupDomainModel {

    private static final Logger LOG = LoggerFactory.getLogger(MovementSearchGroupDomainModelBean.class);

    @EJB
    MovementSearchGroupDao dao;

    public MovementSearchGroup createMovementSearchGroup(MovementSearchGroup movementGroup, String username) throws MovementModelException {
        try {
            LOG.debug("Create movement group.");
            MovementFilterGroup filterGroup = MovementGroupMapper.toGroupEntity(movementGroup, username);
            filterGroup = dao.createMovementFilterGroup(filterGroup);
            return MovementGroupMapper.toMovementSearchGroup(filterGroup);
        } catch (MovementSearchMapperException | MovementSearchGroupDaoException e) {
            LOG.error("[ Error when creating new movement group. ] {}", e.getMessage());
            throw new MovementModelException("Could not create movement search group.", e);
        }
    }

    public MovementSearchGroup getMovementSearchGroup(BigInteger id) throws MovementModelException {
        try {
            LOG.debug("Get movement search group by ID.");
            MovementFilterGroup filterGroup = dao.getMovementFilterGroupById(id.intValue());
            if(filterGroup == null){
                LOG.error(" Error when getting movement search group by ID: {}", id.intValue());
                throw new MovementModelException("Could not get movement search group by group ID:" + id.intValue());
            }
            return MovementGroupMapper.toMovementSearchGroup(filterGroup);
        } catch (MovementSearchGroupDaoException e) {
            LOG.error("Error when getting movement search group by ID: {}", e.getMessage());
            throw new MovementModelException("Could not get movement search group by group ID.", e);
        }
    }

    public List<MovementSearchGroup> getMovementSearchGroupsByUser(String user) throws MovementModelException {
        try {
            LOG.debug("Get movement search groups by user.");
            List<MovementFilterGroup> filterGroups = dao.getMovementFilterGroupsByUser(user);
            List<MovementSearchGroup> searchGroups = new ArrayList<>();
            for (MovementFilterGroup filterGroup : filterGroups) {
                searchGroups.add(MovementGroupMapper.toMovementSearchGroup(filterGroup));
            }

            return searchGroups;
        } catch (MovementSearchGroupDaoException e) {
            LOG.error("[ Error when getting movement search groups by user. ] {}", e.getMessage());
            throw new MovementModelException("Could not get movement search groups by user.", e);
        }
    }

    public MovementSearchGroup updateMovementSearchGroup(MovementSearchGroup searchGroup, String username) throws MovementModelException {
        try {
            MovementFilterGroup currentGroup = dao.getMovementFilterGroupById(searchGroup.getId().intValue());
            if(!currentGroup.getUser().equalsIgnoreCase(searchGroup.getUser())){
                LOG.error("[ Cannot update movement search group because it doesn't belong to this user]");
                throw new MovementModelException("Could not update movement search groups");
            }

            currentGroup = MovementGroupMapper.toGroupEntity(currentGroup, searchGroup, username);
            MovementFilterGroup updatedGroup = dao.updateMovementFilterGroup(currentGroup);
            return MovementGroupMapper.toMovementSearchGroup(updatedGroup);
        } catch (MovementSearchGroupDaoException | MovementSearchMapperException e) {
            LOG.error("[ Error when getting movement search groups by user. ] {}", e.getMessage());
            throw new MovementModelException("Could not get movement search groups by user.", e);
        }
    }

    public MovementSearchGroup deleteMovementSearchGroup(BigInteger groupId) throws MovementModelException {
        try {
            LOG.debug("Delete movement search group.");
            MovementFilterGroup filterGroup = dao.getMovementFilterGroupById(groupId.intValue());
            filterGroup = dao.deleteMovementFilterGroup(filterGroup);
            return MovementGroupMapper.toMovementSearchGroup(filterGroup);
        } catch (MovementSearchGroupDaoException e) {
            LOG.error("[ Error when deleting movement search group. ] {}", e.getMessage());
            throw new MovementModelException("Could not delete movement search group.", e);
        }
    }
}