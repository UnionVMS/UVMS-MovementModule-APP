package eu.europa.ec.fisheries.uvms.movement.rest.dto;

import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended;

import java.util.List;

public class RealTimeMapInitialData {

    List<MicroMovementExtended> microMovements;

    String assetList;

    public RealTimeMapInitialData(List<MicroMovementExtended> microMovements, String assetList) {
        this.microMovements = microMovements;
        this.assetList = assetList;
    }

    public RealTimeMapInitialData() {
    }

    public List<MicroMovementExtended> getMicroMovements() {
        return microMovements;
    }

    public void setMicroMovements(List<MicroMovementExtended> microMovements) {
        this.microMovements = microMovements;
    }

    public String getAssetList() {
        return assetList;
    }

    public void setAssetList(String assetList) {
        this.assetList = assetList;
    }
}
