package eu.europa.ec.fisheries.uvms.movement.service.dto;


import com.vividsolutions.jts.geom.Geometry;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;

import java.time.Instant;
import java.util.UUID;

public class MicroMovementExtended {

    public static final String FIND_ALL_AFTER_DATE = "MicroMovementExtended.findAllAfterDate";
    public static final String FIND_ALL_FOR_ASSET_AFTER_DATE = "MicroMovementExtended.findAllForAssetAfterDate";

    private MicroMovement microMove;

    private String asset;

    private String flagstate;

    private String assetName;

    public MicroMovementExtended() {

    }

    public MicroMovementExtended(Geometry geo, Float heading, UUID guid, MovementConnect asset, Instant timestamp, Float speed, MovementSourceType source) {
        microMove = new MicroMovement(geo, heading, guid, timestamp, speed, source);
        this.asset = asset.getId().toString();
        flagstate = asset.getFlagState();
        assetName = asset.getName();
    }

    public MicroMovement getMicroMove() {
        return microMove;
    }

    public void setMicroMove(MicroMovement microMove) {
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
