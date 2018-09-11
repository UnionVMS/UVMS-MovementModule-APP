package eu.europa.ec.fisheries.uvms.movement.rest.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;

@RunWith(Arquillian.class)
public class TempMovementResourceTest extends BuildMovementRestDeployment {

    private ObjectMapper objectMapper = new ObjectMapper();

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
        
        return readResponseDto(response, TempMovementType.class);
    }
    
    private TempMovementType getTempMovement(String guid) throws Exception {
        String response = getWebTarget()
                .path("tempmovement")
                .path(guid)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
        
        return readResponseDto(response, TempMovementType.class);
    }
    
    private <T> T readResponseDto(String response, Class<T> clazz) throws Exception {
        JsonReader jsonReader = Json.createReader(new StringReader(response));
        JsonObject responseDto = jsonReader.readObject();
        JsonObject data = responseDto.getJsonObject("data");
        return objectMapper.readValue(data.toString(), clazz);
    }
}
