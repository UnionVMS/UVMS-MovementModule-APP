package eu.europa.fisheries.uvms.component.service.arquillian;

import java.io.File;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.movement.service.bean.mapper.MovementDataSourceResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.MovementSearchGroupService;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.SpatialService;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementConfigHelper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementSearchGroupServiceBean;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementServiceBean;
import eu.europa.ec.fisheries.uvms.movement.service.bean.TempMovementServiceBean;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.movement.service.validation.MovementGroupValidator;
import eu.europa.fisheries.uvms.component.service.SpatialServiceMockedBean;

@ArquillianSuiteDeployment
public abstract class BuildMovementServiceTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(BuildMovementServiceTestDeployment.class);

	private static WebArchive createArchive(final String name) {
		File[] files = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();

        // Embedding war package which contains the test class is needed
        // So that Arquillian can invoke test class through its servlet test runner
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, name + ".war");

        testWar.addClass(MovementConfigHelper.class);
        testWar.addClass(TransactionalTests.class);

        testWar.addAsLibraries(files);
        
        testWar.addPackages(true, "eu.europa.fisheries.uvms.component.service.arquillian");
		return testWar;
	}

    @Deployment(name = "movementservice", order = 1)
    public static Archive<?> createMovementSearchDeployment() {
        WebArchive archive = createArchive("movementsearch");

        archive.addClass(MovementSearchGroupServiceBean.class).addClass(MovementSearchGroupService.class);

        archive.addClass(MovementServiceException.class);
        archive.addClass(MovementGroupValidator.class);
        
        //archive.addClass(EventService.class);
        archive.addClass(MovementEventTestHelper.class);
        
        archive.addClass(MovementServiceBean.class)
        .addClass(MovementService.class)
        .addClass(SpatialService.class)
        .addClass(MovementDataSourceResponseMapper.class)
        .addClass(SpatialServiceMockedBean.class)
        .addClass(MovementListResponseDto.class)
        .addClass(MovementDto.class).addClass(MovementMapper.class)
        .addClass(TempMovementServiceBean.class)
        .addClass(MovementServiceException.class);

        archive.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.service.exception");

        return archive;
    }
}
