/*
 * ﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
 * © European Union, 2015-2016. This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM
 * Suite is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with the IFDM Suite. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.movement.service.dto;

import java.time.Instant;
import java.util.UUID;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.model.constants.SatId;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;

public class MovementProjection extends MovementDto {

    public MovementProjection(UUID id, Geometry location, Float speed, Double calculatedSpeed, Float heading,
            UUID asset, String status, MovementSourceType source, MovementTypeType movementType, Instant timestamp,
            Instant lesReportTime, SatId sourceSatelliteId, Instant updated, String updatedBy,
            Short aisPositionAccuracy) {
        super();
        setId(id);
        MovementPoint point = new MovementPoint();
        point.setLongitude(((Point)location).getX());
        point.setLatitude(((Point)location).getY());
        setLocation(point);
        setSpeed(speed);
        setCalculatedSpeed(calculatedSpeed);
        setHeading(heading);
        setAsset(asset.toString());
        setStatus(status);
        setSource(source);
        setMovementType(movementType);
        setTimestamp(timestamp);
        setLesReportTime(lesReportTime);
        setSourceSatelliteId(sourceSatelliteId);
        setUpdated(updated);
        setUpdatedBy(updatedBy);
        setAisPositionAccuracy(aisPositionAccuracy);
    }
}
