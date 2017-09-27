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
package eu.europa.ec.fisheries.uvms.movement.entity.temp;

import eu.europa.ec.fisheries.uvms.movement.model.constants.TempMovementStateEnum;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;
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
    @NamedQuery(name = "TempMovement.findAll", query = "SELECT t FROM TempMovement t"),
    @NamedQuery(name = "TempMovement.findAllOrdered", query = "SELECT t FROM TempMovement t where t.state != 'DELETED' order by timestamp desc"),
    @NamedQuery(name = "TempMovement.findById", query = "SELECT t FROM TempMovement t WHERE t.id = :id"),
    @NamedQuery(name = "TempMovement.findByGuidId", query = "SELECT t FROM TempMovement t WHERE t.guid = :guid"),
    @NamedQuery(name = "TempMovement.findByFlag", query = "SELECT t FROM TempMovement t WHERE t.flag = :flag"),
    @NamedQuery(name = "TempMovement.findByIrcs", query = "SELECT t FROM TempMovement t WHERE t.ircs = :ircs"),
    @NamedQuery(name = "TempMovement.findByCfr", query = "SELECT t FROM TempMovement t WHERE t.cfr = :cfr"),
    @NamedQuery(name = "TempMovement.findByExternalMarkings", query = "SELECT t FROM TempMovement t WHERE t.externalMarkings = :externalMarkings"),
    @NamedQuery(name = "TempMovement.findByName", query = "SELECT t FROM TempMovement t WHERE t.name = :name"),
    @NamedQuery(name = "TempMovement.findByStatus", query = "SELECT t FROM TempMovement t WHERE t.status = :status"),
    @NamedQuery(name = "TempMovement.findByTimestamp", query = "SELECT t FROM TempMovement t WHERE t.timestamp = :timestamp"),
    @NamedQuery(name = "TempMovement.count", query = "SELECT count(t) FROM TempMovement t where t.state != 'DELETED'"),
    @NamedQuery(name = "TempMovement.findByLatitude", query = "SELECT t FROM TempMovement t WHERE t.latitude = :latitude"),
    @NamedQuery(name = "TempMovement.findByLongitude", query = "SELECT t FROM TempMovement t WHERE t.longitude = :longitude"),
    @NamedQuery(name = "TempMovement.findBySpeed", query = "SELECT t FROM TempMovement t WHERE t.speed = :speed"),
    @NamedQuery(name = "TempMovement.findByCourse", query = "SELECT t FROM TempMovement t WHERE t.course = :course"),
    @NamedQuery(name = "TempMovement.findByUpdated", query = "SELECT t FROM TempMovement t WHERE t.updated = :updated"),
    @NamedQuery(name = "TempMovement.findByUpdatedBy", query = "SELECT t FROM TempMovement t WHERE t.updatedBy = :updatedBy") })
//@formatter:on
@DynamicUpdate
@DynamicInsert
public class TempMovement implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "tmpmove_id")
    private Long id;

    @Size(max = 36)
    @Column(name = "tmpmove_guid")
    private String guid;

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

    @Column(name = "tmpmove_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

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

    @Basic(optional = false)
    @NotNull
    @Column(name = "tmpmove_updattim")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "tmpmove_upuser")
    private String updatedBy;

    public TempMovement() {
        this.guid = UUID.randomUUID().toString();
    }

    public TempMovement(Long id) {
        this.id = id;
    }

    public TempMovement(Long id, Date updated, String updatedBy) {
        this.id = id;
        this.updated = updated;
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
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

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
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
        return "TempMovement [id=" + id + ", guid=" + guid + ", flag=" + flag + ", ircs=" + ircs + ", cfr=" + cfr + ", externalMarkings=" + externalMarkings + ", name=" + name + ", status=" + status + ", timestamp=" + timestamp + ", state=" + state + ", latitude=" + latitude + ", longitude=" + longitude + ", speed=" + speed + ", course=" + course + ", updated=" + updated + ", updatedBy=" + updatedBy + "]";
    }
}