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
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.model.OffsetDateTimeDeserializer;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 **/
@Entity
@Table(name = "movementtype")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MovementType.findAll", query = "SELECT m FROM MovementType m"),
    @NamedQuery(name = "MovementType.findById", query = "SELECT m FROM MovementType m WHERE m.id = :id"),
    @NamedQuery(name = "MovementType.findByName", query = "SELECT m FROM MovementType m WHERE m.name = :name"),
    @NamedQuery(name = "MovementType.findByDescription", query = "SELECT m FROM MovementType m WHERE m.description = :description"),
    @NamedQuery(name = "MovementType.findByUpdated", query = "SELECT m FROM MovementType m WHERE m.updated = :updated"),
    @NamedQuery(name = "MovementType.findByUpdatedBy", query = "SELECT m FROM MovementType m WHERE m.updatedBy = :updatedBy")})
@DynamicUpdate
@DynamicInsert
public class MovementType implements Serializable {

    /*@OneToMany(mappedBy = "areatranMovetypeId", fetch = FetchType.LAZY)
     private List<Areatransition> areatransitionList;
     private static final long serialVersionUID = 1L;*/
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "movetyp_id")
    private Long id;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "movetyp_name")
    private String name;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "movetyp_desc")
    private String description;

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    @NotNull
    @Column(name = "movetyp_updattim")
    @Temporal(TemporalType.TIMESTAMP)
    private OffsetDateTime updated;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "movetyp_upuser")
    private String updatedBy;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "movementType", fetch = FetchType.LAZY)
    private List<Movement> movementList;

    public MovementType() {
    }

    public MovementType(Long id) {
        this.id = id;
    }

    public MovementType(Long id, String name, String description, OffsetDateTime updated, String updatedBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.updated = updated;
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @XmlTransient
    public List<Movement> getMovementList() {
        return movementList;
    }

    public void setMovementList(List<Movement> movementList) {
        this.movementList = movementList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MovementType)) {
            return false;
        }
        MovementType other = (MovementType) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "eu.europa.ec.fisheries.uvms.movement.entity.MovementType[ id=" + id + " ]";
    }

    /*@XmlTransient
     public List<Areatransition> getAreatransitionList() {
     return areatransitionList;
     }

     public void setAreatransitionList(List<Areatransition> areatransitionList) {
     this.areatransitionList = areatransitionList;
     }*/
}