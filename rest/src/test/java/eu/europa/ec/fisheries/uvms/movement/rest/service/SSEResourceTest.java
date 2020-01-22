package eu.europa.ec.fisheries.uvms.movement.rest.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.InboundSseEvent;
import javax.ws.rs.sse.SseEventSource;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.movement.rest.AuthorizationHeaderWebTarget;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;

@RunWith(Arquillian.class)
public class SSEResourceTest extends BuildMovementRestDeployment {

    private final static Logger LOG = LoggerFactory.getLogger(SSEResourceTest.class);

    @Inject
    private MovementService movementService;

    private static String dataString = "";
    private static String errorString = "";

    @Before
    public void clearStrings(){
        dataString = "";
        errorString = "";
    }

    @Test
    @OperateOnDeployment("movement")
    public void SSEBroadcastTest() throws Exception{

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080/test/rest/sse/subscribe");
        AuthorizationHeaderWebTarget jwtTarget = new AuthorizationHeaderWebTarget(target, getToken());

        try (SseEventSource source = SseEventSource.target(jwtTarget).reconnectingEvery(1, TimeUnit.SECONDS).build()) {
            source.register(onEvent, onError, onComplete);
            source.open();
            assertTrue(source.isOpen());

            Movement movementBaseType = MovementTestHelper.createMovement();
            Movement createdMovement = movementService.createAndProcessMovement(movementBaseType);

            movementBaseType = MovementTestHelper.createMovement();
            createdMovement = movementService.createAndProcessMovement(movementBaseType);

            movementBaseType = MovementTestHelper.createMovement();
            createdMovement = movementService.createAndProcessMovement(movementBaseType);



            Thread.sleep(1000 * 1 * 1);
            assertTrue(source.isOpen());
            assertTrue(errorString,errorString.isEmpty());
            assertEquals(dataString,3 ,dataString.split("\\}\\{").length);
            Pattern p = Pattern.compile("\"timestamp\":\"\\d{13}\"");
            Matcher m = p.matcher(dataString);
            assertTrue(m.find());
            assertTrue(m.find());
            assertTrue(m.find());

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
