package rest.arquillian;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.fisheries.uvms.audit.model.exception.AuditModelMarshallException;
import eu.europa.ec.fisheries.uvms.audit.model.exception.ModelMapperException;
import eu.europa.ec.fisheries.uvms.movement.message.consumer.MessageConsumer;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.rest.service.MovementRestResource;
import eu.europa.ec.fisheries.uvms.movement.service.MovementSearchGroupService;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.TempMovementService;
import eu.europa.ec.fisheries.uvms.movement.service.bean.*;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import org.jboss.shrinkwrap.api.Archive;
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
import java.util.*;

public  class BuildMovementRestTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(BuildMovementRestTestDeployment.class);

    public Client client = null;
    public WebTarget webLoginTarget = null;
    public  Invocation.Builder invocation = null;

    public  ObjectMapper mapper = new ObjectMapper();

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


    private  static WebArchive createBasic_REST_Deployment() {

        File[] files0 = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();
        printFiles(files0);

        File[] files1 = Maven.resolver().loadPomFromFile("../pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();

        printFiles(files1);

        File[] files2 = Maven.resolver().loadPomFromFile("../../UVMS-MovementModule-MODEL/pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();

        printFiles(files2);

        Set<File> unique = new HashSet<>();
        unique.addAll(Arrays.asList(files0));
        unique.addAll(Arrays.asList(files1));
        unique.addAll(Arrays.asList(files2));
        File[] files = unique.toArray(new File[unique.size()]);
        printFiles(files);


        // Embedding war package which contains the test class is needed
        // So that Arquillian can invoke test class through its servlet test runner
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");
        //testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.rest");

        //testWar.addClass(MovementConfigHelper.class);

        // Empty beans for EE6 CDI
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
  //
              testWar.addAsLibraries(files);


      // testWar.addClass(eu.europa.ec.fisheries.uvms.config.constants.ConfigHelper);
        testWar.addClass(MovementConfigHelper.class);

        return testWar;
    }

    public static  WebArchive  create_REST_Deployment() {

        WebArchive archive = createBasic_REST_Deployment();

        return archive;
    }

    public static Archive<?> create_MovementRestResource_Deployment() {
        WebArchive archive = createBasic_REST_Deployment();

        archive.addClass(eu.europa.ec.fisheries.uvms.movement.rest.service.MovementRestResource.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.movement.service.MovementService.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.movement.service.bean.MovementServiceBean.class);


        archive.addClass(eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMapperException.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDbException.class);

        archive.addClass(eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType.class);

        archive.addClass(UserServiceBean.class);
        archive.addClass(TempMovementService.class);
        //archive.addClass(eu.europa.ec.fisheries.uvms.movement.rest.service.TempMovementResource.class);
        archive.addClass(TempMovementServiceBean.class);
        archive.addClass(MessageConsumer.class);
        archive.addClass(MessageProducer.class);
        archive.addClass(MovementSearchGroupService.class);
        archive.addClass(AuditModelMarshallException.class);
        archive.addClass(ModelMapperException.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.movement.service.SpatialService.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.movement.service.dto.MovementListResponseDto.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseDto.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.movement.rest.dto.RestResponseCode.class);

        archive.addClass(eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeException.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelException.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException.class);
        archive.addClass(eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMapperException.class);





        archive.addClass(MovementRestResource.class);


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
        LOG.info("FROM POM - end " + filesSorted.size());
    }


}
