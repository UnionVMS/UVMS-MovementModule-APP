package eu.europa.ec.fisheries.uvms.movement.rest.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public class MicroMovementsForConnectIdsBetweenDatesRequest implements Serializable {

    private List<String> connectIds;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    private Instant fromDate;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    private Instant toDate;

    public MicroMovementsForConnectIdsBetweenDatesRequest() {
    }

    public MicroMovementsForConnectIdsBetweenDatesRequest(List<String> connectIds, Instant fromDate, Instant toDate) {
        this.connectIds = connectIds;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public List<String> getConnectIds() {
        return connectIds;
    }

    public void setConnectIds(List<String> connectIds) {
        this.connectIds = connectIds;
    }

    public Instant getFromDate() {
        return fromDate;
    }

    public void setFromDate(Instant fromDate) {
        this.fromDate = fromDate;
    }

    public Instant getToDate() {
        return toDate;
    }

    public void setToDate(Instant toDate) {
        this.toDate = toDate;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
