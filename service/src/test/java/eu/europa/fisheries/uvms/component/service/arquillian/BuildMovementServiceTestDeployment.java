package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.uvms.movement.service.*;
import eu.europa.ec.fisheries.uvms.movement.service.bean.*;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.movement.service.validation.MovementGroupValidator;
import eu.europa.fisheries.uvms.component.service.SpatialServiceMockedBean;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Created by andreasw on 2017-02-13.
 */
@ArquillianSuiteDeployment
public abstract class BuildMovementServiceTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(BuildMovementServiceTestDeployment.class);

//    @Deployment(name = "basic", order = 1)
//    private static WebArchive createBasicDeployment() {
//        return createArchive("test");
//    }

	private static WebArchive createArchive(final String name) {
		File[] files = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();

        printFiles(files);

        // Embedding war package which contains the test class is needed
        // So that Arquillian can invoke test class through its servlet test runner
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, name + ".war");

        testWar.addClass(MovementConfigHelper.class);
        testWar.addClass(TransactionalTests.class);

        // Empty beans for EE6 CDI
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addAsLibraries(files);
        
        testWar.addPackages(true, "eu.europa.fisheries.uvms.component.service.arquillian");
		return testWar;
	}

    @Deployment(name = "movementsearch", order = 1)
    public static Archive<?> createMovementSearchDeployment() {
        WebArchive archive = createArchive("movementsearch");

        archive.addClass(MovementSearchGroupServiceBean.class).addClass(MovementSearchGroupService.class);
       // already there  archive.addClass(MovementConfigHelper.class);
        archive.addClass(MovementServiceException.class);
        archive.addClass(MovementGroupValidator.class);


        return archive;
    }

    @Deployment(name = "movementevent", order = 1)
    public static Archive<?> createEventDeployment() {
        WebArchive archive = createArchive("movementevent");

        archive.addClass(MovementEventServiceBean.class);
        archive.addClass(EventService.class);
        archive.addClass(MovementServiceException.class);
        archive.addClass(MovementEventTestHelper.class);

        archive.addClass(MovementServiceBean.class)
                .addClass(MovementService.class)
                .addClass(SpatialService.class)
                .addClass(SpatialServiceMockedBean.class)
                .addClass(MovementListResponseDto.class)
                .addClass(MovementDto.class);
        archive.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.service.exception");

        
        


        return archive;
    }

    @Deployment(name = "movementtemp", order = 1)
    public static Archive<?> createTempMovementDeployment() {
        WebArchive archive = createArchive("movementtemp");
        archive.addClass(TempMovementServiceBean.class).addClass(TempMovementService.class);
        archive.addClass(MovementServiceException.class);

        return archive;
    }

    @Deployment(name = "movementIntTest", order = 1)
    public static Archive<?> createDeployment_FOR_MovementServiceIntTest() {

        WebArchive archive = createArchive("movementIntTest");

        archive.addClass(MovementServiceBean.class)
                .addClass(MovementService.class)
                .addClass(SpatialService.class)
                .addClass(SpatialServiceMockedBean.class)
                .addClass(MovementListResponseDto.class)
                .addClass(MovementDto.class)
                .addClass(MovementMapper.class);
        archive.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.service.exception");

        return archive;
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

    // ToDo: Read the todo in GetMovementListByAreaAndTimeIntervalEventIntTest.java to decide if this deployment method
    // ToDo: should be kept or removed.
    /*
    public static Archive<?> createEventMovementListByAreaAndTimeIntervalDeployment() {
        WebArchive archive = (WebArchive) createEventDeployment();

        archive.addClass(MovementAreaAndTimeIntervalCriteria.class);
        return archive;
    }
    */
}
