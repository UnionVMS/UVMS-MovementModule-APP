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

/**
 **/
public enum SearchTables {

    MOVEMENT("m", "Movement"),
    MINIMAL_MOVEMENT("m", "MinimalMovement"),
    MOVEMENT_CONNECT("mc", "MovementConnect"),
    MOVEMENT_METADATA("mmd", "Movementmetadata"),
    FROM_SEGMENT("fromSeg", "Segment"),
    TO_SEGMENT("toSeg", "Segment"),
    TRACK("tra", "Track"),
    TEMP_TABLE("temp", "temp"),
    MOVEMENT_AREA("marea", "Movementarea"),
    MOVEMENT_AREA_TYPE("mareatype", "AreaType"),
    AREA("area", "Area"),
    ACTIVITY("act", "Activity");

    private final String tableNameAlias;
    private final String tableName;

    private SearchTables(final String tableNameAlias, final String tableName) {
        this.tableNameAlias = tableNameAlias;
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableAlias() {
        return tableNameAlias;
    }

}