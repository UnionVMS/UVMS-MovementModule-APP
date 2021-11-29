package eu.europa.ec.fisheries.uvms.movement.service.util;

import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import org.locationtech.jts.geom.Point;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class PointSerializer implements JsonbSerializer<Point> {
    public void serialize(Point location, JsonGenerator jsonGenerator, SerializationContext serializationContext) {
        if (location != null) {
            MovementPoint point = new MovementPoint();
            point.setLatitude(location.getY());
            point.setLongitude(location.getX());
            serializationContext.serialize( point, jsonGenerator);
        } else {
            serializationContext.serialize(null, jsonGenerator);
        }
    }
}
