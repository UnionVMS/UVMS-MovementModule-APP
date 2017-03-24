package rest.arquillian;

//import eu.europa.ec.mare.usm.authentication.domain.AuthenticationRequest;
//import eu.europa.ec.mare.usm.authentication.service.impl.AuthenticationServiceBean;
//import eu.europa.fisheries.uvms.component.service.arquillian.BuildMovementRestTestDeployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by roblar on 2017-03-17.
 */
@RunWith(Arquillian.class)
public class MovementRestResourceIT  extends BuildMovementRestTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(MovementRestResourceIT.class);

    //public static final String ENDPOINT_ROOT = "http://localhost:8080";
    //public static final String LOGIN_ENDPOINT = "usm-administration/rest/authenticate";
    //http://localhost:8080/usm-administration/rest/authenticate

    /*
    @Inject
    AuthenticationServiceBean authenticationServiceBean;
    */

    @Test
    // Note: Method is called getListByQuery but it is actually a post request. See MovementRestResource.
    @OperateOnDeployment("normal")
    public void test_REST_GetListByQuery() {


        webTarget = client.target(ENDPOINT_ROOT).path("movement").path("rest").path("list");

        String testString = "testString";

        Response response = webTarget
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(testString, MediaType.APPLICATION_JSON));
        String content = response.readEntity(String.class);

        LOG.info("Response object content: " + content);

        //ResponseDto payload = response.readEntity(ResponseDto.class);

        //assertNotNull(payload);

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
