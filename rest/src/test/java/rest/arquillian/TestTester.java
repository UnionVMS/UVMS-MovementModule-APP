package rest.arquillian;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
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
public class TestTester  extends BuildMovementRestTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(TestTester.class);

    public static final String ENDPOINT_ROOT = "http://localhost:28080";



    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }





    @Test
    @RunAsClient
    public void testPOST() {

        // TODO here we shall NOT have a login. we will mock that filter

        webLoginTarget = client.target(ENDPOINT_ROOT).path("usm-authentication").path("rest").path("authenticate");

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
