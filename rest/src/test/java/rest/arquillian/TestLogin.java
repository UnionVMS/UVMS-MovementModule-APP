package rest.arquillian;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
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
    WebTarget loginTarget = null;
    Invocation.Builder invocation = null;

    public static final String ENDPOINT_ROOT = "https://unionvmstest.havochvatten.se";


    @Before
    public void before() {

        client = ClientBuilder.newClient();
        loginTarget = client.target(ENDPOINT_ROOT).path("usm-administration").path("rest").path("authenticate");

    }

    @After
    public void after() {

        if (client != null) {
            client.close();
        }


    }


    @Test
    public void testLogin() {

        AuthenticationRequest authenticationRequest = new AuthenticationRequest("vms_admin_com","password");
        try {

            String json = mapper.writeValueAsString(authenticationRequest);
            LOG.info(json);

            String content = loginTarget.request()
                    .post(Entity.json(authenticationRequest ),String.class);


            try {
                JsonNode tree =  mapper.readTree(content);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (JsonProcessingException e) {
           // e.printStackTrace();
        }
    }


    @Test
    public void testLogin2() {


        AuthenticationRequest authenticationRequest = new AuthenticationRequest("vms_admin_com","password");



        Invocation.Builder invocationBuilder = loginTarget.request(MediaType.APPLICATION_JSON);
        String content = invocationBuilder.post(Entity.json(authenticationRequest ),String.class);
        try {
            JsonNode tree =  mapper.readTree(content);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}


