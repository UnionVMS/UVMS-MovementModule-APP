package eu.europa.ec.fisheries.uvms.movement.rest.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.StringReader;
import java.util.List;
import java.util.UUID;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.fisheries.schema.movement.search.v1.GroupListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKeyType;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;

@RunWith(Arquillian.class)
public class MovementSearchGroupResourceTest extends BuildMovementRestDeployment {

    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    public void createMovementSearchGroup() throws Exception {
        MovementSearchGroup movementSearchGroup = MovementTestHelper.createBasicMovementSearchGroup();
        GroupListCriteria criteria = new GroupListCriteria();
        criteria.setType(SearchKeyType.MOVEMENT);
        criteria.setKey("MOVEMENT_ID");
        criteria.setValue(UUID.randomUUID().toString());
        movementSearchGroup.getSearchFields().add(criteria);
        MovementSearchGroup createdMovementSearchGroup = createMovementSearchGroup(movementSearchGroup);
        
        assertThat(createdMovementSearchGroup, is(notNullValue()));
        assertThat(createdMovementSearchGroup.getId(), is(notNullValue()));
        assertThat(createdMovementSearchGroup.getName(), is(movementSearchGroup.getName()));
        assertThat(createdMovementSearchGroup.getSearchFields().size(), is(1));
        assertThat(createdMovementSearchGroup.getUser(), is(notNullValue()));
    }
    
    @Test
    public void getMovementSearchGroupById() throws Exception {
        MovementSearchGroup movementSearchGroup = MovementTestHelper.createBasicMovementSearchGroup();
        GroupListCriteria criteria = new GroupListCriteria();
        criteria.setType(SearchKeyType.MOVEMENT);
        criteria.setKey("MOVEMENT_ID");
        criteria.setValue(UUID.randomUUID().toString());
        movementSearchGroup.getSearchFields().add(criteria);
        MovementSearchGroup createdMovementSearchGroup = createMovementSearchGroup(movementSearchGroup);
        
        MovementSearchGroup fetchedMovementSearchGroup = getMovementSearchGroup(createdMovementSearchGroup.getId().toString());
        assertThat(fetchedMovementSearchGroup.getId(), is(createdMovementSearchGroup.getId()));
    }
    
    @Test
    public void updateMovementSearchGroup() throws Exception {
        MovementSearchGroup movementSearchGroup = MovementTestHelper.createBasicMovementSearchGroup();
        GroupListCriteria criteria = new GroupListCriteria();
        criteria.setType(SearchKeyType.MOVEMENT);
        criteria.setKey("MOVEMENT_ID");
        criteria.setValue(UUID.randomUUID().toString());
        movementSearchGroup.getSearchFields().add(criteria);
        MovementSearchGroup createdMovementSearchGroup = createMovementSearchGroup(movementSearchGroup);
        
        String newName = "Updated" + MovementTestHelper.getRandomIntegers(5);
        createdMovementSearchGroup.setName(newName);
        updateMovementSearchGroup(createdMovementSearchGroup);
        
        MovementSearchGroup fetchedGroup = getMovementSearchGroup(createdMovementSearchGroup.getId().toString());
        assertThat(fetchedGroup.getId(), is(createdMovementSearchGroup.getId()));
        assertThat(fetchedGroup.getName(), is(newName));
    }
    
    @Test
    public void getMovementSearchGroupByUser() throws Exception {
        MovementSearchGroup movementSearchGroup = MovementTestHelper.createBasicMovementSearchGroup();
        GroupListCriteria criteria = new GroupListCriteria();
        criteria.setType(SearchKeyType.MOVEMENT);
        criteria.setKey("MOVEMENT_ID");
        criteria.setValue(UUID.randomUUID().toString());
        movementSearchGroup.getSearchFields().add(criteria);
        MovementSearchGroup createdMovementSearchGroup = createMovementSearchGroup(movementSearchGroup);
        
        List<MovementSearchGroup> searchGroups = getMovementSearchGroupByUser("TEST");
        assertTrue(searchGroups.contains(createdMovementSearchGroup));
    }
    
    /*
     * Helper functions for REST calls
     */
    private MovementSearchGroup createMovementSearchGroup(MovementSearchGroup searchGroup) throws Exception {
        String response = getWebTarget()
                .path("search")
                .path("group")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(searchGroup), String.class);
            
        return readResponseDto(response, MovementSearchGroup.class);
    }
    
    private MovementSearchGroup getMovementSearchGroup(String id) throws Exception {
        String response = getWebTarget()
                .path("search")
                .path("group")
                .path(id)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
        
        return readResponseDto(response, MovementSearchGroup.class); 
    }
    
    private MovementSearchGroup updateMovementSearchGroup(MovementSearchGroup searchGroup) throws Exception {
        String response = getWebTarget()
                .path("search")
                .path("group")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(searchGroup), String.class);
            
        return readResponseDto(response, MovementSearchGroup.class);
    }

    private List<MovementSearchGroup> getMovementSearchGroupByUser(String user) throws Exception {
        String response = getWebTarget()
                .path("search")
                .path("groups")
                .queryParam("user", user)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
            
        return readResponseDtoList(response, MovementSearchGroup.class);
    }
    
    private <T> T readResponseDto(String response, Class<T> clazz) throws Exception {
        JsonReader jsonReader = Json.createReader(new StringReader(response));
        JsonObject responseDto = jsonReader.readObject();
        JsonObject data = responseDto.getJsonObject("data");
        return objectMapper.readValue(data.toString(), clazz);
    }
    
    private <T> List<T> readResponseDtoList(String response, Class<T> clazz) throws Exception {
        JsonReader jsonReader = Json.createReader(new StringReader(response));
        JsonObject responseDto = jsonReader.readObject();
        JsonArray data = responseDto.getJsonArray("data");
        return objectMapper.readValue(data.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }
}
