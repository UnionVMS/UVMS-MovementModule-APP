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
import org.locationtech.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movement", indexes = {
        @Index(columnList = "move_moveconn_id", name = "movement_moveconn_fk_idx", unique = false),
        @Index(columnList = "move_trac_id", name = "movement_trac_fk_idx", unique = false),
        @Index(columnList = "move_moveconn_id, move_timestamp", name = "movement_count_idx", unique = false)
})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = Movement.FIND_ALL_BY_TRACK, query = "SELECT new eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovement(m.location, m.heading, m.id, m.timestamp, m.speed, m.movementSource) FROM Movement m WHERE m.track = :track ORDER BY m.timestamp DESC"),
    @NamedQuery(name = Movement.FIND_ALL_LOCATIONS_BY_TRACK, query = "SELECT m.location FROM Movement m WHERE m.track = :track ORDER BY m.timestamp DESC"),
    @NamedQuery(name = Movement.FIND_ALL_BY_MOVEMENTCONNECT, query = "SELECT m FROM Movement m WHERE m.movementConnect = :movementConnect ORDER BY m.timestamp ASC"),
    @NamedQuery(name = Movement.FIND_LATEST_BY_MOVEMENT_CONNECT, query = "SELECT m FROM Movement m WHERE m.movementConnect.id = :connectId ORDER BY m.timestamp DESC"),
    @NamedQuery(name = Movement.FIND_PREVIOUS, query = "SELECT m FROM Movement m  WHERE  m.timestamp = (select max(mm.timestamp) from Movement mm where mm.movementConnect.id = :id and mm.timestamp < :date) AND m.movementConnect.id = :id "),
    @NamedQuery(name = Movement.FIND_FIRST, query = "SELECT m FROM Movement m  WHERE m.timestamp = (select min(mm.timestamp) from Movement mm  where mm.movementConnect.id = :id AND mm.timestamp > :date) AND m.movementConnect.id = :id "),
    @NamedQuery(name = Movement.FIND_EXISTING_DATE, query = "SELECT m FROM Movement m WHERE m.movementConnect.id = :id AND m.timestamp = :date "),
    @NamedQuery(name = Movement.NR_OF_MOVEMENTS_FOR_ASSET_IN_TIMESPAN, query = "SELECT COUNT (m) FROM Movement m WHERE m.timestamp BETWEEN :fromDate AND :toDate AND m.movementConnect.id = :asset "),

    @NamedQuery(name = MicroMovementExtended.FIND_ALL_AFTER_DATE, query = "SELECT new eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended(m.location, m.heading, m.id, m.movementConnect.id, m.timestamp, m.speed, m.movementSource) FROM Movement m WHERE m.timestamp > :date "),
    @NamedQuery(name = MicroMovementExtended.FIND_ALL_FOR_ASSET_AFTER_DATE, query = "SELECT new eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovement(m.location, m.heading, m.id, m.timestamp, m.speed, m.movementSource) FROM Movement m WHERE m.timestamp > :startDate AND m.timestamp < :endDate AND m.movementConnect.id = :id ORDER BY m.timestamp DESC"),
    @NamedQuery(name = MicroMovementExtended.FIND_ALL_FOR_CONNECT_IDS_BETWEEN_DATES, query = "SELECT new eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended(m.location, m.heading, m.id, m.movementConnect.id, m.timestamp, m.speed, m.movementSource) FROM Movement m WHERE m.timestamp >= :fromDate AND m.timestamp <= :toDate AND m.movementConnect.id in :connectIds ORDER BY m.timestamp DESC"),
    @NamedQuery(name = Movement.FIND_LATEST_SINCE, query = "SELECT new eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended(m.location, m.heading, m.id, m.movementConnect.id, m.timestamp, m.speed, m.movementSource) FROM Movement m JOIN MovementConnect mc ON m.id = mc.latestMovement.id WHERE mc.updated > :date"),


    @NamedQuery(name = Movement.FIND_LATESTMOVEMENT_BY_MOVEMENT_CONNECT, query = "SELECT m FROM Movement m JOIN MovementConnect mc ON m.id = mc.latestMovement.id WHERE m.movementConnect.id = :connectId"),
    @NamedQuery(name = Movement.FIND_LATESTMOVEMENT_BY_MOVEMENT_CONNECT_LIST, query = "SELECT m FROM Movement m JOIN MovementConnect mc ON m.id = mc.latestMovement.id WHERE m.movementConnect.id in :connectId"),
    @NamedQuery(name = Movement.FIND_LATEST, query = "SELECT mc.latestMovement FROM MovementConnect mc ORDER BY mc.updated DESC")

})
@DynamicUpdate
@DynamicInsert
public class Movement implements Serializable, Comparable<Movement> {

    public static final String FIND_ALL_BY_TRACK = "Movement.findAllByTrack";
    public static final String FIND_ALL_LOCATIONS_BY_TRACK = "Movement.findAllPointsByTrack";
    public static final String FIND_ALL_BY_MOVEMENTCONNECT = "Movement.findAllByMovementConnect";
    public static final String FIND_LATEST_BY_MOVEMENT_CONNECT = "Movement.findLatestByMovementConnect";
    public static final String FIND_PREVIOUS = "Movement.findPrevious";
    public static final String FIND_FIRST = "Movement.findFirst";
    public static final String FIND_EXISTING_DATE = "Movement.findExistingDate";
    public static final String NR_OF_MOVEMENTS_FOR_ASSET_IN_TIMESPAN = "Movement.nrOfMovementsForAssetInTimespan";
    public static final String FIND_LATEST_SINCE = "Movement.findLatestSince";
    public static final String FIND_LATESTMOVEMENT_BY_MOVEMENT_CONNECT = "Movement.findLatestMovementByMovementConnect";
    public static final String FIND_LATESTMOVEMENT_BY_MOVEMENT_CONNECT_LIST = "Movement.findLatestMovementByMovementConnectList";
    public static final String FIND_LATEST = "Movement.findLatest";



    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", name = "move_id")
    private UUID id;

    @NotNull
    @Column(name = "move_location", columnDefinition = "Geometry")
    private Point location;

    @Column(name = "move_speed")
    private Float speed;

    @Column(name = "move_heading")
    private Float heading;

    @Column(name = "move_trip_number")
    private Double tripNumber;

    @Column(name = "move_internal_reference_number")
    @Size(max = 12)
    private String internalReferenceNumber;

    @Size(max = 60)
    @Column(name = "move_status")
    private String status;

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
    @Column(name = "move_lesreporttime")
    private Instant lesReportTime;

    @Column(name = "move_satellite_id")
    private short sourceSatelliteId;

	@JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    @NotNull
    @Column(name = "move_updattim")
    private Instant updated;

    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "move_upuser")
    private String updatedBy;


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
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

    public MovementConnect getMovementConnect() {
        return movementConnect;
    }

    public void setMovementConnect(MovementConnect movementConnect) {
        this.movementConnect = movementConnect;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public Float getHeading() {
        return heading;
    }

    public void setHeading(Float heading) {
        this.heading = heading;
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

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public Instant getLesReportTime() {
		return lesReportTime;
	}

	public void setLesReportTime(Instant lesReportTime) {
		this.lesReportTime = lesReportTime;
	}

    public short getSourceSatelliteId() {
        return sourceSatelliteId;
    }

    public void setSourceSatelliteId(short sourceSatelliteId) {
        this.sourceSatelliteId = sourceSatelliteId;
    }

    @Override
    public int compareTo(Movement o) {
        if (o == null) {
            return ObjectUtils.compare(this, null);
        } else {
            return ObjectUtils.compare(this.getTimestamp(), o.getTimestamp());
        }
    }

}
