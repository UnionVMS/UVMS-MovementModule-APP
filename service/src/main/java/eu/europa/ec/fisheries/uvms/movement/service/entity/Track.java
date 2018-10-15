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
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

/**
 **/
@Entity
@Table(name = "track")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Track.findAll", query = "SELECT t FROM Track t"),
    @NamedQuery(name = "Track.findById", query = "SELECT t FROM Track t WHERE t.id = :id"),
    @NamedQuery(name = "Track.findByDistance", query = "SELECT t FROM Track t WHERE t.distance = :distance"),
    @NamedQuery(name = "Track.findByDuration", query = "SELECT t FROM Track t WHERE t.duration = :duration"),
    @NamedQuery(name = "Track.findByUpdated", query = "SELECT t FROM Track t WHERE t.updated = :updated"),
    @NamedQuery(name = "Track.findByUpdatedBy", query = "SELECT t FROM Track t WHERE t.updatedBy = :updatedBy")})
@DynamicUpdate
@DynamicInsert
public class Track implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "track_seq")
    @Basic(optional = false)
    @Column(name = "trac_id")
    private Long id;

    @Basic(optional = false)
    @NotNull
    @Column(name = "trac_distance")
    private double distance;

    @NotNull
    @Column(name = "trac_duration")
    private double duration;

    @Basic(optional = false)
    @NotNull
    @Column(name = "track_totalsea")
    private double totalTimeAtSea;

    @Basic(optional = false)
    @NotNull
    @Column(name = "trac_avgspeed")
    private double averageSpeed;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    @NotNull
    @Column(name = "trac_updattim")
    private Instant updated;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "trac_upuser")
    private String updatedBy;

    @Type(type = "org.hibernate.spatial.GeometryType")
    @Column(name = "trac_geom", columnDefinition = "Geometry")
    private LineString location;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
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

    public LineString getLocation() {
        return location;
    }

    public void setLocation(LineString location) {
        this.location = location;
    }

    public double getTotalTimeAtSea() {
        return totalTimeAtSea;
    }

    public void setTotalTimeAtSea(double totalTimeAtSea) {
        this.totalTimeAtSea = totalTimeAtSea;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

}