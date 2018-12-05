package eu.europa.ec.fisheries.uvms.movement.service.entity.alarm;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import eu.europa.ec.fisheries.schema.movementrules.exchange.v1.PluginType;
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "alarmreport", indexes = {
        @Index(columnList = "incomingmovement_id", name = "alarmreport_incomingmovement_fk_inx", unique = false)
})
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = AlarmReport.FIND_ALARM_REPORT_BY_ID, query = "SELECT ar FROM AlarmReport ar WHERE ar.id = :id"),
        @NamedQuery(name = AlarmReport.FIND_OPEN_ALARM_REPORT_BY_MOVEMENT_GUID, query = "SELECT ar FROM AlarmReport ar WHERE ar.incomingMovement.id = :movementGuid and ar.status = 'OPEN'"),
        @NamedQuery(name = AlarmReport.FIND_ALARM_BY_GUID, query = "SELECT ar FROM AlarmReport ar WHERE ar.id = :guid"),
        @NamedQuery(name = AlarmReport.FIND_ALARM_REPORT_BY_ASSET_GUID_AND_RULE_GUID, query = "SELECT ar FROM AlarmReport ar left join ar.alarmItemList ai WHERE ar.assetGuid = :assetGuid and ar.status = 'OPEN' and ai.ruleGuid = :ruleGuid"),
        @NamedQuery(name = AlarmReport.COUNT_OPEN_ALARMS, query = "SELECT count(ar) FROM AlarmReport ar where ar.status = 'OPEN'")
})
//@formatter:on
@JsonIdentityInfo(generator= ObjectIdGenerators.UUIDGenerator.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlarmReport implements Serializable {

    public static final String FIND_OPEN_ALARM_REPORT_BY_MOVEMENT_GUID = "AlarmReport.findByMovementGuid";
    public static final String FIND_ALARM_REPORT_BY_ID = "AlarmReport.findById";
    public static final String FIND_ALARM_BY_GUID = "AlarmReport.findByGuid";
    public static final String COUNT_OPEN_ALARMS = "AlarmReport.countOpenAlarms";
    public static final String FIND_ALARM_REPORT_BY_ASSET_GUID_AND_RULE_GUID = "AlarmReport.findByAssetGuidRuleGuid";

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", name = "id")
    private UUID id;        //DB id

    private String pluginType;  //Expects values from the class PluginType, exists in Type, same name  TODO: make the *Type class use an enum instead of a string
    private String assetGuid;   //exists in Type, same name
    private String status;  //Expects values from teh class AlarmsStatusType, exists in Type, same name
    private String recipient;   //exists in Type, same name
    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    private Instant createdDate;   //exists in Type as openDate
    @NotNull
    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
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

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
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