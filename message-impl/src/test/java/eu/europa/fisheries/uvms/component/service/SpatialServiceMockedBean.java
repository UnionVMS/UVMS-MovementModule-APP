package eu.europa.fisheries.uvms.component.service;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementComChannelType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.service.SpatialService;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

import java.util.ArrayList;
import java.util.List;


@LocalBean
@Stateless
public class SpatialServiceMockedBean implements SpatialService {

    public MovementType enrichMovementWithSpatialData(MovementBaseType movement) {
      return  createSmallestPossibleMovementType(movement);
    }

    @Override
    public List<MovementType> enrichMovementBatchWithSpatialData(List<MovementBaseType> movements) throws MovementServiceException {
        ArrayList<MovementType> movementTypes = new ArrayList<>();
        movementTypes.add(createSmallestPossibleMovementType(movements.get(0)));
        return movementTypes;
    }

    private MovementType createSmallestPossibleMovementType(MovementBaseType movement){
        MovementType movementType  = new MovementType();
        movementType.setPositionTime(movement.getPositionTime());
        movementType.setPosition(movement.getPosition());
        movementType.setComChannelType(MovementComChannelType.MANUAL);
        movementType.setTripNumber(movement.getTripNumber());
        movementType.setMovementType(movement.getMovementType());
        movementType.setConnectId(movement.getConnectId());
        movementType.setMetaData(getMappedMovementHelper(1));
        movementType.setSource(MovementSourceType.NAF);
        return movementType;
    }

    public  MovementMetaData getMappedMovementHelper(int numberOfAreas) {
        MovementMetaData metaData = new MovementMetaData();
        for (int i = 0; i < numberOfAreas; i++) {
            metaData.getAreas().add(getMovementMetadataTypeHelper("AREA" + i));
        }
        return metaData;
    }

    public  MovementMetaDataAreaType getMovementMetadataTypeHelper(String areaCode) {
        MovementMetaDataAreaType area = new MovementMetaDataAreaType();
        area.setCode(areaCode);
        area.setName(areaCode);
        area.setAreaType(areaCode);
        return area;
    }

}