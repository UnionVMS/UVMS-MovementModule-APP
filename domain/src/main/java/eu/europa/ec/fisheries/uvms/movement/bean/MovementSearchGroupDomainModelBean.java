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
import eu.europa.ec.fisheries.uvms.movement.entity.group.MovementFilterGroup;
import eu.europa.ec.fisheries.uvms.movement.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainRuntimeException;
import eu.europa.ec.fisheries.uvms.movement.mapper.MovementGroupMapper;
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
public class MovementSearchGroupDomainModelBean {

    private static final Logger LOG = LoggerFactory.getLogger(MovementSearchGroupDomainModelBean.class);

    @EJB
    private MovementSearchGroupDao dao;

    public MovementSearchGroup createMovementSearchGroup(MovementSearchGroup movementGroup, String username) throws MovementDomainException {
        try {
            LOG.debug("Create movement group.");
            MovementFilterGroup filterGroup = MovementGroupMapper.toGroupEntity(movementGroup, username);
            filterGroup = dao.createMovementFilterGroup(filterGroup);
            return MovementGroupMapper.toMovementSearchGroup(filterGroup);
        } catch (MovementDomainException e) {
            LOG.error("[ Cannot create movement search group. Error occurred while trying. ] ");
            throw new MovementDomainException("Could not create movement search groups", ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    public MovementSearchGroup getMovementSearchGroup(BigInteger id) throws MovementDomainException {
        try {
            LOG.debug("Get movement search group by ID.");
            MovementFilterGroup filterGroup = dao.getMovementFilterGroupById(id.intValue());
            if (filterGroup == null) {
                LOG.error(" Error when getting movement search group by ID: {}", id.intValue());
                throw new MovementDomainRuntimeException("Could not get movement search group by group ID:"
                        + id.intValue(), ErrorCode.ILLEGAL_ARGUMENT_ERROR);
            }
            return MovementGroupMapper.toMovementSearchGroup(filterGroup);
        } catch (MovementDomainException e) {
            LOG.error("[ Cannot get movement search group by ID. Error occurred while trying. ] ");
            throw new MovementDomainException("Could not get movement search groups by ID", ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    public List<MovementSearchGroup> getMovementSearchGroupsByUser(String user) throws MovementDomainException {
        try {
            LOG.debug("Get movement search groups by user.");
            List<MovementFilterGroup> filterGroups = dao.getMovementFilterGroupsByUser(user);
            List<MovementSearchGroup> searchGroups = new ArrayList<>();
            for (MovementFilterGroup filterGroup : filterGroups) {
                searchGroups.add(MovementGroupMapper.toMovementSearchGroup(filterGroup));
            }
            return searchGroups;
        } catch (MovementDomainException e) {
            LOG.error("[ Cannot get movement search group. Error occurred while trying. ] ");
            throw new MovementDomainException("Could not get movement search groups", ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    public MovementSearchGroup updateMovementSearchGroup(MovementSearchGroup searchGroup, String username) throws MovementDomainException {
        try {
            MovementFilterGroup currentGroup = dao.getMovementFilterGroupById(searchGroup.getId().intValue());
            if(!currentGroup.getUser().equalsIgnoreCase(searchGroup.getUser())){
                LOG.error("[ Cannot update movement search group because it doesn't belong to this user]");
                throw new MovementDomainException("Could not update movement search groups", ErrorCode.UNSUCCESSFUL_DB_OPERATION);
            }

            currentGroup = MovementGroupMapper.toGroupEntity(currentGroup, searchGroup, username);
            MovementFilterGroup updatedGroup = dao.updateMovementFilterGroup(currentGroup);
            return MovementGroupMapper.toMovementSearchGroup(updatedGroup);
        } catch (MovementDomainException e) {
            LOG.error("[ Error when getting movement search groups by user. ] {}", e.getMessage());
            throw new MovementDomainException("Could not get movement search groups by user.", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    public MovementSearchGroup deleteMovementSearchGroup(BigInteger groupId) throws MovementDomainException {
        try {
            LOG.debug("Delete movement search group.");
            MovementFilterGroup filterGroup = dao.getMovementFilterGroupById(groupId.intValue());
            filterGroup = dao.deleteMovementFilterGroup(filterGroup);
            return MovementGroupMapper.toMovementSearchGroup(filterGroup);
        } catch (Exception e) {
            LOG.error("[ Error when deleting movement search group. ] {}", e.getMessage());
            throw new MovementDomainException("Could not delete movement search group.", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }
}
