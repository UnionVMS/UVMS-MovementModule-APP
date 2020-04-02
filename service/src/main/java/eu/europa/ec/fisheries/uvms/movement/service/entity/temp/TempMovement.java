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
package eu.europa.ec.fisheries.uvms.movement.service.entity.temp;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import eu.europa.ec.fisheries.uvms.movement.model.constants.TempMovementStateEnum;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 **/
//@formatter:off
@Entity
@Table(name = "tempmovement")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = TempMovement.FIND_ALL, query = "SELECT t FROM TempMovement t"),
    @NamedQuery(name = TempMovement.FIND_ALL_ORDERED, query = "SELECT t FROM TempMovement t where t.state != 'DELETED' order by timestamp desc"),
    @NamedQuery(name = TempMovement.FIND_BY_ID, query = "SELECT t FROM TempMovement t WHERE t.id = :id"),
    @NamedQuery(name = TempMovement.FIND_BY_FLAG, query = "SELECT t FROM TempMovement t WHERE t.flag = :flag"),
    @NamedQuery(name = TempMovement.FIND_BY_IRCS, query = "SELECT t FROM TempMovement t WHERE t.ircs = :ircs"),
    @NamedQuery(name = TempMovement.FIND_BY_CFR, query = "SELECT t FROM TempMovement t WHERE t.cfr = :cfr"),
    @NamedQuery(name = TempMovement.FIND_BY_EXTERNAL_MARKING, query = "SELECT t FROM TempMovement t WHERE t.externalMarkings = :externalMarkings"),
    @NamedQuery(name = TempMovement.FIND_BY_NAME, query = "SELECT t FROM TempMovement t WHERE t.name = :name"),
    @NamedQuery(name = TempMovement.FIND_BY_STATUS, query = "SELECT t FROM TempMovement t WHERE t.status = :status"),
    @NamedQuery(name = TempMovement.FIND_BY_TIMESTAMP, query = "SELECT t FROM TempMovement t WHERE t.timestamp = :timestamp"),
    @NamedQuery(name = TempMovement.COUNT, query = "SELECT count(t) FROM TempMovement t where t.state != 'DELETED'"),
    @NamedQuery(name = TempMovement.FIND_BY_LATITUDE, query = "SELECT t FROM TempMovement t WHERE t.latitude = :latitude"),
    @NamedQuery(name = TempMovement.FIND_BY_LONGITUDE, query = "SELECT t FROM TempMovement t WHERE t.longitude = :longitude"),
    @NamedQuery(name = TempMovement.FIND_BY_SPEED, query = "SELECT t FROM TempMovement t WHERE t.speed = :speed"),
    @NamedQuery(name = TempMovement.FIND_BY_COURSE, query = "SELECT t FROM TempMovement t WHERE t.course = :course"),
    @NamedQuery(name = TempMovement.FIND_BY_UPDATED, query = "SELECT t FROM TempMovement t WHERE t.updated = :updated"),
    @NamedQuery(name = TempMovement.FIND_BY_UPDATED_BY, query = "SELECT t FROM TempMovement t WHERE t.updatedBy = :updatedBy") })
//@formatter:on
@DynamicUpdate
@DynamicInsert
public class TempMovement implements Serializable {
    
    public static final String FIND_ALL = "TempMovement.findAll";
    public static final String FIND_ALL_ORDERED = "TempMovement.findAllOrdered";
    public static final String FIND_BY_ID = "TempMovement.findById";
    public static final String FIND_BY_FLAG = "TempMovement.findByFlag";
    public static final String FIND_BY_IRCS = "TempMovement.findByIrcs";
    public static final String FIND_BY_CFR = "TempMovement.findByCfr";
    public static final String FIND_BY_EXTERNAL_MARKING = "TempMovement.findByExternalMarkings";
    public static final String FIND_BY_NAME = "TempMovement.findByName";
    public static final String FIND_BY_STATUS = "TempMovement.findByStatus";
    public static final String FIND_BY_TIMESTAMP = "TempMovement.findByTimestamp";
    public static final String COUNT = "TempMovement.count";
    public static final String FIND_BY_LATITUDE = "TempMovement.findByLatitude";
    public static final String FIND_BY_LONGITUDE = "TempMovement.findByLongitude";
    public static final String FIND_BY_SPEED = "TempMovement.findBySpeed";
    public static final String FIND_BY_COURSE = "TempMovement.findByCourse";
    public static final String FIND_BY_UPDATED = "TempMovement.findByUpdated";
    public static final String FIND_BY_UPDATED_BY = "TempMovement.findByUpdatedBy";
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Basic(optional = false)
    @Column(name = "tmpmove_id")
    private String id;

    @Size(max = 3)
    @Column(name = "tmpmove_flag")
    private String flag;

    @Size(max = 70)
    @Column(name = "tmpmove_ircs")
    private String ircs;

    @Size(max = 12)
    @Column(name = "tmpmove_cfr")
    private String cfr;

    @Size(max = 14)
    @Column(name = "tmpmove_extmark")
    private String externalMarkings;

    @Size(max = 30)
    @Column(name = "tmpmove_name")
    private String name;

    @Size(max = 60)
    @Column(name = "tmpmove_status")
    private String status;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    @Column(name = "tmpmove_timestamp")
    private Instant timestamp;

    //@Column(name = "tmpmove_archive")
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "tmpmove_state")
    private TempMovementStateEnum state;

    // @Max(value=?) @Min(value=?)//if you know range of your decimal fields
    // consider using these annotations to enforce field validation
    @Column(name = "tmpmove_lat")
    private Double latitude;

    @Column(name = "tmpmove_lon")
    private Double longitude;

    @Column(name = "tmpmove_speed")
    private Double speed;

    @Column(name = "tmpmove_course")
    private Double course;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    @NotNull
    @Column(name = "tmpmove_updattim")
    private Instant updated;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "tmpmove_upuser")
    private String updatedBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getIrcs() {
        return ircs;
    }

    public void setIrcs(String ircs) {
        this.ircs = ircs;
    }

    public String getCfr() {
        return cfr;
    }

    public void setCfr(String cfr) {
        this.cfr = cfr;
    }

    public String getExternalMarkings() {
        return externalMarkings;
    }

    public void setExternalMarkings(String externalMarkings) {
        this.externalMarkings = externalMarkings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getCourse() {
        return course;
    }

    public void setCourse(Double course) {
        this.course = course;
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

    public TempMovementStateEnum getState() {
        return state;
    }

    public void setState(TempMovementStateEnum state) {
        this.state = state;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are
        // not set
        if (!(object instanceof TempMovement)) {
            return false;
        }
        TempMovement other = (TempMovement) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TempMovement [id=" + id + ", flag=" + flag + ", ircs=" + ircs + ", cfr=" + cfr + ", externalMarkings=" + externalMarkings + ", name=" + name + ", status=" + status + ", timestamp=" + timestamp + ", state=" + state + ", latitude=" + latitude + ", longitude=" + longitude + ", speed=" + speed + ", course=" + course + ", updated=" + updated + ", updatedBy=" + updatedBy + "]";
    }
}