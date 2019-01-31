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
import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import eu.europa.ec.fisheries.uvms.movement.service.util.MovementComparator;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "latestmovement", indexes = {
        @Index(columnList = "movelate_move_id", name = "movelate_move_fk_idx", unique = false),
        @Index(columnList = "movelate_moveconn_id", name = "movelate_moveconnect_fk_idx", unique = false)
})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = LatestMovement.FIND_LATEST_BY_MOVEMENT_CONNECT, query = "SELECT m FROM LatestMovement m WHERE m.movementConnect.id = :connectId"),
    @NamedQuery(name = LatestMovement.FIND_LATEST_BY_MOVEMENT_CONNECT_LIST, query = "SELECT m FROM LatestMovement m WHERE m.movementConnect.id in :connectId"),
    @NamedQuery(name = LatestMovement.FIND_LATEST, query = "SELECT m FROM LatestMovement m ORDER BY m.timestamp"),
    @NamedQuery(name = LatestMovement.FIND_NEAREST, query = "SELECT new eu.europa.ec.fisheries.uvms.movementrules.model.dto.VicinityInfoDTO(m.movementConnect.id, m.movement.id, function ('distance', m.location, :point))" +
                                                                "FROM LatestMovement m " +
                                                                "WHERE function ('dwithin', m.location, :point, cast((SELECT value FROM Parameter WHERE id = 'maxDistance') as int)) = TRUE " +
                                                                //"WHERE function ('dwithin', m.location, :point, 500) = TRUE " +
                                                                "AND m.movementConnect.id <> :excludedID")

})
@DynamicUpdate
@DynamicInsert
public class LatestMovement implements Serializable, Comparable<LatestMovement> {
    
    public static final String FIND_LATEST_BY_MOVEMENT_CONNECT = "LatestMovement.findLatestByMovementConnect";
    public static final String FIND_LATEST_BY_MOVEMENT_CONNECT_LIST = "LatestMovement.findLatestByMovementConnectList";
    public static final String FIND_LATEST = "LatestMovement.findLatest";
    public static final String FIND_NEAREST = "LatestMovement.findVicinity";

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "latest_movement_seq")
    @Column(columnDefinition = "uuid", name = "movelate_id")
    private UUID id;

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

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    @Column(name = "movelate_timestamp")
    private Instant timestamp;

    @Column(name = "movelate_geom", columnDefinition="geography(POINT, 4326)")
    private Point location;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(LatestMovement o) {
        return MovementComparator.LATEST_MOVEMENT.compare(this, o);
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}