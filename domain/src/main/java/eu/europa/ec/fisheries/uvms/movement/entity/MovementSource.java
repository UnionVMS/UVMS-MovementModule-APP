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

import java.io.Serializable;
import java.util.Date;
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
@Table(name = "movementsource")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MovementSource.findAll", query = "SELECT m FROM MovementSource m"),
    @NamedQuery(name = "MovementSource.findById", query = "SELECT m FROM MovementSource m WHERE m.id = :id"),
    @NamedQuery(name = "MovementSource.findByName", query = "SELECT m FROM MovementSource m WHERE m.name = :name"),
    @NamedQuery(name = "MovementSource.findByDescription", query = "SELECT m FROM MovementSource m WHERE m.description = :description"),
    @NamedQuery(name = "MovementSource.findByUpdated", query = "SELECT m FROM MovementSource m WHERE m.updated = :updated"),
    @NamedQuery(name = "MovementSource.findByUpdatedBy", query = "SELECT m FROM MovementSource m WHERE m.updatedBy = :updatedBy")})
public class MovementSource implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "movesour_id")
    private Long id;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "movesour_name")
    private String name;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "movesour_desc")
    private String description;

    @Basic(optional = false)
    @NotNull
    @Column(name = "movesour_updattim")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "movesour_upuser")
    private String updatedBy;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "movementSource", fetch = FetchType.LAZY)
    private List<Movement> movementList;

    public MovementSource() {
    }

    public MovementSource(final Long id) {
        this.id = id;
    }

    public MovementSource(final Long id, final String name, final String description, final Date updated, final String updatedBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.updated = updated;
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(final Date updated) {
        this.updated = updated;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(final String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @XmlTransient
    public List<Movement> getMovementList() {
        return movementList;
    }

    public void setMovementList(final List<Movement> movementList) {
        this.movementList = movementList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MovementSource)) {
            return false;
        }
        final MovementSource other = (MovementSource) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "eu.europa.ec.fisheries.uvms.movement.entity.MovementSource[ id=" + id + " ]";
    }
    
}