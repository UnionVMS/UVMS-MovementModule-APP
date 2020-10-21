package eu.europa.ec.fisheries.uvms.movement.service.dto;

import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.movement.model.constants.SatId;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class MicroMovement implements Serializable {

    private MovementPoint location;   //vivid solution point causes infinite json recursion ;(

    private Double heading;

    private String id;

    private Instant timestamp;

    private Double speed;

    private MovementSourceType source;

    private SatId sourceSatelliteId;

    private String status;

    private Short aisPositionAccuracy;

    public MicroMovement() {

    }

    public MicroMovement(Movement movement) {
        Point point = movement.getLocation();
        location = new MovementPoint();
        location.setLatitude(point.getY());
        location.setLongitude(point.getX());
        heading = movement.getHeading().doubleValue();
        id = movement.getId().toString();
        timestamp = movement.getTimestamp();
        speed = (movement.getSpeed() == null ? null : movement.getSpeed().doubleValue());
        source = movement.getSource();
        sourceSatelliteId = movement.getSourceSatelliteId();
        status = movement.getStatus();
        aisPositionAccuracy = movement.getAisPositionAccuracy();
    }

    public MicroMovement(Geometry geo, Float heading, UUID id, Instant timestamp, Float speed, MovementSourceType source, SatId sourceSatelliteId, String status, Short aisPositionAccuracy) {
        Point point = (Point)geo;
        location = new MovementPoint();
        location.setLatitude(point.getY());
        location.setLongitude(point.getX());
        this.heading = heading.doubleValue();
        this.id = id.toString();
        this.timestamp = timestamp;
        this.speed = (speed == null ? null : speed.doubleValue());
        this.source = source;
        this.sourceSatelliteId = sourceSatelliteId;
        this.status = status;
        this.aisPositionAccuracy = aisPositionAccuracy;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Short getAisPositionAccuracy() {
        return aisPositionAccuracy;
    }

    public void setAisPositionAccuracy(Short aisPositionAccuracy) {
        this.aisPositionAccuracy = aisPositionAccuracy;
    }
}
