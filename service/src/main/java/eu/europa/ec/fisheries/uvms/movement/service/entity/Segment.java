/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.movement.service.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.vividsolutions.jts.geom.LineString;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import org.hibernate.annotations.*;

@Entity
@Table(name = "segment", indexes = {
        @Index(columnList = "seg_frommove_id", name = "seg_move_from_to_idx", unique = false),
        @Index(columnList = "seg_tomove_id", name = "seg_move_to_to_idx", unique = false),
        @Index(columnList = "seg_segcat_id", name = "seg_segcat_fk_idx", unique = false),
        @Index(columnList = "seg_trac_id", name = "seg_trac_fk_idx", unique = false)
}, uniqueConstraints = {
        @UniqueConstraint(name = "segment_seg_frommove_id_key", columnNames = "seg_frommove_id"),
        @UniqueConstraint(name = "segment_seg_tomove_id_key", columnNames = "seg_tomove_id")
})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = Segment.FIND_ALL, query = "SELECT s FROM Segment s"),
    @NamedQuery(name = Segment.FIND_ALL_BY_TRACK, query = "SELECT s FROM Segment s where s.track = :track"),
    @NamedQuery(name = Segment.FIND_BY_DISTANCE, query = "SELECT s FROM Segment s WHERE s.distance = :distance"),
    @NamedQuery(name = Segment.FIND_BY_DURATION, query = "SELECT s FROM Segment s WHERE s.duration = :duration"),
    @NamedQuery(name = Segment.FIND_BY_SPEED_OVER_GROUND, query = "SELECT s FROM Segment s WHERE s.speedOverGround = :speedOverGround"),
    @NamedQuery(name = Segment.FIND_BY_COURSE_OVER_GROUND, query = "SELECT s FROM Segment s WHERE s.courseOverGround = :courseOverGround"),
    @NamedQuery(name = Segment.FIND_BY_UPDATED, query = "SELECT s FROM Segment s WHERE s.updated = :updated"),
    @NamedQuery(name = Segment.FIND_BY_UPDATED_BY, query = "SELECT s FROM Segment s WHERE s.updatedBy = :updatedBy"),
    @NamedQuery(name = Segment.FIND_FIND_BY_FROM_MOVEMENT, query = "SELECT s FROM Segment s WHERE s.fromMovement = :movement"),
    @NamedQuery(name = Segment.FIND_BY_TO_MOVEMENT, query = "SELECT s FROM Segment s WHERE s.toMovement = :movement"),
    @NamedQuery(name = Segment.FIND_BY_MOVEMENT, query = "SELECT s FROM Segment s WHERE s.toMovement = :movement OR s.fromMovement = :movement"),})
@DynamicUpdate
@DynamicInsert
public class Segment implements Serializable {

    public static final String FIND_ALL = "Segment.findAll";
    public static final String FIND_ALL_BY_TRACK = "Segment.findAllSegementsByTrack";
    public static final String FIND_BY_DISTANCE = "Segment.findByDistance";
    public static final String FIND_BY_DURATION = "Segment.findByDuration";
    public static final String FIND_BY_SPEED_OVER_GROUND = "Segment.findBySpeedOverGround";
    public static final String FIND_BY_COURSE_OVER_GROUND = "Segment.findByCourseOverGround";
    public static final String FIND_BY_UPDATED = "Segment.findByUpdated";
    public static final String FIND_BY_UPDATED_BY = "Segment.findByUpdatedBy";
    public static final String FIND_FIND_BY_FROM_MOVEMENT = "Segment.findByFromMovement";
    public static final String FIND_BY_TO_MOVEMENT = "Segment.findByToMovement";
    public static final String FIND_BY_MOVEMENT = "Segment.findByMovement";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", name = "seg_id")
    private UUID id;

    @Type(type = "org.hibernate.spatial.GeometryType")
    @Column(name = "seg_geom", columnDefinition = "Geometry", nullable = true)
    private LineString location;

    @Column(name = "seg_distance")
    private Double distance;

    @Column(name = "seg_duration")
    private Double duration;

    @Column(name = "seg_sog")
    private Double speedOverGround;

    @Column(name = "seg_cog")
    private Double courseOverGround;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    @NotNull
    @Column(name = "seg_updattim")
    private Instant updated;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "seg_upuser")
    private String updatedBy;

    @Column(name = "seg_segcat_id")
    @Enumerated(EnumType.ORDINAL)
    private SegmentCategoryType segmentCategory;

    //@Fetch(FetchMode.JOIN)
    @JoinColumn(name = "seg_trac_id", referencedColumnName = "trac_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Track track;

    //@Fetch(FetchMode.JOIN)
    @JoinColumn(name = "seg_frommove_id", referencedColumnName = "move_id")
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Movement fromMovement;

    //@Fetch(FetchMode.JOIN)
    @JoinColumn(name = "seg_tomove_id", referencedColumnName = "move_id")
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Movement toMovement;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LineString getLocation() {
        return location;
    }

    public void setLocation(LineString location) {
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

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public Movement getFromMovement() {
        return fromMovement;
    }

    public void setFromMovement(Movement fromMovement) {
        this.fromMovement = fromMovement;
    }

    public Movement getToMovement() {
        return toMovement;
    }

    public void setToMovement(Movement toMovement) {
        this.toMovement = toMovement;
    }

}
