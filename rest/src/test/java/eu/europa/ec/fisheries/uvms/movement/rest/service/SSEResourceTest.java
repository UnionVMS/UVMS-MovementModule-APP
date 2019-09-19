package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.InboundSseEvent;
import javax.ws.rs.sse.SseEventSource;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class SSEResourceTest extends BuildMovementRestDeployment {

    private final static Logger LOG = LoggerFactory.getLogger(SSEResourceTest.class);

    @Inject
    private MovementService movementService;

    private static String dataString = "";
    private static String errorString = "";
    private static String HOST = System.getProperty("wildfly_host","localhost");
    private static String REST_PORT = System.getProperty("rest_port","8080");

    @Test
    @OperateOnDeployment("movement")
    public void SSEBroadcastTest() throws Exception{

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://"+HOST+":"+REST_PORT+"/test/rest/sse/subscribe");

        try (SseEventSource source = SseEventSource.target(target).reconnectingEvery(1, TimeUnit.SECONDS).build()) {
            source.register(onEvent, onError, onComplete);
            source.open();
            assertTrue(source.isOpen());

            Movement movementBaseType = MovementTestHelper.createMovement();
            Movement createdMovement = movementService.createMovement(movementBaseType);

            movementBaseType = MovementTestHelper.createMovement();
            createdMovement = movementService.createMovement(movementBaseType);

            movementBaseType = MovementTestHelper.createMovement();
            createdMovement = movementService.createMovement(movementBaseType);



            Thread.sleep(1000 * 1 * 1);
            assertTrue(source.isOpen());
            assertTrue(errorString,errorString.isEmpty());
            assertEquals(3,dataString.split("\\}\\{").length);
        }


    }

    private static Consumer<InboundSseEvent> onEvent = (inboundSseEvent) -> {
        String data = inboundSseEvent.readData();
        dataString = dataString.concat(data);
    };

    //Error
    private static Consumer<Throwable> onError = (throwable) -> {
        LOG.error("Error while testing sse: ", throwable);
        errorString = throwable.getMessage();
    };

    //Connection close and there is nothing to receive
    private static Runnable onComplete = () -> {
        System.out.println("Done!");
    };

}
