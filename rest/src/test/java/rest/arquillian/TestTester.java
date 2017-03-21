package rest.arquillian;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseDto;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by thofan on 2017-03-15.
 */
@RunWith(Arquillian.class)
public class TestTester  extends BuildMovementServiceTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(TestTester.class);

    public static final String ENDPOINT_ROOT = "http://localhost:28080";

  


    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createDeployment();
    }





    @Test
    @RunAsClient
    public void areas() {


        webTarget = client.target(ENDPOINT_ROOT).path("movement").path("rest").path("areas");

        Response response = webTarget
                .request(MediaType.APPLICATION_JSON)
                .get();

        ResponseDto content = response.readEntity(ResponseDto.class);
    }







}
