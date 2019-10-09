package eu.europa.ec.fisheries.uvms.movement.service.dto;


import org.locationtech.jts.geom.Geometry;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class MicroMovementExtended implements Serializable {

    public static final String FIND_ALL_AFTER_DATE = "MicroMovementExtended.findAllAfterDate";
    public static final String FIND_ALL_FOR_ASSET_AFTER_DATE = "MicroMovementExtended.findAllForAssetAfterDate";
    public static final String FIND_ALL_FOR_CONNECT_IDS_BETWEEN_DATES = "MicroMovementExtended.findAllForConnectIdsBetweenDates";

    private MicroMovement microMove;

    private String asset;

    public MicroMovementExtended() {

    }

    public MicroMovementExtended(Geometry geo, Float heading, UUID guid, UUID assetGuid, Instant timestamp, Float speed, MovementSourceType source) {
        microMove = new MicroMovement(geo, heading, guid, timestamp, speed, source);
        this.asset = assetGuid.toString();
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

}
