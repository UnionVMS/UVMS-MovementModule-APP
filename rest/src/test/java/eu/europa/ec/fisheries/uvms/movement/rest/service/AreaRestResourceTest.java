package eu.europa.ec.fisheries.uvms.movement.rest.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.time.Instant;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;

@RunWith(Arquillian.class)
public class AreaRestResourceTest extends BuildMovementRestDeployment {

    @Inject
    private AreaDao areaDao;

    @Test
    @OperateOnDeployment("movement")
    public void getAreasTest() throws Exception {

        AreaType areaType = createAreaType();
        Area area = createArea(areaType);
        areaDao.createMovementArea(area);

        assertEquals(area.getAreaType().getName(), areaType.getName());

        List<eu.europa.ec.fisheries.schema.movement.area.v1.AreaType> areaTypes = getAreas();
        assertTrue(areaTypes.size() > 0);
    }

    private List<eu.europa.ec.fisheries.schema.movement.area.v1.AreaType> getAreas() throws Exception {
        String response = getWebTarget()
                .path("areas")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        return RestHelper.readResponseDtoList(response, eu.europa.ec.fisheries.schema.movement.area.v1.AreaType.class);
    }

    private AreaType createAreaType() {
        AreaType areaType = new AreaType();
        String input = "TestAreaType" + MovementTestHelper.getRandomIntegers(10);
        areaType.setName(input);
        areaType.setUpdatedTime(Instant.now());
        areaType.setUpdatedUser("TestUser");
        return areaType;
    }

    private Area createArea(AreaType areaType) {
        Area area = new Area();
        area.setAreaName("TestArea");
        area.setAreaCode("AreaCode" + MovementTestHelper.getRandomIntegers(10));
        area.setRemoteId("TestRemoteId");
        area.setAreaUpdattim(Instant.now());
        area.setAreaUpuser("TestUser");
        area.setAreaType(areaType);
        return area;
    }
}
