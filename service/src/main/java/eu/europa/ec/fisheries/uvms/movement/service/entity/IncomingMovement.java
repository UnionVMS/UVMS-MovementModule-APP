package eu.europa.ec.fisheries.uvms.movement.service.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@Entity
@Table(name = "incomingmovement")
@XmlRootElement
@DynamicUpdate
@DynamicInsert
public class IncomingMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String guid;
    private String connectId;
    private String ackResponseMessageID;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateReceived;
    @Temporal(TemporalType.TIMESTAMP)
    private Date positionTime;
    private String status;
    private Double reportedSpeed;
    private Double reportedCourse;
    private String assetName;
    private String flagState;
    private String externalMarking;
    private Double tripNumber;
    private String internalReferenceNumber;

    private String movementType;
    private String movementSourceType;

    private String assetType;
    private String assetID;
    private String assetCFR;
    private String assetIRCS;
    private String assetIMO;
    private String assetMMSI;
    private String assetGuid;

    private Double longitude;
    private Double latitude;
    private Double altitude;

    private String activityMessageType;
    private String activityMessageId;
    private String activityCallback;

    private String comChannelType;

    private String mobileTerminalGuid;
    private String mobileTerminalConnectId;

    private String mobileTerminalSerialNumber;
    private String mobileTerminalLES;
    private String mobileTerminalDNID;
    private String mobileTerminalMemberNumber;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;
    @NotNull
    private String updatedBy;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getConnectId() {
        return connectId;
    }

    public void setConnectId(String connectId) {
        this.connectId = connectId;
    }

    public String getAckResponseMessageID() {
        return ackResponseMessageID;
    }

    public void setAckResponseMessageID(String ackResponseMessageID) {
        this.ackResponseMessageID = ackResponseMessageID;
    }

    public Date getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(Date dateReceived) {
        this.dateReceived = dateReceived;
    }

    public Date getPositionTime() {
        return positionTime;
    }

    public void setPositionTime(Date positionTime) {
        this.positionTime = positionTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getReportedSpeed() {
        return reportedSpeed;
    }

    public void setReportedSpeed(Double reportedSpeed) {
        this.reportedSpeed = reportedSpeed;
    }

    public Double getReportedCourse() {
        return reportedCourse;
    }

    public void setReportedCourse(Double reportedCourse) {
        this.reportedCourse = reportedCourse;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getFlagState() {
        return flagState;
    }

    public void setFlagState(String flagState) {
        this.flagState = flagState;
    }

    public String getExternalMarking() {
        return externalMarking;
    }

    public void setExternalMarking(String externalMarking) {
        this.externalMarking = externalMarking;
    }

    public Double getTripNumber() {
        return tripNumber;
    }

    public void setTripNumber(Double tripNumber) {
        this.tripNumber = tripNumber;
    }

    public String getInternalReferenceNumber() {
        return internalReferenceNumber;
    }

    public void setInternalReferenceNumber(String internalReferenceNumber) {
        this.internalReferenceNumber = internalReferenceNumber;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public String getMovementSourceType() {
        return movementSourceType;
    }

    public void setMovementSourceType(String movementSourceType) {
        this.movementSourceType = movementSourceType;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getAssetID() {
        return assetID;
    }

    public void setAssetID(String assetID) {
        this.assetID = assetID;
    }

    public String getAssetCFR() {
        return assetCFR;
    }

    public void setAssetCFR(String assetCFR) {
        this.assetCFR = assetCFR;
    }

    public String getAssetIRCS() {
        return assetIRCS;
    }

    public void setAssetIRCS(String assetIRCS) {
        this.assetIRCS = assetIRCS;
    }

    public String getAssetIMO() {
        return assetIMO;
    }

    public void setAssetIMO(String assetIMO) {
        this.assetIMO = assetIMO;
    }

    public String getAssetMMSI() {
        return assetMMSI;
    }

    public void setAssetMMSI(String assetMMSI) {
        this.assetMMSI = assetMMSI;
    }

    public String getAssetGuid() {
        return assetGuid;
    }

    public void setAssetGuid(String assetGuid) {
        this.assetGuid = assetGuid;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public String getActivityMessageType() {
        return activityMessageType;
    }

    public void setActivityMessageType(String activityMessageType) {
        this.activityMessageType = activityMessageType;
    }

    public String getActivityMessageId() {
        return activityMessageId;
    }

    public void setActivityMessageId(String activityMessageId) {
        this.activityMessageId = activityMessageId;
    }

    public String getActivityCallback() {
        return activityCallback;
    }

    public void setActivityCallback(String activityCallback) {
        this.activityCallback = activityCallback;
    }

    public String getComChannelType() {
        return comChannelType;
    }

    public void setComChannelType(String comChannelType) {
        this.comChannelType = comChannelType;
    }

    public String getMobileTerminalGuid() {
        return mobileTerminalGuid;
    }

    public void setMobileTerminalGuid(String mobileTerminalGuid) {
        this.mobileTerminalGuid = mobileTerminalGuid;
    }

    public String getMobileTerminalConnectId() {
        return mobileTerminalConnectId;
    }

    public void setMobileTerminalConnectId(String mobileTerminalConnectId) {
        this.mobileTerminalConnectId = mobileTerminalConnectId;
    }

    public String getMobileTerminalSerialNumber() {
        return mobileTerminalSerialNumber;
    }

    public void setMobileTerminalSerialNumber(String mobileTerminalSerialNumber) {
        this.mobileTerminalSerialNumber = mobileTerminalSerialNumber;
    }

    public String getMobileTerminalLES() {
        return mobileTerminalLES;
    }

    public void setMobileTerminalLES(String mobileTerminalLES) {
        this.mobileTerminalLES = mobileTerminalLES;
    }

    public String getMobileTerminalDNID() {
        return mobileTerminalDNID;
    }

    public void setMobileTerminalDNID(String mobileTerminalDNID) {
        this.mobileTerminalDNID = mobileTerminalDNID;
    }

    public String getMobileTerminalMemberNumber() {
        return mobileTerminalMemberNumber;
    }

    public void setMobileTerminalMemberNumber(String mobileTerminalMemberNumber) {
        this.mobileTerminalMemberNumber = mobileTerminalMemberNumber;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
