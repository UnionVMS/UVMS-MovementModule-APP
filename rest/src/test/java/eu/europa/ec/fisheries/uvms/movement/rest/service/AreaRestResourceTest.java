package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.uvms.movement.dao.bean.AreaDaoBean;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class AreaRestResourceTest extends BuildMovementRestDeployment {

    @Inject
    private AreaDaoBean areaDao;

    @Test
    public void getAreasTest() throws Exception {

        AreaType areaType = createAreaType();
        Area area = createArea(areaType);
        areaDao.createMovementArea(area);

        assertEquals(area.getAreaType().getName(), "TestAreaType");

        List<eu.europa.ec.fisheries.schema.movement.area.v1.AreaType> areaTypes = getAreas();
        assertEquals(areaTypes.size(), 1);
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
        String input = "TestAreaType";
        areaType.setName(input);
        areaType.setUpdatedTime(Instant.now());
        areaType.setUpdatedUser("TestUser");
        return areaType;
    }

    private Area createArea(AreaType areaType) {
        Area area = new Area();
        area.setAreaName("TestArea");
        area.setAreaCode(areaType.getName());
        area.setRemoteId("TestRemoteId");
        area.setAreaUpdattim(Instant.now());
        area.setAreaUpuser("TestUser");
        area.setAreaType(areaType);
        return area;
    }
}
