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
package eu.europa.ec.fisheries.uvms.movement.service.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.movement.message.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.movement.message.consumer.MessageConsumer;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.SpatialService;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.spatial.model.exception.SpatialModelMapperException;
import eu.europa.ec.fisheries.uvms.spatial.model.mapper.SpatialModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.spatial.model.mapper.SpatialModuleResponseMapper;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaExtendedIdentifierType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.BatchSpatialEnrichmentRS;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.LocationType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.PointType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRQListElement;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRS;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.UnitType;

@LocalBean
@Stateless
public class MovementSpatialServiceBean implements SpatialService {

    private static final Logger LOG = LoggerFactory.getLogger(MovementSpatialServiceBean.class);

    @EJB
    private MessageConsumer consumer;

    @EJB
    private MessageProducer producer;
    
    @Inject
    private AreaDao areaDao;

    @Override
    public Movement enrichMovementWithSpatialData(Movement movement) throws MovementServiceException {
        try {
            LOG.debug("Enrich movement with spatial data envoked in MovementSpatialServiceBean");
            PointType point = new PointType();
            point.setCrs(4326); //this magical int is the World Geodetic System 1984, aka EPSG:4326. See: https://en.wikipedia.org/wiki/World_Geodetic_System or http://spatialreference.org/ref/epsg/wgs-84/
            point.setLatitude(movement.getLocation().getY());
            point.setLongitude(movement.getLocation().getX());
            List<LocationType> locationTypes = Arrays.asList(LocationType.PORT);
            List<eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaType> areaTypes = Arrays.asList(eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaType.COUNTRY);
            String spatialRequest = SpatialModuleRequestMapper.mapToCreateSpatialEnrichmentRequest(point, UnitType.NAUTICAL_MILES, locationTypes, areaTypes);
            String spatialMessageId = producer.sendModuleMessage(spatialRequest, ModuleQueue.SPATIAL);
            TextMessage spatialResponse = consumer.getMessage(spatialMessageId, TextMessage.class);
            LOG.debug("Got response from Spatial " + spatialResponse.getText());
            SpatialEnrichmentRS enrichment = SpatialModuleResponseMapper.mapToSpatialEnrichmentRSFromResponse(spatialResponse, spatialMessageId);
            Movement enrichedMovement = MovementMapper.enrichMovement(movement, enrichment);
            mapAreas(enrichedMovement, enrichment);
            return enrichedMovement;
        } catch (JMSException | SpatialModelMapperException | MovementMessageException | MessageException ex) {
            throw new MovementServiceException("FAILED TO GET DATA FROM SPATIAL ", ex, ErrorCode.DATA_RETRIEVING_ERROR);
        }
    }

    @Override
    public List<Movement> enrichMovementBatchWithSpatialData(List<Movement> movements) throws MovementServiceException {
        List<SpatialEnrichmentRQListElement> batchReqLements = new ArrayList<>();
        for (Movement movement : movements) {
            PointType point = new PointType();
            point.setCrs(4326);
            point.setLatitude(movement.getLocation().getY());
            point.setLongitude(movement.getLocation().getX());
            List<LocationType> locationTypes = Arrays.asList(LocationType.PORT);
            List<eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaType> areaTypes = Arrays.asList(eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaType.COUNTRY);
            SpatialEnrichmentRQListElement spatialEnrichmentRQListElement = SpatialModuleRequestMapper.mapToCreateSpatialEnrichmentRQElement(point, UnitType.NAUTICAL_MILES, locationTypes, areaTypes);
            batchReqLements.add(spatialEnrichmentRQListElement);
        }
        try {
            LOG.debug("Enrich movement Batch with spatial data envoked in MovementSpatialServiceBean");
            String spatialRequest = SpatialModuleRequestMapper.mapToCreateBatchSpatialEnrichmentRequest(batchReqLements);
            String spatialMessageId = producer.sendModuleMessage(spatialRequest, ModuleQueue.SPATIAL);
            TextMessage spatialJmsMessageRS = consumer.getMessage(spatialMessageId, TextMessage.class);
            LOG.debug("Got response from Spatial " + spatialJmsMessageRS.getText());
            BatchSpatialEnrichmentRS enrichment = SpatialModuleResponseMapper.mapToBatchSpatialEnrichmentRSFromResponse(spatialJmsMessageRS, spatialMessageId);
            List<Movement> enrichedMovements = MovementMapper.enrichAndMapToMovementTypes(movements, enrichment);
            // TODO mapAreas
            return enrichedMovements;
        } catch (JMSException | SpatialModelMapperException | MovementMessageException | MessageException ex) {
            throw new MovementServiceException("FAILED TO GET DATA FROM SPATIAL ", ex, ErrorCode.DATA_RETRIEVING_ERROR);
        }
    }
    
    // TODO check this
    private void mapAreas(Movement movement, SpatialEnrichmentRS spatialData) {
        if (spatialData.getAreasByLocation() != null) {
            for (AreaExtendedIdentifierType area : spatialData.getAreasByLocation().getAreas()) {
                Movementarea movementArea = new Movementarea();
                Area areaEntity = areaDao.getAreaByRemoteIdAndCode(area.getCode(), area.getId());

                if (areaEntity != null) {
                    String wrkRemoteId = areaEntity.getRemoteId();
                    if(wrkRemoteId != null) {
                        if (!wrkRemoteId.equals(area.getId())) {
                            areaEntity.setRemoteId(area.getId());
                        }
                    }
                    movementArea.setMovareaAreaId(areaEntity);
                } else {
                    AreaType areaType = getAreaType(area);
                    Area newArea = maptoArea(area, areaType);
                    try {
                        areaDao.createMovementArea(newArea);
                        movementArea.setMovareaAreaId(newArea);
                    } catch (ConstraintViolationException e) {
                        // Area was created while we tried to create it.
                        LOG.info("Area \"{}\"was created while we tried to create it. Trying to fetch it.", area.getCode());
                        areaEntity = areaDao.getAreaByRemoteIdAndCode(area.getCode(), area.getId());
                        if (areaEntity != null) {
                            if (!areaEntity.getRemoteId().equals(area.getId())) {
                                areaEntity.setRemoteId(area.getId());
                            }
                            movementArea.setMovareaAreaId(areaEntity);
                        }
                    }
                }
                movementArea.setMovareaMoveId(movement);
                movementArea.setMovareaUpdattim(DateUtil.nowUTC());
                movementArea.setMovareaUpuser("UVMS");
                if (movement.getMovementareaList() == null) {
                    movement.setMovementareaList(new ArrayList<>());
                }
                movement.getMovementareaList().add(movementArea);
            }
        } else {
            LOG.error("NO AREAS FOUND IN RESPONSE FROM SPATIAL ");
        }
    }
    
    private AreaType getAreaType(AreaExtendedIdentifierType areaIdentifierType) {
        AreaType areaType = areaDao.getAreaTypeByCode(areaIdentifierType.getAreaType().value());
        if (areaType == null) {
            AreaType newAreaType = mapToAreaType(areaIdentifierType);
            return areaDao.createAreaType(newAreaType);
        } else {
            return areaType;
        }
    }
    
    private AreaType mapToAreaType(AreaExtendedIdentifierType areaIdentifierType) {
        AreaType newAreaType = new AreaType();
        newAreaType.setName(areaIdentifierType.getAreaType().value());
        newAreaType.setUpdatedUser("UVMS");
        newAreaType.setUpdatedTime(DateUtil.nowUTC());
        return newAreaType;
    }
    
    public static Area maptoArea(AreaExtendedIdentifierType area, AreaType areaType) {
        Area newArea = new Area();
        newArea.setAreaCode(area.getCode());
        newArea.setAreaName(area.getName());
        newArea.setRemoteId(area.getId());
        newArea.setAreaType(areaType);
        newArea.setAreaUpuser("UVMS");
        newArea.setAreaUpdattim(DateUtil.nowUTC());
        return newArea;
    }
}
