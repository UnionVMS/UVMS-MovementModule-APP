package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.schema.movement.v1.MovementTrack;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dto.SegmentDTO;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class TrackRestResourceTest extends BuildMovementRestDeployment {

    @Inject
    private MovementService movementService;

    @Test
    @OperateOnDeployment("movement")
    public void getTrackByIdTest() {
        Movement movement = MovementTestHelper.createMovement();
        Movement movementDeparture = movementService.createMovement(movement);

        movement = MovementTestHelper.createMovement(57d,12d);    //plus one on both from above
        movement.setMovementConnect(movementDeparture.getMovementConnect());
        Movement movementDestination = movementService.createMovement(movement);

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
        Movement movementDeparture = movementService.createMovement(movement);

        movement = MovementTestHelper.createMovement(57d,12d);    //plus one on both from above
        movement.setMovementConnect(movementDeparture.getMovementConnect());
        Movement movementDestination = movementService.createMovement(movement);

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
    public void getTrackFromASingleMovementTest() {  //should not result in a 500 bc no track
        Movement movement = MovementTestHelper.createMovement();
        Movement singelMovement = movementService.createMovement(movement);


        MovementTrack response = getWebTarget()
                .path("track/byMovementGUID")
                .path(singelMovement.getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .get(MovementTrack.class);

        assertNotNull(response);

        assertNull(response.getWkt());
    }
}
