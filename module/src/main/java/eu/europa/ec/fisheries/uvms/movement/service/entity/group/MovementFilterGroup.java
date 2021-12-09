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

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "movementfiltergroup")
@NamedQuery(name = MovementFilterGroup.GROUP_VESSEL_FIND_ALL, query = "SELECT f FROM MovementFilterGroup f")
@NamedQuery(name = MovementFilterGroup.GROUP_VESSEL_BY_USER, query="SELECT f FROM MovementFilterGroup f WHERE f.user = :user")
@DynamicUpdate
@DynamicInsert
public class MovementFilterGroup implements Serializable {
    
    public static final String GROUP_VESSEL_FIND_ALL = "Filtergroup.findAll";
    public static final String GROUP_VESSEL_BY_USER = "Filtergroup.findByUser";
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", name = "movefiltgrp_id")
    private UUID id;

    @Column(name = "movefiltgrp_active")
    private String active;

    @Column(name = "movefiltgrp_global")
    private String global;

    @Column(name = "movefiltgrp_dynamic")
    private String dynamic;

    @Column(name = "movefiltgrp_name")
    private String name;

    @NotNull
    @Column(name = "movefiltgrp_updattim")
    private Instant updated;

    @NotNull
    @Column(name = "movefiltgrp_upuser")
    private String updatedBy;

    @Column(name = "movefiltgrp_user_id")
    private String user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy="filterGroup")
    @Column(name = "movefilt_movefiltgrp_id")
    private List<MovementFilter> filters;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getGlobal() {
        return global;
    }

    public void setGlobal(String global) {
        this.global = global;
    }

    public String getDynamic() {
        return dynamic;
    }

    public void setDynamic(String dynamic) {
        this.dynamic = dynamic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setFilters(List<MovementFilter> filters) {
        this.filters = filters;
    }

    public List<MovementFilter> getFilters() {
        if (this.filters == null) {
            this.filters = new ArrayList<>();
        }
        return this.filters;
    }

    public String getFiltgrpDynamic() {
        return dynamic;
    }

    public void setFiltgrpDynamic(String filtgrpDynamic) {
        this.dynamic = filtgrpDynamic;
    }
}
