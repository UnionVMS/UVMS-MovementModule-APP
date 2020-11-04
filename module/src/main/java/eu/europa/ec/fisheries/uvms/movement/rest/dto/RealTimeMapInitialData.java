package eu.europa.ec.fisheries.uvms.movement.rest.dto;

import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;

import java.util.List;

public class RealTimeMapInitialData {

    public static final String ASSET_JSON_PLACE_HERE = "\"AssetJsonPlaceHere\"";

    List<MovementDto> movements;

    String assetList;

    public RealTimeMapInitialData(List<MovementDto> movements) {
        this.movements = movements;
        this.assetList = ASSET_JSON_PLACE_HERE.replace("\"", "");
    }

    public RealTimeMapInitialData() {
    }

    public List<MovementDto> getMovements() {
        return movements;
    }

    public void setMovements(List<MovementDto> movements) {
        this.movements = movements;
    }

    public String getAssetList() {
        return assetList;
    }

    public void setAssetList(String assetList) {
        this.assetList = assetList;
    }
}
