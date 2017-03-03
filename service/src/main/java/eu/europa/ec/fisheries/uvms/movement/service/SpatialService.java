package eu.europa.ec.fisheries.uvms.movement.service;

import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

import javax.ejb.Local;

/**
 * Created by thofan on 2017-03-03.
 */
@Local
public interface SpatialService {
    MovementType enrichMovementWithSpatialData(MovementBaseType movement) throws MovementServiceException;
}
