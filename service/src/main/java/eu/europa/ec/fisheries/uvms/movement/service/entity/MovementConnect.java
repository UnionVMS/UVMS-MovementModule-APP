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

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import eu.europa.ec.fisheries.uvms.movement.service.util.MovementComparator;

@Entity
@Table(name = "movementconnect", uniqueConstraints = {
        @UniqueConstraint(name = "moveconn_value_unique", columnNames = "moveconn_value")
})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = MovementConnect.MOVEMENT_CONNECT_BY_CONNECT_ID, query = "SELECT m FROM MovementConnect m WHERE m.value = :value"),
    @NamedQuery(name = MovementConnect.MOVEMENT_CONNECT_GET_ALL, query = "SELECT m FROM MovementConnect m")
})
@DynamicUpdate
@DynamicInsert
public class MovementConnect implements Serializable, Comparable<MovementConnect> {

    public static final String MOVEMENT_CONNECT_BY_CONNECT_ID = "MovementConnect.findByValue";
    public static final String MOVEMENT_CONNECT_GET_ALL = "MovementConnect.findAll";
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "movement_connect_seq")
    @Basic(optional = false)
    @Column(name = "moveconn_id")
    private Long id;

    @Basic(optional = false)
    @NotNull
    @Column(name = "moveconn_value")
    private String value;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    @NotNull
    @Column(name = "moveconn_updattim")
    private Instant updated;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "moveconn_upuser")
    private String updatedBy;

    public MovementConnect() {
    }

    public MovementConnect(Long id) {
        this.id = id;
    }

    public MovementConnect(Long id, String value, Instant updated, String updatedBy) {
        this.id = id;
        this.value = value;
        this.updated = updated;
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value ;
    }

    public void setValue(String value) {
        this.value = value;
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

	@Override
	public int compareTo(MovementConnect o2) {
		return MovementComparator.MOVEMENT_CONNECT.compare(this, o2);
	}

}
