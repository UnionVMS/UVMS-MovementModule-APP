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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import eu.europa.ec.fisheries.uvms.movement.constant.UvmsConstants;

/**
 * The persistent class for the movementfiltergroup database table.
 * 
 */
@Entity
@NamedQueries({
    @NamedQuery(name = UvmsConstants.GROUP_VESSEL_FIND_ALL, query = "SELECT f FROM MovementFilterGroup f"),
    @NamedQuery(name = UvmsConstants.GROUP_VESSEL_BY_USER, query="SELECT f FROM MovementFilterGroup f WHERE f.user = :user")
})
public class MovementFilterGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "movefiltgrp_id")
    private Long id;

    @Column(name = "movefiltgrp_active")
    private String active;

    @Column(name = "movefiltgrp_global")
    private String global;

    @Column(name = "movefiltgrp_dynamic")
    private String dynamic;

    @Column(name = "movefiltgrp_name")
    private String name;

    @Column(name = "movefiltgrp_updattim")
    private Date updated;

    @Column(name = "movefiltgrp_upuser")
    private String updatedBy;

    @Column(name = "movefiltgrp_user_id")
    private String user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy="filterGroup")
    @Column(name = "movefilt_movefiltgrp_id")
    private List<MovementFilter> filters;

    public MovementFilterGroup() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getActive() {
        return active;
    }

    public void setActive(final String active) {
        this.active = active;
    }

    public String getGlobal() {
        return global;
    }

    public void setGlobal(final String global) {
        this.global = global;
    }

    public String getDynamic() {
        return dynamic;
    }

    public void setDynamic(final String dynamic) {
        this.dynamic = dynamic;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public void setFilters(final List<MovementFilter> filters) {
        this.filters = filters;
    }

    public List<MovementFilter> getFilters() {
        if (this.filters == null) {
            this.filters = new ArrayList<MovementFilter>();
        }
        return this.filters;
    }

    public String getFiltgrpDynamic() {
        return dynamic;
    }

    public void setFiltgrpDynamic(final String filtgrpDynamic) {
        this.dynamic = filtgrpDynamic;
    }
}