package eu.europa.ec.fisheries.uvms.movement.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.DateUtils;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MicroMovementsForConnectIdsBetweenDatesRequest implements Serializable {
    private List<String> assetIds;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    private Instant fromDate = Instant.now().minus(8, ChronoUnit.HOURS);

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = MovementInstantDeserializer.class)
    private Instant toDate = Instant.now();

    private List<String> sources;

    public MicroMovementsForConnectIdsBetweenDatesRequest() {
    }

    public MicroMovementsForConnectIdsBetweenDatesRequest(List<String> assetIds, Instant fromDate, Instant toDate) {
        this.assetIds = assetIds;
        this.fromDate = fromDate;
        this.toDate = toDate;
        sources = new ArrayList<>();
    }

    public List<String> getAssetIds() {
        return assetIds;
    }

    public void setAssetIds(List<String> assetIds) {
        this.assetIds = assetIds;
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

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
