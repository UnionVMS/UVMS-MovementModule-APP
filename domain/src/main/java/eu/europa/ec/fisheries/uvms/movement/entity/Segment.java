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
package eu.europa.ec.fisheries.uvms.movement.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import com.vividsolutions.jts.geom.LineString;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import java.io.Serializable;
import java.time.OffsetDateTime;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import eu.europa.ec.fisheries.uvms.movement.model.OffsetDateTimeDeserializer;
import org.hibernate.annotations.*;

/**
 **/
@Entity
@Table(name = "segment")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Segment.findAll", query = "SELECT s FROM Segment s"),
    @NamedQuery(name = "Segment.findById", query = "SELECT s FROM Segment s WHERE s.id = :id"),
    @NamedQuery(name = "Segment.findByDistance", query = "SELECT s FROM Segment s WHERE s.distance = :distance"),
    @NamedQuery(name = "Segment.findByDuration", query = "SELECT s FROM Segment s WHERE s.duration = :duration"),
    @NamedQuery(name = "Segment.findBySpeedOverGround", query = "SELECT s FROM Segment s WHERE s.speedOverGround = :speedOverGround"),
    @NamedQuery(name = "Segment.findByCourseOverGround", query = "SELECT s FROM Segment s WHERE s.courseOverGround = :courseOverGround"),
    @NamedQuery(name = "Segment.findByUpdated", query = "SELECT s FROM Segment s WHERE s.updated = :updated"),
    @NamedQuery(name = "Segment.findByUpdatedBy", query = "SELECT s FROM Segment s WHERE s.updatedBy = :updatedBy"),
    @NamedQuery(name = "Segment.findByFromMovement", query = "SELECT s FROM Segment s WHERE s.fromMovement = :movement"),
    @NamedQuery(name = "Segment.findByToMovement", query = "SELECT s FROM Segment s WHERE s.toMovement = :movement"),
    @NamedQuery(name = "Segment.findByMovement", query = "SELECT s FROM Segment s WHERE s.toMovement = :movement OR s.fromMovement = :movement"),})
@DynamicUpdate
@DynamicInsert
public class Segment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "seg_id")
    private Long id;

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

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    @NotNull
    @Column(name = "seg_updattim")
    @Temporal(TemporalType.TIMESTAMP)
    private OffsetDateTime updated;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public OffsetDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(OffsetDateTime updated) {
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