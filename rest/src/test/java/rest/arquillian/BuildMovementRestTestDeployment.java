package rest.arquillian;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.fisheries.uvms.config.constants.ConfigHelper;
import eu.europa.ec.fisheries.uvms.config.service.UVMSConfigService;
import eu.europa.ec.fisheries.uvms.config.service.UVMSConfigServiceBean;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseDto;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.RestResponseCode;
import eu.europa.ec.fisheries.uvms.movement.rest.service.MovementRestResource;
import eu.europa.ec.fisheries.uvms.movement.service.MovementSearchGroupService;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.SpatialService;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementConfigHelper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementSearchGroupServiceBean;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementServiceBean;
import eu.europa.ec.fisheries.uvms.movement.service.bean.UserServiceBean;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.validation.MovementGroupValidator;
import eu.europa.ec.mare.usm.administration.rest.security.DummyFilter;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Filters;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import eu.europa.fisheries.uvms.component.service.SpatialServiceMockedBean;

/**
 * Created by andreasw on 2017-02-13.
 */
public abstract class BuildMovementRestTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(BuildMovementRestTestDeployment.class);

    public ObjectMapper mapper = new ObjectMapper();

    public Client client = null;
    public WebTarget webTarget = null;
    public Invocation.Builder invocation = null;

    public static final String ENDPOINT_ROOT = "http://localhost:28080";


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


    public static WebArchive createBasicDeployment() {

        File[] files = Maven.resolver().loadPomFromFile("../service/pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();

        files = ensureUniqueness(files);
        printFiles(files);


        // Embedding war package which contains the test class is needed
        // So that Arquillian can invoke test class through its servlet test runner
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "movement.war");
        testWar.addPackages(true, Filters.exclude("AuthenticationFilter"), "com.europa.ec");
        testWar.addClass(MovementRestResource.class);


        testWar.addClass(MovementServiceException.class);
        testWar.addClass(RestResponseCode.class);
        testWar.addClass(MovementService.class);
        testWar.addClass(MovementServiceBean.class);
        testWar.addClass(UserServiceBean.class);
        testWar.addClass(ResponseDto.class);
        testWar.addClass(SpatialService.class);
        testWar.addClass(SpatialServiceMockedBean.class);
        testWar.addClass(MovementListResponseDto.class);
        testWar.addClass(UVMSConfigService.class);
        testWar.addClass(ConfigHelper.class);
        testWar.addClass(MovementConfigHelper.class);
        testWar.addClass(MovementDto.class);
        testWar.addClass(DummyFilter.class);


        testWar.addClass(BuildMovementRestTestDeployment.class);
        testWar.addClass(TestTester.class);




        /*
        testWar.addClass(MovementConfigHelper.class);
        testWar.addClass(TransactionalTests.class);
        testWar.addClass(ConfigMessageProducer.class);
        */

        // Empty beans for EE6 CDI
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addAsLibraries(files);

        return testWar;
    }

    public static Archive<?> createMovementSearchDeployment() {
        WebArchive archive = createBasicDeployment();

        archive.addClass(MovementSearchGroupServiceBean.class).addClass(MovementSearchGroupService.class);
        // already there  archive.addClass(MovementConfigHelper.class);
        archive.addClass(MovementServiceException.class);
        archive.addClass(MovementGroupValidator.class);


        return archive;
    }


    /**
     * if more than 1 pom is scanned , this is a way of ensuring uniqueness
     *
     * @param files
     * @return
     */
    private static File[] ensureUniqueness(File[]... files) {

        Set<File> unique = new HashSet<>();
        int n = files.length;
        for (int i = 0; i < n; i++) {
            File[] file = files[i];
            unique.addAll(Arrays.asList(file));
        }
        return unique.toArray(new File[unique.size()]);
    }

    private static void printFiles(File[] files) {

        List<File> filesSorted = new ArrayList<>();
        for (File f : files) {
            filesSorted.add(f);
        }

        Collections.sort(filesSorted, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        LOG.info("FROM POM - begin");
        for (File f : filesSorted) {
            LOG.info("       --->>>   " + f.getName());
        }
        LOG.info("FROM POM - end.  files :  " + filesSorted.size());
    }


}
