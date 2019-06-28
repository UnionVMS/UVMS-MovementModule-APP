package eu.europa.ec.fisheries.uvms.movement.rest.service;

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
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class MovementRestResourceTest extends BuildMovementRestDeployment {
    
    @Inject
    private MovementService movementService;
    
    @Test
    @OperateOnDeployment("movement")
    public void getListByQueryByConnectId() {
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
    public void getMinimalListByQueryByConnectId() {
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
    public void getLatestMovementsByConnectIds() {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);
        
        List<MovementDto> latestMovements = getLatestMovementsByConnectIds(
                Collections.singletonList(createdMovement.getMovementConnect().getId().toString()));
        assertThat(latestMovements.size(), is(1));
        assertThat(latestMovements.get(0).getMovementGUID(), is(createdMovement.getId().toString()));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void getLatestMovementsByConnectIdsTwoPositions() {
        UUID connectId = UUID.randomUUID();
        
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        movementBaseType1.getMovementConnect().setId(connectId);
        movementBaseType1.setTimestamp(Instant.now().minusSeconds(60));
        movementService.createAndProcessMovement(movementBaseType1);
        
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType1.getMovementConnect().setId(connectId);
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);
        
        List<MovementDto> latestMovements = getLatestMovementsByConnectIds(
                Collections.singletonList(createdMovement2.getMovementConnect().getId().toString()));
        assertThat(latestMovements.size(), is(1));
        assertThat(latestMovements.get(0).getMovementGUID(), is(createdMovement2.getId().toString()));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void getLatestMovementsByConnectIdsTwoPositionsUnordered() {
        UUID connectId = UUID.randomUUID();
        
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        movementBaseType1.getMovementConnect().setId(connectId);
        Movement createdMovement1 = movementService.createAndProcessMovement(movementBaseType1);
        
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType1.getMovementConnect().setId(connectId);
        movementBaseType2.setTimestamp(Instant.now().minusSeconds(60));
        movementService.createAndProcessMovement(movementBaseType2);
        
        List<MovementDto> latestMovements = getLatestMovementsByConnectIds(
                Collections.singletonList(createdMovement1.getMovementConnect().getId().toString()));
        assertThat(latestMovements.size(), is(1));
        assertThat(latestMovements.get(0).getMovementGUID(), is(createdMovement1.getId().toString()));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void getLatestMovements() {
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        movementService.createAndProcessMovement(movementBaseType1);
        
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementService.createAndProcessMovement(movementBaseType2);
        
        List<MovementDto> latestMovements = getLatestMovements(1);
        assertThat(latestMovements.size(), is(1));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void getMovementById() {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        MovementType fetchedMovement = getMovementById(createdMovement.getId().toString());
        assertThat(fetchedMovement, is(notNullValue()));
        assertThat(fetchedMovement.getGuid(), is(createdMovement.getId().toString()));
    }

    /*
     * Helper functions for REST calls
     */
    private GetMovementListByQueryResponse getListByQuery(MovementQuery query) {
        return getWebTarget()
                .path("movement")
                .path("list")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(query), GetMovementListByQueryResponse.class);
    }
    
    private GetMovementListByQueryResponse getMinimalListByQuery(MovementQuery query) {
        return getWebTarget()
                .path("movement")
                .path("list")
                .path("minimal")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(query), GetMovementListByQueryResponse.class);
    }
    
    private List<MovementDto> getLatestMovementsByConnectIds(List<String> connectIds) {
         return getWebTarget()
                .path("movement")
                .path("latest")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(connectIds), new GenericType<List<MovementDto>>(){});
    }
    
    private List<MovementDto> getLatestMovements(int numberOfMovements) {
        return getWebTarget()
                .path("movement")
                .path("latest/" + numberOfMovements)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<MovementDto>>(){});
    }
    
    private MovementType getMovementById(String id) {
        return getWebTarget()
                .path("movement")
                .path(id)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(MovementType.class);
    }
}
