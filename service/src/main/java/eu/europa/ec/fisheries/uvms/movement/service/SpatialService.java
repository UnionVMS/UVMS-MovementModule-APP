package eu.europa.ec.fisheries.uvms.movement.service;

import java.util.List;
import javax.ejb.Local;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

/**
 * Created by thofan on 2017-03-03.
 */
@Local
public interface SpatialService {
    Movement enrichMovementWithSpatialData(Movement movement) throws MovementServiceException;

    List<Movement> enrichMovementBatchWithSpatialData(List<Movement> movements) throws MovementServiceException;
}
