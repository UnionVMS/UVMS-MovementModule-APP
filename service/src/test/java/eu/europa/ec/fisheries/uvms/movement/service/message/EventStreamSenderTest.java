package eu.europa.ec.fisheries.uvms.movement.service.message;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.service.BuildMovementServiceTestDeployment;
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

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class EventStreamSenderTest extends BuildMovementServiceTestDeployment {

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    JMSHelper jmsHelper;
    MessageConsumer subscriber;
    Topic eventBus;
    Session session;

    private ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    }
    @Before
    public void cleanJMS() throws Exception {
        jmsHelper = new JMSHelper(connectionFactory);
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
        assertNull(message.getStringProperty(MessageConstants.EVENT_STREAM_SUBSCRIBER_LIST));

        MicroMovementExtended micro = mapper.readValue(message.getText(), MicroMovementExtended.class);
        assertNotNull(micro);
        assertEquals(incomingMovement.getMovementSourceType(), micro.getMicroMove().getSource().name());
        assertEquals(incomingMovement.getReportedCourse(), micro.getMicroMove().getHeading());
        assertEquals(incomingMovement.getAssetGuid(), micro.getAsset());

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
