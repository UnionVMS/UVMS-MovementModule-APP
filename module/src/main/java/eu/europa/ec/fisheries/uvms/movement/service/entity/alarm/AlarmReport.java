package eu.europa.ec.fisheries.uvms.movement.service.entity.alarm;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.movement.service.dto.AlarmStatusType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "alarmreport")
@NamedQuery(name = AlarmReport.FIND_ALARM_REPORT_BY_MOVEMENT_GUID_AND_STATUS, query = "SELECT ar FROM AlarmReport ar WHERE ar.incomingMovement.id = :movementGuid and ar.status = :status")
@NamedQuery(name = AlarmReport.FIND_ALARM_REPORT_BY_ASSET_GUID_AND_RULE_GUID_AND_STATUS, query = "SELECT ar FROM AlarmReport ar left join ar.alarmItemList ai WHERE ar.assetGuid = :assetGuid and ar.status = :status and ai.ruleGuid = :ruleGuid")
@NamedQuery(name = AlarmReport.COUNT_ALARMS_BY_STATUS, query = "SELECT count(ar) FROM AlarmReport ar where ar.status = :status")
public class AlarmReport {

    public static final String FIND_ALARM_REPORT_BY_MOVEMENT_GUID_AND_STATUS = "AlarmReport.findByMovementGuid";
    public static final String COUNT_ALARMS_BY_STATUS = "AlarmReport.countOpenAlarms";
    public static final String FIND_ALARM_REPORT_BY_ASSET_GUID_AND_RULE_GUID_AND_STATUS = "AlarmReport.findByAssetGuidRuleGuid";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", name = "id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    private PluginType pluginType;

    private String assetGuid;

    @Enumerated(EnumType.STRING)
    private AlarmStatusType status;

    private String recipient;

    private Instant createdDate;

    @NotNull
    private Instant updated;

    @NotNull
    private String updatedBy;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private IncomingMovement incomingMovement;

    @OneToMany(mappedBy = "alarmReport", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<AlarmItem> alarmItemList;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }

    public String getAssetGuid() {
        return assetGuid;
    }

    public void setAssetGuid(String assetGuid) {
        this.assetGuid = assetGuid;
    }

    public AlarmStatusType getStatus() {
        return status;
    }

    public void setStatus(AlarmStatusType status) {
        this.status = status;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
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