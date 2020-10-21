package eu.europa.ec.fisheries.uvms.movement.service.util;

import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

public class JsonBConfiguratorMovement extends JsonBConfigurator {

    public JsonBConfiguratorMovement() {
        super();
        config.withSerializers(new PointSerializer()).withDeserializers(new PointDeserializer());
    }

    @Override
    public Jsonb getContext(Class<?> type) {
        return JsonbBuilder.newBuilder()
                .withConfig(config)
                .build();
    }
}
