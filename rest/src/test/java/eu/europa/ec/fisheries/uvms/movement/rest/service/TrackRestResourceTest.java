package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.schema.movement.v1.MovementTrack;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementDtoV2;
import eu.europa.ec.fisheries.uvms.movement.service.dto.SegmentDTO;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class TrackRestResourceTest extends BuildMovementRestDeployment {

    @Inject
    private MovementService movementService;

    @Test
    @OperateOnDeployment("movement")
    public void getTrackByIdTest() {
        Movement movement = MovementTestHelper.createMovement();
        Movement movementDeparture = movementService.createAndProcessMovement(movement);

        movement = MovementTestHelper.createMovement(57d,12d);    //plus one on both from above
        movement.setMovementConnect(movementDeparture.getMovementConnect());
        Movement movementDestination = movementService.createAndProcessMovement(movement);

        MovementTrack response = getWebTarget()
                .path("track")
                .path(movementDestination.getTrack().getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .get(MovementTrack.class);

        assertNotNull(response);

        assertEquals(movementDestination.getTrack().getId().toString(), response.getId());
    }

    @Test
    @OperateOnDeployment("movement")
    public void getTrackByMovementGuidTest() {
        Movement movement = MovementTestHelper.createMovement();
        Movement movementDeparture = movementService.createAndProcessMovement(movement);

        movement = MovementTestHelper.createMovement(57d,12d);    //plus one on both from above
        movement.setMovementConnect(movementDeparture.getMovementConnect());
        Movement movementDestination = movementService.createAndProcessMovement(movement);

        MovementTrack response = getWebTarget()
                .path("track/byMovementGUID")
                .path(movementDestination.getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .get(MovementTrack.class);

        assertNotNull(response);

        assertEquals(movementDestination.getTrack().getId().toString(), response.getId());
        assertNotNull(response.getWkt());
    }

    @Test
    @OperateOnDeployment("movement")
    public void getMicroMovementTrackByMovementGuidTest() {
        Movement movement = MovementTestHelper.createMovement();
        Movement movementDeparture = movementService.createAndProcessMovement(movement);

        movement = MovementTestHelper.createMovement(57d,12d);    //plus one on both from above
        movement.setMovementConnect(movementDeparture.getMovementConnect());
        Movement movementDestination = movementService.createAndProcessMovement(movement);

        List<MicroMovementDtoV2> response = getWebTarget()
                .path("track/microMovement/byMovementGUID")
                .path(movementDestination.getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<MicroMovementDtoV2>>() {});

        assertNotNull(response);

        assertFalse(response.isEmpty());
        assertEquals(movement.getId().toString(), response.get(0).getGuid());
        assertEquals(movement.getLocation().getX(), response.get(0).getLocation().getLongitude(), 0);
        assertEquals(movement.getLocation().getY(), response.get(0).getLocation().getLatitude(), 0);
    }

    @Test
    @OperateOnDeployment("movement")
    public void getTrackFromASingleMovementTest() {  //should not result in a 500 bc no track
        Movement movement = MovementTestHelper.createMovement();
        Movement singelMovement = movementService.createAndProcessMovement(movement);


        MovementTrack response = getWebTarget()
                .path("track/byMovementGUID")
                .path(singelMovement.getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .get(MovementTrack.class);

        assertNotNull(response);

        assertNull(response.getWkt());
    }

    @Test
    @OperateOnDeployment("movement")
    public void getMaxTrackTest() {
        Movement movement = MovementTestHelper.createMovement();
        Movement movementDeparture = movementService.createAndProcessMovement(movement);

        movement = MovementTestHelper.createMovement(57d,12d);    //plus one on both from above
        movement.setMovementConnect(movementDeparture.getMovementConnect());
        Movement movementMiddle = movementService.createAndProcessMovement(movement);

        movement = MovementTestHelper.createMovement(58d,13d);    //plus one on both from above
        movement.setMovementConnect(movementDeparture.getMovementConnect());
        Movement movementDestination = movementService.createAndProcessMovement(movement);


        MovementTrack response = getWebTarget()
                .path("track/byMovementGUID")
                .path(movementDestination.getId().toString())
                .queryParam("maxNbr", 2)
                .request(MediaType.APPLICATION_JSON)
                .get(MovementTrack.class);

        assertNotNull(response);

        assertEquals(response.getWkt(), 2, response.getWkt().split(",").length);
        String wkt = response.getWkt();

        response = getWebTarget()
                .path("track")
                .path(movementDestination.getTrack().getId().toString())
                .queryParam("maxNbr", 2)
                .request(MediaType.APPLICATION_JSON)
                .get(MovementTrack.class);

        assertNotNull(response);

        assertEquals(wkt , response.getWkt());
    }
}
