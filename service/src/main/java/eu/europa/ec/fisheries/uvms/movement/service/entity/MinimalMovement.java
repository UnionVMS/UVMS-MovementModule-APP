/*
 Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
 © European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
 redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
 the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
 copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

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
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import eu.europa.ec.fisheries.uvms.movement.service.util.MovementComparator;
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

import java.io.Serializable;

import java.time.Instant;
import java.util.UUID;

/**
 **/
@Entity
@Table(name = "movement")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MinimalMovement.findAll", query = "SELECT m FROM MinimalMovement m WHERE m.duplicate = false"),
    @NamedQuery(name = "MinimalMovement.findById", query = "SELECT m FROM MinimalMovement m WHERE m.id = :id AND m.duplicate = false"),
    @NamedQuery(name = "MinimalMovement.findByGUID", query = "SELECT m FROM MinimalMovement m WHERE m.guid = :guid AND m.duplicate = false"),
    @NamedQuery(name = "MinimalMovement.findBySpeed", query = "SELECT m FROM MinimalMovement m WHERE m.speed = :speed AND m.duplicate = false"),
    @NamedQuery(name = "MinimalMovement.findByHeading", query = "SELECT m FROM MinimalMovement m WHERE m.heading = :heading AND m.duplicate = false"),
    @NamedQuery(name = "MinimalMovement.findByStatus", query = "SELECT m FROM MinimalMovement m WHERE m.status = :status AND m.duplicate = false"),
    @NamedQuery(name = "MinimalMovement.findLatestByMovementConnect", query = "SELECT m FROM MinimalMovement m WHERE m.movementConnect.value = :connectId AND m.duplicate = false ORDER BY m.timestamp DESC"),
    @NamedQuery(name = "MinimalMovement.findLatest", query = "SELECT m FROM MinimalMovement m INNER JOIN m.movementConnect mc2 WHERE m.timestamp = (select max(mm.timestamp) from MinimalMovement mm INNER JOIN mm.movementConnect mc where mc.value = :id and mm.timestamp < :date ) AND m.duplicate = false AND mc2.value = :id"),
    @NamedQuery(name = "MinimalMovement.findFirst", query = "SELECT m FROM MinimalMovement m INNER JOIN m.movementConnect mc2 WHERE m.timestamp = (select min(mm.timestamp) from MinimalMovement mm INNER JOIN mm.movementConnect mc where mc.value = :id ) AND m.duplicate = false AND mc2.value = :id"),
    @NamedQuery(name = "MinimalMovement.findExistingDate", query = "SELECT m FROM MinimalMovement m WHERE m.movementConnect.value = :id AND m.timestamp = :date AND m.duplicate = false")
})
@DynamicUpdate
@DynamicInsert
public class MinimalMovement implements Serializable, Comparable<MinimalMovement> {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "minimal_mov_seq")
    @Basic(optional = false)
    @Column(name = "move_id")
    private Long id;

    @NotNull
    @Type(type = "org.hibernate.spatial.GeometryType")
    @Column(name = "move_location", columnDefinition = "Geometry")
    private Point location;

    @Column(name = "move_speed")
    private Double speed;

    @Column(name = "move_heading")
    private Double heading;

    @NotNull
    @Size(max = 36)
    @Column(name = "move_guid", nullable = false)
    private String guid;

    @Size(max = 60)
    @Column(name = "move_status")
    private String status;

    @NotNull
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "move_moveconn_id", referencedColumnName = "moveconn_asset_id")
    @ManyToOne(cascade = CascadeType.PERSIST)
    private MovementConnect movementConnect;
    
    @Fetch(FetchMode.JOIN)
    @OneToOne(cascade = CascadeType.PERSIST, mappedBy = "toMovement")
    private Segment fromSegment;

    @Column(name = "move_movesour_id")
    @Enumerated(EnumType.ORDINAL)
    private MovementSourceType movementSource;

    @Column(name = "move_movetyp_id")
    @Enumerated(EnumType.ORDINAL)
    private MovementTypeType movementType;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    @Column(name = "move_timestamp")
    private Instant timestamp;

    @Column(name = "move_processed")
    private Boolean processed;

    @Column(name = "move_duplicate")
    private Boolean duplicate;

    @Column(name = "move_duplicate_id")
    private String duplicateId;

    @PrePersist
    public void setGuid() {
        this.guid = UUID.randomUUID().toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public MovementConnect getMovementConnect() {
        return movementConnect;
    }

    public void setMovementConnect(MovementConnect movementConnect) {
        this.movementConnect = movementConnect;
    }

    public Segment getFromSegment() {
		return fromSegment;
	}

	public void setFromSegment(Segment fromSegment) {
		this.fromSegment = fromSegment;
	}

	public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public MovementTypeType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementTypeType movementType) {
        this.movementType = movementType;
    }

    public MovementSourceType getMovementSource() {
        return movementSource;
    }

    public void setMovementSource(MovementSourceType movementSource) {
        this.movementSource = movementSource;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public Boolean getDuplicate() {
        return duplicate;
    }

    public String getDuplicateId() {
        return duplicateId;
    }

    public void setDuplicate(Boolean duplicate) {
        this.duplicate = duplicate;
    }

    public void setDuplicateId(String duplicateId) {
        this.duplicateId = duplicateId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    @Override
    public int compareTo(MinimalMovement o) {
        return MovementComparator.MINIMAL_MOVEMENT.compare(this, o);
    }

}