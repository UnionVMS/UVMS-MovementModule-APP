
package eu.europa.ec.fisheries.uvms.movement.service.dto;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AlarmSearchKey.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AlarmSearchKey"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="ALARM_GUID"/&gt;
 *     &lt;enumeration value="ASSET_GUID"/&gt;
 *     &lt;enumeration value="STATUS"/&gt;
 *     &lt;enumeration value="RULE_RECIPIENT"/&gt;
 *     &lt;enumeration value="FROM_DATE"/&gt;
 *     &lt;enumeration value="TO_DATE"/&gt;
 *     &lt;enumeration value="RULE_GUID"/&gt;
 *     &lt;enumeration value="RULE_NAME"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "AlarmSearchKey")
@XmlEnum
public enum AlarmSearchKey {

    ALARM_GUID,
    ASSET_GUID,
    STATUS,
    RULE_RECIPIENT,
    FROM_DATE,
    TO_DATE,
    RULE_GUID,
    RULE_NAME;

    public String value() {
        return name();
    }

    public static AlarmSearchKey fromValue(String v) {
        return valueOf(v);
    }

}
