package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.schema.movement.search.v1.GroupListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKeyType;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class MovementSearchGroupResourceTest extends BuildMovementRestDeployment {
    
    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementSearchGroup() {
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
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroupById() {
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
    @OperateOnDeployment("movementservice")
    public void updateMovementSearchGroup() {
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
    @OperateOnDeployment("movementservice")
    public void getMovementSearchGroupByUser() {
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
    private MovementSearchGroup createMovementSearchGroup(MovementSearchGroup searchGroup) {
        return getWebTarget()
                .path("search")
                .path("group")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(searchGroup), MovementSearchGroup.class);
    }
    
    private MovementSearchGroup getMovementSearchGroup(String id) {
        return getWebTarget()
                .path("search")
                .path("group")
                .path(id)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(MovementSearchGroup.class);
    }
    
    private MovementSearchGroup updateMovementSearchGroup(MovementSearchGroup searchGroup) {
        return getWebTarget()
                .path("search")
                .path("group")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(searchGroup), MovementSearchGroup.class);
    }

    private List<MovementSearchGroup> getMovementSearchGroupByUser(String user) {
        return getWebTarget()
                .path("search")
                .path("groups")
                .queryParam("user", user)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<MovementSearchGroup>>(){});
    }
}
