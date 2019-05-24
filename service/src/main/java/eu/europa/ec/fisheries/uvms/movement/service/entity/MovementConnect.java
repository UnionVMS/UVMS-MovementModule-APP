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

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import eu.europa.ec.fisheries.uvms.movement.service.util.MovementComparator;

@Entity
@Table(name = "movementconnect")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = MovementConnect.MOVEMENT_CONNECT_GET_ALL, query = "SELECT m FROM MovementConnect m")
})
@DynamicUpdate
@DynamicInsert
public class MovementConnect implements Serializable, Comparable<MovementConnect> {

    public static final String MOVEMENT_CONNECT_GET_ALL = "MovementConnect.findAll";

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

    @NotNull
    @Column(name = "moveconn_updattim")
    private Instant updated;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "moveconn_upuser")
    private String updatedBy;

    public MovementConnect() {
    }

    public MovementConnect(UUID id, String flagState, String name, Instant updated, String updatedBy) {
        this.id = id;
        this.flagState = flagState;
        this.name = name;
        this.updated = updated;
        this.updatedBy = updatedBy;
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

    @Override
	public int compareTo(MovementConnect o2) {
		return MovementComparator.MOVEMENT_CONNECT.compare(this, o2);
	}

}
