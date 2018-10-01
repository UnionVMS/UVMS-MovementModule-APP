package eu.europa.ec.fisheries.uvms.movement.message;

import java.io.File;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

@ArquillianSuiteDeployment
public abstract class BuildMovementServiceTestDeployment {

    @Deployment(name = "movement", order = 2)
    public static Archive<?> createDeployment() {

        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");

        File[] files = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeAndTestDependencies().resolve()
                .withTransitivity().asFile();
        testWar.addAsLibraries(files);
        
//        File[] files = Maven.configureResolver().loadPomFromFile("pom.xml")
//                .resolve("eu.europa.ec.fisheries.uvms.movement:movement-model",
//                         "eu.europa.ec.fisheries.uvms.movement:movement-domain",
//                         "eu.europa.ec.fisheries.uvms.movement:movement-service",
//                         "eu.europa.ec.fisheries.uvms:uvms-config",
//                         "eu.europa.ec.fisheries.uvms.commons:uvms-commons-message",
//                         "org.apache.activemq:activemq-client")
//                .withTransitivity().asFile();
//        testWar.addAsLibraries(files);

        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.message");

        return testWar;
    }
    
    @Deployment(name = "uvms", order = 1)
    public static Archive<?> createSpatialMock() {

        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "unionvms.war");

        File[] files = Maven.configureResolver().loadPomFromFile("pom.xml")
                .resolve("eu.europa.ec.fisheries.uvms.spatial:spatial-model:1.0.12")
                .withTransitivity().asFile();
        testWar.addAsLibraries(files);

        testWar.addClass(UnionVMSMock.class);
        testWar.addClass(SpatialModuleMock.class);

        return testWar;
    }

}
