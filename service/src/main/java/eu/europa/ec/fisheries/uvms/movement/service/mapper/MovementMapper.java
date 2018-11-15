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

import java.util.ArrayList;
import java.util.Date;  //leave be for now
import java.util.List;

import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MicroMovement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movementmetadata;
import eu.europa.ec.fisheries.uvms.movement.service.entity.temp.TempMovement;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.Area;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaExtendedIdentifierType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.BatchSpatialEnrichmentRS;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.Location;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.LocationType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRS;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRSListElement;


public class MovementMapper {

    private static final Logger LOG = LoggerFactory.getLogger(MovementMapper.class);

    private MovementMapper() {}
    
    public static MovementType mapMovementBaseTypeToMovementType(MovementBaseType movementBaseType) {
        MovementType movementType = new MovementType();
        movementType.setGuid(movementBaseType.getGuid());
        movementType.setConnectId(movementBaseType.getConnectId());
        movementType.setAssetId(movementBaseType.getAssetId());
        movementType.setPosition(movementBaseType.getPosition());
        movementType.setPositionTime(movementBaseType.getPositionTime());
        movementType.setStatus(movementBaseType.getStatus());
        movementType.setReportedSpeed(movementBaseType.getReportedSpeed());
        movementType.setReportedCourse(movementBaseType.getReportedCourse());
        movementType.setMovementType(movementBaseType.getMovementType());
        movementType.setSource(movementBaseType.getSource());
        movementType.setActivity(movementBaseType.getActivity());
        movementType.setTripNumber(movementBaseType.getTripNumber());
        movementType.setInternalReferenceNumber(movementBaseType.getInternalReferenceNumber());
        movementType.setProcessed(movementBaseType.isProcessed());
        movementType.setDuplicate(movementBaseType.isDuplicate());
        movementType.setDuplicates(movementBaseType.getDuplicates());
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

    public static Movement enrichMovement(Movement movement, SpatialEnrichmentRS enrichment) {

        Movementmetadata metadata = new Movementmetadata();

        if (enrichment.getClosestLocations() != null) {
            enrichWithPortData(enrichment.getClosestLocations().getClosestLocations(), LocationType.PORT, metadata);
        } else {
            LOG.error("NO CLOSEST LOCATIONS FOUND IN RESPONSE FROM SPATIAL ");
        }

        if (enrichment.getClosestAreas() != null) {
            enrichWithCountryData(enrichment.getClosestAreas().getClosestAreas(), AreaType.COUNTRY, metadata);
        } else {
            LOG.error("NO CLOSEST AREAS FOUND IN RESPONSE FROM SPATIAL ");
        }

        metadata.setMovemetUpdattim(DateUtil.nowUTC());
        metadata.setMovemetUpuser("UVMS");
        movement.setMetadata(metadata);
        return movement;
    }

    public static List<Movement> enrichAndMapToMovementTypes(List<Movement> movements, BatchSpatialEnrichmentRS enrichment) {
        int index = 0;
        List<SpatialEnrichmentRSListElement> enrichmentRespLists = enrichment.getEnrichmentRespLists();
        List<Movement> enrichedList = new ArrayList<>();
        for (Movement movement : movements) {
            SpatialEnrichmentRSListElement enrichmentRSListElement = enrichmentRespLists.get(index);
            Movementmetadata metadata = new Movementmetadata();
            if (enrichmentRSListElement.getClosestLocations() != null) {
                enrichWithPortData(enrichmentRSListElement.getClosestLocations().getClosestLocations(), LocationType.PORT, metadata);
            } else {
                LOG.error("NO CLOSEST LOCATIONS FOUND IN RESPONSE FROM SPATIAL ");
            }
            if (enrichmentRSListElement.getClosestAreas() != null) {
                enrichWithCountryData(enrichmentRSListElement.getClosestAreas().getClosestAreas(), AreaType.COUNTRY, metadata);
            } else {
                LOG.error("NO CLOSEST AREAS FOUND IN RESPONSE FROM SPATIAL ");
            }
            index++;
            
            metadata.setMovemetUpdattim(DateUtil.nowUTC());
            metadata.setMovemetUpuser("UVMS");
            movement.setMetadata(metadata);
            enrichedList.add(movement);
        }
        return enrichedList;
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

    private static void enrichWithPortData(List<Location> locations, LocationType type, Movementmetadata meta) {
        for (Location location : locations) {
            if (location.getLocationType().equals(type)) {
                meta.setClosestPortRemoteId(location.getId());
                meta.setClosestPortDistance(location.getDistance());
                meta.setClosestPortCode(location.getCode());
                meta.setClosestPortName(location.getName());
            }
        }
    }

    private static void enrichWithCountryData(List<Area> locations, AreaType areaType, Movementmetadata meta) {
        for (Area location : locations) {
            if (location.getAreaType() != null &&
                    location.getAreaType().equals(areaType)) {
                meta.setClosestCountryRemoteId(location.getId());
                meta.setClosestCountryDistance(location.getDistance());
                meta.setClosestCountryCode(location.getCode());
                meta.setClosestCountryName(location.getName());
            }
        }
    }

    public static SetReportMovementType mapToSetReportMovementType(TempMovement movement) {

        SetReportMovementType report = new SetReportMovementType();
        report.setPluginName("ManualMovement");
        report.setPluginType(PluginType.MANUAL);
        report.setTimestamp(Date.from(DateUtil.nowUTC()));

        eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementBaseType exchangeMovementBaseType =
                new eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementBaseType();
        eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId exchangeAssetId =
                new eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId();

        exchangeAssetId.setAssetType(AssetType.VESSEL);

        AssetIdList cfr = new AssetIdList();
        cfr.setIdType(AssetIdType.CFR);
        cfr.setValue(movement.getCfr());

        AssetIdList ircs = new AssetIdList();
        ircs.setIdType(AssetIdType.IRCS);
        ircs.setValue(movement.getIrcs());

        exchangeAssetId.getAssetIdList().add(cfr);
        exchangeAssetId.getAssetIdList().add(ircs);

        exchangeMovementBaseType.setAssetId(exchangeAssetId);

        eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementPoint exchangeMovementPoint = new MovementPoint();
        if (movement.getLatitude() != null)
            exchangeMovementPoint.setLatitude(movement.getLatitude());
        if (movement.getLongitude() != null)
            exchangeMovementPoint.setLongitude(movement.getLongitude());
        exchangeMovementBaseType.setPosition(exchangeMovementPoint);

        exchangeMovementBaseType.setReportedCourse(movement.getCourse());
        exchangeMovementBaseType.setReportedSpeed(movement.getSpeed());
        exchangeMovementBaseType.setStatus(movement.getStatus());

        exchangeMovementBaseType.setAssetName(movement.getName());
        exchangeMovementBaseType.setFlagState(movement.getFlag());
        exchangeMovementBaseType.setExternalMarking(movement.getExternalMarkings());
        exchangeMovementBaseType.setMovementType(MovementTypeType.MAN);
        exchangeMovementBaseType.setSource(MovementSourceType.MANUAL);

        try {
            Date date = Date.from(movement.getTimestamp());
            exchangeMovementBaseType.setPositionTime(date);
        } catch (Exception e) {
            LOG.error("Error when parsing position date for temp movement continuing ");
        }
        exchangeMovementBaseType.setComChannelType(MovementComChannelType.MANUAL);
        report.setMovement(exchangeMovementBaseType);
        return report;
    }


    public static MicroMovementDto mapToMicroMovement(MicroMovement mm) {
        MicroMovementDto dto = new MicroMovementDto();
        dto.setAsset(mm.getMovementConnect().getValue().toString());
        dto.setGuid(mm.getGuid());
        dto.setHeading(mm.getHeading());
        eu.europa.ec.fisheries.schema.movement.v1.MovementPoint mp = new eu.europa.ec.fisheries.schema.movement.v1.MovementPoint();
        mp.setLatitude(mm.getLocation().getY());
        mp.setLongitude(mm.getLocation().getX());
        dto.setLocation(mp);
        dto.setTimestamp(mm.getTimestamp());
        dto.setSpeed(mm.getSpeed());
        return dto;
    }
}
