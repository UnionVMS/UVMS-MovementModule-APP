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

import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementComChannelType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.ManualMovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MovementMapper {

    private static final Logger LOG = LoggerFactory.getLogger(MovementMapper.class);

    private MovementMapper() {}


    public static MovementType mapMovementToMovementTypeForSpatial(Movement movement){
        MovementType movementType = new MovementType();
        movementType.setPositionTime(Date.from(movement.getTimestamp()));
        eu.europa.ec.fisheries.schema.movement.v1.MovementPoint movementPoint = new eu.europa.ec.fisheries.schema.movement.v1.MovementPoint();
        movementPoint.setLatitude(movement.getLocation().getY());
        movementPoint.setLongitude(movement.getLocation().getX());
        movementType.setPosition(movementPoint);

        return movementType;
    }
    
    public static List<MovementDto> mapToMovementDtoList(List<Movement> movmements) {
        List<MovementDto> mappedMovements = new ArrayList<>();
        for (Movement mappedMovement : movmements) {
            mappedMovements.add(mapToMovementDto(mappedMovement));
        }
        return mappedMovements;
    }

    public static MovementDto mapToMovementDto(Movement movement) {
        if(movement == null){
            return null;
        }
        MovementDto dto = new MovementDto();
        dto.setHeading(movement.getHeading());

        if (movement.getLocation() != null) {
            Point point = movement.getLocation();
            eu.europa.ec.fisheries.schema.movement.v1.MovementPoint location = new eu.europa.ec.fisheries.schema.movement.v1.MovementPoint();
            location.setLatitude(point.getY());
            location.setLongitude(point.getX());
            dto.setLocation(location);
        }

        dto.setSpeed(movement.getSpeed());
        dto.setMovementType(movement.getMovementType());
        dto.setSource(movement.getSource());
        dto.setStatus(movement.getStatus());
        dto.setTimestamp(movement.getTimestamp());
        dto.setAsset(movement.getMovementConnect().getId().toString());
        dto.setId(movement.getId());

        dto.setAisPositionAccuracy(movement.getAisPositionAccuracy());
        dto.setLesReportTime(movement.getLesReportTime());
        dto.setSourceSatelliteId(movement.getSourceSatelliteId());
        dto.setUpdated(movement.getUpdated());
        dto.setUpdatedBy(movement.getUpdatedBy());
        return dto;
    }

    public static IncomingMovement manualMovementToIncomingMovement(ManualMovementDto movement, String username) {

        IncomingMovement report = new IncomingMovement();
        report.setPluginType(PluginType.MANUAL.value());
        report.setMovementType(MovementTypeType.MAN.value());
        report.setMovementSourceType(MovementSourceType.MANUAL.value());
        report.setComChannelType(MovementComChannelType.MANUAL.value());
        report.setDateReceived(Instant.now());

        report.setAssetCFR(movement.getAsset().getCfr());
        report.setAssetIRCS(movement.getAsset().getIrcs());

        report.setLatitude(movement.getMovement().getLocation().getLatitude());
        report.setLongitude(movement.getMovement().getLocation().getLongitude());

        report.setReportedCourse(movement.getMovement().getHeading() != null ? movement.getMovement().getHeading().doubleValue() : null);
        report.setReportedSpeed(movement.getMovement().getSpeed() != null ? movement.getMovement().getSpeed().doubleValue() : null);
        report.setStatus("10");

        report.setPositionTime(movement.getMovement().getTimestamp());
        report.setUpdatedBy(username);

        return report;
    }
}
