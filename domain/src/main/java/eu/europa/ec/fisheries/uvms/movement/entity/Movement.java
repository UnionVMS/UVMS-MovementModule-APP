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
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Movementarea;
import com.vividsolutions.jts.geom.Point;

import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import eu.europa.ec.fisheries.uvms.movement.util.MovementComparator;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.util.MovementComparator;

/**
 **/
@Entity
@Table(name = "movement")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = Movement.FIND_ALL, query = "SELECT m FROM Movement m WHERE m.duplicate = false"),
    @NamedQuery(name = Movement.FIND_UNPROCESSED, query = "SELECT m FROM Movement m WHERE m.processed = false ORDER BY m.timestamp ASC"),
    @NamedQuery(name = Movement.FIND_UNPROCESSED_ID, query = "SELECT m.id FROM Movement m WHERE m.processed = false ORDER BY m.timestamp ASC"),
    @NamedQuery(name = Movement.FIND_BY_ID, query = "SELECT m FROM Movement m WHERE m.id = :id AND m.duplicate = false"),
    @NamedQuery(name = Movement.FIND_BY_GUID, query = "SELECT m FROM Movement m WHERE m.guid = :guid AND m.duplicate = false"),
    @NamedQuery(name = Movement.FIND_BY_ALTITUDE, query = "SELECT m FROM Movement m WHERE m.altitude = :altitude AND m.duplicate = false"),
    @NamedQuery(name = Movement.FIND_BY_SPEED, query = "SELECT m FROM Movement m WHERE m.speed = :speed AND m.duplicate = false"),
    @NamedQuery(name = Movement.FIND_BY_HEADING, query = "SELECT m FROM Movement m WHERE m.heading = :heading AND m.duplicate = false"),
    @NamedQuery(name = Movement.FIND_BY_STATUS, query = "SELECT m FROM Movement m WHERE m.status = :status AND m.duplicate = false"),
    @NamedQuery(name = Movement.FIND_BY_UPDATED, query = "SELECT m FROM Movement m WHERE m.updated = :updated AND m.duplicate = false"),
    @NamedQuery(name = Movement.FIND_BY_UPDATED_BY, query = "SELECT m FROM Movement m WHERE m.updatedBy = :updatedBy AND m.duplicate = false"),
    @NamedQuery(name = Movement.FIND_LATEST_BY_MOVEMENT_CONNECT, query = "SELECT m FROM Movement m WHERE m.movementConnect.value = :connectId AND m.duplicate = false ORDER BY m.timestamp DESC"),
    @NamedQuery(name = Movement.FIND_LATEST, query = "SELECT m FROM Movement m INNER JOIN m.movementConnect mc2 WHERE m.duplicate = false AND m.timestamp = (select max(mm.timestamp) from Movement mm INNER JOIN mm.movementConnect mc where mc.value = :id and mm.timestamp < :date and mm.processed = true) AND mc2.value = :id and m.processed = true"),
    @NamedQuery(name = Movement.FIND_FIRST, query = "SELECT m FROM Movement m INNER JOIN m.movementConnect mc2 WHERE m.duplicate = false AND m.timestamp = (select min(mm.timestamp) from Movement mm INNER JOIN mm.movementConnect mc where mc.value = :id and mm.duplicate = false and mm.processed = true) AND mc2.value = :id and m.processed = true"),
    @NamedQuery(name = Movement.LIST_BY_AREA_TIME_INTERVAL, query = "SELECT m FROM Movement m INNER JOIN m.movementareaList mal WHERE (m.timestamp BETWEEN :fromDate AND :toDate) AND m.duplicate = false AND mal.movareaMoveId.id = m.id AND mal.movareaAreaId.areaId = :areaId"),
    																			   //SELECT mo FROM Movement mo INNER JOIN mo.movementarea ma WHERE (mo.timestamp BETWEEN :fromDate AND :toDate) AND mo.move_duplicate = false AND ma.movareaId = mo.id AND ma.movareaId = :areaId 	
    																			   //SELECT movement FROM Movement movement INNER JOIN movement.movementareaList movementArea WHERE (movement.timestamp BETWEEN :fromDate AND :toDate) AND movement.duplicate = false AND movementArea.movareaMoveId = movement.id AND movementArea.movareaAreaId.areaId = :areaId
    @NamedQuery(name = Movement.FIND_EXISTING_DATE, query = "SELECT m FROM Movement m WHERE m.movementConnect.value = :id AND m.timestamp = :date AND m.duplicate = false")
})
@DynamicUpdate
@DynamicInsert
public class Movement implements Serializable, Comparable<Movement> {

    public static final String FIND_ALL = "Movement.findAll";
    public static final String FIND_UNPROCESSED = "Movement.findUnprocessed";
    public static final String FIND_UNPROCESSED_ID = "Movement.findUnprocessedId";
    public static final String FIND_BY_ID = "Movement.findById";
    public static final String FIND_BY_GUID = "Movement.findByGUID";
    public static final String FIND_BY_ALTITUDE = "Movement.findByAltitude";
    public static final String FIND_BY_SPEED = "Movement.findBySpeed";
    public static final String FIND_BY_HEADING = "Movement.findByHeading";
    public static final String FIND_BY_STATUS = "Movement.findByStatus";
    public static final String FIND_BY_UPDATED = "Movement.findByUpdated";
    public static final String FIND_BY_UPDATED_BY = "Movement.findByUpdatedBy";
    public static final String FIND_LATEST_BY_MOVEMENT_CONNECT = "Movement.findLatestByMovementConnect";
    public static final String FIND_LATEST = "Movement.findLatest";
    public static final String FIND_FIRST = "Movement.findFirst";
    public static final String LIST_BY_AREA_TIME_INTERVAL = "Movement.findMovementByAreaAndTimestampInterval";
    public static final String FIND_EXISTING_DATE = "Movement.findExistingDate";
    
    private static final long serialVersionUID = 1L;

    /*@JoinColumn(name = "move_movetyp_id", referencedColumnName = "movetyp_id")
     @ManyToOne(optional = false, fetch = FetchType.LAZY)
     private MovementType moveMovetypId;
    
     @JoinColumn(name = "move_movesour_id", referencedColumnName = "movesour_id")
     @ManyToOne(optional = false, fetch = FetchType.LAZY)
     private MovementSource moveMovesourId;*/
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @Column(name = "move_trip_number")
    private Double tripNumber;

    @Column(name = "move_internal_reference_number")
    @Size(max = 12)
    private String internalReferenceNumber;

    @Column(name = "move_altitude")
    private Integer altitude;

    @NotNull
    @Size(max = 36)
    @Column(name = "move_guid", nullable = false)
    private String guid;

    @Size(max = 60)
    @Column(name = "move_status")
    private String status;

    @Fetch(FetchMode.JOIN)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "movareaMoveId")
    private List<Movementarea> movementareaList;

    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "move_movemet_id", referencedColumnName = "movemet_id")
    @ManyToOne(cascade = CascadeType.ALL)
    private Movementmetadata metadata;

    @NotNull
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "move_moveconn_id", referencedColumnName = "moveconn_id")
    @ManyToOne(cascade = CascadeType.MERGE)
    private MovementConnect movementConnect;

    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "move_act_id", referencedColumnName = "act_id")
    @ManyToOne(optional = true, cascade = CascadeType.ALL)
    private Activity activity;

    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "move_trac_id", referencedColumnName = "trac_id")
    @ManyToOne(optional = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Track track;

    @Fetch(FetchMode.JOIN)
    @OneToOne(cascade = CascadeType.PERSIST, mappedBy = "toMovement")
    private Segment fromSegment;

    @Fetch(FetchMode.JOIN)
    @OneToOne(cascade = CascadeType.PERSIST, mappedBy = "fromMovement")
    private Segment toSegment;

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

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    @NotNull
    @Column(name = "move_updattim")
    private Instant updated;

    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "move_upuser")
    private String updatedBy;

    @OneToMany(mappedBy = "areatranMoveId", fetch = FetchType.LAZY)
    private List<Areatransition> areatransitionList;

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

    public Integer getMoveAltitude() {
        return altitude;
    }

    public void setMoveAltitude(Integer moveAltitude) {
        this.altitude = moveAltitude;
    }

    public Double getTripNumber() {
        return tripNumber;
    }

    public void setTripNumber(Double tripNumber) {
        this.tripNumber = tripNumber;
    }

    public String getInternalReferenceNumber() {
        return internalReferenceNumber;
    }

    public void setInternalReferenceNumber(String internalReferenceNumber) {
        this.internalReferenceNumber = internalReferenceNumber;
    }

    public Movementmetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Movementmetadata metadata) {
        this.metadata = metadata;
    }

    public MovementConnect getMovementConnect() {
        return movementConnect;
    }

    public void setMovementConnect(MovementConnect movementConnect) {
        this.movementConnect = movementConnect;
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

    public List<Movementarea> getMovementareaList() {
        return movementareaList;
    }

    public void setMovementareaList(List<Movementarea> movementareaList) {
        this.movementareaList = movementareaList;
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

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Segment getFromSegment() {
        return fromSegment;
    }

    public void setFromSegment(Segment fromSegment) {
        this.fromSegment = fromSegment;
    }

    public Segment getToSegment() {
        return toSegment;
    }

    public void setToSegment(Segment toSegment) {
        this.toSegment = toSegment;
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

    public Integer getAltitude() {
        return altitude;
    }

    public void setAltitude(Integer altitude) {
        this.altitude = altitude;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
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

    @XmlTransient
    public List<Areatransition> getAreatransitionList() {
        return areatransitionList;
    }

    public void setAreatransitionList(List<Areatransition> areatransitionList) {
        this.areatransitionList = areatransitionList;
    }

    public Boolean isProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    @Override
    public int compareTo(Movement o) {
        return MovementComparator.MOVEMENT.compare(this, o);
    }

}
