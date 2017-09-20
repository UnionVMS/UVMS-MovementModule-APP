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
package eu.europa.ec.fisheries.uvms.movement.entity.group;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

/**
 * The persistent class for the filter database table.
 *
 */
@Entity
@NamedQuery(name = "Filter.findAll", query = "SELECT f FROM MovementFilter f")
public class MovementFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "movefilt_id")
    private Long id;

    @Column(name = "movefilt_field")
    private String field;

    @Column(name = "movefilt_upuser")
    private String updatedBy;

    @Column(name = "movefilt_value")
    private String value;

    @Column(name = "movefilt_updattim")
    private Date updated;

    @ManyToOne
    @JoinColumn(name="movefilt_movefiltgrp_id")
    private MovementFilterGroup filterGroup;

    @Column(name = "movefilt_movefilttyp_id")
    @Enumerated(EnumType.ORDINAL)
    private SearchKeyType movementFilterType;

    public MovementFilter() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(final String field) {
        this.field = field;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(final String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(final Date updated) {
        this.updated = updated;
    }

    public MovementFilterGroup getFilterGroup() {
        return filterGroup;
    }

    public void setFilterGroup(final MovementFilterGroup filterGroup) {
        this.filterGroup = filterGroup;
    }

    public SearchKeyType getMovementFilterType() {
        return movementFilterType;
    }

    public void setMovementFilterType(final SearchKeyType movementFilterType) {
        this.movementFilterType = movementFilterType;
    }

}