package eu.europa.ec.fisheries.uvms.movement.service.util;

import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;

public class PointDeserializer implements JsonbDeserializer<Point> {

    @Override
    public Point deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        MovementPoint movementPoint = ctx.deserialize(MovementPoint.class, parser);
        Coordinate coordinate = new Coordinate(movementPoint.getLongitude(), movementPoint.getLatitude());
        GeometryFactory factory = new GeometryFactory();
        Point point = factory.createPoint(coordinate);
        point.setSRID(4326);
        return point;
    }
}
