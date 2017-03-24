package eu.europa.ec.fisheries.uvms.movement.arquillian;

import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.TestUtil;
import eu.europa.ec.fisheries.uvms.movement.bean.*;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;

/**
 * Created by andreasw on 2017-02-13.
 */
@ArquillianSuiteDeployment
public abstract class BuildMovementTestDeployment {

    @Deployment(name = "normal", order = 1)
    public static Archive<?> createDeployment() {

        // Import Maven runtime dependencies
        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeDependencies().resolve().withTransitivity().asFile();

        // Embedding war package which contains the test class is needed
        // So that Arquillian can invoke test class through its servlet test runner
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.constant");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.entity");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.dao");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.mapper");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.model.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.util");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.dto");
        // No no, starts threaded job...
        // Need to exclude job first...
        //testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement");
        //testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.bean");
        testWar.addClass(MovementBatchModelBean.class).addClass(MovementDomainModelBean.class)
                .addClass(MovementSearchGroupDomainModelBean.class).addClass(TempMovementDomainModelBean.class)
                .addClass(SegmentBean.class).addClass(IncomingMovementBean.class);

        testWar.addPackages(true,"eu.europa.ec.fisheries.uvms.movement.model");
        
        testWar.addPackages(true,"eu.europa.ec.fisheries.uvms.movement.arquillian");
        testWar.addPackages(true,"eu.europa.ec.fisheries.uvms.movement.arquillian.bean");
        
        testWar.addPackages(true, "eu.europa.ec.fisheries.schema");
        testWar.addClass(TransactionalTests.class);
        testWar.addClass(TestUtil.class);



        testWar.addAsResource("persistence-integration.xml", "META-INF/persistence.xml");
        // Empty beans for EE6 CDI
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addAsLibraries(files);

        return testWar;
    }

}
