package eu.europa.ec.fisheries.uvms.movement.service.entity.alarm;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alarmitem")
public class AlarmItem {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", name = "id")
    private UUID id;
    private String ruleName;
    private String ruleGuid;
    @NotNull
    private Instant updated;
    @NotNull
    private String updatedBy;

    /*@JoinColumn(name = "alarmitem_alarmrep_id", referencedColumnName = "alarmrep_id")*/
    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    private AlarmReport alarmReport;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleGuid() {
        return ruleGuid;
    }

    public void setRuleGuid(String ruleGuid) {
        this.ruleGuid = ruleGuid;
    }

    public AlarmReport getAlarmReport() {
        return alarmReport;
    }

    public void setAlarmReport(AlarmReport alarmReport) {
        this.alarmReport = alarmReport;
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

    @Override
    public String toString() {
        return "AlarmItem{" +
                "id=" + id +
                ", ruleName='" + ruleName + '\'' +
                ", ruleGuid='" + ruleGuid + '\'' +
                ", updated=" + updated +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}