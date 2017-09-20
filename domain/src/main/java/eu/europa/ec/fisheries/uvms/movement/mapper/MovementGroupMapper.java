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
package eu.europa.ec.fisheries.uvms.movement.mapper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import eu.europa.ec.fisheries.uvms.movement.constant.UvmsConstants;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementSearchMapperException;
import eu.europa.ec.fisheries.uvms.movement.entity.group.MovementFilter;
import eu.europa.ec.fisheries.uvms.movement.entity.group.MovementFilterGroup;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;

public class MovementGroupMapper {

    public static MovementFilterGroup toGroupEntity(final MovementFilterGroup filterGroup, final MovementSearchGroup searchGroup, final String username) throws MovementSearchMapperException {
        validateMovementSearchGroup(searchGroup);

        filterGroup.setActive(UvmsConstants.TRUE);
        filterGroup.setGlobal(UvmsConstants.TRUE);
        filterGroup.setDynamic(searchGroup.isDynamic() ? UvmsConstants.TRUE : UvmsConstants.FALSE);
        filterGroup.setUpdated(DateUtil.nowUTC());
        filterGroup.setUpdatedBy(username);
        filterGroup.setName(searchGroup.getName());
        filterGroup.setUser(searchGroup.getUser());

        filterGroup.getFilters().clear();
        final List<GroupListCriteria> searchFields = searchGroup.getSearchFields();
        final List<MovementFilter> newFilters = new ArrayList<>();
        if (searchFields != null) {
            for (final GroupListCriteria searchField : searchFields) {
                newFilters.add(toFilterEntity(filterGroup, searchField, username));
            }

            filterGroup.getFilters().addAll(newFilters);
        }

        return filterGroup;
    }

    public static MovementFilterGroup toGroupEntity(final MovementSearchGroup searchGroup, final String username) throws MovementSearchMapperException {
        final MovementFilterGroup filterGroup = new MovementFilterGroup();
        return toGroupEntity(filterGroup, searchGroup, username);
    }

    public static MovementSearchGroup toMovementSearchGroup(final MovementFilterGroup filterGroup) {
        final MovementSearchGroup group = new MovementSearchGroup();
        group.setDynamic(getBoolean(filterGroup.getFiltgrpDynamic()));
        group.setName(filterGroup.getName());
        group.setUser(filterGroup.getUser());

        if (filterGroup.getId() != null) {
            group.setId(BigInteger.valueOf(filterGroup.getId().longValue()));
        }

        for (final MovementFilter filter : filterGroup.getFilters()) {
            group.getSearchFields().add(toGroupListCriteria(filter));
        }

        return group;
    }

    private static MovementFilter toFilterEntity(final MovementFilterGroup parent, final GroupListCriteria searchField, final String username) {
        final MovementFilter filter = new MovementFilter();
        //filter.setId(parent.getId());
        filter.setField(searchField.getKey());
        filter.setValue(searchField.getValue());
        filter.setUpdated(DateUtil.nowUTC());
        filter.setUpdatedBy(username);
        filter.setMovementFilterType(searchField.getType());
        filter.setFilterGroup(parent);
        return filter;
    }

    private static GroupListCriteria toGroupListCriteria(final MovementFilter filter) {
        final GroupListCriteria searchField = new GroupListCriteria();
        searchField.setKey(filter.getField());
        searchField.setValue(filter.getValue());
        searchField.setType(filter.getMovementFilterType());
        return searchField;
    }

    //ToDo: Setter in class ListCriteria called setValue() allows setting an arbitrary String when requiring the
    //ToDo: following specific MovementTypeType enum values only: POS, ENT, EXI or MAN.
    //ToDo: Method toListCriteria() is the only usage of this setter but toListCriteria() is itself never used.
    //ToDo: Needs decision on if toListCriteria() method and the setter method ListCriteria.setValue() should be removed.
    private static ListCriteria toListCriteria(final MovementFilter filter) {
        final ListCriteria searchField = new ListCriteria();
        searchField.setKey(SearchKey.fromValue(filter.getField()));
        searchField.setValue(filter.getValue());
        return searchField;
    }

    private static boolean getBoolean(final String value) {
        if (value != null && !value.isEmpty()) {
            return value.equalsIgnoreCase(UvmsConstants.TRUE);
        }

        return false;
    }

    private static void validateMovementSearchGroup(final MovementSearchGroup searchGroup) throws MovementSearchMapperException {
        if (searchGroup.getName() == null) {
            throw new MovementSearchMapperException("MovementSearchGroupName cannot be null");
        }
    }
}