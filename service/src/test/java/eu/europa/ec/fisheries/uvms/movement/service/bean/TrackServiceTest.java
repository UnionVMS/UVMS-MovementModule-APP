package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import java.time.Instant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.service.clients.SpatialRestClient;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Track;

@RunWith(MockitoJUnitRunner.class)
public class TrackServiceTest {

    @Mock
    private SpatialRestClient spatialClient;
    
    @InjectMocks
    private TrackService trackService;
    
    @Test
    public void nonExistingTrackTest() {
        Movement previous = getMovement();
        Movement current = getMovement();
        trackService.upsertTrack(previous, current);
        assertThat(previous.getTrack(), is(notNullValue()));
        assertThat(current.getTrack(), is(notNullValue()));
        assertThat(previous.getTrack(), is(current.getTrack()));
    }
    
    @Test
    public void exitPortTest() {
        doReturn(SegmentCategoryType.EXIT_PORT).when(spatialClient).getSegmentCategoryType(any(), any());
        Movement previous = getMovement();
        previous.setTrack(new Track());
        Movement current = getMovement();
        trackService.upsertTrack(previous, current);
        assertThat(previous.getTrack(), is(notNullValue()));
        assertThat(current.getTrack(), is(notNullValue()));
        assertThat(previous.getTrack(), is(not(current.getTrack())));
    }
    
    @Test
    public void updateTrackTest() {
        doReturn(SegmentCategoryType.GAP).when(spatialClient).getSegmentCategoryType(any(), any());
        Movement previous = getMovement();
        previous.setTrack(new Track());
        Movement current = getMovement();
        current.getLocation().getCoordinate().setX(2);
        trackService.upsertTrack(previous, current);
        assertThat(previous.getTrack(), is(notNullValue()));
        assertThat(current.getTrack(), is(notNullValue()));
        assertThat(previous.getTrack(), is(current.getTrack()));
        assertThat(current.getTrack().getDistance(), is(notNullValue()));
        assertThat(current.getTrack().getDuration(), is(notNullValue()));
    }
    
    private Movement getMovement() {
        Movement movement = new Movement();
        Coordinate coordinate = new Coordinate(1, 1);
        GeometryFactory factory = new GeometryFactory();
        Point point = factory.createPoint(coordinate);
        point.setSRID(4326);
        movement.setLocation(point);
        movement.setTimestamp(Instant.now());
        return movement;
    }
}
