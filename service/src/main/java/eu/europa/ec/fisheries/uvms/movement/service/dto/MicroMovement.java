package eu.europa.ec.fisheries.uvms.movement.service.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;

import java.time.Instant;
import java.util.UUID;

public class MicroMovement {

    public static final String FIND_ALL_AFTER_DATE = "MicroMovementDtoV2.findAllAfterDate";

    private MovementPoint location;   //vivid solution point causes infinite json recursion ;(

    private Double heading;

    private String guid;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    private Instant timestamp;

    private Double speed;

    private MovementSourceType source;

    public MicroMovement() {

    }

    public MicroMovement(Movement movement) {
        Point point = movement.getLocation();
        location = new MovementPoint();
        location.setLatitude(point.getY());
        location.setLongitude(point.getX());
        heading = movement.getHeading().doubleValue();
        guid = movement.getId().toString();
        timestamp = movement.getTimestamp();
        speed = movement.getSpeed().doubleValue();
    }

    public MicroMovement(Geometry geo, Float heading, UUID guid, Instant timestamp, Float speed, MovementSourceType source) {
        Point point = (Point)geo;
        location = new MovementPoint();
        location.setLatitude(point.getY());
        location.setLongitude(point.getX());
        this.heading = heading.doubleValue();
        this.guid = guid.toString();
        this.timestamp = timestamp;
        this.speed = speed.doubleValue();
        this.source = source;
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

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
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
}
