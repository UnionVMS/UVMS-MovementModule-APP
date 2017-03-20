package rest.arquillian;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by thofan on 2017-03-17.
 */
public class TestLogin {


    final static Logger LOG = LoggerFactory.getLogger(TestLogin.class);


    ObjectMapper mapper = new ObjectMapper();

    Client client = null;
    WebTarget webLoginTarget = null;
    Invocation.Builder invocation = null;

    public static final String ENDPOINT_ROOT = "http://localhost:28080";


    @Before
    public void before() {

        client = ClientBuilder.newClient();
        webLoginTarget = client.target(ENDPOINT_ROOT).path("usm-authentication").path("rest").path("authenticate");
    }

    @After
    public void after() {
        if (client != null) {
            client.close();
        }
    }


    @Test
    public void testLogin() {

        AuthenticationRequest authenticationRequest = new AuthenticationRequest("vms_admin_com", "password");
        String json = null;
        try {
            json = mapper.writeValueAsString(authenticationRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        LOG.info(json);
        Response response = webLoginTarget
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));
        String content = response.readEntity(String.class);
        try {
            JsonNode tree = mapper.readTree(content);

            String value = tree.get("authenticated").asText();
            Assert.assertTrue(value.equals("true"));
        } catch (IOException e) {
            Assert.fail();
        }
    }


}


