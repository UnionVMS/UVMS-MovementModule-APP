package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.movement.model.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.rest.RestUtilMapper;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.RealTimeMapInitialData;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class MovementRestResourceTest extends BuildMovementRestDeployment {
    
    @Inject
    private MovementService movementService;

    @Test
    @OperateOnDeployment("movementservice")
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
    @OperateOnDeployment("movementservice")
    public void getLatestMovementsByConnectIds() {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);
        
        List<MovementDto> latestMovements = getLatestMovementsByConnectIds(
                Collections.singletonList(createdMovement.getMovementConnect().getId().toString()));
        assertThat(latestMovements.size(), is(1));
        assertThat(latestMovements.get(0).getId(), is(createdMovement.getId()));
    }
    
    @Test
    @OperateOnDeployment("movementservice")
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
        assertThat(latestMovements.get(0).getId(), is(createdMovement2.getId()));
    }
    
    @Test
    @OperateOnDeployment("movementservice")
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
        assertThat(latestMovements.get(0).getId(), is(createdMovement1.getId()));
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovements() {
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        movementService.createAndProcessMovement(movementBaseType1);
        
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementService.createAndProcessMovement(movementBaseType2);
        
        List<MovementDto> latestMovements = getLatestMovements(1);
        assertThat(latestMovements.size(), is(1));
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementById() {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        MovementType fetchedMovement = getMovementById(createdMovement.getId().toString());
        assertThat(fetchedMovement, is(notNullValue()));
        assertThat(fetchedMovement.getGuid(), is(createdMovement.getId().toString()));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestXNumberOfMovementsForAssetTest() {
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


        List<MovementDto> latestMovements = getWebTarget()
                .path("movement")
                .path("track")
                .path("latest")
                .path("asset")
                .path(connectId.toString())
                .queryParam("maxNbr", 2) 
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(""), new GenericType<List<MovementDto>>() {});

        assertFalse(latestMovements.isEmpty());
        assertTrue(latestMovements.stream().
                noneMatch(m -> m.getId().equals(createdMovement.getId())));
        assertTrue(latestMovements.stream()
                .anyMatch(m -> m.getId().equals(createdMovement2.getId())));
        assertTrue(latestMovements.stream()
                .anyMatch(m -> m.getId().equals(createdMovement3.getId())));
    }
    
    /*
     * Moved from microMovementRestResourceTest
     */
    
    @Test
    @OperateOnDeployment("movementservice")
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


        List<MovementDto> latestMovements = getWebTarget()
                .path("movement")
                .path("track")
                .path("asset")
                .path(connectId.toString())
                .queryParam("startDate", DateUtils.dateToEpochMilliseconds(startTime)) //yyyy-MM-dd HH:mm:ss Z
                .queryParam("endDate", DateUtils.dateToHumanReadableString(endTime))
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(""), new GenericType<List<MovementDto>>() {});

        assertFalse(latestMovements.isEmpty());
        assertTrue(latestMovements.stream().
                noneMatch(m -> m.getId().equals(createdMovement.getId())));
        assertTrue(latestMovements.stream()
                .anyMatch(m -> m.getId().equals(createdMovement2.getId())));
        assertTrue(latestMovements.stream()
                .noneMatch(m -> m.getId().equals(createdMovement3.getId())));
    }
    

    /*
     * Helper functions for REST calls
     */

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
