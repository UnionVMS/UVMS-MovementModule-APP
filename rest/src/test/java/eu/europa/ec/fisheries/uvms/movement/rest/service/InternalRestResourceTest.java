package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.math.BigInteger;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class InternalRestResourceTest extends BuildMovementRestDeployment {

    @Inject
    private MovementService movementService;

    @Test
    @OperateOnDeployment("movement")
    public void countMovementsInTheLastDayForAssetTest() {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createMovement(movementBaseType);
        Instant now = Instant.now().plusSeconds(60);

        long response = getWebTarget()
                .path("internal/countMovementsInDateAndTheDayBeforeForAsset/" + createdMovement.getMovementConnect().getId().toString())
                .queryParam("after", DateUtil.parseUTCDateToString(now))    //yyyy-MM-dd HH:mm:ss Z
                .request(MediaType.APPLICATION_JSON)
                .get(long.class);

        assertEquals(1, response);
    }

    @Test
    @OperateOnDeployment("movement")
    public void getMovementMapByQuery() {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createMovement(movementBaseType);

        MovementQuery query = createMovementQuery(createdMovement);

        Response response = getWebTarget()
                .path("internal/movementMapByQuery/")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(query));

        assertEquals(200, response.getStatus());

        GetMovementMapByQueryResponse movMap = response.readEntity(GetMovementMapByQueryResponse.class);
        assertNotNull(movMap);
        assertEquals(1, movMap.getMovementMap().size());

        assertNotNull(response);

    }

    private MovementQuery createMovementQuery(Movement createdMovement) {
        MovementQuery query = new MovementQuery();
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.MOVEMENT_ID);
        criteria.setValue(createdMovement.getId().toString());
        query.getMovementSearchCriteria().add(criteria);
        return query;
    }
}
