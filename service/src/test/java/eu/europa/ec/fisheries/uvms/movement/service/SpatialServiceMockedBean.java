package eu.europa.ec.fisheries.uvms.movement.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movementmetadata;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;


@LocalBean
@Stateless
public class SpatialServiceMockedBean  implements SpatialService {

    public static final String MESSAGE_PRODUCER_METHODS_FAIL = "MESSAGE_PRODUCER_METHODS_FAIL";

    @Inject
    private AreaDao areaDao;
    
    private void shouldIFail() throws MovementServiceException {
        String fail = System.getProperty(MESSAGE_PRODUCER_METHODS_FAIL, "false");
        if(!"false".equals(fail.toLowerCase())) {
            throw new MovementServiceException(ErrorCode.JMS_SENDING_ERROR);
        }
    }
    
    public Movement enrichMovementWithSpatialData(Movement movement) throws MovementServiceException {
        shouldIFail();
        movement.setMetadata(getMetadata());
        movement.setMovementareaList(getMovementAreas(1, movement));
        return movement;
    }

    @Override
    public List<Movement> enrichMovementBatchWithSpatialData(List<Movement> movements) throws MovementServiceException {
        shouldIFail();
        for(Movement movement : movements) {
            movement.setMetadata(getMetadata());
            movement.setMovementareaList(getMovementAreas(1, movement));
        }
        return movements;
    }

    public Movementmetadata getMetadata() {
        Movementmetadata metadata = new Movementmetadata();
        metadata.setMovemetUpdattim(Instant.now());
        metadata.setMovemetUpuser("Test");
        return metadata;
    }
    
    public List<Movementarea> getMovementAreas(int numberOfAreas, Movement movement) {
        List<Movementarea> areas = new ArrayList<>();
        for (int i = 0; i < numberOfAreas; i++) {
            areas.add(getMovementAreaHelper("AREA" + MovementHelpers.getRandomIntegers(10), movement));
        }
        return areas;
    }

    public Movementarea getMovementAreaHelper(String areaCode, Movement movement) {
        Movementarea movementArea = new Movementarea();
        Area area = new Area();
        area.setAreaCode(areaCode);
        area.setAreaName(areaCode);
        area.setAreaUpdattim(Instant.now());
        area.setAreaUpuser("Test");
        AreaType areaType = new AreaType();
        areaType.setName(areaCode);
        areaType.setUpdatedTime(Instant.now());
        areaType.setUpdatedUser("Test");
        area.setAreaType(areaType);
        areaDao.createMovementArea(area);
        movementArea.setMovareaAreaId(area);
        movementArea.setMovareaMoveId(movement);
        movementArea.setMovareaUpdattim(DateUtil.nowUTC());
        movementArea.setMovareaUpuser("TEST");
        return movementArea;
    }

}