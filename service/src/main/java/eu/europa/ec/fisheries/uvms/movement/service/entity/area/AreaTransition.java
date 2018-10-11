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
package eu.europa.ec.fisheries.uvms.movement.service.entity.area;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "areatransition", indexes = {
        @Index(columnList = "areatran_area_id", name = "areatransition_i_1", unique = false),
        @Index(columnList = "areatran_move_id", name = "areatransition_i_2", unique = false),
        @Index(columnList = "areatran_movetype_id", name = "areatransition_i_3", unique = false)
})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AreaTransition.findAll", query = "SELECT a FROM AreaTransition a"),
    @NamedQuery(name = "AreaTransition.findById", query = "SELECT a FROM AreaTransition a WHERE a.id = :areatranId"),
    @NamedQuery(name = "AreaTransition.findByUpdateTime", query = "SELECT a FROM AreaTransition a WHERE a.updateTime = :areatranUpdattim"),
    @NamedQuery(name = "AreaTransition.findByUpdateUser", query = "SELECT a FROM AreaTransition a WHERE a.updateUser = :areatranUpuser")})
@DynamicUpdate
@DynamicInsert
public class AreaTransition implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "area_trans_seq")
    @Basic(optional = false)
    @Column(name = "areatran_id")
    private Long id;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    @NotNull
    @Column(name = "areatran_updattim")
    private Instant updateTime;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "areatran_upuser")
    private String updateUser;
   
    @Column(name = "areatran_movetype_id")
    @Enumerated(EnumType.ORDINAL)
    private MovementTypeType movementType;

    @JoinColumn(name = "areatran_move_id", referencedColumnName = "move_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Movement movementId;

    @JoinColumn(name = "areatran_area_id", referencedColumnName = "area_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Area areaId;

    public AreaTransition() {
    }

    public AreaTransition(Long id) {
        this.id = id;
    }

    public AreaTransition(Long id, Instant updateTime, String updateUser) {
        this.id = id;
        this.updateTime = updateTime;
        this.updateUser = updateUser;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Instant updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public MovementTypeType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementTypeType movementType) {
        this.movementType = movementType;
    }

    public Movement getMovementId() {
        return movementId;
    }

    public void setMovementId(Movement movementId) {
        this.movementId = movementId;
    }

    public Area getAreaId() {
        return areaId;
    }

    public void setAreaId(Area areaId) {
        this.areaId = areaId;
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
        if (!(object instanceof AreaTransition)) {
            return false;
        }
        AreaTransition other = (AreaTransition) object;

        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "eu.europa.ec.fisheries.uvms.movement.entity.area.AreaTransition[ id = " + id + " ]";
    }

}