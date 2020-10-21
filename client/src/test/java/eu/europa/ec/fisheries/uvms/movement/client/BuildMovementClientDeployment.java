package eu.europa.ec.fisheries.uvms.movement.client;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;

@ArquillianSuiteDeployment
public abstract class BuildMovementClientDeployment {

    @Deployment(name = "movement", order = 1)
    public static Archive<?> createDeployment() {
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "movement.war");
        File[] files = Maven.configureResolver().loadPomFromFile("pom.xml")
                    .resolve("eu.europa.ec.fisheries.uvms.movement:movement-module:jar:classes:?")
                    .withTransitivity().asFile();
        testWar.addAsLibraries(files);

        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.client");

        testWar.addAsResource("beans.xml", "META-INF/beans.xml");

        testWar.addClass(ConfigServiceMock.class);

        return testWar;
    }
}
