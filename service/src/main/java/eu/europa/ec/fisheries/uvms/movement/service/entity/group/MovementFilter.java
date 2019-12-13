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
package eu.europa.ec.fisheries.uvms.movement.service.entity.group;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKeyType;
import eu.europa.ec.fisheries.uvms.commons.date.JsonBInstantDeserializer;
import eu.europa.ec.fisheries.uvms.commons.date.UVMSInstantDeserializer;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movementfilter", indexes = {
        @Index(columnList = "movefilt_movefiltgrp_id", name = "movementfilter_filtergroup_fk_inx", unique = false),
        @Index(columnList = "movefilt_movefilttyp_id", name = "movementfilter_movefilttyp_fk_inx", unique = false)
})
@NamedQuery(name = "Filter.findAll", query = "SELECT f FROM MovementFilter f")
@DynamicUpdate
@DynamicInsert
public class MovementFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", name = "movefilt_id")
    private UUID id;

    @Column(name = "movefilt_field")
    private String field;

    @Column(name = "movefilt_upuser")
    private String updatedBy;

    @Column(name = "movefilt_value")
    private String value;

    @JsonDeserialize(using = UVMSInstantDeserializer.class)
    @JsonbTypeAdapter(JsonBInstantDeserializer.class)
    @Column(name = "movefilt_updattim")
    private Instant updated;

    @ManyToOne
    @JoinColumn(name="movefilt_movefiltgrp_id")
    private MovementFilterGroup filterGroup;

    @Column(name = "movefilt_movefilttyp_id")
    @Enumerated(EnumType.ORDINAL)
    private SearchKeyType movementFilterType;

    public MovementFilter() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getValue() {
        return value;
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

    public MovementFilterGroup getFilterGroup() {
        return filterGroup;
    }

    public void setFilterGroup(MovementFilterGroup filterGroup) {
        this.filterGroup = filterGroup;
    }

    public SearchKeyType getMovementFilterType() {
        return movementFilterType;
    }

    public void setMovementFilterType(SearchKeyType movementFilterType) {
        this.movementFilterType = movementFilterType;
    }

}
