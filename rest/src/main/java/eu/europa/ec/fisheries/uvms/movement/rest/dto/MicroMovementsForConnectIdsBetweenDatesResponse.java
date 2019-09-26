package eu.europa.ec.fisheries.uvms.movement.rest.dto;

import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroMovementsForConnectIdsBetweenDatesResponse implements Serializable {
    private List<MicroMovementExtended> microMovementExtendedList = new ArrayList<>();

    public MicroMovementsForConnectIdsBetweenDatesResponse() {
    }

    public List<MicroMovementExtended> getMicroMovementExtendedList() {
        return microMovementExtendedList;
    }

    public void setMicroMovementExtendedList(List<MicroMovementExtended> microMovementExtendedList) {
        this.microMovementExtendedList = microMovementExtendedList;
    }
}
