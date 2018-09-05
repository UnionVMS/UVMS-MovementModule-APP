package eu.europa.ec.fisheries.uvms.movement.service;

import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

import javax.ejb.Local;
import java.util.List;

/**
 * Created by thofan on 2017-03-03.
 */
@Local
public interface SpatialService {
    MovementType enrichMovementWithSpatialData(MovementBaseType movement) throws MovementServiceException;

    List<MovementType> enrichMovementBatchWithSpatialData(List<MovementBaseType> movements) throws MovementServiceException;
}
