package eu.europa.ec.fisheries.uvms.movement.service.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;

import java.time.Instant;

public class SegmentDTO {

    private Long id;

    private String location;    //LineString in WKT

    private Double distance;

    private Double duration;

    private Double speedOverGround;

    private Double courseOverGround;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    private Instant updated;

    private String updatedBy;

    private SegmentCategoryType segmentCategory;

    private Long track;

    private String fromMovement;

    private String toMovement;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Double getSpeedOverGround() {
        return speedOverGround;
    }

    public void setSpeedOverGround(Double speedOverGround) {
        this.speedOverGround = speedOverGround;
    }

    public Double getCourseOverGround() {
        return courseOverGround;
    }

    public void setCourseOverGround(Double courseOverGround) {
        this.courseOverGround = courseOverGround;
    }

    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public SegmentCategoryType getSegmentCategory() {
        return segmentCategory;
    }

    public void setSegmentCategory(SegmentCategoryType segmentCategory) {
        this.segmentCategory = segmentCategory;
    }

    public Long getTrack() {
        return track;
    }

    public void setTrack(Long track) {
        this.track = track;
    }

    public String getFromMovement() {
        return fromMovement;
    }

    public void setFromMovement(String fromMovement) {
        this.fromMovement = fromMovement;
    }

    public String getToMovement() {
        return toMovement;
    }

    public void setToMovement(String toMovement) {
        this.toMovement = toMovement;
    }
}
