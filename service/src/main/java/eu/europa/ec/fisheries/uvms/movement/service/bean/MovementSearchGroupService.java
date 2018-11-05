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
package eu.europa.ec.fisheries.uvms.movement.service.bean;

import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementSearchGroupDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.group.MovementFilterGroup;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceRuntimeException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementGroupMapper;
import eu.europa.ec.fisheries.uvms.movement.service.validation.MovementGroupValidatorBean;

@Stateless
public class MovementSearchGroupService {

    @Inject
    private MovementSearchGroupDao dao;

    public MovementFilterGroup createMovementFilterGroup(MovementSearchGroup searchGroup, String username) throws MovementServiceException {
        if(searchGroup.getName() == null || searchGroup.getName().isEmpty()){
            throw new MovementServiceRuntimeException("Search group must have a name", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if(username == null){
            throw new MovementServiceRuntimeException("Create MovementSearchGroup must have username set, cannot be null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        try {
            if (MovementGroupValidatorBean.isMovementGroupOk(searchGroup)) {
                MovementFilterGroup filterGroup = MovementGroupMapper.toGroupEntity(searchGroup, username);
                return dao.createMovementFilterGroup(filterGroup);
            } else {
                throw new MovementServiceException("One or several movement types are misspelled or non existent." +
                        " Allowed values are: [ " + MovementGroupValidatorBean.ALLOWED_FIELD_VALUES + " ]", ErrorCode.UNSUCCESSFUL_DB_OPERATION);
            }
        } catch (Exception e) {
            throw new MovementServiceException("Error when creating movement search group", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    public MovementFilterGroup getMovementFilterGroup(Long id) throws MovementServiceException {
        try {
            MovementFilterGroup filterGroup = dao.getMovementFilterGroupById(id.intValue());
            if (filterGroup == null) {
                throw new MovementServiceRuntimeException("Could not get movement search group by group ID:"
                        + id.intValue(), ErrorCode.ILLEGAL_ARGUMENT_ERROR);
            }
            return filterGroup;
        } catch (Exception e) {
            throw new MovementServiceException("Error when getting movement search group", e, ErrorCode.DATA_RETRIEVING_ERROR);
        }
    }

    public List<MovementFilterGroup> getMovementFilterGroupsByUser(String user) throws MovementServiceException {
        try {
            return dao.getMovementFilterGroupsByUser(user);
        } catch (Exception e) {
            throw new MovementServiceException("Error when getting movement search groups by user", e, ErrorCode.DATA_RETRIEVING_ERROR);
        }
    }

    public MovementFilterGroup updateMovementFilterGroup(MovementSearchGroup updatedDTO, String username) throws MovementServiceException {
        if(updatedDTO.getId() == null || username == null){
            throw new MovementServiceRuntimeException("Error when updating movement search group." +
                    " MovementSearchGroup has no id set or the username is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        try {
            MovementFilterGroup currentGroup = dao.getMovementFilterGroupById(updatedDTO.getId().intValue());
            if(!currentGroup.getUser().equalsIgnoreCase(updatedDTO.getUser())){
                throw new MovementServiceException("Could not update movement search groups", ErrorCode.INVALID_USERNAME);
            }

            currentGroup = MovementGroupMapper.toGroupEntity(currentGroup, updatedDTO, username);
            return dao.updateMovementFilterGroup(currentGroup);
        } catch (MovementServiceException e) {
            throw new MovementServiceException("Error when updating movement search group.", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    public MovementFilterGroup deleteMovementFilterGroup(Long id) throws MovementServiceException {
        try {
            MovementFilterGroup filterGroup = dao.getMovementFilterGroupById(id.intValue());
            filterGroup = dao.deleteMovementFilterGroup(filterGroup);
            return filterGroup;
        } catch (Exception e) {
            throw new MovementServiceException("Error when deleting movement search group", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }
}
