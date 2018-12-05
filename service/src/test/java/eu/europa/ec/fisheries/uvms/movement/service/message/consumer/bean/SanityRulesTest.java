package eu.europa.ec.fisheries.uvms.movement.service.message.consumer.bean;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AlarmDAO;
import eu.europa.ec.fisheries.uvms.movement.service.dto.AlarmStatusType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmItem;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmReport;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.europa.ec.fisheries.schema.exchange.module.v1.ProcessedMovementResponse;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefTypeType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.service.BuildMovementServiceTestDeployment;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.message.JMSHelper;
import eu.europa.ec.fisheries.uvms.movement.service.message.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movementrules.model.mapper.JAXBMarshaller;

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

     @Inject
    AlarmDAO dao;

    @Test
    @OperateOnDeployment("movementservice")
    public void basicAlarmPersistTest(){
        AlarmReport alarmReport;
        alarmReport = new AlarmReport();
        alarmReport.setAssetGuid(UUID.randomUUID().toString());
        alarmReport.setCreatedDate(Instant.now());
        alarmReport.setPluginType(PluginType.MANUAL.value());
        //alarmReport.setRecipient();
        alarmReport.setStatus(AlarmStatusType.OPEN.value());
        alarmReport.setUpdated(Instant.now());
        alarmReport.setUpdatedBy("UVMS");
        alarmReport.setIncomingMovement(null);
        alarmReport.setAlarmItemList(new ArrayList<>());
        dao.save(alarmReport);

        assertNotNull(alarmReport.getId());

        AlarmItem item = new AlarmItem();
        //alarmReport.getAlarmItemList().add(item);
        item.setAlarmReport(alarmReport);
        item.setRuleGuid("Test rules"); // WTF?
        item.setRuleName("Test rules");
        item.setUpdated(Instant.now());
        item.setUpdatedBy("UVMS");
        dao.save(item);

        assertNotNull(item.getId());

        AlarmReport ar = dao.getAlarmReportByGuid(alarmReport.getId());

        assertTrue(ar.getAlarmItemList().size() > 0);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportNullLatitudeShouldTriggerSanityRuleTest() throws Exception {

        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setLatitude(null);
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportNullLongitudeShouldTriggerSanityRuleTest() throws Exception {

        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setLongitude(null);
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportFutureDateShouldTriggerSanityRuleTest() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setPositionTime(Instant.now().plusSeconds(60 * 60));
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }
    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportMissingPositionTimeSanityRuleTest() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setPositionTime(null);
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportPluginTypeMissingSanityRuleTest() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setPluginType(null);
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportCFRAndIRCSMissingWhileManualOrFluxSanityRuleTest() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setPluginType("FLUX");
        incomingMovement.setAssetCFR(null);
        incomingMovement.setAssetIRCS(null);
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));

        incomingMovement.setPluginType("NOT_FLUX");
        incomingMovement.setComChannelType("MANUAL");
        response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportComChannelTypeMissingSanityRuleTest() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setComChannelType(null);
        incomingMovement.setPluginType("SATELLITE_RECEIVER");
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportMemberNumberMissingSanityRuleTest() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setPluginType("SATELLITE_RECEIVER");
        incomingMovement.setMovementSourceType("INMARSAT_C");
        incomingMovement.setMobileTerminalMemberNumber(null);
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportMemberDNIDSanityRuleTest() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setPluginType("SATELLITE_RECEIVER");
        incomingMovement.setMovementSourceType("INMARSAT_C");
        incomingMovement.setMobileTerminalDNID(null);
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportMemberMTSerialNumberSanityRuleTest() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setPluginType("SATELLITE_RECEIVER");
        incomingMovement.setMovementSourceType("IRIDIUM");
        incomingMovement.setMobileTerminalSerialNumber(null);
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
