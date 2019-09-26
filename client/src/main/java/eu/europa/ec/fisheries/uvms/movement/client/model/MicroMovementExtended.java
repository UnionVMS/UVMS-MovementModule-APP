package eu.europa.ec.fisheries.uvms.movement.client.model;

public class MicroMovementExtended {

    private MicroMovement microMove;

    private String asset;

    public MicroMovementExtended() {}

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
