package eu.europa.ec.fisheries.uvms.movement.service.entity.alarm;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

//@formatter:off
@Entity
@Table(name = "alarmitem")
@XmlRootElement
//@formatter:on
public class AlarmItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;                //internal DB ID
    private String guid;            //Globally unique ID, exists in Type, same name
    private String ruleName;        //exists in Type, same name
    private String ruleGuid;        //exists in Type, same name
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;
    @NotNull
    private String updatedBy;

    /*@JoinColumn(name = "alarmitem_alarmrep_id", referencedColumnName = "alarmrep_id")*/
    @ManyToOne(fetch = FetchType.LAZY)
    private AlarmReport alarmReport;

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

    @Override
    public String toString() {
        return "AlarmItem{" +
                "id=" + id +
                ", guid='" + guid + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", ruleGuid='" + ruleGuid + '\'' +
                ", updated=" + updated +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}