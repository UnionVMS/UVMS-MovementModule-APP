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
package eu.europa.ec.fisheries.uvms.movement.service.mapper;

import eu.europa.ec.fisheries.schema.movement.search.v1.GroupListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
import eu.europa.ec.fisheries.uvms.movement.service.entity.group.MovementFilter;
import eu.europa.ec.fisheries.uvms.movement.service.entity.group.MovementFilterGroup;
import eu.europa.ec.fisheries.uvms.movement.service.util.CalculationUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MovementGroupMapper {

    private MovementGroupMapper() {}

    public static MovementFilterGroup toGroupEntity(MovementFilterGroup filterGroup, MovementSearchGroup searchGroup, String username) {
        validateMovementSearchGroup(searchGroup);

        filterGroup.setActive(Boolean.TRUE.toString());
        filterGroup.setGlobal(Boolean.TRUE.toString());
        filterGroup.setDynamic(searchGroup.isDynamic() ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
        filterGroup.setUpdated(Instant.now());
        filterGroup.setUpdatedBy(username);
        filterGroup.setName(searchGroup.getName());
        filterGroup.setUser(searchGroup.getUser());

        filterGroup.getFilters().clear();
        List<GroupListCriteria> searchFields = searchGroup.getSearchFields();
        List<MovementFilter> newFilters = new ArrayList<>();
        if (searchFields != null) {
            for (GroupListCriteria searchField : searchFields) {
                newFilters.add(toFilterEntity(filterGroup, searchField, username));
            }
            filterGroup.getFilters().addAll(newFilters);
        }
        return filterGroup;
    }

    public static MovementFilterGroup toGroupEntity(MovementSearchGroup searchGroup, String username) {
        MovementFilterGroup filterGroup = new MovementFilterGroup();
        return toGroupEntity(filterGroup, searchGroup, username);
    }

    public static MovementSearchGroup toMovementSearchGroup(MovementFilterGroup filterGroup) {
        MovementSearchGroup group = new MovementSearchGroup();
        group.setDynamic(getBoolean(filterGroup.getDynamic()));
        group.setName(filterGroup.getName());
        group.setUser(filterGroup.getUser());

        if (filterGroup.getId() != null) {
            group.setId(CalculationUtil.convertToBigInteger(filterGroup.getId()));
        }

        for (MovementFilter filter : filterGroup.getFilters()) {
            group.getSearchFields().add(toGroupListCriteria(filter));
        }

        return group;
    }

    private static MovementFilter toFilterEntity(MovementFilterGroup parent, GroupListCriteria searchField, String username) {
        MovementFilter filter = new MovementFilter();
        filter.setField(searchField.getKey());
        filter.setValue(searchField.getValue());
        filter.setUpdated(Instant.now());
        filter.setUpdatedBy(username);
        filter.setMovementFilterType(searchField.getType());
        filter.setFilterGroup(parent);
        return filter;
    }

    private static GroupListCriteria toGroupListCriteria(MovementFilter filter) {
        GroupListCriteria searchField = new GroupListCriteria();
        searchField.setKey(filter.getField());
        searchField.setValue(filter.getValue());
        searchField.setType(filter.getMovementFilterType());
        return searchField;
    }

    private static boolean getBoolean(String value) {
        if (value != null && !value.isEmpty()) {
            return value.equalsIgnoreCase(Boolean.TRUE.toString());
        }

        return false;
    }

    private static void validateMovementSearchGroup(MovementSearchGroup searchGroup) {
        if (searchGroup.getName() == null) {
            throw new IllegalArgumentException("MovementSearchGroupName cannot be null");
        }
    }
}