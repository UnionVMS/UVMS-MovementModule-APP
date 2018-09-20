package eu.europa.ec.fisheries.uvms.movement.rest.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
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
    public void getListByQueryByConnectId() throws Exception {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createMovement(movementBaseType, "Test");
        
        // This is needed until background job have been removed
        Thread.sleep(5000);
        
        MovementQuery query = MovementTestHelper.createMovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement.getMovementConnect().getValue());
        query.getMovementSearchCriteria().add(criteria);
        
        GetMovementListByQueryResponse queryResponse = getListByQuery(query);
        
        assertThat(queryResponse, is(notNullValue()));
        List<MovementType> movements = queryResponse.getMovement();
        assertThat(movements.size(), is(1));
        
        assertThat(movements.get(0).getGuid(), is(createdMovement.getGuid()));
    }
    
    @Test
    public void getMinimalListByQueryByConnectId() throws Exception {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createMovement(movementBaseType, "Test");
        
        // This is needed until background job have been removed
        Thread.sleep(5000);
        
        MovementQuery query = MovementTestHelper.createMovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement.getMovementConnect().getValue());
        query.getMovementSearchCriteria().add(criteria);
        
        GetMovementListByQueryResponse queryResponse = getMinimalListByQuery(query);
        
        assertThat(queryResponse, is(notNullValue()));
        List<MovementType> movements = queryResponse.getMovement();
        assertThat(movements.size(), is(1));
        
        assertThat(movements.get(0).getGuid(), is(createdMovement.getGuid()));
    }
    
    @Test
    public void getLatestMovementsByConnectIds() throws Exception {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createMovement(movementBaseType, "Test");
        
        // This is needed until background job have been removed
        Thread.sleep(5000);
        
        List<MovementDto> latestMovements = getLatestMovementsByConnectIds(Arrays.asList(createdMovement.getMovementConnect().getValue()));
        assertThat(latestMovements.size(), is(1));
        assertThat(latestMovements.get(0).getMovementGUID(), is(createdMovement.getGuid()));
    }
    
    @Test
    public void getLatestMovementsByConnectIdsTwoPositions() throws Exception {
        String connectId = UUID.randomUUID().toString();
        
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        movementBaseType1.getMovementConnect().setValue(connectId);
        movementBaseType1.setTimestamp(Instant.now().minusSeconds(60));
        movementService.createMovement(movementBaseType1, "Test");
        
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType1.getMovementConnect().setValue(connectId);
        Movement createdMovement2 = movementService.createMovement(movementBaseType2, "Test");
        
        // This is needed until background job have been removed
        Thread.sleep(5000);
        
        List<MovementDto> latestMovements = getLatestMovementsByConnectIds(Arrays.asList(createdMovement2.getMovementConnect().getValue()));
        assertThat(latestMovements.size(), is(1));
        assertThat(latestMovements.get(0).getMovementGUID(), is(createdMovement2.getGuid()));
    }
    
    @Test
    public void getLatestMovementsByConnectIdsTwoPositionsUnordered() throws Exception {
        String connectId = UUID.randomUUID().toString();
        
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        movementBaseType1.getMovementConnect().setValue(connectId);
        Movement createdMovement1 = movementService.createMovement(movementBaseType1, "Test");
        
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType1.getMovementConnect().setValue(connectId);
        movementBaseType2.setTimestamp(Instant.now().minusSeconds(60));
        movementService.createMovement(movementBaseType2, "Test");
        
        // This is needed until background job have been removed
        Thread.sleep(5000);
        
        List<MovementDto> latestMovements = getLatestMovementsByConnectIds(Arrays.asList(createdMovement1.getMovementConnect().getValue()));
        assertThat(latestMovements.size(), is(1));
        assertThat(latestMovements.get(0).getMovementGUID(), is(createdMovement1.getGuid()));
    }
    
    @Test
    public void getLatestMovements() throws Exception {
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        movementService.createMovement(movementBaseType1, "Test");
        
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementService.createMovement(movementBaseType2, "Test");
        
        // This is needed until background job have been removed
        Thread.sleep(5000);
        
        List<MovementDto> latestMovements = getLatestMovements(1);
        assertThat(latestMovements.size(), is(1));
    }
    
    @Test
    public void getMovementById() throws Exception {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createMovement(movementBaseType, "Test");

        // This is needed until background job have been removed
        Thread.sleep(5000);
        
        MovementType fetchedMovement = getMovementById(createdMovement.getGuid());
        assertThat(fetchedMovement, is(notNullValue()));
        assertThat(fetchedMovement.getGuid(), is(createdMovement.getGuid()));
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
