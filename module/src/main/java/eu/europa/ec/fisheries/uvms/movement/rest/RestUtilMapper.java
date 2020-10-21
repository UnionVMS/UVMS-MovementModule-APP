package eu.europa.ec.fisheries.uvms.movement.rest;

import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestUtilMapper {

    public static List<MovementSourceType> convertToMovementSourceTypes (List<String> sources) {
        List<MovementSourceType> sourceTypes = new ArrayList<>();
        if (sources == null || sources.isEmpty()) {
            sourceTypes = Arrays.asList(MovementSourceType.values());
        } else {
            for (String source : sources) {
                sourceTypes.add(MovementSourceType.fromValue(source));
            }
        }
        return sourceTypes;
    }
}
