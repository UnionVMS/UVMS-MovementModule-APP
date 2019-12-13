package eu.europa.ec.fisheries.uvms.movement.service.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.commons.date.UVMSInstantDeserializer;

import java.time.Instant;
import java.util.UUID;

public class SegmentDTO {

    private UUID id;

    private String location;    //LineString in WKT

    private Double distance;

    private Integer duration;

    private Double speedOverGround;

    private Double courseOverGround;

    @JsonDeserialize(using = UVMSInstantDeserializer.class)
    private Instant updated;

    private String updatedBy;

    private SegmentCategoryType segmentCategory;

    private UUID track;

    private String fromMovement;

    private String toMovement;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
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

    public UUID getTrack() {
        return track;
    }

    public void setTrack(UUID track) {
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
