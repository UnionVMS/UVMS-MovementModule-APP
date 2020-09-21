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
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movementconnect")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = MovementConnect.MOVEMENT_CONNECT_GET_ALL, query = "SELECT m FROM MovementConnect m"),
    @NamedQuery(name = MovementConnect.FIND_NEAREST_AFTER, query = "SELECT new eu.europa.ec.fisheries.uvms.movementrules.model.dto.VicinityInfoDTO(mc.id, mc.latestMovement.id, distance(mc.latestLocation, :point))" +
                "FROM MovementConnect mc " +
                "WHERE DWithin(mc.latestLocation, :point, :maxDistance, false) = true " +
                "AND mc.updated > :time AND mc.id <> :excludedID")
})
@DynamicUpdate
@DynamicInsert
public class MovementConnect implements Serializable, Comparable<MovementConnect> {

    public static final String MOVEMENT_CONNECT_GET_ALL = "MovementConnect.findAll";
    public static final String FIND_NEAREST_AFTER = "MovementConnect.findVicinityAfter";

    private static final long serialVersionUID = 1L;

    @Id
    @Column(columnDefinition = "uuid", name = "moveconn_id")
    private UUID id;    //this is the asset ID

    @Column(name = "moveconn_flagstate")
    private String flagState;

    @Column(name = "moveconn_name")
    private String name;
    
    @JoinColumn(name = "moveconn_latest_move", referencedColumnName = "move_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Movement latestMovement;

    @JoinColumn(name = "moveconn_latest_vms", referencedColumnName = "move_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Movement latestVMS;

    @NotNull
    @Column(name = "moveconn_updattim")
    private Instant updated;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "moveconn_upuser")
    private String updatedBy;

    @Column(name = "moveconn_latest_location")
    private Point latestLocation;

    public MovementConnect() {
    }

    public MovementConnect(UUID id, String flagState, String name, Instant updated, String updatedBy, Point latestLocation) {
        this.id = id;
        this.flagState = flagState;
        this.name = name;
        this.updated = updated;
        this.updatedBy = updatedBy;
        this.latestLocation = latestLocation;
    }

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

    public String getFlagState() {
        return flagState;
    }

    public void setFlagState(String flagState) {
        this.flagState = flagState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
