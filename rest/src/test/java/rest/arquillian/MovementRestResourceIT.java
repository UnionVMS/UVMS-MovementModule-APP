package rest.arquillian;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import eu.europa.ec.mare.usm.authentication.domain.AuthenticationRequest;
//import eu.europa.ec.mare.usm.authentication.service.impl.AuthenticationServiceBean;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseDto;
//import eu.europa.fisheries.uvms.component.service.arquillian.BuildMovementRestTestDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by roblar on 2017-03-17.
 */
@RunWith(Arquillian.class)
public class MovementRestResourceIT  extends BuildMovementRestTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(MovementRestResourceIT.class);

    public static final String ENDPOINT_ROOT = "http://localhost:8080";
    //public static final String LOGIN_ENDPOINT = "usm-administration/rest/authenticate";
    //http://localhost:8080/usm-administration/rest/authenticate




    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementRestTestDeployment.createBasicDeployment();
    }

    /*
    @Inject
    AuthenticationServiceBean authenticationServiceBean;
    */





    @Test
    @RunAsClient
    public void test_REST_GetListByQuery() {



        webTarget = client.target(ENDPOINT_ROOT).path("movement").path("rest").path("list");

            Response response = webTarget
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            ResponseDto content = response.readEntity(ResponseDto.class);

            assertNotNull(content);


            LOG.info("Headers: " + response.getHeaders().toString());
            LOG.info("Allowed methods: " + response.getAllowedMethods().toString());
            LOG.info("Status info: " + response.getStatusInfo().toString());
            LOG.info("Cookies: " + response.getCookies().toString());
            LOG.info("MediaType: "  + response.getMediaType().toString());

            assertThat(response.getStatus(), is(200));

            //assertEquals(response.getMediaType().toString(), "application/json");
            assertEquals(response.getMediaType().toString(), "text/html");

        /*} catch (JsonProcessingException e) {
            e.printStackTrace();
        }*/
    }
}
