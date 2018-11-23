package eu.europa.ec.fisheries.uvms.movement.rest.service;

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

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class SegmentRestResourceTest extends BuildMovementRestDeployment {

    @Inject
    private MovementService movementService;

    @Test
    @OperateOnDeployment("movement")
    public void getSegmentByMovementsTest(){

        Movement movement = MovementTestHelper.createMovement();
        Movement movementDeparture = movementService.createMovement(movement);

        movement = MovementTestHelper.createMovement(57d,12d);    //plus one on both from above
        movement.setMovementConnect(movementDeparture.getMovementConnect());
        Movement movementDestination = movementService.createMovement(movement);

        SegmentDTO response = getWebTarget()
                .path("segment")
                .path("segmentByDestinationMovement/" + movementDestination.getGuid().toString())
                .request(MediaType.APPLICATION_JSON)
                .get(SegmentDTO.class);

        assertEquals(movementDeparture.getGuid().toString(), response.getFromMovement());
        assertEquals(movementDestination.getGuid().toString(), response.getToMovement());


    }
}
