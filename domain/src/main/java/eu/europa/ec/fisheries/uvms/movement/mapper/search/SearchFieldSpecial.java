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
package eu.europa.ec.fisheries.uvms.movement.mapper.search;

import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;

/**
 **/
public enum SearchFieldSpecial implements SearchFieldType {

    TO_SEGMENT_CATEGORY("segmentCategory", SearchTables.TO_SEGMENT, SegmentCategoryType.class),
    FROM_SEGMENT_CATEGORY("segmentCategory", SearchTables.FROM_SEGMENT, SegmentCategoryType.class),
    TO_SEGMENT_DURATION("duration", SearchTables.TO_SEGMENT, Double.class),
    FROM_SEGMENT_DURATION("duration", SearchTables.FROM_SEGMENT, Double.class),
    TO_SEGMENT_ID("id", SearchTables.TO_SEGMENT, Integer.class),
    FROM_SEGMENT_ID("id", SearchTables.FROM_SEGMENT, Integer.class),
    TO_SEGMENT_SPEED("speedOverGround", SearchTables.TO_SEGMENT, Integer.class),
    FROM_SEGMENT_SPEED("speedOverGround", SearchTables.FROM_SEGMENT, Integer.class),
    FROM_SEGMENT_LENGTH("distance", SearchTables.FROM_SEGMENT, Double.class),
    TO_SEGMENT_LENGTH("distance", SearchTables.TO_SEGMENT, Double.class);

    private final String fieldName;
    private final SearchTables searchTables;
    private final Class clazz;

    private SearchFieldSpecial(String fieldName, SearchTables searchTables, Class clazz) {
        this.fieldName = fieldName;
        this.searchTables = searchTables;
        this.clazz = clazz;
    }

    /**
     *
     * @return The fieldname in the Entity. Must be exact
     */
    @Override
    public String getFieldName() {
        return fieldName;
    }

    /**
     *
     * @return
     */
    @Override
    public SearchTables getSearchTables() {
        return searchTables;
    }

    /**
     *
     * @return
     */
    @Override
    public Class getClazz() {
        return clazz;
    }

}