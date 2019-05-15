package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementMapResponseType;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.RangeCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.RangeKeyType;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTrack;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.LatestMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;

@RunWith(Arquillian.class)
public class MovementServiceIntTest extends TransactionalTests {

    private Random rnd = new Random();
    private static int NumberOfMovements = 3;

    @EJB
    MovementService movementService;


    @Test
    @OperateOnDeployment("movementservice")
    public void createMovement() {

        Instant now = DateUtil.nowUTC();
        double longitude = 9.140625D;
        double latitude = 57.683804D;

        // create a MovementConnect
        UUID connectId = UUID.randomUUID();
        Movement movementType = MockData.createMovement(longitude, latitude, connectId);
        try {
            Movement createdMovementType = movementService.createAndProcessMovement(movementType);
            assertNotNull(createdMovementType);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovementsByConnectIds_EmptyList() {

        List<UUID> connectionIds = new ArrayList<>();
        List<Movement> movements =  movementService.getLatestMovementsByConnectIds(connectionIds);
        assertThat(movements.size(), CoreMatchers.is(0));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getList() {

        MovementQuery query = createMovementQuery(true);
        try {
            movementService.getList(query);
            //Assert.assertTrue(getMovementListByQueryResponse != null); //changes to the error handling a few functions down means that the above call will throw an exception
            fail("The above call should throw an exception since query is incomplete");
        } catch (EJBTransactionRolledbackException e) {
            e.printStackTrace();
            //Assert.fail();
        	assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getListByMovementQuery() {

        Movement createdMovement = null;

        double longitude = 9.140625D;
        double latitude = 57.683804D;

        // create a MovementConnect
        UUID connectId = UUID.randomUUID();
        Movement movementType = MockData.createMovement(longitude, latitude, connectId);
        try {
            createdMovement = movementService.createAndProcessMovement(movementType);
            assertNotNull(createdMovement);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        MovementQuery query = createMovementQuery(true);
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement.getMovementConnect().getId().toString());
        query.getMovementSearchCriteria().add(criteria);

        try {
            GetMovementListByQueryResponse list = movementService.getList(query);
            assertNotNull(list);
            List<MovementType> movementList = list.getMovement();
            assertNotNull(movementList);
            assertTrue(!movementList.isEmpty());
            assertEquals(connectId.toString(), movementList.get(0).getConnectId());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void getListByAssetIdQuery() {
        UUID assetId = UUID.randomUUID();
        UUID connectId = UUID.randomUUID();
        
        Movement movement1 = MockData.createMovement(1d, 1d, connectId);
        movement1.getMovementConnect().setAssetId(assetId);
        Movement createdMovement1 = movementService.createAndProcessMovement(movement1);
        
        Movement movement2 = MockData.createMovement(2d, 2d, connectId);
        movement2.getMovementConnect().setAssetId(assetId);
        Movement createdMovement2 = movementService.createAndProcessMovement(movement2);
        
        // new connect id
        connectId = UUID.randomUUID();
        Movement movement3 = MockData.createMovement(3d, 3d, connectId);
        movement3.getMovementConnect().setAssetId(assetId);
        Movement createdMovement3 = movementService.createAndProcessMovement(movement3);

        MovementQuery query = createMovementQuery(true);
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.ASSET_ID);
        criteria.setValue(assetId.toString());
        query.getMovementSearchCriteria().add(criteria);

        GetMovementListByQueryResponse list = movementService.getList(query);
        List<MovementType> movementList = list.getMovement();
        assertThat(movementList.size(), CoreMatchers.is(3));
        
        Map<String, MovementType> movementMap = new HashMap<>();
        for (MovementType movementType : movementList) {
            movementMap.put(movementType.getGuid(), movementType);
        }
        MovementType m1 = movementMap.get(createdMovement1.getId().toString());
        assertThat(m1.getConnectId(), CoreMatchers.is(createdMovement1.getMovementConnect().getId().toString()));
        assertThat(m1.getAssetId().getValue(), CoreMatchers.is(createdMovement1.getMovementConnect().getAssetId().toString()));
        
        MovementType m2 = movementMap.get(createdMovement2.getId().toString());
        assertThat(m2.getConnectId(), CoreMatchers.is(createdMovement2.getMovementConnect().getId().toString()));
        assertThat(m2.getAssetId().getValue(), CoreMatchers.is(createdMovement2.getMovementConnect().getAssetId().toString()));
        
        MovementType m3 = movementMap.get(createdMovement3.getId().toString());
        assertThat(m3.getConnectId(), CoreMatchers.is(createdMovement3.getMovementConnect().getId().toString()));
        assertThat(m3.getAssetId().getValue(), CoreMatchers.is(createdMovement3.getMovementConnect().getAssetId().toString()));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMapByQuery_LATEST() {

        MovementQuery query = createMovementQuery(false);

        ListCriteria listCriteria = new ListCriteria();
        listCriteria.setKey(SearchKey.NR_OF_LATEST_REPORTS);
        listCriteria.setValue("3");

        query.getMovementSearchCriteria().add(listCriteria);
        try {
            GetMovementMapByQueryResponse response = movementService.getMapByQuery(query);
            assertNotNull(response);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMapByQuery_LATEST_with_pagination() {

        MovementQuery query = createMovementQuery(true);

        ListCriteria listCriteria = new ListCriteria();
        listCriteria.setKey(SearchKey.NR_OF_LATEST_REPORTS);
        listCriteria.setValue("3");

        query.getMovementSearchCriteria().add(listCriteria);
        try {
            movementService.getMapByQuery(query);
            fail();
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void trackLineStringTest() {
        UUID connectId = UUID.randomUUID();
        
        Movement movement1 = MockData.createMovement(1d, 1d, connectId);
        movementService.createAndProcessMovement(movement1);
        Movement movement2 = MockData.createMovement(1d, 2d, connectId);
        movementService.createAndProcessMovement(movement2);
        Movement movement3 = MockData.createMovement(1d, 3d, connectId);
        movementService.createAndProcessMovement(movement3);
        
        MovementQuery query = createMovementQuery(false);
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(connectId.toString());
        query.getMovementSearchCriteria().add(criteria);
        
        GetMovementMapByQueryResponse mapByQuery = movementService.getMapByQuery(query);
        assertThat(mapByQuery.getMovementMap().size(), CoreMatchers.is(1));
        MovementMapResponseType movement = mapByQuery.getMovementMap().get(0);
        List<MovementTrack> tracks = movement.getTracks();
        assertThat(tracks.size(), CoreMatchers.is(1));
        MovementTrack movementTrack = tracks.get(0);
        String expectedWktString = "LINESTRING (1.0 3.0, 1.0 2.0, 1.0 1.0)";
        assertThat(movementTrack.getWkt(), CoreMatchers.is(expectedWktString));
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void createBatch() {

        double longitude = rnd.nextDouble();
        double latitude = rnd.nextDouble();
        List<Movement> movementTypeList = new ArrayList<>();
        for(int i = 0 ; i < NumberOfMovements ; i++){
            movementTypeList.add(MockData.createMovement(longitude, latitude, UUID.randomUUID()));
            longitude = longitude  + 0.05;
            latitude = latitude +  0.05;
        }

        List<Movement> movementBatch = movementService.createMovementBatch(movementTypeList);
        assertNotNull(movementBatch);
        assertTrue(!movementBatch.isEmpty());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getById() {

        try {
            Instant now = DateUtil.nowUTC();
            double longitude = 9.140625D;
            double latitude = 57.683804D;

            // create a MovementConnect
            UUID connectId = UUID.randomUUID();
            Movement movementType = MockData.createMovement(longitude, latitude, connectId);
            assertNotNull(movementService);
            Movement createdMovementType = movementService.createAndProcessMovement(movementType);
            em.flush();
            assertNotNull(createdMovementType);

            UUID guid = createdMovementType.getId();
            assertNotNull(guid);

            Movement fetchedMovement = movementService.getById(guid);
            assertNotNull(fetchedMovement);
            UUID fetchedGuid = fetchedMovement.getId();
            assertNotNull(fetchedGuid);
            assertEquals(fetchedGuid, guid);

        } catch (Exception e) {
            // TODO  check this it is suspect
            //Assert.fail();
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getById_Null_ID() {
        try {
            UUID connectId = null;
            Movement byId = movementService.getById(connectId);
            fail();
        }catch (EJBTransactionRolledbackException e){

        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovements() {
        List<LatestMovement> movements = movementService.getLatestMovements(5);
        assertNotNull(movements);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void getLatestMovements_NumberNULL() {
        movementService.getLatestMovements(null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovements_NegativeNumber() {
        try {
            movementService.getLatestMovements(-3);
            fail();
        } catch (Exception e) {
            assertNotNull(e);
        }
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementConnect() {
        // Note getOrCreateMovementConnectByConnectId CREATES one if it does not exists  (probably to force a batch import to succeed)
        MovementConnect mc = new MovementConnect();
        mc.setId(UUID.randomUUID());
        mc.setUpdated(Instant.now());
        mc.setUpdatedBy("Test Connector");
        MovementConnect fetchedMovementConnect = movementService.getOrCreateMovementConnectByConnectId(mc);
        assertNotNull(fetchedMovementConnect);
        assertEquals(fetchedMovementConnect.getId(), mc.getId());
    }


    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementConnect_ZEROISH_GUID() {
        MovementConnect mc = new MovementConnect();
        mc.setId(UUID.fromString("100000-0000-0000-0000-000000000000"));
        mc.setUpdated(Instant.now());
        mc.setUpdatedBy("Test Connector");
        // Note getOrCreateMovementConnectByConnectId CREATES one if it does not exists  (probably to force a batchimport to succeed)
        MovementConnect fetchedMovementConnect = movementService.getOrCreateMovementConnectByConnectId(mc);
        assertNotNull(fetchedMovementConnect);
        assertEquals(fetchedMovementConnect.getId(), mc.getId());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementConnect_NULL_GUID() {
        assertNull(movementService.getOrCreateMovementConnectByConnectId(null));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovement2() {
        double longitude = rnd.nextDouble();
        double latitude = rnd.nextDouble();

        UUID randomUUID = UUID.randomUUID();
        Movement movement = MockData.createMovement(longitude, latitude, randomUUID);
        movement.getMovementConnect().setId(randomUUID);

        Movement created = movementService.createMovement(movement);
        assertNotNull(created);
        assertEquals(randomUUID, created.getMovementConnect().getId());

    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementListBySegmentDuration() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Movement movementType = MockData.createMovement(1d, 1d, connectId);
        movementType.setTimestamp(timestamp);
        Movement createdMovementType = movementService.createAndProcessMovement(movementType);

        Movement movementType2 = MockData.createMovement(2d, 2d, connectId);
        movementType2.setTimestamp(timestamp.plusSeconds(10));
        Movement createdMovementType2 = movementService.createAndProcessMovement(movementType2);

        MovementQuery query = createMovementQuery(true);

        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(connectId.toString());
        query.getMovementSearchCriteria().add(criteria);

        RangeCriteria rangeCriteria = new RangeCriteria();
        rangeCriteria.setKey(RangeKeyType.SEGMENT_DURATION);
        rangeCriteria.setFrom("9");
        rangeCriteria.setTo("11");
        query.getMovementRangeSearchCriteria().add(rangeCriteria);

        GetMovementListByQueryResponse response = movementService.getList(query);
        assertNotNull(response);

        List<MovementType> movements = response.getMovement();
        assertThat(movements, is(notNullValue()));

        assertThat(movements.size(), is(2));
        assertTrue(movements.stream().anyMatch(m -> m.getGuid().equals(createdMovementType.getId().toString())));
        assertTrue(movements.stream().anyMatch(m -> m.getGuid().equals(createdMovementType2.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementMapBySegmentDuration() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Movement movementType = MockData.createMovement(1d, 1d, connectId);
        movementType.setTimestamp(timestamp);
        Movement createdMovementType = movementService.createAndProcessMovement(movementType);

        Movement movementType2 = MockData.createMovement(2d, 2d, connectId);
        movementType2.setTimestamp(timestamp.plusSeconds(10));
        Movement createdMovementType2 = movementService.createAndProcessMovement(movementType2);

        MovementQuery query = createMovementQuery(false);

        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(connectId.toString());
        query.getMovementSearchCriteria().add(criteria);

        RangeCriteria rangeCriteria = new RangeCriteria();
        rangeCriteria.setKey(RangeKeyType.SEGMENT_DURATION);
        rangeCriteria.setFrom("9");
        rangeCriteria.setTo("11");
        query.getMovementRangeSearchCriteria().add(rangeCriteria);

        GetMovementMapByQueryResponse response = movementService.getMapByQuery(query);
        assertNotNull(response);

        List<MovementMapResponseType> movementMap = response.getMovementMap();
        assertThat(movementMap, is(notNullValue()));

        assertThat(movementMap.size(), is(1));
        MovementMapResponseType movementMapType = movementMap.get(0);

        assertThat(movementMapType.getKey(), is(connectId.toString()));
        assertThat(movementMapType.getMovements().size(), is(2));
        assertThat(movementMapType.getSegments().size(), is(1));

        assertTrue(movementMapType.getMovements().stream().anyMatch(m -> m.getGuid().equals(createdMovementType.getId().toString())));
        assertTrue(movementMapType.getMovements().stream().anyMatch(m -> m.getGuid().equals(createdMovementType2.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementMapBySegmentLength() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Movement movementType = MockData.createMovement(57.715303, 11.973323, connectId);
        movementType.setTimestamp(timestamp);
        Movement createdMovementType = movementService.createAndProcessMovement(movementType);

        Movement movementType2 = MockData.createMovement(57.714580, 11.972838, connectId);
        movementType2.setTimestamp(timestamp.plusSeconds(10));
        Movement createdMovementType2 = movementService.createAndProcessMovement(movementType2);

        MovementQuery query = createMovementQuery(false);

        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(connectId.toString());
        query.getMovementSearchCriteria().add(criteria);

        RangeCriteria rangeCriteria = new RangeCriteria();
        rangeCriteria.setKey(RangeKeyType.SEGMENT_LENGTH);
        rangeCriteria.setFrom("0.05");
        rangeCriteria.setTo("0.06");
        query.getMovementRangeSearchCriteria().add(rangeCriteria);

        GetMovementMapByQueryResponse response = movementService.getMapByQuery(query);
        assertNotNull(response);

        List<MovementMapResponseType> movementMap = response.getMovementMap();
        assertThat(movementMap, is(notNullValue()));

        assertThat(movementMap.size(), is(1));
        MovementMapResponseType movementMapType = movementMap.get(0);

        assertThat(movementMapType.getKey(), is(connectId.toString()));
        assertThat(movementMapType.getMovements().size(), is(2));
        assertThat(movementMapType.getSegments().size(), is(1));

        assertTrue(movementMapType.getMovements().stream().anyMatch(m -> m.getGuid().equals(createdMovementType.getId().toString())));
        assertTrue(movementMapType.getMovements().stream().anyMatch(m -> m.getGuid().equals(createdMovementType2.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementMapBySegmentSpeed() throws Exception {
        UUID connectId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Movement movementType = MockData.createMovement(57.715303, 11.973323, connectId);
        movementType.setTimestamp(timestamp);
        Movement createdMovementType = movementService.createAndProcessMovement(movementType);

        Movement movementType2 = MockData.createMovement(57.714580, 11.972838, connectId);
        movementType2.setTimestamp(timestamp.plusSeconds(10));
        Movement createdMovementType2 = movementService.createAndProcessMovement(movementType2);

        MovementQuery query = createMovementQuery(false);

        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(connectId.toString());
        query.getMovementSearchCriteria().add(criteria);

        RangeCriteria rangeCriteria = new RangeCriteria();
        rangeCriteria.setKey(RangeKeyType.SEGMENT_SPEED);
        rangeCriteria.setFrom("18");
        rangeCriteria.setTo("19");
        query.getMovementRangeSearchCriteria().add(rangeCriteria);

        GetMovementMapByQueryResponse response = movementService.getMapByQuery(query);
        assertNotNull(response);

        List<MovementMapResponseType> movementMap = response.getMovementMap();
        assertThat(movementMap, is(notNullValue()));

        assertThat(movementMap.size(), is(1));
        MovementMapResponseType movementMapType = movementMap.get(0);

        assertThat(movementMapType.getKey(), is(connectId.toString()));
        assertThat(movementMapType.getMovements().size(), is(2));
        assertThat(movementMapType.getSegments().size(), is(1));

        assertTrue(movementMapType.getMovements().stream().anyMatch(m -> m.getGuid().equals(createdMovementType.getId().toString())));
        assertTrue(movementMapType.getMovements().stream().anyMatch(m -> m.getGuid().equals(createdMovementType2.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void findLastestTest() {

        UUID connectId = UUID.randomUUID();
        Movement movementType = MockData.createMovement(57.715303, 11.973323, connectId);
        movementType.setTimestamp(Instant.now().minus(5, ChronoUnit.MINUTES));
        Movement createdMovement = movementService.createAndProcessMovement(movementType);

        Movement movementType2 = MockData.createMovement(57.715303, 11.973323, connectId);
        movementType.setTimestamp(Instant.now().minus(1, ChronoUnit.MINUTES));
        Movement createdMovement2 = movementService.createAndProcessMovement(movementType2);

        List<Movement> latest = movementService.getLatestMovementsAfter(Instant.now().minus(10, ChronoUnit.MINUTES));
        assertFalse(latest.contains(createdMovement));
        assertTrue(latest.contains(createdMovement2));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void findLastestMultipleConnectIdsTest() {
        UUID connectId = UUID.randomUUID();
        Movement movementType = MockData.createMovement(57.715303, 11.973323, connectId);
        movementType.setTimestamp(Instant.now().minus(5, ChronoUnit.MINUTES));
        Movement createdMovement = movementService.createAndProcessMovement(movementType);

        UUID connectId2 = UUID.randomUUID();
        Movement movementType2 = MockData.createMovement(57.715303, 11.973323, connectId2);
        movementType.setTimestamp(Instant.now().minus(1, ChronoUnit.MINUTES));
        Movement createdMovement2 = movementService.createAndProcessMovement(movementType2);

        List<Movement> latest = movementService.getLatestMovementsAfter(Instant.now().minus(10, ChronoUnit.MINUTES));
        assertTrue(latest.contains(createdMovement));
        assertTrue(latest.contains(createdMovement2));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void findLastestMultipleConnectIdsWithOldTimestampTest() {
        UUID connectId = UUID.randomUUID();
        Movement movementType = MockData.createMovement(57.715303, 11.973323, connectId);
        movementType.setTimestamp(Instant.now().minus(15, ChronoUnit.MINUTES));
        Movement createdMovement = movementService.createAndProcessMovement(movementType);

        UUID connectId2 = UUID.randomUUID();
        Movement movementType2 = MockData.createMovement(57.715303, 11.973323, connectId2);
        movementType.setTimestamp(Instant.now().minus(1, ChronoUnit.MINUTES));
        Movement createdMovement2 = movementService.createAndProcessMovement(movementType2);

        List<Movement> latest = movementService.getLatestMovementsAfter(Instant.now().minus(10, ChronoUnit.MINUTES));
        assertFalse(latest.contains(createdMovement));
        assertTrue(latest.contains(createdMovement2));
    }

    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/
 
    private MovementQuery createMovementQuery(boolean usePagination) {

        MovementQuery query = new MovementQuery();
        if (usePagination) {
            BigInteger listSize = BigInteger.valueOf(100L);
            BigInteger page = BigInteger.valueOf(1L);
            ListPagination listPagination = new ListPagination();
            listPagination.setListSize(listSize);
            listPagination.setPage(page);
            query.setPagination(listPagination);
        }
        return query;
    }
}
