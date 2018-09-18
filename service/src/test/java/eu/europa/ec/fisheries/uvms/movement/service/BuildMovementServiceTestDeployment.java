package eu.europa.ec.fisheries.uvms.movement.service;

import java.io.File;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementSpatialServiceBean;

@ArquillianSuiteDeployment
public abstract class BuildMovementServiceTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(BuildMovementServiceTestDeployment.class);

    @Deployment(name = "movementservice", order = 1)
    public static Archive<?> createDeployment() {
        // Embedding war package which contains the test class is needed
        // So that Arquillian can invoke test class through its servlet test runner
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "movementsearch.war");

        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();
        testWar.addAsLibraries(files);
        
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.service");
        
        testWar.deleteClass(MovementSpatialServiceBean.class);
        testWar.addClass(SpatialServiceMockedBean.class);
        
        testWar.addAsResource("persistence-integration.xml", "META-INF/persistence.xml");
        
		return testWar;
	}
}
