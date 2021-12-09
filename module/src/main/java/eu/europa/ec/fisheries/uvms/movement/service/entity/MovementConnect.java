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

import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.locationtech.jts.geom.Point;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@NamedQuery(name = MovementConnect.MOVEMENT_CONNECT_GET_ALL, query = "SELECT m FROM MovementConnect m")
@NamedQuery(name = MovementConnect.FIND_NEAREST_AFTER, query = "SELECT new eu.europa.ec.fisheries.uvms.movementrules.model.dto.VicinityInfoDTO(mc.id, mc.latestMovement.id, distance(mc.latestLocation, :point))" +
            "FROM MovementConnect mc " +
            "WHERE DWithin(mc.latestLocation, :point, :maxDistance, false) = true " +
            "AND mc.updated > :time AND mc.id <> :excludedID")
@NamedQuery(name = MovementConnect.FIND_LATEST_MOVEMENT_BY_ID, query = "SELECT mc.latestMovement FROM MovementConnect mc WHERE mc.id = :connectId")
@NamedQuery(name = MovementConnect.FIND_LATEST_MOVEMENT_BY_IDS, query = "SELECT mc.latestMovement FROM MovementConnect mc WHERE mc.id in :connectId")
@NamedQuery(name = MovementConnect.FIND_LATEST_MOVEMENT, query = "SELECT mc.latestMovement FROM MovementConnect mc ORDER BY mc.latestMovement.timestamp DESC")
@NamedQuery(name = MovementConnect.FIND_LATEST_MOVEMENT_SINCE, query = "SELECT new eu.europa.ec.fisheries.uvms.movement.service.dto.MovementProjection(mc.latestMovement.id, mc.latestMovement.location, mc.latestMovement.speed, mc.latestMovement.calculatedSpeed, mc.latestMovement.heading, mc.latestMovement.movementConnect.id, mc.latestMovement.status, mc.latestMovement.source, mc.latestMovement.movementType, mc.latestMovement.timestamp, mc.latestMovement.lesReportTime, mc.latestMovement.sourceSatelliteId, mc.latestMovement.updated, mc.latestMovement.updatedBy, mc.latestMovement.aisPositionAccuracy) FROM MovementConnect mc WHERE mc.latestMovement.timestamp >= :date AND mc.latestMovement.source in :sources" )

@Entity
@Table(name = "movementconnect")
@DynamicUpdate
@DynamicInsert
public class MovementConnect implements Serializable, Comparable<MovementConnect> {

    public static final String MOVEMENT_CONNECT_GET_ALL = "MovementConnect.findAll";
    public static final String FIND_NEAREST_AFTER = "MovementConnect.findVicinityAfter";
    public static final String FIND_LATEST_MOVEMENT_BY_ID = "MovementConnect.findLatestMovementById";
    public static final String FIND_LATEST_MOVEMENT_BY_IDS = "MovementConnect.findLatestMovementByIds";
    public static final String FIND_LATEST_MOVEMENT = "MovementConnect.findLatestMovement";
    public static final String FIND_LATEST_MOVEMENT_SINCE = "MovementConnect.findLatestMovementSince";

    private static final long serialVersionUID = 1L;

    @Id
    @Column(columnDefinition = "uuid", name = "id")
    private UUID id;    //this is the asset ID

    @JoinColumn(name = "latest_movement", referencedColumnName = "id")
    @JoinColumn(name = "latest_movement_timestamp", referencedColumnName = "timestamp")
    @OneToOne(fetch = FetchType.LAZY)
    private Movement latestMovement;

    @JoinColumn(name = "latest_vms", referencedColumnName = "id")
    @JoinColumn(name = "latest_vms_timestamp", referencedColumnName = "timestamp")
    @OneToOne(fetch = FetchType.LAZY)
    private Movement latestVMS;

    @NotNull
    @Column(name = "update_time")
    private Instant updated;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "update_user")
    private String updatedBy;

    @Column(name = "latest_location")
    private Point latestLocation;

    @PreUpdate
    public void preUpdate(){
        updated = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Movement getLatestMovement() {
        return latestMovement;
    }

    public void setLatestMovement(Movement latestMovement) {
        this.latestMovement = latestMovement;
    }

    public Movement getLatestVMS() {
        return latestVMS;
    }

    public void setLatestVMS(Movement latestVMS) {
        this.latestVMS = latestVMS;
    }

    public Point getLatestLocation() {
        return latestLocation;
    }

    public void setLatestLocation(Point latestLocation) {
        this.latestLocation = latestLocation;
    }

    @Override
	public int compareTo(MovementConnect o) {
        if (o == null) {
            return ObjectUtils.compare(this, null);
        } else {
            return ObjectUtils.compare(this.getId(), o.getId());
        }
	}
}
