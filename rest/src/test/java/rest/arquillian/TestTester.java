package rest.arquillian;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.fisheries.uvms.config.constants.ConfigHelper;
import eu.europa.ec.fisheries.uvms.longpolling.notifications.NotificationMessage;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by thofan on 2017-03-15.
 */
@RunWith(Arquillian.class)
public class TestTester {

    final static Logger LOG = LoggerFactory.getLogger(TestTester.class);


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

    private static void printFiles(File[] files) {

        List<File> filesSorted = new ArrayList<>();
        for(File f : files){
            filesSorted.add(f);
        }

        Collections.sort(filesSorted, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        LOG.info("FROM POM - begin");
        for(File f : filesSorted){
            LOG.info("       --->>>   "   +   f.getName());
        }
        LOG.info("FROM POM - end");
    }


    @Deployment
    public static WebArchive createDeployment() {


        File[] files = Maven.resolver().loadPomFromFile("../pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();


        files = Maven.resolver().loadPomFromFile("../pom.xml")
                .importCompileAndRuntimeDependencies().importTestDependencies().resolve().withTransitivity().asFile();

        printFiles(files);



        // TODO this is mostly nonsense but it makes the test run so  we will look into it . . .
        return ShrinkWrap
                .create(WebArchive.class, "movement.war")
                .addPackages(true, "eu.europa.ec.fisheries.uvms.movement")
                //.addPackages(true, "rest.arquillian")
                .addAsResource("persistence-integration.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .setWebXML(new File("src/test/resources/web.xml"))
                 .addAsLibraries(files)
               // .addClass(ConfigHelper.class)
               // .addClass(NotificationMessage.class)
                ;

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


    //@Test
    //@RunAsClient
    public void testFOREVER() {


        while(true){

            try {
                Thread.sleep(10000);
            }catch(InterruptedException e){

            }
        }
    }


   // @Test
    public void testPOST() {

        Form form = new Form();
        form.param("x", "foo");
        form.param("y", "bar");
        String str = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);


        System.out.println(str);

    }





}
