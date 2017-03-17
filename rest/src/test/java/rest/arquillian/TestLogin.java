package rest.arquillian;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by thofan on 2017-03-17.
 */
public class TestLogin {



    final static Logger LOG = LoggerFactory.getLogger(TestLogin.class);


    ObjectMapper mapper = new ObjectMapper();

    Client client = null;
    WebTarget loginTarget = null;
    Invocation.Builder invocation = null;

//    public static final String ENDPOINT_ROOT = "https://unionvmstest.havochvatten.se";
    public static final String ENDPOINT_ROOT = "http://192.168.105.96:8080/";


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

            Response r = loginTarget.request()
                    .post(Entity.entity(authenticationRequest,  MediaType.TEXT_PLAIN ),Response.class);

            String msg = r.readEntity(String.class);


            LOG.info(msg);


        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }






    }




}


