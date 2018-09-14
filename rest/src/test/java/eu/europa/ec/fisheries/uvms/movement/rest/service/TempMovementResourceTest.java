package eu.europa.ec.fisheries.uvms.movement.rest.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;

@RunWith(Arquillian.class)
public class TempMovementResourceTest extends BuildMovementRestDeployment {

    @Test
    public void createTempMovement() throws Exception {
        TempMovementType tempMovement = MovementTestHelper.createTempMovementType();
        TempMovementType createdTempMovement = createTempMovement(tempMovement);
        
        assertThat(createdTempMovement.getGuid(), is(notNullValue()));
        assertThat(createdTempMovement.getAsset(), is(tempMovement.getAsset()));
        assertThat(createdTempMovement.getPosition(), is(tempMovement.getPosition()));
    }
    
    @Test
    public void getTempMovementById() throws Exception {
        TempMovementType tempMovement = MovementTestHelper.createTempMovementType();
        TempMovementType createdTempMovement = createTempMovement(tempMovement);
        
        TempMovementType fetchedTempMovement = getTempMovement(createdTempMovement.getGuid());
        assertThat(fetchedTempMovement.getGuid(), is(createdTempMovement.getGuid()));
    }
    
    /*
     * Helper functions for REST calls
     */
    private TempMovementType createTempMovement(TempMovementType tempMovement) throws Exception {
        String response = getWebTarget()
                .path("tempmovement")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(tempMovement), String.class);
        
        return RestHelper.readResponseDto(response, TempMovementType.class);
    }
    
    private TempMovementType getTempMovement(String guid) throws Exception {
        String response = getWebTarget()
                .path("tempmovement")
                .path(guid)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
        
        return RestHelper.readResponseDto(response, TempMovementType.class);
    }
}
