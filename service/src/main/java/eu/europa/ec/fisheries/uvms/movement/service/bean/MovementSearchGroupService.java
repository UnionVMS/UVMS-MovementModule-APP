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
import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementSearchGroupDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.group.MovementFilterGroup;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementGroupMapper;
import eu.europa.ec.fisheries.uvms.movement.service.util.CalculationUtil;
import eu.europa.ec.fisheries.uvms.movement.service.validation.MovementGroupValidatorBean;

@Stateless
public class MovementSearchGroupService {

    @Inject
    private MovementSearchGroupDao dao;

    public MovementFilterGroup createMovementFilterGroup(MovementSearchGroup searchGroup, String username) {
        if(searchGroup.getName() == null || searchGroup.getName().isEmpty()){
            throw new IllegalArgumentException("Search group must have a name");
        }
        if(username == null){
            throw new IllegalArgumentException("Create MovementSearchGroup must have username set, cannot be null");
        }
            if (MovementGroupValidatorBean.isMovementGroupOk(searchGroup)) {
                MovementFilterGroup filterGroup = MovementGroupMapper.toGroupEntity(searchGroup, username);
                return dao.createMovementFilterGroup(filterGroup);
            } else {
                throw new IllegalArgumentException("One or several movement types are misspelled or non existent." +
                        " Allowed values are: [ " + MovementGroupValidatorBean.ALLOWED_FIELD_VALUES + " ]");
            }
    }

    public MovementFilterGroup getMovementFilterGroup(UUID id) {
            MovementFilterGroup filterGroup = dao.getMovementFilterGroupById(id);
            if (filterGroup == null) {
                throw new IllegalArgumentException("Could not get movement search group by group ID:"
                        + id);
            }
            return filterGroup;
    }

    public List<MovementFilterGroup> getMovementFilterGroupsByUser(String user){
            return dao.getMovementFilterGroupsByUser(user);

    }

    public MovementFilterGroup updateMovementFilterGroup(MovementSearchGroup updatedDTO, String username) {
        if(updatedDTO.getId() == null || username == null){
            throw new IllegalArgumentException("Error when updating movement search group." +
                    " MovementSearchGroup has no id set or the username is null");
        }
            MovementFilterGroup currentGroup = dao.getMovementFilterGroupById(CalculationUtil.convertFromBigInteger(updatedDTO.getId()));
            if(!currentGroup.getUser().equalsIgnoreCase(updatedDTO.getUser())){
                throw new IllegalArgumentException("Could not update movement search groups due to invalid username");
            }

            currentGroup = MovementGroupMapper.toGroupEntity(currentGroup, updatedDTO, username);
            return dao.updateMovementFilterGroup(currentGroup);
    }

    public MovementFilterGroup deleteMovementFilterGroup(UUID id) {
        try {
            MovementFilterGroup filterGroup = dao.getMovementFilterGroupById(id);
            filterGroup = dao.deleteMovementFilterGroup(filterGroup);
            return filterGroup;
        } catch (Exception e) {
            throw new RuntimeException("Error when deleting movement search group", e);
        }
    }
}
