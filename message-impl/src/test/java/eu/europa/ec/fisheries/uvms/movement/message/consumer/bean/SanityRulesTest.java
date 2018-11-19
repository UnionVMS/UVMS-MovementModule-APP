package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.europa.ec.fisheries.schema.exchange.module.v1.ProcessedMovementResponse;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefTypeType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.message.BuildMovementServiceTestDeployment;
import eu.europa.ec.fisheries.uvms.movement.message.JMSHelper;
import eu.europa.ec.fisheries.uvms.movement.message.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movementrules.model.mapper.JAXBMarshaller;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.time.Instant;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class SanityRulesTest extends BuildMovementServiceTestDeployment {

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    private ObjectMapper mapper = new ObjectMapper();

    JMSHelper jmsHelper;

    @PostConstruct
    public void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    }

    @Before
    public void cleanJMS() throws Exception {
        jmsHelper = new JMSHelper(connectionFactory);
        jmsHelper.clearQueue("UVMSMovementRulesEvent");
        jmsHelper.clearQueue(MessageConstants.QUEUE_EXCHANGE_EVENT_NAME);
    }

    @Test
    @OperateOnDeployment("movement")
    public void setMovementReportNullLatitudeShouldTriggerSanityRuleTest() throws Exception {

        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setLatitude(null);
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }

    @Test
    @OperateOnDeployment("movement")
    public void setMovementReportFutureDateShouldTriggerSanityRuleTest() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setLatitude(null);
        incomingMovement.setPositionTime(Instant.now().plusSeconds(60 * 60));
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }

    private ProcessedMovementResponse sendIncomingMovementAndReturnAlarmResponse(IncomingMovement incomingMovement) throws Exception{
        String json = mapper.writeValueAsString(incomingMovement);
        jmsHelper.sendMovementMessage(json, incomingMovement.getAssetHistoryId(), "CREATE");   //grouping on null.....

        Message response = jmsHelper.listenOnQueue(MessageConstants.QUEUE_EXCHANGE_EVENT_NAME);
        ProcessedMovementResponse movementResponse = JAXBMarshaller.unmarshallTextMessage((TextMessage) response, ProcessedMovementResponse.class);
        return movementResponse;
    }
}
