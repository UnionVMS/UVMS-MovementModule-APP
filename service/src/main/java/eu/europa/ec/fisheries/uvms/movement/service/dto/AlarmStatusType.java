
package eu.europa.ec.fisheries.uvms.movement.service.dto;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AlarmStatusType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AlarmStatusType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="OPEN"/&gt;
 *     &lt;enumeration value="REJECTED"/&gt;
 *     &lt;enumeration value="REPROCESSED"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "AlarmStatusType")
@XmlEnum
public enum AlarmStatusType {

    OPEN,
    REJECTED,
    REPROCESSED;

    public String value() {
        return name();
    }

    public static AlarmStatusType fromValue(String v) {
        return valueOf(v);
    }

}
