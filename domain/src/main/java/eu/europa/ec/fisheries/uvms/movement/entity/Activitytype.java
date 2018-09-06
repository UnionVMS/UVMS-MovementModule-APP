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
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 **/
@Entity
@Table(name = "activitytype")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Activitytype.findAll", query = "SELECT a FROM Activitytype a"),
    @NamedQuery(name = "Activitytype.findByActtypId", query = "SELECT a FROM Activitytype a WHERE a.acttypId = :acttypId"),
    @NamedQuery(name = "Activitytype.findByActtypName", query = "SELECT a FROM Activitytype a WHERE a.acttypName = :acttypName"),
    @NamedQuery(name = "Activitytype.findByActtypDesc", query = "SELECT a FROM Activitytype a WHERE a.acttypDesc = :acttypDesc"),
    @NamedQuery(name = "Activitytype.findByActtypUpdattim", query = "SELECT a FROM Activitytype a WHERE a.acttypUpdattim = :acttypUpdattim"),
    @NamedQuery(name = "Activitytype.findByActtypUpuser", query = "SELECT a FROM Activitytype a WHERE a.acttypUpuser = :acttypUpuser")})
@DynamicUpdate
@DynamicInsert
public class Activitytype implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "acttyp_id")
    private Long acttypId;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "acttyp_name")
    private String acttypName;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "acttyp_desc")
    private String acttypDesc;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    @NotNull
    @Column(name = "acttyp_updattim")
    private Instant acttypUpdattim;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "acttyp_upuser")
    private String acttypUpuser;

    /*@OneToMany(cascade = CascadeType.ALL, mappedBy = "actActtypId", fetch = FetchType.LAZY)
     private List<Activity> activityList;*/
    public Long getActtypId() {
        return acttypId;
    }

    public void setActtypId(Long acttypId) {
        this.acttypId = acttypId;
    }

    public String getActtypName() {
        return acttypName;
    }

    public void setActtypName(String acttypName) {
        this.acttypName = acttypName;
    }

    public String getActtypDesc() {
        return acttypDesc;
    }

    public void setActtypDesc(String acttypDesc) {
        this.acttypDesc = acttypDesc;
    }

    public Instant getActtypUpdattim() {
        return acttypUpdattim;
    }

    public void setActtypUpdattim(Instant acttypUpdattim) {
        this.acttypUpdattim = acttypUpdattim;
    }

    public String getActtypUpuser() {
        return acttypUpuser;
    }

    public void setActtypUpuser(String acttypUpuser) {
        this.acttypUpuser = acttypUpuser;
    }

    /*@XmlTransient
     public List<Activity> getActivityList() {
     return activityList;
     }

     public void setActivityList(List<Activity> activityList) {
     this.activityList = activityList;
     }*/
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (acttypId != null ? acttypId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Activitytype)) {
            return false;
        }
        Activitytype other = (Activitytype) object;
        if ((this.acttypId == null && other.acttypId != null) || (this.acttypId != null && !this.acttypId.equals(other.acttypId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "eu.europa.ec.fisheries.uvms.movement.entity.area.Activitytype[ acttypId=" + acttypId + " ]";
    }

}