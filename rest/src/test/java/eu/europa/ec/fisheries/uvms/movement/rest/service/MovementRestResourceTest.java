package eu.europa.ec.fisheries.uvms.movement.rest.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementDto;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;

@RunWith(Arquillian.class)
public class MovementRestResourceTest extends BuildMovementRestDeployment {
    
    @Inject
    private MovementService movementService;
    
    @Test
    @OperateOnDeployment("movement")
    public void getListByQueryByConnectId() throws Exception {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);
        
        MovementQuery query = MovementTestHelper.createMovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement.getMovementConnect().getId().toString());
        query.getMovementSearchCriteria().add(criteria);
        
        GetMovementListByQueryResponse queryResponse = getListByQuery(query);
        
        assertThat(queryResponse, is(notNullValue()));
        List<MovementType> movements = queryResponse.getMovement();
        assertThat(movements.size(), is(1));
        
        assertThat(movements.get(0).getGuid(), is(createdMovement.getId().toString()));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void getListByQueryByAssetId() throws Exception {
        UUID assetId = UUID.randomUUID();
        Movement movementBaseType = MovementTestHelper.createMovement();
        movementBaseType.getMovementConnect().setAssetId(assetId);
        movementService.createAndProcessMovement(movementBaseType);
        
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType2.getMovementConnect().setId(UUID.randomUUID());
        movementBaseType2.getMovementConnect().setAssetId(assetId);
        movementService.createAndProcessMovement(movementBaseType2);
        
        MovementQuery query = MovementTestHelper.createMovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.ASSET_ID);
        criteria.setValue(assetId.toString());
        query.getMovementSearchCriteria().add(criteria);
        
        GetMovementListByQueryResponse queryResponse = getListByQuery(query);
        
        assertThat(queryResponse, is(notNullValue()));
        List<MovementType> movements = queryResponse.getMovement();
        assertThat(movements.size(), is(2));
        
        assertThat(movements.get(0).getAssetId().getValue(), is(assetId.toString()));
        assertThat(movements.get(1).getAssetId().getValue(), is(assetId.toString()));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void getMinimalListByQueryByConnectId() throws Exception {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);
        
        MovementQuery query = MovementTestHelper.createMovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement.getMovementConnect().getId().toString());
        query.getMovementSearchCriteria().add(criteria);
        
        GetMovementListByQueryResponse queryResponse = getMinimalListByQuery(query);
        
        assertThat(queryResponse, is(notNullValue()));
        List<MovementType> movements = queryResponse.getMovement();
        assertThat(movements.size(), is(1));
        
        assertThat(movements.get(0).getGuid(), is(createdMovement.getId().toString()));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void getLatestMovementsByConnectIds() throws Exception {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);
        
        List<MovementDto> latestMovements = getLatestMovementsByConnectIds(Arrays.asList(createdMovement.getMovementConnect().getId().toString()));
        assertThat(latestMovements.size(), is(1));
        assertThat(latestMovements.get(0).getMovementGUID(), is(createdMovement.getId().toString()));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void getLatestMovementsByConnectIdsTwoPositions() throws Exception {
        UUID connectId = UUID.randomUUID();
        
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        movementBaseType1.getMovementConnect().setId(connectId);
        movementBaseType1.setTimestamp(Instant.now().minusSeconds(60));
        movementService.createAndProcessMovement(movementBaseType1);
        
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType1.getMovementConnect().setId(connectId);
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);
        
        List<MovementDto> latestMovements = getLatestMovementsByConnectIds(Arrays.asList(createdMovement2.getMovementConnect().getId().toString()));
        assertThat(latestMovements.size(), is(1));
        assertThat(latestMovements.get(0).getMovementGUID(), is(createdMovement2.getId().toString()));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void getLatestMovementsByConnectIdsTwoPositionsUnordered() throws Exception {
        UUID connectId = UUID.randomUUID();
        
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        movementBaseType1.getMovementConnect().setId(connectId);
        Movement createdMovement1 = movementService.createAndProcessMovement(movementBaseType1);
        
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType1.getMovementConnect().setId(connectId);
        movementBaseType2.setTimestamp(Instant.now().minusSeconds(60));
        movementService.createAndProcessMovement(movementBaseType2);
        
        List<MovementDto> latestMovements = getLatestMovementsByConnectIds(Arrays.asList(createdMovement1.getMovementConnect().getId().toString()));
        assertThat(latestMovements.size(), is(1));
        assertThat(latestMovements.get(0).getMovementGUID(), is(createdMovement1.getId().toString()));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void getLatestMovements() throws Exception {
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        movementService.createAndProcessMovement(movementBaseType1);
        
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementService.createAndProcessMovement(movementBaseType2);
        
        List<MovementDto> latestMovements = getLatestMovements(1);
        assertThat(latestMovements.size(), is(1));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void getMovementById() throws Exception {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        MovementType fetchedMovement = getMovementById(createdMovement.getId().toString());
        assertThat(fetchedMovement, is(notNullValue()));
        assertThat(fetchedMovement.getGuid(), is(createdMovement.getId().toString()));
    }

    @Test
    @OperateOnDeployment("movement")
    public void getMicroMovementsTest() throws Exception {
        Instant time = Instant.now();
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        String response = getWebTarget()
                .path("movement")
                .path("microMovementListAfter/" + DateUtil.parseUTCDateToString(time))
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertTrue(response.contains(createdMovement.getId().toString()));
        assertTrue(response.contains(createdMovement.getMovementConnect().getId().toString()));

    }

    @Test
    @OperateOnDeployment("movement")
    public void getMicroMovementsFutureTimeTest() throws Exception {
        Instant time = Instant.ofEpochMilli(System.currentTimeMillis() + 1000l * 60l * 60l * 24l);
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        Map<String, MicroMovementDto> response = getWebTarget()
                .path("movement")
                .path("microMovementListAfter/" + DateUtil.parseUTCDateToString(time))
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<Map<String, MicroMovementDto>>() {});


        assertTrue(response.isEmpty());

    }

    @Test
    @OperateOnDeployment("movement")
    public void getLastMicroMovementForAllAssetsTest() throws Exception {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        List<MicroMovementDto> response = getWebTarget()
                .path("movement")
                .path("lastMicroMovementForAllAssets")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<MicroMovementDto>>() {});


        assertFalse(response.isEmpty());

    }
    /*
     * Helper functions for REST calls
     */
    private GetMovementListByQueryResponse getListByQuery(MovementQuery query) throws Exception {
        String response = getWebTarget()
                .path("movement")
                .path("list")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(query), String.class);
            
        return RestHelper.readResponseDto(response, GetMovementListByQueryResponse.class);
    }
    
    private GetMovementListByQueryResponse getMinimalListByQuery(MovementQuery query) throws Exception {
        String response = getWebTarget()
                .path("movement")
                .path("list")
                .path("minimal")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(query), String.class);
            
        return RestHelper.readResponseDto(response, GetMovementListByQueryResponse.class);
    }
    
    private List<MovementDto> getLatestMovementsByConnectIds(List<String> connectIds) throws Exception {
        String response = getWebTarget()
                .path("movement")
                .path("latest")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(connectIds), String.class);
            
        return RestHelper.readResponseDtoList(response, MovementDto.class);
    }
    
    private List<MovementDto> getLatestMovements(int numberOfMovements) throws Exception {
        String response = getWebTarget()
                .path("movement")
                .path("latest/" + numberOfMovements)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
            
        return RestHelper.readResponseDtoList(response, MovementDto.class);
    }
    
    private MovementType getMovementById(String id) throws Exception {
        String response = getWebTarget()
                .path("movement")
                .path(id)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
        
        return RestHelper.readResponseDto(response, MovementType.class);
    }
}
