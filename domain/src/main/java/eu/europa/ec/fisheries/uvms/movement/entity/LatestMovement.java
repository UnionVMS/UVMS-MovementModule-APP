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

import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.util.MovementComparator;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "latestmovement")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "LatestMovement.findLatestByMovementConnect", query = "SELECT m FROM LatestMovement m WHERE m.movementConnect.value = :connectId"),
    @NamedQuery(name = "LatestMovement.findLatestByMovementConnectList", query = "SELECT m FROM LatestMovement m WHERE m.movementConnect.value in (:connectId)"),
    @NamedQuery(name = "LatestMovement.findLatest", query = "SELECT m FROM LatestMovement m ORDER BY m.timestamp")

})
@DynamicUpdate
@DynamicInsert
public class LatestMovement implements Serializable, Comparable<LatestMovement> {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "movelate_id")
    private Long id;

    @NotNull
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "movelate_moveconn_id", referencedColumnName = "moveconn_id")
    @ManyToOne(cascade = CascadeType.PERSIST)
    private MovementConnect movementConnect;

    @NotNull
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "movelate_move_id", referencedColumnName = "move_id")
    @OneToOne(cascade = CascadeType.PERSIST)
    private Movement movement;

    @Column(name = "movelate_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MovementConnect getMovementConnect() {
        return movementConnect;
    }

    public void setMovementConnect(MovementConnect movementConnect) {
        this.movementConnect = movementConnect;
    }

    public Movement getMovement() {
        return movement;
    }

    public void setMovement(Movement movement) {
        this.movement = movement;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(LatestMovement o) {
        return MovementComparator.LATEST_MOVEMENT.compare(this, o);
    }
}