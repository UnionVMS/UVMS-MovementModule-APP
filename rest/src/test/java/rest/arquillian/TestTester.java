package rest.arquillian;

import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseDto;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//import eu.europa.fisheries.uvms.component.service.arquillian.BuildMovementRestTestDeployment;

/**
 * Created by thofan on 2017-03-15.
 */
@RunWith(Arquillian.class)
public class TestTester  extends BuildMovementRestTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(TestTester.class);

    public static final String ENDPOINT_ROOT = "http://localhost:28080";




    @Deployment(testable=false)
    public static Archive<?> createDeployment() {
        return BuildMovementRestTestDeployment.createBasicDeployment();
    }





    @Test
    public void areas() {


        webTarget = client.target(ENDPOINT_ROOT).path("movement").path("rest").path("areas");

        Response response = webTarget
                .request(MediaType.APPLICATION_JSON)
                .get();

        //ResponseDto content = response.readEntity(ResponseDto.class);

    }
}
