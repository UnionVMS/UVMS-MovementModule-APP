package eu.europa.ec.fisheries.uvms.movement.rest.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.math.BigInteger;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetTempMovementListResponse;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;

@RunWith(Arquillian.class)
public class DraftMovementResourceTest extends BuildMovementRestDeployment {

    @Test
    @OperateOnDeployment("movement")
    public void createTempMovement() throws Exception {
        TempMovementType tempMovement = MovementTestHelper.createTempMovementType();
        TempMovementType createdTempMovement = createTempMovement(tempMovement);
        
        assertThat(createdTempMovement.getGuid(), is(notNullValue()));
        assertThat(createdTempMovement.getAsset(), is(tempMovement.getAsset()));
        assertThat(createdTempMovement.getPosition(), is(tempMovement.getPosition()));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void getTempMovementById() throws Exception {
        TempMovementType tempMovement = MovementTestHelper.createTempMovementType();
        TempMovementType createdTempMovement = createTempMovement(tempMovement);
        
        TempMovementType fetchedTempMovement = getTempMovement(createdTempMovement.getGuid());
        assertThat(fetchedTempMovement.getGuid(), is(createdTempMovement.getGuid()));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void updateTempMovement() throws Exception {
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
    public void getTempMovements() throws Exception {

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
    private TempMovementType createTempMovement(TempMovementType tempMovement) throws Exception {
        String response = getWebTarget()
                .path("tempmovement")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(tempMovement), String.class);
        
        return RestHelper.readResponseDto(response, TempMovementType.class);
    }
    
    private TempMovementType updateTempMovement(TempMovementType tempMovement) throws Exception {
        String response = getWebTarget()
                .path("tempmovement")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(tempMovement), String.class);
        
        return RestHelper.readResponseDto(response, TempMovementType.class);
    }
    
    private TempMovementType getTempMovement(String guid) throws Exception {
        String response = getWebTarget()
                .path("tempmovement")
                .path(guid)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(String.class);
        
        return RestHelper.readResponseDto(response, TempMovementType.class);
    }

    private GetTempMovementListResponse getTempMovements(MovementQuery query) throws Exception {
        String response = getWebTarget()
                .path("tempmovement")
                .path("list")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(query), String.class);

        return RestHelper.readResponseDto(response, GetTempMovementListResponse.class);
    }
}
