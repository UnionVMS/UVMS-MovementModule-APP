package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import eu.europa.ec.fisheries.uvms.movement.model.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.model.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MicroMovementsForConnectIdsBetweenDatesRequest;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovement;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.util.JsonBConfiguratorMovement;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class InternalRestResourceTest extends BuildMovementRestDeployment {

    @Inject
    private MovementService movementService;

    @Inject
    private MovementDao movementDao;

    private Jsonb jsonb;

    @Before
    public void init(){
        jsonb = new JsonBConfiguratorMovement().getContext(null);
    }

    @Test
    @OperateOnDeployment("movement")
    public void getMovementListByQuery() {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        assertNotNull(createdMovement.getId());

        MovementQuery query = createMovementQuery(null);

        String response = getWebTarget()
                .path("internal/list")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getTokenInternalRest())
                .post(Entity.json(query), String.class);
        assertNotNull(response);

        GetMovementListByQueryResponse movList =
                jsonb.fromJson(response, GetMovementListByQueryResponse.class);
        assertNotNull(movList);
        assertTrue(movList.getMovement().size() > 0);
    }

    @Test
    @OperateOnDeployment("movement")
    public void getLatestMovementsByConnectIds() {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        assertNotNull(createdMovement.getId());

        UUID movConnectId = createdMovement.getMovementConnect().getId();

        String response = getWebTarget()
                .path("internal/latest")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getTokenInternalRest())
                .post(Entity.json(Collections.singletonList(movConnectId)), String.class);
        assertNotNull(response);

        List<MovementType> movements = Arrays.asList(jsonb.fromJson(response, MovementType[].class));

        assertNotNull(movements);
        assertEquals(1, movements.size());
    }

    @Test
    @OperateOnDeployment("movement")
    public void countMovementsInTheLastDayForAssetTest() {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);
        Instant in60Seconds = Instant.now().plusSeconds(60);

        long response = getWebTarget()
                .path("internal/countMovementsInDateAndTheDayBeforeForAsset/")
                .path(createdMovement.getMovementConnect().getId().toString())
                .queryParam("after", DateUtils.dateToEpochMilliseconds(in60Seconds)) //yyyy-MM-dd HH:mm:ss Z
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getTokenInternalRest())
                .get(long.class);

        assertEquals(1, response);
    }

    @Test
    @OperateOnDeployment("movement")
    public void createAndGetMicroMovementById() {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        MicroMovement response = getWebTarget()
                .path("internal/getMicroMovement/")
                .path(createdMovement.getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getTokenInternalRest())
                .get(MicroMovement.class);

        assertNotNull(response);
    }

    @Test
    @OperateOnDeployment("movement")
    public void getMovementMapByQuery() throws IOException {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        MovementQuery query = createMovementQuery(createdMovement);

        String response = getWebTarget()
                .path("internal/movementMapByQuery")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getTokenInternalRest())
                .post(Entity.json(query), String.class);
        assertNotNull(response);

        GetMovementMapByQueryResponse movMap = jsonb.fromJson(response, GetMovementMapByQueryResponse.class);
        assertNotNull(movMap);
        assertEquals(1, movMap.getMovementMap().size());
    }

    @Test
    @OperateOnDeployment("movement")
    public void remapMovementConnectInMovementTest() {
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        Movement createdMovement1 = movementService.createAndProcessMovement(movementBaseType1);
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);

        Response remapResponse = getWebTarget()
                .path("internal/remapMovementConnectInMovement")
                .queryParam("MovementConnectFrom", createdMovement1.getMovementConnect().getId().toString())
                .queryParam("MovementConnectTo", createdMovement2.getMovementConnect().getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getTokenInternalRest())
                .put(Entity.json(""), Response.class);
        assertEquals(200, remapResponse.getStatus());
        int nrOfChanges = remapResponse.readEntity(Integer.class);
        assertEquals(1, nrOfChanges);

        MovementQuery movementQuery = createMovementQuery(null);
        movementQuery.getMovementSearchCriteria().clear();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement2.getMovementConnect().getId().toString());
        movementQuery.getMovementSearchCriteria().add(criteria);

        String response = getWebTarget()
                .path("internal/list")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getTokenInternalRest())
                .post(Entity.json(movementQuery), String.class);
        assertNotNull(response);

        GetMovementListByQueryResponse movList =
                jsonb.fromJson(response, GetMovementListByQueryResponse.class);
        assertEquals(2, movList.getMovement().size());
        assertTrue(movList.getMovement().stream().anyMatch(m -> m.getGuid().equals(createdMovement1.getId().toString())));
        assertTrue(movList.getMovement().stream().anyMatch(m -> m.getGuid().equals(createdMovement2.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movement")
    public void remapMovementConnectInMovementAndDeleteOldMovementConnectTest() {
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        Movement movementBaseType2 = MovementTestHelper.createMovement();
        Movement createdMovement1 = movementService.createAndProcessMovement(movementBaseType1);
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);


        Response remapResponse = getWebTarget()
                .path("internal/remapMovementConnectInMovement")
                .queryParam("MovementConnectFrom", createdMovement1.getMovementConnect().getId().toString())
                .queryParam("MovementConnectTo", createdMovement2.getMovementConnect().getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getTokenInternalRest())
                .put(Entity.json(""), Response.class);
        assertEquals(200, remapResponse.getStatus());
        int nrOfChanges = remapResponse.readEntity(Integer.class);
        assertEquals(1, nrOfChanges);

        Response deleteResponse = getWebTarget()
                .path("internal/removeMovementConnect")
                .queryParam("MovementConnectId", createdMovement1.getMovementConnect().getId().toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getTokenInternalRest())
                .delete(Response.class);
        assertEquals(200, deleteResponse.getStatus());

        assertNull(movementDao.getMovementConnectByConnectId(createdMovement1.getMovementConnect().getId()));
    }

    @Test
    @OperateOnDeployment("movement")
    public void removeNonExistantMCTest() {
        Response deleteResponse = getWebTarget()
                .path("internal/removeMovementConnect")
                .queryParam("MovementConnectId", UUID.randomUUID().toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getTokenInternalRest())
                .delete(Response.class);
        assertEquals(200, deleteResponse.getStatus());
    }

    @Test
    @OperateOnDeployment("movement")
    public void microMovementsForConnectIdsBetweenDates() {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

        assertNotNull(createdMovement.getId());

        List<String> connectIds = new ArrayList<>();
        connectIds.add(movementBaseType.getMovementConnect().getId().toString());

        Instant now = Instant.now();
        Instant dayBefore = now.minus(1, ChronoUnit.DAYS);

        MicroMovementsForConnectIdsBetweenDatesRequest request = new MicroMovementsForConnectIdsBetweenDatesRequest(connectIds, dayBefore, now);

        Response response = getWebTarget()
                .path("internal/microMovementsForConnectIdsBetweenDates")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getTokenInternalRest())
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE), Response.class);

        List<MicroMovementExtended> microMovementExtendedList = response.readEntity(new GenericType<List<MicroMovementExtended>>() {});

        assertEquals(1, microMovementExtendedList.size());
    }

    private MovementQuery createMovementQuery(Movement createdMovement) {
        MovementQuery query = new MovementQuery();
        if(createdMovement != null) {
            ListCriteria criteria = new ListCriteria();
            criteria.setKey(SearchKey.MOVEMENT_ID);
            criteria.setValue(createdMovement.getId().toString());
            query.getMovementSearchCriteria().add(criteria);
        } else {
            ListCriteria criteria = new ListCriteria();
            criteria.setKey(SearchKey.STATUS);
            criteria.setValue("TEST");
            query.getMovementSearchCriteria().add(criteria);

            ListPagination pagination = new ListPagination();
            pagination.setPage(BigInteger.ONE);
            pagination.setListSize(BigInteger.valueOf(10));
            query.setPagination(pagination);
        }
        return query;
    }
}
