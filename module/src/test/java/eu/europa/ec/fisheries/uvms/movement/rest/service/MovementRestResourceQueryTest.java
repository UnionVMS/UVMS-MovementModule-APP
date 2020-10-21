package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.schema.movement.search.v1.*;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.model.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class MovementRestResourceQueryTest extends BuildMovementRestDeployment {

    @Inject
    private MovementService movementService;

    @Test
    @OperateOnDeployment("movementservice")
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
    @OperateOnDeployment("movementservice")
    public void getSeveralMovesByConnectId () {
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        Movement createdMovement1 = movementService.createAndProcessMovement(movementBaseType1);

        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType2.setMovementConnect(movementBaseType1.getMovementConnect());
        movementBaseType2.setTimestamp(Instant.now().minus(10, ChronoUnit.SECONDS));
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);

        MovementQuery query = MovementTestHelper.createMovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement1.getMovementConnect().getId().toString());
        query.getMovementSearchCriteria().add(criteria);

        GetMovementListByQueryResponse queryResponse = getListByQuery(query);

        assertThat(queryResponse, is(notNullValue()));
        List<MovementType> movements = queryResponse.getMovement();
        assertThat(movements.size(), is(2));

        assertTrue(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement1.getId().toString())));
        assertTrue(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement2.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMoveBySourceAndConnectId () {
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        Movement createdMovement1 = movementService.createAndProcessMovement(movementBaseType1);

        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType2.setMovementConnect(movementBaseType1.getMovementConnect());
        movementBaseType2.setTimestamp(Instant.now().minus(10, ChronoUnit.SECONDS));
        movementBaseType2.setSource(MovementSourceType.MANUAL);
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);

        MovementQuery query = MovementTestHelper.createMovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement1.getMovementConnect().getId().toString());
        query.getMovementSearchCriteria().add(criteria);

        criteria = new ListCriteria();
        criteria.setKey(SearchKey.SOURCE);
        criteria.setValue(createdMovement1.getSource().value());
        query.getMovementSearchCriteria().add(criteria);

        GetMovementListByQueryResponse queryResponse = getListByQuery(query);

        assertThat(queryResponse, is(notNullValue()));
        List<MovementType> movements = queryResponse.getMovement();
        assertThat(movements.size(), is(1));

        assertTrue(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement1.getId().toString())));
        assertFalse(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement2.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMoveByAreaAndConnectId () {
        Movement movementBaseType1 = MovementTestHelper.createMovement(11d,56d);
        Movement createdMovement1 = movementService.createAndProcessMovement(movementBaseType1);

        Movement movementBaseType2 = MovementTestHelper.createMovement(12d,57d);
        movementBaseType2.setMovementConnect(movementBaseType1.getMovementConnect());
        movementBaseType2.setTimestamp(Instant.now().minus(10, ChronoUnit.SECONDS));
        movementBaseType2.setSource(MovementSourceType.MANUAL);
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);

        MovementQuery query = MovementTestHelper.createMovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement1.getMovementConnect().getId().toString());
        query.getMovementSearchCriteria().add(criteria);

        criteria = new ListCriteria();
        criteria.setKey(SearchKey.AREA);
        criteria.setValue("POLYGON((11.5 55.5,10.5 55.5,10.5 56.5,11.5 56.5,11.5 55.5))");
        query.getMovementSearchCriteria().add(criteria);

        GetMovementListByQueryResponse queryResponse = getListByQuery(query);

        assertThat(queryResponse, is(notNullValue()));
        List<MovementType> movements = queryResponse.getMovement();
        assertThat(movements.size(), is(1));

        assertTrue(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement1.getId().toString())));
        assertFalse(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement2.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMoveBySourceAndStatus () {
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        Movement createdMovement1 = movementService.createAndProcessMovement(movementBaseType1);

        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType2.setMovementConnect(movementBaseType1.getMovementConnect());
        movementBaseType2.setTimestamp(Instant.now().minus(10, ChronoUnit.SECONDS));
        movementBaseType2.setSource(MovementSourceType.MANUAL);
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);

        MovementQuery query = MovementTestHelper.createMovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.STATUS);
        criteria.setValue(createdMovement1.getStatus());
        query.getMovementSearchCriteria().add(criteria);

        criteria = new ListCriteria();
        criteria.setKey(SearchKey.SOURCE);
        criteria.setValue(createdMovement1.getSource().value());
        query.getMovementSearchCriteria().add(criteria);

        GetMovementListByQueryResponse queryResponse = getListByQuery(query);

        assertThat(queryResponse, is(notNullValue()));
        List<MovementType> movements = queryResponse.getMovement();

        assertTrue(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement1.getId().toString())));
        assertFalse(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement2.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMoveBySourceAndType () {
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        Movement createdMovement1 = movementService.createAndProcessMovement(movementBaseType1);

        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType2.setMovementConnect(movementBaseType1.getMovementConnect());
        movementBaseType2.setTimestamp(Instant.now().minus(10, ChronoUnit.SECONDS));
        movementBaseType2.setSource(MovementSourceType.MANUAL);
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);

        MovementQuery query = MovementTestHelper.createMovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.MOVEMENT_TYPE);
        criteria.setValue(createdMovement1.getMovementType().value());
        query.getMovementSearchCriteria().add(criteria);

        criteria = new ListCriteria();
        criteria.setKey(SearchKey.SOURCE);
        criteria.setValue(createdMovement1.getSource().value());
        query.getMovementSearchCriteria().add(criteria);

        GetMovementListByQueryResponse queryResponse = getListByQuery(query);

        assertThat(queryResponse, is(notNullValue()));
        List<MovementType> movements = queryResponse.getMovement();

        assertTrue(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement1.getId().toString())));
        assertFalse(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement2.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMoveByConnectIdAndDateRange () {
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        Movement createdMovement1 = movementService.createAndProcessMovement(movementBaseType1);

        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType2.setMovementConnect(movementBaseType1.getMovementConnect());
        movementBaseType2.setTimestamp(Instant.now().minus(10, ChronoUnit.SECONDS));
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);

        MovementQuery query = MovementTestHelper.createMovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement1.getMovementConnect().getId().toString());
        query.getMovementSearchCriteria().add(criteria);

        RangeCriteria rangeCriteria = new RangeCriteria();
        rangeCriteria.setKey(RangeKeyType.DATE);
        rangeCriteria.setFrom("" + createdMovement1.getTimestamp().minus(5,ChronoUnit.SECONDS).toEpochMilli());
        rangeCriteria.setTo("" + createdMovement1.getTimestamp().plus(5,ChronoUnit.SECONDS).toEpochMilli());
        query.getMovementRangeSearchCriteria().add(rangeCriteria);

        GetMovementListByQueryResponse queryResponse = getListByQuery(query);

        assertThat(queryResponse, is(notNullValue()));
        List<MovementType> movements = queryResponse.getMovement();
        assertThat(movements.size(), is(1));

        assertTrue(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement1.getId().toString())));
        assertFalse(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement2.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMoveByConnectIdAndSpeedRange () {
        Movement movementBaseType1 = MovementTestHelper.createMovement();
        Movement createdMovement1 = movementService.createAndProcessMovement(movementBaseType1);

        Movement movementBaseType2 = MovementTestHelper.createMovement();
        movementBaseType2.setMovementConnect(movementBaseType1.getMovementConnect());
        movementBaseType2.setTimestamp(Instant.now().minus(10, ChronoUnit.SECONDS));
        movementBaseType2.setSpeed(5f);
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);

        MovementQuery query = MovementTestHelper.createMovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement1.getMovementConnect().getId().toString());
        query.getMovementSearchCriteria().add(criteria);

        RangeCriteria rangeCriteria = new RangeCriteria();
        rangeCriteria.setKey(RangeKeyType.MOVEMENT_SPEED);
        rangeCriteria.setFrom("" + (createdMovement1.getSpeed() - 1d) );
        rangeCriteria.setTo("" + (createdMovement1.getSpeed() + 1d));
        query.getMovementRangeSearchCriteria().add(rangeCriteria);

        GetMovementListByQueryResponse queryResponse = getListByQuery(query);

        assertThat(queryResponse, is(notNullValue()));
        List<MovementType> movements = queryResponse.getMovement();
        assertThat(movements.size(), is(1));

        assertTrue(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement1.getId().toString())));
        assertFalse(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement2.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMoveByConnectIdAndTrackDurationRange () {
        Movement movementBaseType1 = MovementTestHelper.createMovement(56d,11d);
        Movement createdMovement1 = movementService.createAndProcessMovement(movementBaseType1);

        Movement movementBaseType2 = MovementTestHelper.createMovement(56.1d,11.1d);
        movementBaseType2.setMovementConnect(movementBaseType1.getMovementConnect());
        movementBaseType2.setTimestamp(Instant.now().minus(10, ChronoUnit.SECONDS));
        movementBaseType2.setSpeed(5f);
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);

        MovementQuery query = MovementTestHelper.createMovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement1.getMovementConnect().getId().toString());
        query.getMovementSearchCriteria().add(criteria);

        RangeCriteria rangeCriteria = new RangeCriteria();
        rangeCriteria.setKey(RangeKeyType.TRACK_DURATION);
        rangeCriteria.setFrom("" + 5 );
        rangeCriteria.setTo("" + 15);
        query.getMovementRangeSearchCriteria().add(rangeCriteria);

        GetMovementListByQueryResponse queryResponse = getListByQuery(query);

        assertThat(queryResponse, is(notNullValue()));
        List<MovementType> movements = queryResponse.getMovement();
        assertThat(movements.size(), is(2));

        assertTrue(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement1.getId().toString())));
        assertTrue(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement2.getId().toString())));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMoveByConnectIdAndTrackLengthRange () {
        Movement movementBaseType1 = MovementTestHelper.createMovement(56d,11d);
        Movement createdMovement1 = movementService.createAndProcessMovement(movementBaseType1);

        Movement movementBaseType2 = MovementTestHelper.createMovement(56.1d,11.1d);
        movementBaseType2.setMovementConnect(movementBaseType1.getMovementConnect());
        movementBaseType2.setTimestamp(Instant.now().minus(10, ChronoUnit.SECONDS));
        movementBaseType2.setSpeed(5f);
        Movement createdMovement2 = movementService.createAndProcessMovement(movementBaseType2);

        MovementQuery query = MovementTestHelper.createMovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement1.getMovementConnect().getId().toString());
        query.getMovementSearchCriteria().add(criteria);

        RangeCriteria rangeCriteria = new RangeCriteria();
        rangeCriteria.setKey(RangeKeyType.TRACK_LENGTH);
        rangeCriteria.setFrom("" + 5 );
        rangeCriteria.setTo("" + 15);
        query.getMovementRangeSearchCriteria().add(rangeCriteria);

        GetMovementListByQueryResponse queryResponse = getListByQuery(query);

        assertThat(queryResponse, is(notNullValue()));
        List<MovementType> movements = queryResponse.getMovement();
        assertThat(movements.size(), is(2));

        assertTrue(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement1.getId().toString())));
        assertTrue(movements.stream().anyMatch(type -> type.getGuid().equals(createdMovement2.getId().toString())));
    }

    private GetMovementListByQueryResponse getListByQuery(MovementQuery query) {
        return getWebTarget()
                .path("movement")
                .path("list")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(query), GetMovementListByQueryResponse.class);
    }
}
