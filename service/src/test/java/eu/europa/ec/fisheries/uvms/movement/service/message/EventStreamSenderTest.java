package eu.europa.ec.fisheries.uvms.movement.service.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.service.exception.ObjectMapperContextResolver;
import eu.europa.ec.fisheries.uvms.movement.service.BuildMovementServiceTestDeployment;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovement;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jms.*;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class EventStreamSenderTest extends BuildMovementServiceTestDeployment {

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    JMSHelper jmsHelper;
    MessageConsumer subscriber;
    Topic eventBus;
    Session session;

    private ObjectMapper mapper;

    @PostConstruct
    public void init() {

    }
    @Before
    public void cleanJMS() {
        jmsHelper = new JMSHelper(connectionFactory);
        ObjectMapperContextResolver resolver = new ObjectMapperContextResolver();
        mapper = resolver.getContext(MicroMovement.class);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void EventStreamSenderTest() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetIRCS("TestIrcs:" + incomingMovement.getAssetGuid());
        registerSubscriber();
        sendIncomingMovement(incomingMovement);

        TextMessage message = (TextMessage) listenOnEventStream(5000l);
        assertNotNull(message);

        assertEquals("Movement", message.getStringProperty(MessageConstants.EVENT_STREAM_EVENT));
        assertEquals(incomingMovement.getMovementSourceType(), message.getStringProperty(MessageConstants.EVENT_STREAM_MOVEMENT_SOURCE));
        assertNull(message.getStringProperty(MessageConstants.EVENT_STREAM_SUBSCRIBER_LIST));

        String messageJson = message.getText();
        Pattern p = Pattern.compile("\"timestamp\":\\d{13},");
        Matcher m = p.matcher(messageJson);
        assertTrue(m.find());

        MicroMovementExtended micro = mapper.readValue(messageJson, MicroMovementExtended.class);
        assertNotNull(micro);
        assertEquals(incomingMovement.getMovementSourceType(), micro.getMicroMove().getSource().name());
        assertEquals(incomingMovement.getReportedCourse(), micro.getMicroMove().getHeading());
        assertEquals(incomingMovement.getAssetGuid(), micro.getAsset());
        assertEquals(incomingMovement.getPositionTime().truncatedTo(ChronoUnit.MILLIS), micro.getMicroMove().getTimestamp());

    }


    private void sendIncomingMovement(IncomingMovement incomingMovement) throws Exception {
        sendIncomingMovement(incomingMovement, incomingMovement.getAssetGuid());
    }

    private void sendIncomingMovement(IncomingMovement incomingMovement, String groupId) throws Exception {
        String json = mapper.writeValueAsString(incomingMovement);
        jmsHelper.sendMovementMessage(json, groupId, "CREATE");
    }

    public void registerSubscriber() throws Exception {
        Connection connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        eventBus = session.createTopic("EventStream");
        subscriber = session.createConsumer(eventBus, null, true);
    }


    public Message listenOnEventStream(Long timeoutInMillis) throws Exception {

        try {
            return subscriber.receive(timeoutInMillis);
        } finally {
            subscriber.close();
        }
    }
}
