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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import java.io.Serializable;
import java.time.OffsetDateTime;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import eu.europa.ec.fisheries.uvms.movement.model.OffsetDateTimeDeserializer;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 **/
@Entity
@Table(name = "movementarea")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Movementarea.findAll", query = "SELECT m FROM Movementarea m")})
@DynamicUpdate
@DynamicInsert
public class Movementarea implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "movarea_id")
    private Long movareaId;

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    @NotNull
    @Column(name = "movarea_updattim")
    private OffsetDateTime movareaUpdattim;

    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "movarea_upuser")
    private String movareaUpuser;

    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "movarea_move_id", referencedColumnName = "move_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Movement movareaMoveId;

    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "movarea_area_id", referencedColumnName = "area_id")
    @ManyToOne(optional = false)
    private Area movareaAreaId;

    public Movementarea() {
    }

    public Movementarea(Long movareaId) {
        this.movareaId = movareaId;
    }

    public Movementarea(Long movareaId, OffsetDateTime movareaUpdattim, String movareaUpuser) {
        this.movareaId = movareaId;
        this.movareaUpdattim = movareaUpdattim;
        this.movareaUpuser = movareaUpuser;
    }

    public Long getMovareaId() {
        return movareaId;
    }

    public void setMovareaId(Long movareaId) {
        this.movareaId = movareaId;
    }

    public OffsetDateTime getMovareaUpdattim() {
        return movareaUpdattim;
    }

    public void setMovareaUpdattim(OffsetDateTime movareaUpdattim) {
        this.movareaUpdattim = movareaUpdattim;
    }

    public String getMovareaUpuser() {
        return movareaUpuser;
    }

    public void setMovareaUpuser(String movareaUpuser) {
        this.movareaUpuser = movareaUpuser;
    }

    public Movement getMovareaMoveId() {
        return movareaMoveId;
    }

    public void setMovareaMoveId(Movement movareaMoveId) {
        this.movareaMoveId = movareaMoveId;
    }

    public Area getMovareaAreaId() {
        return movareaAreaId;
    }

    public void setMovareaAreaId(Area movareaAreaId) {
        this.movareaAreaId = movareaAreaId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (movareaId != null ? movareaId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Movementarea)) {
            return false;
        }
        Movementarea other = (Movementarea) object;
        if ((this.movareaId == null && other.movareaId != null) || (this.movareaId != null && !this.movareaId.equals(other.movareaId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "eu.europa.ec.fisheries.uvms.movement.entity.Movementarea[ movareaId=" + movareaId + " ]";
    }

}