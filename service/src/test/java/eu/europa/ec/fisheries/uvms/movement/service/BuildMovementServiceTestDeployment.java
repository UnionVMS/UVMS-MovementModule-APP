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
import eu.europa.ec.fisheries.uvms.movement.service.message.SpatialModuleMock;
import eu.europa.ec.fisheries.uvms.movement.service.message.UnionVMSMock;
import eu.europa.ec.fisheries.uvms.movement.service.message.rest.mock.AssetMTRestMock;

@ArquillianSuiteDeployment
public abstract class BuildMovementServiceTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(BuildMovementServiceTestDeployment.class);

    @Deployment(name = "movementservice", order = 2)
    public static Archive<?> createDeployment() {
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "movementsearch.war");

        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();
        testWar.addAsLibraries(files);
        
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.service");

        testWar.deleteClass(UnionVMSMock.class);
        testWar.deleteClass(SpatialModuleMock.class);
        testWar.deleteClass(AssetMTRestMock.class);
        
        testWar.addAsWebInfResource("ejb-jar.xml");
        testWar.addAsResource("persistence-integration.xml", "META-INF/persistence.xml");
        testWar.addAsResource("beans.xml", "META-INF/beans.xml");

		return testWar;
	}
    
    @Deployment(name = "uvms", order = 1)
    public static Archive<?> createSpatialMock() {

        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "unionvms.war");

        File[] files = Maven.configureResolver().loadPomFromFile("pom.xml")
                .resolve("eu.europa.ec.fisheries.uvms.movement:movement-model",
                        "eu.europa.ec.fisheries.uvms.asset:asset-model",
                        "eu.europa.ec.fisheries.uvms.commons:uvms-commons-message",
                        "eu.europa.ec.fisheries.uvms.asset:asset-client",
                        "eu.europa.ec.fisheries.uvms:usm4uvms")
                .withTransitivity().asFile();
        testWar.addAsLibraries(files);

        testWar.addClass(UnionVMSMock.class);
        testWar.addClass(SpatialModuleMock.class);
        testWar.addClass(AssetMTRestMock.class);

        return testWar;
    }
}
