package eu.europa.ec.fisheries.uvms.movement.client.model;

import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.movement.model.constants.SatId;

import java.time.Instant;

public class MicroMovement {

    private MovementPoint location;

    private Double heading;

    private String id;

    private Instant timestamp;

    private Double speed;

    private MovementSourceType source;

    private SatId sourceSatelliteId;

    public MicroMovement() {}

    public MovementPoint getLocation() {
        return location;
    }

    public void setLocation(MovementPoint location) {
        this.location = location;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public MovementSourceType getSource() {
        return source;
    }

    public void setSource(MovementSourceType source) {
        this.source = source;
    }

    public SatId getSourceSatelliteId() {
        return sourceSatelliteId;
    }

    public void setSourceSatelliteId(SatId sourceSatelliteId) {
        this.sourceSatelliteId = sourceSatelliteId;
    }
}
