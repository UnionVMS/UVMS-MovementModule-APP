package eu.europa.ec.fisheries.uvms.movement.service.message.consumer.bean;

import eu.europa.ec.fisheries.schema.exchange.module.v1.ProcessedMovementResponse;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefTypeType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.service.BuildMovementServiceTestDeployment;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AlarmDAO;
import eu.europa.ec.fisheries.uvms.movement.service.dto.AlarmStatusType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmItem;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmReport;
import eu.europa.ec.fisheries.uvms.movement.service.message.JMSHelper;
import eu.europa.ec.fisheries.uvms.movement.service.message.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.util.JsonBConfiguratorMovement;
import eu.europa.ec.fisheries.uvms.movement.service.validation.SanityRule;
import eu.europa.ec.fisheries.uvms.movementrules.model.mapper.JAXBMarshaller;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.json.bind.Jsonb;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class SanityRulesTest extends BuildMovementServiceTestDeployment {

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    private Jsonb jsonb;

    JMSHelper jmsHelper;

    @Before
    public void cleanJMS() throws Exception {
        jmsHelper = new JMSHelper(connectionFactory);
        jmsHelper.clearQueue("UVMSMovementRulesEvent");
        jmsHelper.clearQueue(MessageConstants.QUEUE_EXCHANGE_EVENT_NAME);

        jsonb = new JsonBConfiguratorMovement().getContext(null);
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
    public void setMovementReportLatitudeOver90ShouldTriggerSanityRuleTest() throws Exception {

        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setLatitude(91d);
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportLongitudeOver180ShouldTriggerSanityRuleTest() throws Exception {

        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setLongitude(1024d);
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
    public void setMovementReportMissingPositionTimeWithPreviousMovement() throws Exception {

        UUID id = UUID.randomUUID();
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(id.toString());
        incomingMovement.setAssetIRCS("TestIrcs:" + id);
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.MOVEMENT));


        IncomingMovement incomingMovement2 = MovementTestHelper.createIncomingMovementType();
        incomingMovement2.setAssetGuid(null);
        incomingMovement2.setAssetHistoryId(null);
        incomingMovement2.setAssetIRCS("TestIrcs:" + id);
        incomingMovement2.setPositionTime(null);
        response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement2);

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
        System.setProperty("AssetShouldBeEmpty", "true");
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
        System.clearProperty("AssetShouldBeEmpty");
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportCFRAndIRCSMissingButExistsInAssetTest() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setPluginType("FLUX");
        incomingMovement.setAssetCFR(null);
        incomingMovement.setAssetIRCS(null);
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.MOVEMENT));

        incomingMovement.setPluginType("NOT_FLUX");
        incomingMovement.setComChannelType("MANUAL");
        response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.MOVEMENT));
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

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportMemberMmsiLongerThen9CharsSanityRuleTest() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setAssetMMSI("1024307102");
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportDuplicateMovementSanityRuleTest() throws Exception {

        UUID id = UUID.randomUUID();
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(id.toString());
        incomingMovement.setAssetIRCS("TestIrcs:" + id);
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.MOVEMENT));

        response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportExitReportWOPreviousMovementTest() throws Exception {

        UUID id = UUID.randomUUID();
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setLongitude(null);
        incomingMovement.setLatitude(null);
        incomingMovement.setAssetIRCS("TestIrcs:" + id);
        incomingMovement.setMovementType(MovementTypeType.EXI.value());
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));

    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportExitReportWithPreviousMovementTest() throws Exception {

        UUID id = UUID.randomUUID();
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetIRCS("TestIrcs:" + id);
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.MOVEMENT));

        Thread.sleep(1000); //added delay to avoid the duplication code

        incomingMovement.setMovementType(MovementTypeType.EXI.value());
        incomingMovement.setLatitude(null);
        incomingMovement.setLongitude(null);

        response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.MOVEMENT));

    }

    @Test
    @Ignore
    @OperateOnDeployment("movementservice")
    public void setMovementReportMissingCourseSanityRuleTest() throws Exception {

        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setReportedCourse(null);
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void setMovementReportTransponderInactiveTest() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setMobileTerminalLES("inactive"); // Used to control AssetMTRestMock
        ProcessedMovementResponse response = sendIncomingMovementAndReturnAlarmResponse(incomingMovement);

        assertThat(response.getMovementRefType().getType(), is(MovementRefTypeType.ALARM));
        AlarmReport alarmReport = dao.getAlarmReportByGuid(UUID.fromString(response.getMovementRefType().getMovementRefGuid()));
        assertThat(alarmReport.getAlarmItemList().size(), is(1));
        AlarmItem alarmItem = alarmReport.getAlarmItemList().get(0);
        assertThat(alarmItem.getRuleName(), is(SanityRule.TRANSPONDER_INACTIVE.getRuleName()));
    }

    private ProcessedMovementResponse sendIncomingMovementAndReturnAlarmResponse(IncomingMovement incomingMovement) throws Exception{
        String json = jsonb.toJson(incomingMovement);
        jmsHelper.sendMovementMessage(json, incomingMovement.getAssetGuid(), "CREATE");   //grouping on null.....

        Message response = jmsHelper.listenOnQueue(MessageConstants.QUEUE_EXCHANGE_EVENT_NAME);
        ProcessedMovementResponse movementResponse = JAXBMarshaller.unmarshallTextMessage((TextMessage) response, ProcessedMovementResponse.class);
        return movementResponse;
    }
}
