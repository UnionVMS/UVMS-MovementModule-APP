package eu.europa.ec.fisheries.uvms.movement.rest.dto;

import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended;

import java.util.List;

public class RealTimeMapInitialData {

    public static final String ASSET_JSON_PLACE_HERE = "\"AssetJsonPlaceHere\"";

    List<MicroMovementExtended> microMovements;

    String assetList;

    public RealTimeMapInitialData(List<MicroMovementExtended> microMovements) {
        this.microMovements = microMovements;
        this.assetList = ASSET_JSON_PLACE_HERE.replace("\"", "");
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
