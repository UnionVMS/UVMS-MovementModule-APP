package eu.europa.ec.fisheries.uvms.movement.service.dto;


import com.vividsolutions.jts.geom.Geometry;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;

import java.time.Instant;
import java.util.UUID;

public class MicroMovementDtoV2Extended {  //TODO: find better name for this

    public static final String FIND_ALL_AFTER_DATE = "MicroMovementDtoV2Extended.findAllAfterDate";

    private MicroMovementDtoV2 microMove;

    private String asset;

    private String flagstate;

    private String assetName;

    public MicroMovementDtoV2Extended() {

    }

    public MicroMovementDtoV2Extended(Geometry geo, Float heading, UUID guid, MovementConnect asset, Instant timestamp, Float speed) {
        microMove = new MicroMovementDtoV2(geo, heading, guid, timestamp, speed);
        this.asset = asset.getId().toString();
        flagstate = asset.getFlagState();
        assetName = asset.getName();
    }

    public MicroMovementDtoV2 getMicroMove() {
        return microMove;
    }

    public void setMicroMove(MicroMovementDtoV2 microMove) {
        this.microMove = microMove;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public String getFlagstate() {
        return flagstate;
    }

    public void setFlagstate(String flagstate) {
        this.flagstate = flagstate;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
}
