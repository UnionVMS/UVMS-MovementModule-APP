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

import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdList;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.*;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.service.dto.ManualMovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
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
    
    public static List<MovementDto> mapToMovementDtoList(List<MovementType> movmements) {
        List<MovementDto> mappedMovements = new ArrayList<>();
        for (MovementType mappedMovement : movmements) {
            mappedMovements.add(mapTomovementDto(mappedMovement));
        }
        return mappedMovements;
    }

    private static MovementDto mapTomovementDto(MovementType movement) {
        MovementDto dto = new MovementDto();
        dto.setCalculatedSpeed(movement.getCalculatedSpeed());
        dto.setCourse(movement.getCalculatedCourse());

        if (movement.getPosition() != null) {
            dto.setLatitude(movement.getPosition().getLatitude());
            dto.setLongitude(movement.getPosition().getLongitude());
        }

        dto.setMeasuredSpeed(movement.getReportedSpeed());
        dto.setMovementType(movement.getMovementType());
        dto.setSource(movement.getSource());
        dto.setStatus(movement.getStatus());
        dto.setTime(movement.getPositionTime());
        dto.setConnectId(movement.getConnectId());
        dto.setMovementGUID(movement.getGuid());
        return dto;
    }

    public static SetReportMovementType mapToSetReportMovementType(ManualMovementDto movement) {

        SetReportMovementType report = new SetReportMovementType();
        report.setPluginName("ManualMovement");
        report.setPluginType(PluginType.MANUAL);
        report.setTimestamp(Date.from(Instant.now()));

        eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementBaseType exchangeMovementBaseType =
                new eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementBaseType();
        eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId exchangeAssetId =
                new eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId();

        exchangeAssetId.setAssetType(AssetType.VESSEL);

        AssetIdList cfr = new AssetIdList();
        cfr.setIdType(AssetIdType.CFR);
        cfr.setValue(movement.getAsset().getCfr());

        AssetIdList ircs = new AssetIdList();
        ircs.setIdType(AssetIdType.IRCS);
        ircs.setValue(movement.getAsset().getIrcs());

        exchangeAssetId.getAssetIdList().add(cfr);
        exchangeAssetId.getAssetIdList().add(ircs);

        exchangeMovementBaseType.setAssetId(exchangeAssetId);

        eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint exchangeMovementPoint = new MovementPoint();
        if (movement.getMovement().getLocation().getLatitude() != null)
            exchangeMovementPoint.setLatitude(movement.getMovement().getLocation().getLatitude());
        if (movement.getMovement().getLocation().getLongitude() != null)
            exchangeMovementPoint.setLongitude(movement.getMovement().getLocation().getLongitude());
        exchangeMovementBaseType.setPosition(exchangeMovementPoint);

        exchangeMovementBaseType.setReportedCourse(movement.getMovement().getHeading());
        exchangeMovementBaseType.setReportedSpeed(movement.getMovement().getSpeed());
        exchangeMovementBaseType.setStatus("10");

        exchangeMovementBaseType.setMovementType(MovementTypeType.MAN);
        exchangeMovementBaseType.setSource(MovementSourceType.MANUAL);

        try {
            Date date = Date.from(movement.getMovement().getTimestamp());
            exchangeMovementBaseType.setPositionTime(date);
        } catch (Exception e) {
            LOG.error("Error when parsing position date for temp movement continuing ");
        }
        exchangeMovementBaseType.setComChannelType(MovementComChannelType.MANUAL);
        report.setMovement(exchangeMovementBaseType);
        return report;
    }
}
