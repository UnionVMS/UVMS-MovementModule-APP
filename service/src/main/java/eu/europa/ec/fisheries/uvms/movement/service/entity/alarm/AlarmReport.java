package eu.europa.ec.fisheries.uvms.movement.service.entity.alarm;

import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "alarmreport")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = AlarmReport.FIND_ALARM_REPORT_BY_ID, query = "SELECT ar FROM AlarmReport ar WHERE ar.id = :id"),
        @NamedQuery(name = AlarmReport.FIND_OPEN_ALARM_REPORT_BY_MOVEMENT_GUID, query = "SELECT ar FROM AlarmReport ar WHERE ar.incomingMovement.guid = :movementGuid and ar.status = 'OPEN'"),
        @NamedQuery(name = AlarmReport.FIND_ALARM_BY_GUID, query = "SELECT ar FROM AlarmReport ar WHERE ar.guid = :guid"),
        @NamedQuery(name = AlarmReport.FIND_ALARM_REPORT_BY_ASSET_GUID_AND_RULE_GUID, query = "SELECT ar FROM AlarmReport ar left join ar.alarmItemList ai WHERE ar.assetGuid = :assetGuid and ar.status = 'OPEN' and ai.ruleGuid = :ruleGuid"),
        @NamedQuery(name = AlarmReport.COUNT_OPEN_ALARMS, query = "SELECT count(ar) FROM AlarmReport ar where ar.status = 'OPEN'")
})
//@formatter:on
public class AlarmReport implements Serializable {

    public static final String FIND_OPEN_ALARM_REPORT_BY_MOVEMENT_GUID = "AlarmReport.findByMovementGuid";
    public static final String FIND_ALARM_REPORT_BY_ID = "AlarmReport.findById";
    public static final String FIND_ALARM_BY_GUID = "AlarmReport.findByGuid";
    public static final String COUNT_OPEN_ALARMS = "AlarmReport.countOpenAlarms";
    public static final String FIND_ALARM_REPORT_BY_ASSET_GUID_AND_RULE_GUID = "AlarmReport.findByAssetGuidRuleGuid";

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;        //internal DB id
    private String pluginType;  //Expects values from the class PluginType, exists in Type, same name  TODO: make the *Type class use an enum instead of a string
    private String guid;    //exists in Type, same name
    private String assetGuid;   //exists in Type, same name
    private String status;  //Expects values from teh class AlarmsStatusType, exists in Type, same name
    private String recipient;   //exists in Type, same name
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;   //exists in Type as openDate
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;       //exists in Type, same name
    @NotNull
    private String updatedBy;   //exists in Type, same name

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private IncomingMovement incomingMovement;    //exists in Type, same name

    @OneToMany(mappedBy = "alarmReport", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AlarmItem> alarmItemList;  //exists in Type, same name

    @PrePersist
    public void prePersist() {
        this.guid = UUID.randomUUID().toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getAssetGuid() {
        return assetGuid;
    }

    public void setAssetGuid(String assetGuid) {
        this.assetGuid = assetGuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
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

    public IncomingMovement getIncomingMovement() {
        return incomingMovement;
    }

    public void setIncomingMovement(IncomingMovement incomingMovement) {
        this.incomingMovement = incomingMovement;
    }

    public List<AlarmItem> getAlarmItemList() {
        if (alarmItemList == null) {
            alarmItemList = new ArrayList<>();
        }
        return alarmItemList;
    }

    public void setAlarmItemList(List<AlarmItem> alarmItemList) {
        this.alarmItemList = alarmItemList;
    }

    @Override
    public String toString() {
        return "AlarmReport{" +
                "id=" + id +
                ", pluginType='" + pluginType + '\'' +
                ", guid='" + guid + '\'' +
                ", assetGuid='" + assetGuid + '\'' +
                ", status='" + status + '\'' +
                ", recipient='" + recipient + '\'' +
                ", createdDate=" + createdDate +
                ", updated=" + updated +
                ", updatedBy='" + updatedBy + '\'' +
//                ", rawMovement=" + rawMovement +
//                ", alarmItemList=" + alarmItemList +
                '}';
    }

}