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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 **/
@Entity
@Table(name = "parameter")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Parameter.findAll", query = "SELECT p FROM Parameter p")})
public class Parameter implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "param_id")
    private String paramId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "param_description")
    private String paramDescription;
    @Size(max = 500)
    @Column(name = "param_value")
    private String paramValue;

    public Parameter() {
    }

    public Parameter(String paramId) {
        this.paramId = paramId;
    }

    public Parameter(String paramId, String paramDescription) {
        this.paramId = paramId;
        this.paramDescription = paramDescription;
    }

    public String getParamId() {
        return paramId;
    }

    public void setParamId(String paramId) {
        this.paramId = paramId;
    }

    public String getParamDescription() {
        return paramDescription;
    }

    public void setParamDescription(String paramDescription) {
        this.paramDescription = paramDescription;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (paramId != null ? paramId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Parameter)) {
            return false;
        }
        Parameter other = (Parameter) object;
        if ((this.paramId == null && other.paramId != null) || (this.paramId != null && !this.paramId.equals(other.paramId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "eu.europa.ec.fisheries.uvms.movement.entity.Parameter[ paramId=" + paramId + " ]";
    }
    
}