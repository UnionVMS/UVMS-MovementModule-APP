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
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.clients.SpatialClient;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaExtendedIdentifierType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.BatchSpatialEnrichmentRS;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRS;

@Stateless
public class SpatialService {

    private static final Logger LOG = LoggerFactory.getLogger(SpatialService.class);
    
    @Inject
    private SpatialClient spatialClient;

    @Inject
    private AreaDao areaDao;
    
    public Movement enrichMovementWithSpatialData(Movement movement) throws MovementServiceException {
        try {
            SpatialEnrichmentRS enrichment = spatialClient.getEnrichment(movement.getLocation());
            Movement enrichedMovement = MovementMapper.enrichMovement(movement, enrichment);
            mapAreas(enrichedMovement, enrichment);
            return enrichedMovement;
        } catch (Exception ex) {
            throw new MovementServiceException("FAILED TO GET DATA FROM SPATIAL ", ex, ErrorCode.DATA_RETRIEVING_ERROR);
        }
    }

    public List<Movement> enrichMovementBatchWithSpatialData(List<Movement> movements) throws MovementServiceException {
        List<Point> locations = movements.stream().map(Movement::getLocation).collect(Collectors.toList());
        try {
            BatchSpatialEnrichmentRS enrichment = spatialClient.getBatchEnrichment(locations);
            List<Movement> enrichedMovements = MovementMapper.enrichAndMapToMovementTypes(movements, enrichment);
            // TODO mapAreas
            return enrichedMovements;
        } catch (Exception ex) {
            throw new MovementServiceException("FAILED TO GET DATA FROM SPATIAL ", ex, ErrorCode.DATA_RETRIEVING_ERROR);
        }
    }
    
    // TODO check this
    private void mapAreas(Movement movement, SpatialEnrichmentRS spatialData) {
        if (spatialData.getAreasByLocation() != null) {
            for (AreaExtendedIdentifierType area : spatialData.getAreasByLocation().getAreas()) {
                Movementarea movementArea = new Movementarea();
                Area areaEntity = areaDao.getAreaByCode(area.getCode());

                if (areaEntity != null) {
                    String wrkRemoteId = areaEntity.getRemoteId();
                    if (wrkRemoteId != null && !wrkRemoteId.equals(area.getId())) {
                        areaEntity.setRemoteId(area.getId());
                    }
                    movementArea.setMovareaAreaId(areaEntity);
                } else {
                    AreaType areaType = getAreaType(area.getAreaType().value());
                    Area newArea = mapToArea(area, areaType);
                    try {
                        areaDao.createMovementArea(newArea);
                        movementArea.setMovareaAreaId(newArea);
                    } catch (ConstraintViolationException e) {
                        // Area was created while we tried to create it.
                        LOG.info("Area \"{}\"was created while we tried to create it. Trying to fetch it.", area.getCode());
                        areaEntity = areaDao.getAreaByCode(area.getCode());
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
    
    private AreaType getAreaType(String areaTypeCode) {
        AreaType areaType = areaDao.getAreaTypeByCode(areaTypeCode);
        if (areaType == null) {
            AreaType newAreaType = mapToAreaType(areaTypeCode);
            return areaDao.createAreaType(newAreaType);
        } else {
            return areaType;
        }
    }
    
    private AreaType mapToAreaType(String areaTypeCode) {
        AreaType newAreaType = new AreaType();
        newAreaType.setName(areaTypeCode);
        newAreaType.setUpdatedUser("UVMS");
        newAreaType.setUpdatedTime(DateUtil.nowUTC());
        return newAreaType;
    }
    
    private Area mapToArea(AreaExtendedIdentifierType area, AreaType areaType) {
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
