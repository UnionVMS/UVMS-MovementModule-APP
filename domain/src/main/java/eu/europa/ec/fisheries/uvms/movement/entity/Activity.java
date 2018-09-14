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
import java.time.Instant;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityTypeType;
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;


/**
 **/
@Entity
@Table(name = "activity")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Activity.findAll", query = "SELECT a FROM Activity a"),
    @NamedQuery(name = "Activity.findById", query = "SELECT a FROM Activity a WHERE a.id = :id"),
    @NamedQuery(name = "Activity.findByMessageId", query = "SELECT a FROM Activity a WHERE a.messageId = :messageId"),
    @NamedQuery(name = "Activity.findByCallback", query = "SELECT a FROM Activity a WHERE a.callback = :callback"),
    @NamedQuery(name = "Activity.findByUpdated", query = "SELECT a FROM Activity a WHERE a.updated = :updated"),
    @NamedQuery(name = "Activity.findByUpdatedBy", query = "SELECT a FROM Activity a WHERE a.updatedBy = :updatedBy")})
@DynamicUpdate
@DynamicInsert
public class Activity implements Serializable {

    /**
     * @JoinColumn(name = "act_acttyp_id", referencedColumnName = "acttyp_id")
     * @ManyToOne(optional = false, fetch = FetchType.LAZY) private Activitytype actActtypId;
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "act_id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "act_messid")
    private String messageId;

    @Basic(optional = false)
    @Size(min = 1, max = 400)
    @Column(name = "act_callback")
    private String callback;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    @NotNull
    @Column(name = "act_updattim")
    private Instant updated;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "act_upuser")
    private String updatedBy;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "activity", fetch = FetchType.LAZY)
    private List<Movement> movementList;

    @Column(name = "act_acttyp_id")
    @Enumerated(EnumType.ORDINAL)
    private MovementActivityTypeType activityType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
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

    public List<Movement> getMovementList() {
        return movementList;
    }

    public void setMovementList(List<Movement> movementList) {
        this.movementList = movementList;
    }

    public MovementActivityTypeType getActivityType() {
        return activityType;
    }

    public void setActivityType(MovementActivityTypeType activityType) {
        this.activityType = activityType;
    }

    /*public Activitytype getActActtypId() {
     return actActtypId;
     }

     public void setActActtypId(Activitytype actActtypId) {
     this.actActtypId = actActtypId;
     }*/
}