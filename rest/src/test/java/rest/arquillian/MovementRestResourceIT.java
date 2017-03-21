package rest.arquillian;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import eu.europa.ec.mare.usm.authentication.domain.AuthenticationRequest;
//import eu.europa.ec.mare.usm.authentication.service.impl.AuthenticationServiceBean;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseDto;
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
public class MovementRestResourceIT {

    final static Logger LOG = LoggerFactory.getLogger(MovementRestResourceIT.class);

    public static final String ENDPOINT_ROOT = "http://localhost:8080";
    //public static final String LOGIN_ENDPOINT = "usm-administration/rest/authenticate";
    //http://localhost:8080/usm-administration/rest/authenticate

    Client client = null;
    WebTarget target = null;
    ObjectMapper mapper = new ObjectMapper();


    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementRestTestDeployment.create_MovementRestResource_Deployment();
    }

    /*
    @Inject
    AuthenticationServiceBean authenticationServiceBean;
    */

    @Before
    public void before() {

        client = ClientBuilder.newClient();
        target = client.target(ENDPOINT_ROOT);
    }

    @After
    public void after() {

        if (client != null) {
            client.close();
        }
    }

    @Test
    @RunAsClient
    public void test_REST_GetListByQuery() {

        /*
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(); //("vms_admin_com","password");
        authenticationRequest.setUserName("vms_admin_com");
        authenticationRequest.setPassword("password");
        */

        //try {

            target = client.target(ENDPOINT_ROOT).path("movement").path("rest").path("list");

            Response response = target
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            ResponseDto content = response.readEntity(ResponseDto.class);

            assertNotNull(content);
            //String json = mapper.writeValueAsString(authenticationRequest);
            //LOG.info(json);

            //String r
            /*
            Response response = target.request()
                    .post(Entity.entity(authenticationRequest, MediaType.TEXT_PLAIN), Response.class);
            */

            //Response response = target.request()
            //        .post(Entity.entity(jsonData, MediaType.APPLICATION_JSON));

            //String jsonResponse = mapper.writeValueAsString(response);
            //LOG.info("jsonResponse: " + jsonResponse);

            /*
            @Override
                public boolean isWriteable(Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
                    return false;
                }
             */


            //JSONObjectProvider jsonObjectProvider = new JSONObjectProvider();

            //jsonObjectProvider.isWriteable(response);
            //MessageBodyWriter messageBodyWriter = JSONObjectProvider.
            //response.

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
