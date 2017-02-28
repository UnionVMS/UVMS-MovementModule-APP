package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.uvms.config.constants.ConfigHelper;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementDomainModelBean;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementSearchGroupDomainModelBean;
import eu.europa.ec.fisheries.uvms.movement.bean.TempMovementDomainModelBean;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.exception.SearchMapperException;
import eu.europa.ec.fisheries.uvms.movement.service.MovementSearchGroupService;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementSearchGroupServiceBean;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import org.eclipse.aether.spi.log.Logger;
import org.eclipse.aether.spi.log.LoggerFactory;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;

/**
 * Created by andreasw on 2017-02-13.
 */
public abstract class BuildMovementServiceTestDeployment {

    final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("BuildMovementServiceTestDeployment");

    public static Archive<?> createDeployment() {

        // Import Maven runtime dependencies
        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeDependencies().resolve().withTransitivity().asFile();
        if(files != null){
            for(File f : files){
               LOG.info("frompom --------------------------------->>>>>>>>>>>>>>>>>> " + f.getAbsolutePath());
            }
        }


        // Embedding war package which contains the test class is needed
        // So that Arquillian can invoke test class through its servlet test runner
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");



        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.constant");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.entity");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.dao");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.mapper");

/*
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.service.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.model.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.message.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.service");
        //testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.message.consumer");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.movement.message.producer");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.movement.message.event.carrier");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.movement.message.constants");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.movement.service.bean");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.movement.model");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.config.constants");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.config.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.config.module.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.config.model.exception");
        //testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.config.message");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.config.service");
        testWar.addPackages(true, "eu.europa.ec.fisheries.schema");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.schema.config.types.v1");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.schema.movement.search.v1");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.schema.config.module.v1");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.user.model.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.audit.model.exception");
        testWar.addPackages(true,  " eu.europa.ec.fisheries.uvms.spatial.model.exception");
        testWar.addPackages(true,  "org.jvnet.jaxb2_commons.lang");  // << URRRK
*/

        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.util");
        // No no, starts threaded job...
        // Need to exclude job first...
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.bean");
        testWar.addClass(MovementBatchModelBean.class).addClass(MovementDomainModelBean.class)
                .addClass(MovementSearchGroupDomainModelBean.class)
                .addClass(TempMovementDomainModelBean.class)
                .addClass(MovementServiceException.class)
                .addClass(SearchMapperException.class)
                .addClass(GeometryUtilException.class)
                .addClass(MovementSearchGroupService.class);

        testWar.addClass(TransactionalTests.class);


        testWar.addClass(TransactionalTests.class);
        testWar.addClass(MovementSearchGroupServiceBean.class);

        testWar.addAsResource("persistence-integration.xml", "META-INF/persistence.xml");
        // Empty beans for EE6 CDI
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addAsLibraries(files);

        return testWar;
    }

}
