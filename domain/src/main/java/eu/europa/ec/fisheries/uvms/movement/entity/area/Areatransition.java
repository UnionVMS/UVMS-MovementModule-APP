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
package eu.europa.ec.fisheries.uvms.movement.entity.area;

import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementType;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 **/
@Entity
@Table(name = "areatransition")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Areatransition.findAll", query = "SELECT a FROM Areatransition a"),
    @NamedQuery(name = "Areatransition.findByAreatranId", query = "SELECT a FROM Areatransition a WHERE a.areatranId = :areatranId"),
    @NamedQuery(name = "Areatransition.findByAreatranUpdattim", query = "SELECT a FROM Areatransition a WHERE a.areatranUpdattim = :areatranUpdattim"),
    @NamedQuery(name = "Areatransition.findByAreatranUpuser", query = "SELECT a FROM Areatransition a WHERE a.areatranUpuser = :areatranUpuser")})
public class Areatransition implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "areatran_id")
    private Long areatranId;

    @Basic(optional = false)
    @NotNull
    @Column(name = "areatran_updattim")
    @Temporal(TemporalType.TIMESTAMP)
    private Date areatranUpdattim;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "areatran_upuser")
    private String areatranUpuser;
   
    @Column(name = "areatran_movetype_id")
    @Enumerated(EnumType.ORDINAL)
    private MovementTypeType movementType;

    @JoinColumn(name = "areatran_move_id", referencedColumnName = "move_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Movement areatranMoveId;

    @JoinColumn(name = "areatran_area_id", referencedColumnName = "area_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Area areatranAreaId;

    public Areatransition() {
    }

    public Areatransition(Long areatranId) {
        this.areatranId = areatranId;
    }

    public Areatransition(Long areatranId, Date areatranUpdattim, String areatranUpuser) {
        this.areatranId = areatranId;
        this.areatranUpdattim = areatranUpdattim;
        this.areatranUpuser = areatranUpuser;
    }

    public Long getAreatranId() {
        return areatranId;
    }

    public void setAreatranId(Long areatranId) {
        this.areatranId = areatranId;
    }

    public Date getAreatranUpdattim() {
        return areatranUpdattim;
    }

    public void setAreatranUpdattim(Date areatranUpdattim) {
        this.areatranUpdattim = areatranUpdattim;
    }

    public String getAreatranUpuser() {
        return areatranUpuser;
    }

    public void setAreatranUpuser(String areatranUpuser) {
        this.areatranUpuser = areatranUpuser;
    }

    /*public MovementType getAreatranMovetypeId() {
     return areatranMovetypeId;
     }

     public void setAreatranMovetypeId(MovementType areatranMovetypeId) {
     this.areatranMovetypeId = areatranMovetypeId;
     }*/
    public MovementTypeType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementTypeType movementType) {
        this.movementType = movementType;
    }

    public Movement getAreatranMoveId() {
        return areatranMoveId;
    }

    public void setAreatranMoveId(Movement areatranMoveId) {
        this.areatranMoveId = areatranMoveId;
    }

    public Area getAreatranAreaId() {
        return areatranAreaId;
    }

    public void setAreatranAreaId(Area areatranAreaId) {
        this.areatranAreaId = areatranAreaId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (areatranId != null ? areatranId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Areatransition)) {
            return false;
        }
        Areatransition other = (Areatransition) object;
        if ((this.areatranId == null && other.areatranId != null) || (this.areatranId != null && !this.areatranId.equals(other.areatranId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition[ areatranId=" + areatranId + " ]";
    }

}