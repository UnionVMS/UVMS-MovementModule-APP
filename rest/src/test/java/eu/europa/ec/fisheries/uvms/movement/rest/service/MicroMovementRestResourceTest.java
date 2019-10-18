package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.RealTimeMapInitialData;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovement;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class MicroMovementRestResourceTest extends BuildMovementRestDeployment {

    @Inject
    private MovementService movementService;

    @Test
    @OperateOnDeployment("movement")
    public void getTrackForAssetTest() {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        OffsetDateTime timestamp = createdMovement.getTimestamp().minus(5, ChronoUnit.MINUTES).atOffset(ZoneOffset.UTC);
        List<MicroMovement> latestMovements = getWebTarget()
                .path("micro")
                .path("track")
                .path("asset")
                .path(createdMovement.getMovementConnect().getId().toString())
                .queryParam("startDate", timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")))
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<MicroMovement>>() {});

        assertTrue(latestMovements
                .stream()
                .anyMatch(m -> m.getGuid().equals(createdMovement.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movement")
    public void getTrackForOnlyOneSourceTest() {
        UUID connectId = UUID.randomUUID();
        Movement nafMovement = MovementTestHelper.createMovement();
        nafMovement.getMovementConnect().setId(connectId);
        nafMovement.setMovementSource(MovementSourceType.NAF);
        Movement nafCreated = movementService.createAndProcessMovement(nafMovement);

        Movement aisMovement = MovementTestHelper.createMovement();
        aisMovement.getMovementConnect().setId(connectId);
        aisMovement.setMovementSource(MovementSourceType.AIS);
        Movement aisCreated = movementService.createAndProcessMovement(aisMovement);

        OffsetDateTime timestamp = aisCreated.getTimestamp().minus(5, ChronoUnit.MINUTES).atOffset(ZoneOffset.UTC);
        List<MicroMovement> latestMovements = getWebTarget()
                .path("micro")
                .path("track")
                .path("asset")
                .path(connectId.toString())
                .queryParam("startDate", timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")))
                .queryParam("sources", MovementSourceType.AIS.value())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<MicroMovement>>() {});

        assertTrue(latestMovements
                .stream()
                .anyMatch(m -> m.getGuid().equals(aisCreated.getId().toString())));

        assertFalse(latestMovements
                .stream()
                .anyMatch(m -> m.getGuid().equals(nafCreated.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movement")
    public void getTrackForTwoOutOfThreeSourcesTest() {
        UUID connectId = UUID.randomUUID();
        Movement nafMovement = MovementTestHelper.createMovement();
        nafMovement.getMovementConnect().setId(connectId);
        nafMovement.setMovementSource(MovementSourceType.NAF);
        Movement nafCreated = movementService.createAndProcessMovement(nafMovement);

        Movement aisMovement = MovementTestHelper.createMovement();
        aisMovement.getMovementConnect().setId(connectId);
        aisMovement.setMovementSource(MovementSourceType.AIS);
        Movement aisCreated = movementService.createAndProcessMovement(aisMovement);

        Movement iridiumMovement = MovementTestHelper.createMovement();
        iridiumMovement.getMovementConnect().setId(connectId);
        iridiumMovement.setMovementSource(MovementSourceType.IRIDIUM);
        Movement iridiumCreated = movementService.createAndProcessMovement(iridiumMovement);

        OffsetDateTime timestamp = aisCreated.getTimestamp().minus(5, ChronoUnit.MINUTES).atOffset(ZoneOffset.UTC);
        List<MicroMovement> latestMovements = getWebTarget()
                .path("micro")
                .path("track")
                .path("asset")
                .path(connectId.toString())
                .queryParam("startDate", timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")))
                .queryParam("sources", MovementSourceType.NAF.value(), MovementSourceType.IRIDIUM)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<MicroMovement>>() {});

        assertFalse(latestMovements
                .stream()
                .anyMatch(m -> m.getGuid().equals(aisCreated.getId().toString())));

        assertTrue(latestMovements
                .stream()
                .anyMatch(m -> m.getGuid().equals(nafCreated.getId().toString())));

        assertTrue(latestMovements
                .stream()
                .anyMatch(m -> m.getGuid().equals(iridiumCreated.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movement")
    public void getTrackBetweenTimesForAssetTest() {
        UUID connectId = UUID.randomUUID();
        Movement movementBaseType = MovementTestHelper.createMovement();
        movementBaseType.getMovementConnect().setId(connectId);
        movementBaseType.setTimestamp(Instant.now().minusSeconds(4));
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType2.getMovementConnect().setId(connectId);
        movementBaseType2.setTimestamp(Instant.now().minusSeconds(2));
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);
        Movement movementBaseType3 = MovementTestHelper.createMovement();
        movementBaseType3.getMovementConnect().setId(connectId);
        Movement createdMovement3 = movementService.createAndProcessMovement(movementBaseType3);

        Instant startTime  = createdMovement2.getTimestamp().minusSeconds(1);
        Instant endTime  = createdMovement2.getTimestamp().plusSeconds(1);


        List<MicroMovement> latestMovements = getWebTarget()
                .path("micro")
                .path("track")
                .path("asset")
                .path(connectId.toString())
                .queryParam("startDate", DateUtil.parseUTCDateToString(startTime)) //yyyy-MM-dd HH:mm:ss Z
                .queryParam("endDate", DateUtil.parseUTCDateToString(endTime))
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<MicroMovement>>() {});

        assertFalse(latestMovements.isEmpty());
        assertTrue(latestMovements.stream().
                noneMatch(m -> m.getGuid().equals(createdMovement.getId().toString())));
        assertTrue(latestMovements.stream()
                .anyMatch(m -> m.getGuid().equals(createdMovement2.getId().toString())));
        assertTrue(latestMovements.stream()
                .noneMatch(m -> m.getGuid().equals(createdMovement3.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movement")
    public void getTrackForMovementTest() {
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
    public void getLatestMovementsTest() {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        List<MicroMovementExtended> latestMovements = getWebTarget()
                .path("micro")
                .path("latest")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(RealTimeMapInitialData.class).getMicroMovements();

        assertTrue(latestMovements
                .stream()
                .anyMatch(m -> m.getMicroMove().getGuid().equals(createdMovement.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movement")
    public void getLastMicroMovementForAllAssetsTest() {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        RealTimeMapInitialData output = getWebTarget()
                .path("micro")
                .path("latest")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(RealTimeMapInitialData.class);

        assertTrue(output.getMicroMovements().size() > 0);
        assertTrue(output.getMicroMovements()
                .stream()
                .anyMatch(m -> m.getMicroMove().getGuid().equals(createdMovement.getId().toString())));
        assertEquals("AssetMT rest mock in movement rest module", output.getAssetList());
    }

    @Test
    @OperateOnDeployment("movement")
    public void getLastMicroMovementFromOnlyOneSourceTest() {
        Movement nafBaseType = MovementTestHelper.createMovement();
        nafBaseType.setMovementSource(MovementSourceType.NAF);
        Movement nafMovement = movementService.createAndProcessMovement(nafBaseType);

        Movement manualBaseType = MovementTestHelper.createMovement();
        manualBaseType.setMovementSource(MovementSourceType.MANUAL);
        Movement manualMovement = movementService.createAndProcessMovement(manualBaseType);

        RealTimeMapInitialData output = getWebTarget()
                .path("micro")
                .path("latest")
                .queryParam("sources", MovementSourceType.NAF.value())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(RealTimeMapInitialData.class);

        assertTrue(output.getMicroMovements().size() > 0);
        assertTrue(output.getMicroMovements()
                .stream()
                .anyMatch(m -> m.getMicroMove().getGuid().equals(nafMovement.getId().toString())));
        assertFalse(output.getMicroMovements()
                .stream()
                .anyMatch(m -> m.getMicroMove().getGuid().equals(manualMovement.getId().toString())));
        assertEquals("AssetMT rest mock in movement rest module", output.getAssetList());
    }


}
