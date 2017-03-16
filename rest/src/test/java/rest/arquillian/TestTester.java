package rest.arquillian;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * Created by thofan on 2017-03-15.
 */
@RunWith(Arquillian.class)
public class TestTester {

    ObjectMapper mapper = new ObjectMapper();

    Client client = null;
    WebTarget target = null;
    Invocation.Builder invocation = null;

    // normally you only test one baseroot at a time
    public static final String ENDPOINT_ROOT = "http://fanto.se";


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




    @Deployment
    public static WebArchive createDeployment() {

        // TODO this is mostly nonsense but it makes the test run so  we will look into it . . .
        return ShrinkWrap
                .create(WebArchive.class)
              //  .addPackages(true, "eu.europa.ec.fisheries.uvms.movement", "rest.arquillian")
                .addAsResource("persistence-integration.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .setWebXML(new File("src/test/resources/web.xml"));
                /*.addAsLibraries(
                        Maven.resolver().resolve("com.google.code.gson:gson:2.3.1", "org.mockito:mockito-core:1.9.5")
                                .withTransitivity().asFile());
                                */
    }




    @Test
    @RunAsClient
    public void testGET_alternativ1() {

        invocation = target.request(MediaType.APPLICATION_JSON);

        Response response = invocation.get();
        String rs = response.readEntity(String.class);

        System.out.println(rs);

    }

    @Test
    @RunAsClient
    public void testGET_alternativ2() {


        Response response = target.request(MediaType.APPLICATION_JSON).get();
        String rs = response.readEntity(String.class);

        System.out.println(rs);

    }

    @Test
    public void testPOST() {

        Form form = new Form();
        form.param("x", "foo");
        form.param("y", "bar");
        String str = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);


        System.out.println(str);

    }





}
