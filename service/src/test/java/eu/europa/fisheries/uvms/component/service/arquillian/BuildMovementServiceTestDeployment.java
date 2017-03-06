package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementDomainModelBean;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementSearchGroupDomainModelBean;
import eu.europa.ec.fisheries.uvms.movement.bean.TempMovementDomainModelBean;
import eu.europa.ec.fisheries.uvms.movement.service.MovementSearchGroupService;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.SpatialService;
import eu.europa.ec.fisheries.uvms.movement.service.TempMovementService;
import eu.europa.ec.fisheries.uvms.movement.service.bean.*;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementListResponseDto;
import eu.europa.ec.fisheries.uvms.spatial.model.exception.SpatialException;
import eu.europa.ec.fisheries.uvms.spatial.model.exception.SpatialModelException;
import eu.europa.ec.fisheries.uvms.spatial.model.exception.SpatialModelMapperException;
import eu.europa.fisheries.uvms.component.service.SpatialServiceMockedBean;
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

    private static WebArchive createBasicDeployment() {

        // Import Maven runtime dependencies
        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeDependencies().resolve().withTransitivity().asFile();




        // Embedding war package which contains the test class is needed
        // So that Arquillian can invoke test class through its servlet test runner
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");

        // BEGIN DB and entities
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.constant");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.entity");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.dao");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.mapper");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.model");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.service.validation");
        // No no, starts threaded job...
        // Need to exclude job first...
        testWar.addClass(MovementBatchModelBean.class).addClass(MovementDomainModelBean.class)
                .addClass(MovementSearchGroupDomainModelBean.class).addClass(TempMovementDomainModelBean.class);


        // END

        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.service.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.model.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.message.exception");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.exchange.model");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.movement.model");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.config.constants");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.config.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.schema");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.schema.config.types.v1");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.schema.movement.search.v1");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.schema.config.module.v1");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.user.model.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.audit.model.exception");
        testWar.addPackages(true,  "org.jvnet.jaxb2_commons");  // << URRRK
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.util");

        testWar.addClass(MovementConfigHelper.class);
        testWar.addClass(TransactionalTests.class);

        testWar.addAsResource("persistence-integration.xml", "META-INF/persistence.xml");
        // Empty beans for EE6 CDI
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addAsLibraries(files);

        return testWar;
    }

    public static Archive<?> createMovementSearchDeployment() {
        WebArchive archive = createBasicDeployment();

        archive.addClass(MovementSearchGroupServiceBean.class).addClass(MovementSearchGroupService.class);
        archive.addClass(MovementConfigHelper.class);

        return archive;
    }


    public static Archive<?> createTempMovementDeployment() {
        WebArchive archive = createBasicDeployment();
        archive.addClass(TempMovementServiceBean.class).addClass(TempMovementService.class);

        return archive;
    }



    public static Archive<?> createDeployment_FOR_MovementServiceIntTest() {

        // Import Maven runtime dependencies
        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeDependencies().resolve().withTransitivity().asFile();


        // Embedding war package which contains the test class is needed
        // So that Arquillian can invoke test class through its servlet test runner
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");

        // BEGIN DB and entities
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.constant");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.entity");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.dao");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.mapper");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.model");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.service.validation");
        // No no, starts threaded job...
        // Need to exclude job first...
        testWar.addClass(MovementBatchModelBean.class).addClass(MovementDomainModelBean.class)
                .addClass(MovementSearchGroupDomainModelBean.class)
                .addClass(TempMovementDomainModelBean.class)
                .addClass(MovementServiceBean.class)
                .addClass(MovementService.class)
                .addClass(SpatialService.class)
                .addClass(SpatialServiceMockedBean.class)
                .addClass(SpatialModelMapperException.class)
                .addClass(SpatialModelException.class)
                .addClass(SpatialException.class);
        // END


        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.service.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.model.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.message.exception");
        testWar.addClass(MovementServiceBean.class).addClass(MovementService.class).addClass(MovementListResponseDto.class).addClass(MovementDto.class);
        //   testWar.addClass(MessageProducer.class).addClass(MessageProducerBean.class);
        //       testWar.addClass(MessageConsumer.class);
        //             testWar.addClass(MessageConsumerBean.class);
        //      testWar.addClass(ConfigMessageProducer.class).addClass(AbstractProducer.class);
        //      testWar.addClass(JMSConnectorBean.class).addClass(MessageConstants.class);
        //      testWar.addClass(ModuleQueue.class).addClass(EventMessage.class);
        testWar.addClass(MovementConfigHelper.class);


        //testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.service");
        //testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.message.consumer");
        //testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.movement.message.producer");
        //testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.movement.message.event.carrier");
        //testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.movement.message.constants");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.movement.model");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.config.constants");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.config.exception");
        //testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.config.module.exception");
        //testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.config.model.exception");
        //testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.config.message");
        //testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.config.service");
        testWar.addPackages(true, "eu.europa.ec.fisheries.schema");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.schema.config.types.v1");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.schema.movement.search.v1");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.schema.config.module.v1");
        testWar.addPackages(true,  "eu.europa.ec.fisheries.uvms.user.model.exception");
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.audit.model.exception");
        //testWar.addPackages(true,  " eu.europa.ec.fisheries.uvms.spatial.model.exception");
        testWar.addPackages(true,  "org.jvnet.jaxb2_commons");  // << URRRK


        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.util");

        testWar.addClass(TransactionalTests.class);

        //testWar.addClass(MovementSearchGroupServiceBean.class);

        testWar.addAsResource("persistence-integration.xml", "META-INF/persistence.xml");
        // Empty beans for EE6 CDI
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addAsLibraries(files);

        return testWar;
    }



}
