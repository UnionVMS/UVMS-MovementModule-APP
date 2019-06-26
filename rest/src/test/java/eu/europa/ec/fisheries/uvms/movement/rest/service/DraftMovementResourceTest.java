package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetTempMovementListResponse;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class DraftMovementResourceTest extends BuildMovementRestDeployment {

    @Test
    @OperateOnDeployment("movement")
    public void createTempMovement() {
        TempMovementType tempMovement = MovementTestHelper.createTempMovementType();
        TempMovementType createdTempMovement = createTempMovement(tempMovement);
        
        assertThat(createdTempMovement.getGuid(), is(notNullValue()));
        assertThat(createdTempMovement.getAsset(), is(tempMovement.getAsset()));
        assertThat(createdTempMovement.getPosition(), is(tempMovement.getPosition()));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void getTempMovementById() {
        TempMovementType tempMovement = MovementTestHelper.createTempMovementType();
        TempMovementType createdTempMovement = createTempMovement(tempMovement);
        
        TempMovementType fetchedTempMovement = getTempMovement(createdTempMovement.getGuid());
        assertThat(fetchedTempMovement.getGuid(), is(createdTempMovement.getGuid()));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void updateTempMovement() {
        TempMovementType tempMovement = MovementTestHelper.createTempMovementType();
        TempMovementType createdTempMovement = createTempMovement(tempMovement);
        
        double newCourse = 42d;
        createdTempMovement.setCourse(newCourse);
        
        TempMovementType updatedTempMovement = updateTempMovement(createdTempMovement);
        assertThat(updatedTempMovement.getGuid(), is(createdTempMovement.getGuid()));
        assertThat(updatedTempMovement.getCourse(), is(newCourse));
    }
   
    @Test
    @OperateOnDeployment("movement")
    public void getTempMovements() {
        TempMovementType tempMovement = MovementTestHelper.createTempMovementType();
        TempMovementType createdTempMovement = createTempMovement(tempMovement);

        assertThat(createdTempMovement.getGuid(), is(notNullValue()));
        assertThat(createdTempMovement.getAsset(), is(tempMovement.getAsset()));
        assertThat(createdTempMovement.getPosition(), is(tempMovement.getPosition()));

        MovementQuery query = new MovementQuery();
        ListPagination listPagination = new ListPagination();
        listPagination.setPage(BigInteger.valueOf(1));
        listPagination.setListSize(BigInteger.valueOf(1));
        query.setPagination(listPagination);

        GetTempMovementListResponse tempMovements = getTempMovements(query);
        assertNotNull(tempMovements);
        assertTrue(tempMovements.getMovement().size() > 0);
    }

    /*
     * Helper functions for REST calls
     */
    private TempMovementType createTempMovement(TempMovementType tempMovement) {
        return getWebTarget()
                .path("tempmovement")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(tempMovement), TempMovementType.class);
    }
    
    private TempMovementType updateTempMovement(TempMovementType tempMovement) {
        return getWebTarget()
                .path("tempmovement")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(tempMovement), TempMovementType.class);
    }
    
    private TempMovementType getTempMovement(String guid) {
        return getWebTarget()
                .path("tempmovement")
                .path(guid)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(TempMovementType.class);
    }

    private GetTempMovementListResponse getTempMovements(MovementQuery query) {
        return getWebTarget()
                .path("tempmovement")
                .path("list")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(query), GetTempMovementListResponse.class);
    }
}
