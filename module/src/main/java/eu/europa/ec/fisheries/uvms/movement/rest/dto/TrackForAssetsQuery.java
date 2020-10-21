package eu.europa.ec.fisheries.uvms.movement.rest.dto;

import java.util.List;
import java.util.UUID;

public class TrackForAssetsQuery {

    List<String> sources;
    List<UUID> assetIds;

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public List<UUID> getAssetIds() {
        return assetIds;
    }

    public void setAssetIds(List<UUID> assetIds) {
        this.assetIds = assetIds;
    }
}
