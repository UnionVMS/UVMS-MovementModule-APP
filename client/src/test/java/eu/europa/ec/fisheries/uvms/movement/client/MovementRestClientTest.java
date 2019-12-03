package eu.europa.ec.fisheries.uvms.movement.client;

import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetDTO;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovement;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.IncomingMovementMapper;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.GenericType;
import java.time.Instant;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class MovementRestClientTest extends BuildMovementClientDeployment {

    @Inject
    private MovementRestClient movementRestClient;

    @Inject
    private MovementService movementService;

    @Before
    public void before() throws NamingException {
        InitialContext ctx = new InitialContext();
        ctx.rebind("java:global/movement_endpoint", "http://localhost:8080/movement/rest");
    }

    @Test
    public void getMicroMovementsForConnectIdsBetweenDates() {
        // Given
        AssetDTO asset = createBasicAsset();
        Instant positionTime = Instant.parse("2019-01-24T09:00:00Z");

        IncomingMovement incomingMovement = createIncomingMovement(asset, positionTime);
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        movementService.createAndProcessMovement(movement);

        List<String> connectIds = new ArrayList<>();
        connectIds.add(asset.getId().toString());

        // When
        List<MicroMovementExtended> movementsForVesselsIds = movementRestClient.getMicroMovementsForConnectIdsBetweenDates(connectIds, positionTime, positionTime);

        // Then
        assertEquals(1, movementsForVesselsIds.size());

        MicroMovementExtended microMovementExtended = movementsForVesselsIds.get(0);
        MicroMovement microMove = microMovementExtended.getMicroMove();
        MovementPoint location = microMove.getLocation();

        assertEquals(incomingMovement.getLatitude(), location.getLatitude());
        assertEquals(incomingMovement.getLongitude(), location.getLongitude());
    }

    @Test
    public void getMicroMovementsForConnectIdsBetweenDatesEmptySourceList() {
        // Given
        AssetDTO asset = createBasicAsset();
        Instant positionTime = Instant.now();

        IncomingMovement incomingMovement = createIncomingMovement(asset, Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        Movement createdMovement = movementService.createAndProcessMovement(movement);

        List<String> connectIds = new ArrayList<>();
        connectIds.add(asset.getId().toString());

        // When
        String jsonOutput = movementRestClient.getMicroMovementsForConnectIdsBetweenDates(connectIds, positionTime, Instant.now(), new ArrayList<>());

        // Then
        assertTrue(jsonOutput.contains(createdMovement.getId().toString()));
    }

    @Test
    public void getMicroMovementsForTwoAssetsBetweenDatesOneOfTwoSources() {
        // Given
        AssetDTO asset1 = createBasicAsset();
        AssetDTO asset2 = createBasicAsset();
        Instant positionTime = Instant.now();

        IncomingMovement incomingMovement = createIncomingMovement(asset1, Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        movement.setMovementSource(MovementSourceType.OTHER);
        Movement createdMovement1 = movementService.createAndProcessMovement(movement);

        incomingMovement = createIncomingMovement(asset2, Instant.now());
        movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        movement.setMovementSource(MovementSourceType.MANUAL);
        Movement createdMovement2 = movementService.createAndProcessMovement(movement);

        List<String> connectIds = new ArrayList<>();
        connectIds.add(asset1.getId().toString());
        connectIds.add(asset2.getId().toString());

        // When
        String jsonOutput = movementRestClient.getMicroMovementsForConnectIdsBetweenDates(connectIds, positionTime, Instant.now(), Arrays.asList(MovementSourceType.MANUAL.value(), MovementSourceType.AIS.value()));

        // Then
        assertFalse(jsonOutput.contains(createdMovement1.getId().toString()));
        assertTrue(jsonOutput.contains(createdMovement2.getId().toString()));
    }


    @Test
    public void getMicroMovementsForConnectIdsBetweenDatesNullDates() {
        // Given
        AssetDTO asset = createBasicAsset();
        Instant positionTime = Instant.now();

        IncomingMovement incomingMovement = createIncomingMovement(asset, Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        Movement createdMovement = movementService.createAndProcessMovement(movement);

        List<String> connectIds = new ArrayList<>();
        connectIds.add(asset.getId().toString());


        // When
        String jsonOutput =  movementRestClient.getMicroMovementsForConnectIdsBetweenDates(connectIds, null, null, new ArrayList<>());

        // Then
        assertTrue(jsonOutput.contains(createdMovement.getId().toString()));
    }

    @Test
    public void getMicroMovementById() {
        // Given
        AssetDTO asset = createBasicAsset();
        IncomingMovement incomingMovement = createIncomingMovement(asset, Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        Movement createdMovement = movementService.createAndProcessMovement(movement);
       // When
        MicroMovement movementById = movementRestClient.getMicroMovementById(createdMovement.getId());
        // Then
        assertNotNull(movementById);
    }

    private IncomingMovement createIncomingMovement(AssetDTO testAsset, Instant positionTime) {

        IncomingMovement incomingMovement = new IncomingMovement();

        incomingMovement.setAssetGuid(testAsset.getId().toString());
        incomingMovement.setAssetCFR(testAsset.getCfr());
        incomingMovement.setAssetIRCS(testAsset.getIrcs());
        incomingMovement.setAssetName(testAsset.getName());

        incomingMovement.setActivityMessageId(UUID.randomUUID().toString());
        incomingMovement.setActivityMessageType(MovementActivityTypeType.ANC.value());
        incomingMovement.setActivityCallback("callback");

        incomingMovement.setLatitude(57.678440);
        incomingMovement.setLongitude(11.616953);
        incomingMovement.setAltitude((double) 5);
        incomingMovement.setPositionTime(positionTime);

        incomingMovement.setMovementType(MovementTypeType.POS.value());
        incomingMovement.setReportedCourse(0d);
        incomingMovement.setReportedSpeed(0d);

        incomingMovement.setAckResponseMessageId(null);

        incomingMovement.setUpdatedBy("Test");

        incomingMovement.setPluginType("NAF");

        incomingMovement.setMovementSourceType(MovementSourceType.OTHER.value());

        return incomingMovement;
    }

    private static AssetDTO createBasicAsset() {
        AssetDTO asset = new AssetDTO();

        asset.setActive(true);
        asset.setId(UUID.randomUUID());

        asset.setName("Ship" + generateARandomStringWithMaxLength(10));
        asset.setCfr("CFR" + generateARandomStringWithMaxLength(9));
        asset.setFlagStateCode("SWE");
        asset.setIrcsIndicator(true);
        asset.setIrcs("F" + generateARandomStringWithMaxLength(7));
        asset.setExternalMarking("EXT3");
        asset.setImo("0" + generateARandomStringWithMaxLength(6));
        asset.setMmsi(generateARandomStringWithMaxLength(9));

        asset.setSource("INTERNAL");

        asset.setMainFishingGearCode("DERMERSAL");
        asset.setHasLicence(true);
        asset.setLicenceType("MOCK-license-DB");
        asset.setPortOfRegistration("TEST_GOT");
        asset.setLengthOverAll(15.0);
        asset.setLengthBetweenPerpendiculars(3.0);
        asset.setGrossTonnage(200.0);

        asset.setGrossTonnageUnit("OSLO");
        asset.setSafteyGrossTonnage(80.0);
        asset.setPowerOfMainEngine(10.0);
        asset.setPowerOfAuxEngine(10.0);

        return asset;
    }

    public static String generateARandomStringWithMaxLength(int len) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int randomInt = new Random().nextInt(10);
            ret.append(randomInt);
        }
        return ret.toString();
    }
}
