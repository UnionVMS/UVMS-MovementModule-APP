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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;  //leave be for now
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.schema.movement.asset.v1.VesselIdentifyingProperties;
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
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
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
import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.FLUXPartyType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.FLUXReportDocumentType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselCountryType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselGeographicalCoordinateType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselPositionEventType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.VesselTransportMeansType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.CodeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.DateTimeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.IDType;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.MeasureType;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;


public class MovementMapper {

    private static final String PURPOSE_CODE = "9";
    private static final String FLUX_GP_PARTY = "FLUX_GP_PARTY";
    public static final String POS = "POS";
    private static final Logger LOG = LoggerFactory.getLogger(MovementMapper.class);

    enum FLUXVesselIDType {
        CFR,
        EXT_MARK,
        IRCS
    }

    private MovementMapper() {}
    
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


    public static MicroMovementDto mapToMicroMovement(MicroMovement mm) {
        MicroMovementDto dto = new MicroMovementDto();
        dto.setAsset(mm.getMovementConnect().getValue());
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

    public static FLUXVesselPositionMessage mapToFLUXVesselPositionMessage(TempMovement movement, String guid){
        FLUXVesselPositionMessage fluxVesselPositionMessage = new FLUXVesselPositionMessage();
        fluxVesselPositionMessage.setFLUXReportDocument(mapToMovementReportDocument(guid));
        fluxVesselPositionMessage.setVesselTransportMeans(mapToVesselTransportMeans(movement));
        return fluxVesselPositionMessage;
    }

    public static FLUXVesselPositionMessage mapToFLUXVesselPositionMessage(String guid, VesselIdentifyingProperties vesselIdentifyingProperties, List<Movement> movements){
        FLUXVesselPositionMessage fluxVesselPositionMessage = new FLUXVesselPositionMessage();
        fluxVesselPositionMessage.setFLUXReportDocument(mapToReportDocument(guid));
        fluxVesselPositionMessage.setVesselTransportMeans(mapToVesselTransportMeans(vesselIdentifyingProperties, movements));

        return fluxVesselPositionMessage;
    }

    private static VesselTransportMeansType mapToVesselTransportMeans(TempMovement movement) {
        VesselTransportMeansType retVal = new VesselTransportMeansType();
        addId(retVal.getIDS(), FLUXVesselIDType.IRCS.name(), movement.getIrcs());
        addId(retVal.getIDS(), FLUXVesselIDType.EXT_MARK.name(), movement.getExternalMarkings());
        addId(retVal.getIDS(), FLUXVesselIDType.CFR.name(), movement.getCfr());
        retVal.setRegistrationVesselCountry(mapToVesselCountry(movement.getFlag()));
        retVal.getSpecifiedVesselPositionEvents().add(mapToVesselPosition(movement));
        return retVal;
    }

    private static VesselTransportMeansType mapToVesselTransportMeans(VesselIdentifyingProperties vesselIdentifyingProperties, List<Movement> movements) {
        VesselTransportMeansType retVal = new VesselTransportMeansType();
        addId(retVal.getIDS(), FLUXVesselIDType.IRCS.name(), vesselIdentifyingProperties.getIrcs());
        addId(retVal.getIDS(), FLUXVesselIDType.EXT_MARK.name(), vesselIdentifyingProperties.getExtMarking());
        addId(retVal.getIDS(), FLUXVesselIDType.CFR.name(), vesselIdentifyingProperties.getCfr());
        retVal.setRegistrationVesselCountry(mapToVesselCountry(vesselIdentifyingProperties.getFlagState()));

        List<VesselPositionEventType> vesselPositionEventTypes = movements.stream()
                .map(MovementMapper::mapToVesselPosition)
                .collect(Collectors.toList());
        retVal.getSpecifiedVesselPositionEvents().addAll(vesselPositionEventTypes);

        return retVal;
    }

    private static void addId(List<IDType> ids, String schemeId, String value) {
        if (value != null) {
            ids.add(mapToIdType(schemeId, value));
        }
    }

    private static IDType mapToIdType(String schemeId, String value) {
        IDType idType = new IDType();
        idType.setSchemeID(schemeId);
        idType.setValue(value);
        return idType;
    }

    private static FLUXReportDocumentType mapToReportDocument( String guid) {
        FLUXReportDocumentType doc = new FLUXReportDocumentType();
        doc.getIDS().add(mapToIdType(guid));
        doc.setCreationDateTime(mapToNowDateTime());
        doc.setPurposeCode(mapToCodeType(PURPOSE_CODE));
        doc.setOwnerFLUXParty(mapToFluxPartyType(FLUX_GP_PARTY));
        return doc;
    }

    private static FLUXReportDocumentType mapToMovementReportDocument( String guid) {
        FLUXReportDocumentType doc = new FLUXReportDocumentType();
        doc.getIDS().add(mapToIdType(guid));
        doc.setCreationDateTime(mapToNowDateTime());
        doc.setPurposeCode(mapToCodeType(PURPOSE_CODE));
        doc.setOwnerFLUXParty( mapToFluxOwnerPartyType(FLUX_GP_PARTY));
        return doc;
    }

    private static IDType mapToIdType(String value) {
        IDType id = new IDType();
        id.setValue(value);
        return id;
    }

    private static FLUXPartyType mapToFluxOwnerPartyType(String ad) {
        FLUXPartyType partyType = new FLUXPartyType();
        partyType.getIDS().add(mapToIdType(ad,"SRC"));
        return partyType;
    }

    private static DateTimeType mapToNowDateTime() {
        return mapToDateTime(new Date());
    }

    private static DateTimeType mapToDateTime(Date date) {
        try {
            DateTimeType dateTime = new DateTimeType();
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            dateTime.setDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar));
            return dateTime;
        } catch (DatatypeConfigurationException ex) {
            return new DateTimeType();
        }
    }

    private static CodeType mapToCodeType(String value) {
        CodeType codeType = new CodeType();
        codeType.setValue(value);
        return codeType;
    }

    private static FLUXPartyType mapToFluxPartyType(String ad) {
        FLUXPartyType partyType = new FLUXPartyType();
        partyType.getIDS().add(mapToIdType(ad));
        return partyType;
    }

    private static VesselCountryType mapToVesselCountry(String countryCode) {
        VesselCountryType vesselCountry = new VesselCountryType();
        vesselCountry.setID(mapToIdType(countryCode));
        return vesselCountry;
    }

    private static MeasureType mapToMeasureType(Double measuredValue) {
        MeasureType measureType = new MeasureType();
        measureType.setValue(measuredValue == null ? null: BigDecimal.valueOf(measuredValue));
        return measureType;
    }

    private static VesselPositionEventType mapToVesselPosition(TempMovement movement) {
        VesselPositionEventType position = new VesselPositionEventType();
        position.setObtainedOccurrenceDateTime(mapToDateTime(Date.from(movement.getTimestamp())));
        position.setCourseValueMeasure(mapToMeasureType(movement.getCourse()));
        position.setSpeedValueMeasure(mapToMeasureType(movement.getSpeed()));
        position.setTypeCode(mapToCodeType(POS));

        VesselGeographicalCoordinateType geoType = new VesselGeographicalCoordinateType();
        geoType.setLatitudeMeasure(mapToMeasureType(movement.getLatitude()));
        geoType.setLongitudeMeasure(mapToMeasureType(movement.getLongitude()));
        position.setSpecifiedVesselGeographicalCoordinate(geoType);
        return position;
    }

    private static VesselPositionEventType mapToVesselPosition(Movement movement) {
        VesselPositionEventType position = new VesselPositionEventType();
        position.setObtainedOccurrenceDateTime(mapToDateTime(Date.from(movement.getTimestamp())));
        position.setCourseValueMeasure(mapToMeasureType(movement.getHeading()));
        position.setSpeedValueMeasure(mapToMeasureType(movement.getSpeed()));
        position.setTypeCode(mapToCodeType(POS));

        VesselGeographicalCoordinateType geoType = new VesselGeographicalCoordinateType();
        geoType.setLatitudeMeasure(mapToMeasureType(movement.getLocation().getX()));
        geoType.setLongitudeMeasure(mapToMeasureType(movement.getLocation().getY()));
        position.setSpecifiedVesselGeographicalCoordinate(geoType);
        return position;
    }

}
