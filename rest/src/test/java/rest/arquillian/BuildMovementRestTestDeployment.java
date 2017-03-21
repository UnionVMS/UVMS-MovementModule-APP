package rest.arquillian;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.*;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import java.io.File;
import java.util.*;

public  class BuildMovementRestTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(BuildMovementRestTestDeployment.class);

    public Client client = null;
    public WebTarget webLoginTarget = null;
    public  Invocation.Builder invocation = null;


    public  ObjectMapper mapper = new ObjectMapper();



    @Before
    public void before() {
        client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        if (client != null) {
            client.close();
        }
    }





    private  static WebArchive createBasic_REST_Deployment() {



        File[] files1 = Maven.resolver().loadPomFromFile("../pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();

        printFiles(files1);

        File[] files2 = Maven.resolver().loadPomFromFile("../../UVMS-MovementModule-MODEL/pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();

        printFiles(files2);

        Set<File> unique = new HashSet<>();
        unique.addAll(Arrays.asList(files1));
        unique.addAll(Arrays.asList(files2));

        File[] files = new File[unique.size()];
        int i = 0;
        for(File f : unique){
            files[i] = f;
            i++;
        }
        printFiles(files);


        // Embedding war package which contains the test class is needed
        // So that Arquillian can invoke test class through its servlet test runner
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");

        testWar.addClass(MovementConfigHelper.class);

        // Empty beans for EE6 CDI
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
  //
              testWar.addAsLibraries(files);


      // testWar.addClass(eu.europa.ec.fisheries.uvms.config.constants.ConfigHelper);
        testWar.addClass(MovementConfigHelper.class);

        return testWar;
    }

    public static  WebArchive  create_REST_Deployment() {
        WebArchive archive = createBasic_REST_Deployment();



        return archive;
    }





    private static  void printFiles(File[] files) {

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


}
