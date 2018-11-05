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

import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.uvms.movement.service.clients.SpatialClient;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.BatchSpatialEnrichmentRS;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRS;

@Stateless
public class SpatialService {

    private static final Logger LOG = LoggerFactory.getLogger(SpatialService.class);
    
    @Inject
    private SpatialClient spatialClient;

    
    public Movement enrichMovementWithSpatialData(Movement movement) throws MovementServiceException {
        try {
            SpatialEnrichmentRS enrichment = spatialClient.getEnrichment(movement.getLocation());
            MovementMapper.enrichMovement(movement, enrichment);
            return movement;
        } catch (Exception ex) {
            throw new MovementServiceException("FAILED TO GET DATA FROM SPATIAL ", ex, ErrorCode.DATA_RETRIEVING_ERROR);
        }
    }

    public List<Movement> enrichMovementBatchWithSpatialData(List<Movement> movements) throws MovementServiceException {
        List<Point> locations = movements.stream().map(Movement::getLocation).collect(Collectors.toList());
        try {
            BatchSpatialEnrichmentRS enrichment = spatialClient.getBatchEnrichment(locations);
            MovementMapper.enrichAndMapToMovementTypes(movements, enrichment);

            return movements;
        } catch (Exception ex) {
            throw new MovementServiceException("FAILED TO GET DATA FROM SPATIAL ", ex, ErrorCode.DATA_RETRIEVING_ERROR);
        }
    }
}
