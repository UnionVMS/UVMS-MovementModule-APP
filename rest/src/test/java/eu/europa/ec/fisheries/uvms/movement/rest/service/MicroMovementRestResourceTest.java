package eu.europa.ec.fisheries.uvms.movement.rest.service;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovement;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;

@RunWith(Arquillian.class)
public class MicroMovementRestResourceTest extends BuildMovementRestDeployment {

    @Inject
    private MovementService movementService;

    @Test
    @OperateOnDeployment("movement")
    public void getTrackForAssetTest() throws Exception {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        OffsetDateTime timestamp = createdMovement.getTimestamp().minus(5, ChronoUnit.MINUTES).atOffset(ZoneOffset.UTC);
        List<MicroMovement> latestMovements = getWebTarget()
                .path("micro")
                .path("track")
                .path("asset")
                .path(createdMovement.getMovementConnect().getId().toString())
                .path(timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")))
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<MicroMovement>>() {});

        assertTrue(latestMovements
                .stream()
                .anyMatch(m -> m.getGuid().equals(createdMovement.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movement")
    public void getTrackForMovementTest() throws Exception {
        UUID connectId = UUID.randomUUID();
        Movement movementBaseType = MovementTestHelper.createMovement();
        movementBaseType.getMovementConnect().setId(connectId);
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType2.getMovementConnect().setId(connectId);
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);

        List<MicroMovement> track = getWebTarget()
                .path("micro")
                .path("track")
                .path("movement")
                .path(createdMovement.getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<MicroMovement>>() {});

        assertThat(track.size(), CoreMatchers.is(2));
        assertTrue(track
                .stream()
                .anyMatch(m -> m.getGuid().equals(createdMovement.getId().toString())));
        assertTrue(track
                .stream()
                .anyMatch(m -> m.getGuid().equals(createdMovement2.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movement")
    public void getLatestMovementsTest() throws Exception {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        List<MicroMovementExtended> latestMovements = getWebTarget()
                .path("micro")
                .path("latest")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<MicroMovementExtended>>() {});
        assertTrue(latestMovements
                .stream()
                .anyMatch(m -> m.getMicroMove().getGuid().equals(createdMovement.getId().toString())));
    }
}
