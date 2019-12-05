package eu.europa.ec.fisheries.uvms.movement.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.fisheries.schema.movement.asset.v1.VesselType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.service.dto.ManualMovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovement;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class ManualMovementRestResourceTest extends BuildMovementRestDeployment {

    @Test
    @OperateOnDeployment("movement")
    public void createManualMovementTest() {
        ManualMovementDto movement = createManualMovement();

        Response response = getWebTarget()
                .path("manualMovement")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(movement), Response.class);
        assertEquals(200, response.getStatus());


        }

    @Test
    @OperateOnDeployment("movement")
    public void createManualMovementLessInfoInVesselTypeTest() {
        String movement = "{\"movement\":{\"location\":{\"longitude\":0.0,\"latitude\":0.0,\"altitude\":null},\"heading\":0.0,\"timestamp\":1575545948.469924300,\"speed\":0.0,\"source\":\"MANUAL\"},\"asset\":{\"ircs\":\"T\",\"cfr\":\"T\"}}";

        Response response = getWebTarget()
                .path("manualMovement")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(movement), Response.class);
        assertEquals(200, response.getStatus());
    }

    @Test
    @OperateOnDeployment("movement")
    public void createManualMovementOnlySecondsInTimestampTest() {
        String movement = "{\"movement\":{\"location\":{\"longitude\":0.0,\"latitude\":0.0,\"altitude\":null},\"heading\":0.0,\"timestamp\":1575545948,\"speed\":0.0,\"source\":\"MANUAL\"},\"asset\":{\"ircs\":\"T\",\"cfr\":\"T\"}}";

        Response response = getWebTarget()
                .path("manualMovement")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(movement), Response.class);
        assertEquals(200, response.getStatus());
    }


    /*
     * Helper functions for REST calls
     */
    private ManualMovementDto createManualMovement() {
        ManualMovementDto movement = new ManualMovementDto();
        VesselType asset = new VesselType();
        asset.setCfr("T");
        asset.setExtMarking("T");
        asset.setFlagState("T");
        asset.setIrcs("T");
        asset.setName("T");
        movement.setAsset(asset);

        MicroMovement micro = new MicroMovement();
        MovementPoint location = new MovementPoint();
        location.setLatitude(0.0);
        location.setLongitude(0.0);
        micro.setLocation(location);
        micro.setTimestamp(Instant.now());
        micro.setHeading(0.0);
        micro.setSpeed(0.0);
        micro.setSource(MovementSourceType.MANUAL);
        movement.setMovement(micro);

        return movement;
    }
    
}
