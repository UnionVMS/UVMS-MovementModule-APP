package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class InternalRestResourceTest extends BuildMovementRestDeployment {

    @Inject
    MovementService movementService;

    @Test
    @OperateOnDeployment("movement")
    public void countMovementsInTheLastDayForAssetTest() throws Exception {
        Movement movementBaseType = MovementTestHelper.createMovement();
        Movement createdMovement = movementService.createMovement(movementBaseType);
        Instant now = Instant.now().plusSeconds(60);

        long response = getWebTarget()
                .path("internal/countMovementsInDateAndTheDayBeforeForAsset/" + createdMovement.getMovementConnect().getValue().toString())
                .queryParam("after", DateUtil.parseUTCDateToString(now))    //yyyy-MM-dd HH:mm:ss Z
                .request(MediaType.APPLICATION_JSON)
                .get(long.class);

        assertEquals(1, response);
    }
}
