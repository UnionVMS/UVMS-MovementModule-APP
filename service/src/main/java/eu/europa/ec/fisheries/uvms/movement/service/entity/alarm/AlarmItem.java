package eu.europa.ec.fisheries.uvms.movement.service.entity.alarm;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

//@formatter:off
@Entity
@Table(name = "alarmitem", indexes = {
        @Index(columnList = "alarmreport_id", name = "alarmitem_alarmreport_fk_inx", unique = false)
})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = AlarmItem.FIND_RULE_NAMES, query = "SELECT DISTINCT item.ruleName FROM AlarmItem item")
})
//@formatter:on
public class AlarmItem implements Serializable {

    public static final String FIND_RULE_NAMES = "AlarmItem.ruleNames";
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", name = "id")
    private UUID id;                // DB ID
    private String ruleName;        //exists in Type, same name
    private String ruleGuid;        //exists in Type, same name
    @NotNull
    private Instant updated;
    @NotNull
    private String updatedBy;

    /*@JoinColumn(name = "alarmitem_alarmrep_id", referencedColumnName = "alarmrep_id")*/
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