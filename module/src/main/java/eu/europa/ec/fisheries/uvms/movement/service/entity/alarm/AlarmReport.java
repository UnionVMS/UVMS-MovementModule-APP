package eu.europa.ec.fisheries.uvms.movement.service.entity.alarm;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.movement.service.dto.AlarmStatusType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "alarmreport", indexes = {
        @Index(columnList = "incomingmovement_id", name = "alarmreport_incomingmovement_fk_inx", unique = false)
})
@NamedQueries({
        @NamedQuery(name = AlarmReport.FIND_OPEN_ALARM_REPORT_BY_MOVEMENT_GUID, query = "SELECT ar FROM AlarmReport ar WHERE ar.incomingMovement.id = :movementGuid and ar.status = 'OPEN'"),
        @NamedQuery(name = AlarmReport.FIND_ALARM_REPORT_BY_ASSET_GUID_AND_RULE_GUID, query = "SELECT ar FROM AlarmReport ar left join ar.alarmItemList ai WHERE ar.assetGuid = :assetGuid and ar.status = 'OPEN' and ai.ruleGuid = :ruleGuid"),
        @NamedQuery(name = AlarmReport.COUNT_OPEN_ALARMS, query = "SELECT count(ar) FROM AlarmReport ar where ar.status = 'OPEN'")
})
//@formatter:on
public class AlarmReport implements Serializable {

    public static final String FIND_OPEN_ALARM_REPORT_BY_MOVEMENT_GUID = "AlarmReport.findByMovementGuid";
    public static final String COUNT_OPEN_ALARMS = "AlarmReport.countOpenAlarms";
    public static final String FIND_ALARM_REPORT_BY_ASSET_GUID_AND_RULE_GUID = "AlarmReport.findByAssetGuidRuleGuid";

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", name = "id")
    private UUID id;        //DB id

    @Enumerated(EnumType.STRING)
    private PluginType pluginType;  //Expects values from the class PluginType, exists in Type, same name  TODO: make the *Type class use an enum instead of a string
    private String assetGuid;   //exists in Type, same name

    @Enumerated(EnumType.STRING)
    private AlarmStatusType status;  //Expects values from teh class AlarmStatusType, exists in Type, same name
    private String recipient;   //exists in Type, same name

    private Instant createdDate;   //exists in Type as openDate

    @NotNull
    private Instant updated;       //exists in Type, same name
    @NotNull
    private String updatedBy;   //exists in Type, same name

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private IncomingMovement incomingMovement;    //exists in Type, same name

    @OneToMany(mappedBy = "alarmReport", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<AlarmItem> alarmItemList;  //exists in Type, same name

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