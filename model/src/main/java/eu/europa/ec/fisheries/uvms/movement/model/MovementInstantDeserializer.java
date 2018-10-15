package eu.europa.ec.fisheries.uvms.movement.model;

import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class MovementInstantDeserializer extends InstantDeserializer {
    public MovementInstantDeserializer(){
        super(InstantDeserializer.INSTANT ,DateTimeFormatter.ISO_INSTANT);
    }
}
