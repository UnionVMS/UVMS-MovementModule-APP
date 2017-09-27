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

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import javax.xml.bind.annotation.XmlTransient;

/**
 **/
@Entity
@Table(name = "movementmetadata")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Movementmetadata.findAll", query = "SELECT m FROM Movementmetadata m")})
@DynamicUpdate
@DynamicInsert
public class Movementmetadata implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "movemet_id")
    private Long movemetId;

    @Column(name = "movemet_closestport_remoteid")
    private String closestPortRemoteId;

    @Column(name = "movemet_closestport_code")
    private String closestPortCode;

    @Column(name = "movemet_closestport_dist")
    private Double closestPortDistance;

    @Column(name = "movemet_closestport_name")
    private String closestPortName;

    @Column(name = "movemet_closecounty_remoteid")
    private String closestCountryRemoteId;

    @Column(name = "movemet_closecounty_code")
    private String closestCountryCode;

    @Column(name = "movemet_closecounty_name")
    private String closestCountryName;

    @Column(name = "movemet_closecounty_dist")
    private Double closestCountryDistance;

    @Basic(optional = false)
    @NotNull
    @Column(name = "movemet_updattim")
    @Temporal(TemporalType.TIMESTAMP)
    private Date movemetUpdattim;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "movemet_upuser")
    private String movemetUpuser;

    @OneToMany(mappedBy = "metadata", fetch = FetchType.LAZY)
    private List<Movement> movementList;

    public Movementmetadata() {
    }

    public Long getMovemetId() {
        return movemetId;
    }

    public void setMovemetId(Long movemetId) {
        this.movemetId = movemetId;
    }

    public String getClosestPortRemoteId() {
        return closestPortRemoteId;
    }

    public void setClosestPortRemoteId(String closestPortRemoteId) {
        this.closestPortRemoteId = closestPortRemoteId;
    }

    public String getClosestPortCode() {
        return closestPortCode;
    }

    public void setClosestPortCode(String closestPortCode) {
        this.closestPortCode = closestPortCode;
    }

    public Double getClosestPortDistance() {
        return closestPortDistance;
    }

    public void setClosestPortDistance(Double closestPortDistance) {
        this.closestPortDistance = closestPortDistance;
    }

    public String getClosestCountryRemoteId() {
        return closestCountryRemoteId;
    }

    public void setClosestCountryRemoteId(String closestCountryRemoteId) {
        this.closestCountryRemoteId = closestCountryRemoteId;
    }

    public String getClosestCountryCode() {
        return closestCountryCode;
    }

    public void setClosestCountryCode(String closestCountryCode) {
        this.closestCountryCode = closestCountryCode;
    }

    public Double getClosestCountryDistance() {
        return closestCountryDistance;
    }

    public void setClosestCountryDistance(Double closestCountryDistance) {
        this.closestCountryDistance = closestCountryDistance;
    }

    public String getClosestPortName() {
        return closestPortName;
    }

    public void setClosestPortName(String closestPortName) {
        this.closestPortName = closestPortName;
    }

    public String getClosestCountryName() {
        return closestCountryName;
    }

    public void setClosestCountryName(String closestCountryName) {
        this.closestCountryName = closestCountryName;
    }

    public Date getMovemetUpdattim() {
        return movemetUpdattim;
    }

    public void setMovemetUpdattim(Date movemetUpdattim) {
        this.movemetUpdattim = movemetUpdattim;
    }

    public String getMovemetUpuser() {
        return movemetUpuser;
    }

    public void setMovemetUpuser(String movemetUpuser) {
        this.movemetUpuser = movemetUpuser;
    }

    @XmlTransient
    public List<Movement> getMovementList() {
        return movementList;
    }

    public void setMovementList(List<Movement> movementList) {
        this.movementList = movementList;
    }

}