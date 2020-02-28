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
package eu.europa.ec.fisheries.uvms.movement.service.mapper.search;

import org.locationtech.jts.geom.Geometry;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;

import java.time.Instant;

/**
 **/
public enum SearchField implements SearchFieldType {

    /**
     * The ID of the movement
     */
    MOVEMENT_ID("id", SearchTables.MOVEMENT, String.class),
    /**
     * The id of the Track
     */
    TRACK_ID("id", SearchTables.TRACK, Integer.class),
    /**
     * The id of the Segment
     */
    SEGMENT_ID("id", SearchTables.FROM_SEGMENT, Integer.class),
    /**
     * The value of the connected Object in MovementConnect
     */
    CONNECT_ID("movementConnect", SearchTables.MOVEMENT, String.class),
    /**
     * The Type of movement
     */
    MOVMENT_TYPE("movementType", SearchTables.MOVEMENT, MovementTypeType.class),
    /**
     * The date the movementment was reported
     */
    DATE("timestamp", SearchTables.MOVEMENT, Instant.class),
    /**
     * The location ( POINT ) ot the movement
     */
    AREA("location", SearchTables.MOVEMENT, Geometry.class),
    /**
     * The reported speed of the movement
     */
    MOVEMENT_SPEED("speed", SearchTables.MOVEMENT, Double.class),
    /**
     * The calculated speed of the segment ( avg speed )
     */
    SEGMENT_SPEED("speedOverGround", SearchTables.FROM_SEGMENT, Double.class),
    /**
     * The calculated speed of the Track ( avg speed )
     */
    TRACK_SPEED("averageSpeed", SearchTables.TRACK, Double.class),
    /**
     * The calculated length ( distance ) of the Track
     */
    TRACK_LENGTH("distance", SearchTables.TRACK, Double.class),
    /**
     * The calculated duration for the track at sea. Total time at sea excludes
     * ENT and EXI for the track
     */
    TRACK_TOTAL_TIME_AT_SEA("totalTimeAtSea", SearchTables.TRACK, Double.class),
    /**
     * The calculated length ( distance ) of the Segmetn
     */
    SEGMENT_LENGTH("distance", SearchTables.MOVEMENT, Double.class),
    /**
     * The calculated duration ( time ) of the Track
     */
    TRACK_DURATION("duration", SearchTables.TRACK, Double.class),
    /**
     * The calculated duration ( time ) of the Segment
     */
    SEGMENT_DURATION("duration", SearchTables.FROM_SEGMENT, Double.class),
    /**
     * The Reported status of the movement
     */
    STATUS("status", SearchTables.MOVEMENT, String.class),
    /**
     * The Source where the movement report originates from
     */
    SOURCE("movementSource", SearchTables.MOVEMENT, MovementSourceType.class),
    /**
     * The calculated category of the Segment
     */
    CATEGORY("segmentCategory", SearchTables.TO_SEGMENT, SegmentCategoryType.class);

    private final String fieldName;
    private final SearchTables searchTables;
    private final Class clazz;

    private SearchField(String fieldName, SearchTables searchTables, Class clazz) {
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