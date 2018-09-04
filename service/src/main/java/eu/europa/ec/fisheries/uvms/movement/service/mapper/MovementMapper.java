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
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementComChannelType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.ClosestLocationType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementListResponseDto;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.Area;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaExtendedIdentifierType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.Location;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.LocationType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRS;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;  //leave be for now
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MovementMapper {

    final static Logger LOG = LoggerFactory.getLogger(MovementMapper.class);

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
        dto.setTime(OffsetDateTime.ofInstant(movement.getPositionTime().toInstant(), ZoneId.of("UTC")));
        dto.setConnectId(movement.getConnectId());
        dto.setMovementGUID(movement.getGuid());
        return dto;
    }

    public static MovementListResponseDto mapToMovementListDto(GetMovementListByQueryResponse response) {
        MovementListResponseDto dto = new MovementListResponseDto();
        dto.setCurrentPage(response.getCurrentPage());
        dto.setTotalNumberOfPages(response.getTotalNumberOfPages());

        List<MovementDto> movmements = new ArrayList<>();
        for (MovementBaseType movement : response.getMovement()) {
            movmements.add(mapTomovementDto((MovementType) movement));
        }

        dto.setMovement(movmements);
        return dto;
    }

    public static MovementType enrichAndMapToMovementType(MovementBaseType movement, SpatialEnrichmentRS enrichment) {

        MovementType movementType = Mapper.getInstance().getMapper().map(movement, MovementType.class);
        MovementMetaData movementMeta = new MovementMetaData();

        if (enrichment.getClosestLocations() != null) {
            enrichWithPortData(enrichment.getClosestLocations().getClosestLocations(), LocationType.PORT, movementMeta);
        } else {
            LOG.error("NO CLOSEST LOCATIONS FOUND IN RESPONSE FROM SPATIAL ");
        }

        if (enrichment.getClosestAreas() != null) {
            enrichWithCountryData(enrichment.getClosestAreas().getClosestAreas(), AreaType.COUNTRY, movementMeta);
        } else {
            LOG.error("NO CLOSEST AREAS FOUND IN RESPONSE FROM SPATIAL ");
        }

        if (enrichment.getAreasByLocation() != null) {
            movementMeta.getAreas().addAll(mapToAreas(enrichment.getAreasByLocation().getAreas()));
        } else {
            LOG.error("NO AREAS FOUND IN RESPONSE FROM SPATIAL ");
        }

        movementType.setMetaData(movementMeta);
        return movementType;
    }

    private static List<MovementMetaDataAreaType> mapToAreas(List<AreaExtendedIdentifierType> areas) {
        List<MovementMetaDataAreaType> mappedAreas = new ArrayList<>();
        for (AreaExtendedIdentifierType area : areas) {
            MovementMetaDataAreaType areaType = new MovementMetaDataAreaType();
            areaType.setRemoteId(area.getId());
            if (area.getAreaType() != null) {
                areaType.setAreaType(area.getAreaType().value());
            }
            areaType.setCode(area.getCode());
            areaType.setName(area.getName());
            mappedAreas.add(areaType);
        }
        return mappedAreas;
    }

    private static void enrichWithPortData(List<Location> locations, LocationType type, MovementMetaData meta) {
        for (Location location : locations) {
            if (location.getLocationType().equals(type)) {
                ClosestLocationType locationType = new ClosestLocationType();
                locationType.setRemoteId(location.getId());
                locationType.setDistance(location.getDistance());
                locationType.setCode(location.getCode());
                locationType.setName(location.getName());
                meta.setClosestPort(locationType);
            }
        }
    }

    private static void enrichWithCountryData(List<Area> locations, AreaType areaType, MovementMetaData meta) {
        for (Area location : locations) {
            if (location.getAreaType() != null &&
                    location.getAreaType().equals(areaType)) {
                ClosestLocationType locationType = new ClosestLocationType();
                locationType.setRemoteId(location.getId());
                locationType.setDistance(location.getDistance());
                locationType.setCode(location.getCode());
                locationType.setName(location.getName());
                meta.setClosestCountry(locationType);
            }
        }
    }

    public static SetReportMovementType mapToSetReportMovementType(TempMovementType movement) {

        SetReportMovementType report = new SetReportMovementType();
        report.setPluginName("ManualMovement");
        report.setPluginType(PluginType.MANUAL);
        report.setTimestamp(Date.from(DateUtil.nowUTC().toInstant()));

        eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementBaseType exchangeMovementBaseType = new eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementBaseType();
        eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId exchangeAssetId = new eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId();

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
        if (movement.getPosition() != null) {
            exchangeMovementPoint.setLatitude(movement.getPosition().getLatitude());
            exchangeMovementPoint.setLongitude(movement.getPosition().getLongitude());
        }
        exchangeMovementBaseType.setPosition(exchangeMovementPoint);

        exchangeMovementBaseType.setReportedCourse(movement.getCourse());
        exchangeMovementBaseType.setReportedSpeed(movement.getSpeed());
        exchangeMovementBaseType.setStatus(movement.getStatus());

        exchangeMovementBaseType.setAssetName(movement.getAsset().getName());
        exchangeMovementBaseType.setFlagState(movement.getAsset().getFlagState());
        exchangeMovementBaseType.setExternalMarking(movement.getAsset().getExtMarking());
        exchangeMovementBaseType.setMovementType(MovementTypeType.MAN);
        exchangeMovementBaseType.setSource(MovementSourceType.MANUAL);

        try {
            Date date = Date.from(DateUtil.parseToUTCDate(movement.getTime()).toInstant());
            exchangeMovementBaseType.setPositionTime(date);
        } catch (Exception e) {
            LOG.error("Error when parsing position date for temp movement continuing ");
        }

        exchangeMovementBaseType.setComChannelType(MovementComChannelType.MANUAL);

        report.setMovement(exchangeMovementBaseType);

        return report;

    }

}