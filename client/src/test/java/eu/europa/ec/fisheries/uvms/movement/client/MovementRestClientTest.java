package eu.europa.ec.fisheries.uvms.movement.client;

import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.RangeCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.RangeKeyType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetDTO;
import eu.europa.ec.fisheries.uvms.movement.client.model.CursorPagination;
import eu.europa.ec.fisheries.uvms.movement.model.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.model.constants.SatId;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.IncomingMovementMapper;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.math.BigInteger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    public void pingTest() {
        String expected = "pong";
        String ping = movementRestClient.ping();
        assertThat(ping, CoreMatchers.is(expected));
    }

    @Test
    public void getMovementsForConnectIdsBetweenDates() {
        // Given
        AssetDTO asset = createBasicAsset();
        Instant positionTime = Instant.parse("2019-01-24T09:00:00Z");

        IncomingMovement incomingMovement = createIncomingMovement(asset, positionTime);
        incomingMovement.setSourceSatelliteId((short)3);
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        movement.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy()));
        movementService.createAndProcessMovement(movement);

        List<String> connectIds = new ArrayList<>();
        connectIds.add(asset.getId().toString());

        // When
        List<MovementDto> movementsForVesselsIds = movementRestClient.getMovementsForConnectIdsBetweenDates(connectIds, positionTime, positionTime);

        // Then
        assertEquals(1, movementsForVesselsIds.size());

        MovementDto dto = movementsForVesselsIds.get(0);
        MovementPoint location = dto.getLocation();

        assertEquals(incomingMovement.getLatitude(), location.getLatitude());
        assertEquals(incomingMovement.getLongitude(), location.getLongitude());

        assertEquals(SatId.IOR, dto.getSourceSatelliteId());
    }

    @Test
    public void getMovementsForConnectIdsBetweenDatesEmptySourceList() {
        // Given
        AssetDTO asset = createBasicAsset();
        Instant positionTime = Instant.now();

        IncomingMovement incomingMovement = createIncomingMovement(asset, Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        movement.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy()));
        Movement createdMovement = movementService.createAndProcessMovement(movement);

        List<String> connectIds = new ArrayList<>();
        connectIds.add(asset.getId().toString());

        // When
        String jsonOutput = movementRestClient.getMovementsForConnectIdsBetweenDates(connectIds, positionTime, Instant.now(), new ArrayList<>());

        // Then
        assertTrue(jsonOutput.contains(createdMovement.getId().toString()));
    }

    @Test
    public void getMovementsForTwoAssetsBetweenDatesOneOfTwoSources() {
        // Given
        AssetDTO asset1 = createBasicAsset();
        AssetDTO asset2 = createBasicAsset();
        Instant positionTime = Instant.now();

        IncomingMovement incomingMovement = createIncomingMovement(asset1, Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        movement.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy()));
        movement.setSource(MovementSourceType.OTHER);
        Movement createdMovement1 = movementService.createAndProcessMovement(movement);

        incomingMovement = createIncomingMovement(asset2, Instant.now());
        movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        movement.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy()));
        movement.setSource(MovementSourceType.MANUAL);
        Movement createdMovement2 = movementService.createAndProcessMovement(movement);

        List<String> connectIds = new ArrayList<>();
        connectIds.add(asset1.getId().toString());
        connectIds.add(asset2.getId().toString());

        // When
        String jsonOutput = movementRestClient.getMovementsForConnectIdsBetweenDates(connectIds, positionTime, Instant.now(), Arrays.asList(MovementSourceType.MANUAL.value(), MovementSourceType.AIS.value()));

        // Then
        assertFalse(jsonOutput.contains(createdMovement1.getId().toString()));
        assertTrue(jsonOutput.contains(createdMovement2.getId().toString()));
    }


    @Test
    public void getMovementsForConnectIdsBetweenDatesNullDates() {
        // Given
        AssetDTO asset = createBasicAsset();

        IncomingMovement incomingMovement = createIncomingMovement(asset, Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        movement.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy()));
        Movement createdMovement = movementService.createAndProcessMovement(movement);

        List<String> connectIds = new ArrayList<>();
        connectIds.add(asset.getId().toString());


        // When
        String jsonOutput =  movementRestClient.getMovementsForConnectIdsBetweenDates(connectIds, null, null, new ArrayList<>());

        // Then
        assertTrue(jsonOutput, jsonOutput.contains(createdMovement.getId().toString()));
    }

    @Test
    public void getMicroMovementById() {
        // Given
        AssetDTO asset = createBasicAsset();
        IncomingMovement incomingMovement = createIncomingMovement(asset, Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        movement.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy()));
        Movement createdMovement = movementService.createAndProcessMovement(movement);

       // When
        MovementDto movementById = movementRestClient.getMovementById(createdMovement.getId());

        // Then
        assertNotNull(movementById);
        assertEquals(createdMovement.getId(), movementById.getId());
        assertEquals(createdMovement.getHeading(), movementById.getHeading(), 0);
        assertEquals(incomingMovement.getLatitude(), movementById.getLocation().getLatitude(), 0);
        assertEquals(incomingMovement.getLongitude(), movementById.getLocation().getLongitude(), 0);
        assertEquals(createdMovement.getSource(), movementById.getSource());
        assertEquals(createdMovement.getSpeed().doubleValue(), movementById.getSpeed().doubleValue(), 0);
        assertEquals(createdMovement.getTimestamp().truncatedTo(ChronoUnit.MILLIS), movementById.getTimestamp());
    }

    @Test
    public void getMovementById() {
        // Given
        AssetDTO asset = createBasicAsset();
        IncomingMovement incomingMovement = createIncomingMovement(asset, Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        movement.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy()));
        Movement createdMovement = movementService.createAndProcessMovement(movement);

        // When
        MovementDto movementById = movementRestClient.getMovementById(createdMovement.getId());

        // Then
        assertNotNull(movementById);
        assertEquals(createdMovement.getId(), movementById.getId());
        assertEquals(createdMovement.getHeading(), movementById.getHeading(), 0);
        assertEquals(incomingMovement.getLatitude(), movementById.getLocation().getLatitude(), 0);
        assertEquals(incomingMovement.getLongitude(), movementById.getLocation().getLongitude(), 0);
        assertEquals(createdMovement.getSource(), movementById.getSource());
        assertEquals(createdMovement.getSpeed().doubleValue(), movementById.getSpeed().doubleValue(), 0);
        assertEquals(createdMovement.getTimestamp().truncatedTo(ChronoUnit.MILLIS), movementById.getTimestamp());
    }
    
    @Test
    public void getMovementListByQueryResponseDateTest() {
        AssetDTO asset = createBasicAsset();
        IncomingMovement incomingMovement = createIncomingMovement(asset,  Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        movement.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy()));
        movement.setTimestamp(Instant.now());
        Movement createdMovement = movementService.createAndProcessMovement(movement);
   
        MovementQuery movementQuery = new MovementQuery();
        movementQuery.setPagination(listPaginationDefault());
        movementQuery.getMovementRangeSearchCriteria().add(createRangeCriteriaDate(1));
        GetMovementListByQueryResponse movementListBy = movementRestClient.getMovementList(movementQuery);
       
        assertNotNull(movementListBy);
        assertTrue(movementListBy.getMovement().size() > 0);
        assertTrue(movementListBy.getMovement().stream().anyMatch(m -> m.getGuid().equals(createdMovement.getId().toString())));
        assertTrue(movementListBy.getMovement().stream().anyMatch(m -> m.getConnectId().equals(asset.getId().toString())));   
    }
    
    @Test
    public void getMovementListByQueryResponseTwoYearsBackTest() {
        AssetDTO asset = createBasicAsset();
        IncomingMovement incomingMovement = createIncomingMovement(asset,  Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
       
        movement.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy()));
        movement.setTimestamp(Instant.now().minus(730,ChronoUnit.DAYS));
        movement.setUpdated(Instant.now().minus(730,ChronoUnit.DAYS));
        Movement createdMovement = movementService.createAndProcessMovement(movement);
   
        MovementQuery movementQuery = new MovementQuery();
        movementQuery.setPagination(listPaginationDefault());
        movementQuery.getMovementRangeSearchCriteria().add(createRangeCriteriaDate(731));
    	
        GetMovementListByQueryResponse movementListBy = movementRestClient.getMovementList(movementQuery);
        
        assertNotNull(movementListBy);
        assertTrue(movementListBy.getMovement().size() > 0);
        assertTrue(movementListBy.getMovement().stream().anyMatch(m -> m.getGuid().equals(createdMovement.getId().toString())));
        assertTrue(movementListBy.getMovement().stream().anyMatch(m -> m.getConnectId().equals(asset.getId().toString())));  
    }
    
    @Test
    public void getCursorBasedListTest() {
        AssetDTO asset = createBasicAsset();
        IncomingMovement incomingMovement = createIncomingMovement(asset,  Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        movement.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy()));
        movement.setTimestamp(Instant.now());
        Movement createdMovement = movementService.createAndProcessMovement(movement);
   
        CursorPagination cursorPagination = new CursorPagination();
        cursorPagination.setFrom(createdMovement.getTimestamp().minus(1, ChronoUnit.HOURS));
        cursorPagination.setTo(createdMovement.getTimestamp().plus(1, ChronoUnit.HOURS));
        cursorPagination.setConnectIds(Arrays.asList(createdMovement.getMovementConnect().getId()));
        List<MovementDto> movements = movementRestClient.getCursorBasedList(cursorPagination);
       
        assertNotNull(movements);
        assertTrue(movements.size() > 0);
        assertTrue(movements.stream().anyMatch(m -> m.getId().equals(createdMovement.getId())));
        assertTrue(movements.stream().anyMatch(m -> m.getAsset().equals(asset.getId().toString())));
    }
    
    @Test
    public void getMovementByIdListTest() {
        AssetDTO asset = createBasicAsset();
        IncomingMovement incomingMovement = createIncomingMovement(asset,  Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());    
        movement.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy()));
        Movement createdMovement = movementService.createAndProcessMovement(movement);
        
        List<UUID> ids = new ArrayList<UUID>();
        ids.add(createdMovement.getId());
        List<MovementDto> microMovementList = movementRestClient.getMovementDtoByIdList(ids);
        assertNotNull(microMovementList);
        assertEquals(1,microMovementList.size());
        assertEquals(microMovementList.get(0).getId(), createdMovement.getId());
        assertEquals(Double.valueOf(createdMovement.getHeading()), Double.valueOf(microMovementList.get(0).getHeading()));
    }
    
    @Test
    public void getMicroMovementByIdListTwoIdsTest() {
        AssetDTO asset = createBasicAsset();
        IncomingMovement incomingMovement = createIncomingMovement(asset,  Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());    
        movement.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy()));
        Movement createdMovement = movementService.createAndProcessMovement(movement);
        
        AssetDTO asset2 = createBasicAsset();
        IncomingMovement incomingMovement2 = createIncomingMovement(asset2,  Instant.now());
        Movement movement2 = IncomingMovementMapper.mapNewMovementEntity(incomingMovement2, incomingMovement2.getUpdatedBy());    
        movement2.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement2, incomingMovement2.getUpdatedBy()));
        Movement createdMovement2 = movementService.createAndProcessMovement(movement2);
        
        List<UUID> ids = new ArrayList<UUID>();
        ids.add(createdMovement.getId());
        ids.add(createdMovement2.getId());
        List<MovementDto> microMovementList = movementRestClient.getMovementDtoByIdList(ids);
        assertNotNull(microMovementList);
        assertEquals(2,microMovementList.size());
    }

    @Test
    public void getMovementDtoByIdListTest() {
        AssetDTO asset = createBasicAsset();
        IncomingMovement incomingMovement = createIncomingMovement(asset,  Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        movement.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy()));
        movement.setMovementType(MovementTypeType.EXI);
        Movement createdMovement = movementService.createAndProcessMovement(movement);

        List<UUID> ids = new ArrayList<UUID>();
        ids.add(createdMovement.getId());
        List<MovementDto> MovementDtoList = movementRestClient.getMovementDtoByIdList(ids);
        assertNotNull(MovementDtoList);
        assertEquals(1, MovementDtoList.size());
        assertEquals(MovementDtoList.get(0).getId(), (createdMovement.getId()));
        assertEquals(Double.valueOf(MovementDtoList.get(0).getHeading()), Double.valueOf(createdMovement.getHeading()));
        assertEquals(createdMovement.getMovementType(), MovementDtoList.get(0).getMovementType());
    }

    @Test
    public void getMovementDtoByIdListTwoIdsTest() {
        AssetDTO asset = createBasicAsset();
        IncomingMovement incomingMovement = createIncomingMovement(asset,  Instant.now());
        Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
        movement.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy()));
        Movement createdMovement = movementService.createAndProcessMovement(movement);

        AssetDTO asset2 = createBasicAsset();
        IncomingMovement incomingMovement2 = createIncomingMovement(asset2,  Instant.now());
        Movement movement2 = IncomingMovementMapper.mapNewMovementEntity(incomingMovement2, incomingMovement2.getUpdatedBy());
        movement2.setMovementConnect(IncomingMovementMapper.mapNewMovementConnect(incomingMovement2, incomingMovement2.getUpdatedBy()));
        Movement createdMovement2 = movementService.createAndProcessMovement(movement2);

        List<UUID> ids = new ArrayList<UUID>();
        ids.add(createdMovement.getId());
        ids.add(createdMovement2.getId());
        List<MovementDto> movementDtoList = movementRestClient.getMovementDtoByIdList(ids);
        assertNotNull(movementDtoList);
        assertEquals(2, movementDtoList.size());
    }
    
    private RangeCriteria createRangeCriteriaDate(int daysFromNow) {
        RangeCriteria rangeCriteria1 = new RangeCriteria();
        rangeCriteria1.setKey(RangeKeyType.DATE);
        rangeCriteria1.setTo(Long.toString(Instant.now().toEpochMilli()));
        rangeCriteria1.setFrom(Long.toString(Instant.now().minus(daysFromNow,ChronoUnit.DAYS).toEpochMilli()));
        return rangeCriteria1;
    }
    
    private ListPagination listPaginationDefault() {
    	ListPagination pagination = new ListPagination();
        pagination.setPage(BigInteger.ONE);
        pagination.setListSize(BigInteger.valueOf(1000));
        return pagination;
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
