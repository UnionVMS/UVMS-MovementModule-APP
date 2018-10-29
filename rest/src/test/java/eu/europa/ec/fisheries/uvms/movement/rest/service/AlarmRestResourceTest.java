package eu.europa.ec.fisheries.uvms.movement.rest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.rest.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AlarmDAO;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.*;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmReport;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class AlarmRestResourceTest extends BuildMovementRestDeployment {

    @Inject
    private AlarmDAO alarmDao;


    private static AlarmReport getBasicAlarmReport() {
        AlarmReport alarmReport = new AlarmReport();
        alarmReport.setAssetGuid(UUID.randomUUID().toString());
        alarmReport.setStatus(AlarmStatusType.OPEN.value());
        alarmReport.setUpdated(new Date());
        alarmReport.setUpdatedBy("Test user");
        return alarmReport;
    }

    private static AlarmQuery getBasicAlarmQuery() {
        AlarmQuery query = new AlarmQuery();
        query.setDynamic(true);
        ListPagination pagination = new ListPagination();
        pagination.setPage(BigInteger.valueOf(1));
        pagination.setListSize(BigInteger.valueOf(100));
        query.setPagination(pagination);
        return query;
    }


    @Test
    @OperateOnDeployment("movement")
    public void getAlarmListTest() throws Exception {
        AlarmQuery basicAlarmQuery = getBasicAlarmQuery();
        AlarmListCriteria criteria = new AlarmListCriteria();
        criteria.setKey(AlarmSearchKey.RULE_GUID);
        criteria.setValue("TEST_GUID");
        basicAlarmQuery.getAlarmSearchCriteria().add(criteria);

        String response = getWebTarget()
                .path("alarms/list")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(basicAlarmQuery), String.class);

        AlarmListResponseDto alarmList = deserializeResponseDto(response, AlarmListResponseDto.class);
        assertThat(alarmList.getAlarmList().size(), is(notNullValue()));

        int prevNumberOfReports = alarmList.getAlarmList().size();

        AlarmReport alarmReport = getBasicAlarmReport();
        alarmDao.save(alarmReport);
        criteria.setKey(AlarmSearchKey.ALARM_GUID);
        criteria.setValue(alarmReport.getGuid());

        response = getWebTarget()
                .path("alarms/list")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(basicAlarmQuery), String.class);

        alarmList = deserializeResponseDto(response, AlarmListResponseDto.class);
        assertThat(alarmList.getAlarmList().size(), is(prevNumberOfReports + 1));
        assertEquals(alarmReport.getGuid(), alarmList.getAlarmList().get(0).getGuid());
        assertEquals(alarmReport.getStatus(), alarmList.getAlarmList().get(0).getStatus());

        alarmDao.removeAlarmReportAfterTests(alarmReport);
    }

    @Test
    @OperateOnDeployment("movement")
    public void negativeGetAlarmListTest() throws Exception{
        String response = getWebTarget()
                .path("alarms/list")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new AlarmQuery()), String.class);

        assertEquals(500, getReturnCode(response));
    }

    @Test
    @OperateOnDeployment("movement")
    public void updateAlarmStatusTest() throws Exception {
        AlarmReport alarmReport = getBasicAlarmReport();
        alarmDao.save(alarmReport);

        alarmReport.setStatus(AlarmStatusType.REJECTED.value());

        String response = getWebTarget()
                .path("alarms")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(alarmReport), String.class);

        assertThat(response, is(notNullValue()));
        AlarmReport output = deserializeResponseDto(response, AlarmReport.class);
        assertEquals(alarmReport.getGuid(), output.getGuid());
        assertEquals(AlarmStatusType.REJECTED, output.getStatus());

        alarmDao.removeAlarmReportAfterTests(alarmReport);
    }

    @Test
    @OperateOnDeployment("movement")
    public void negativeUpdateAlarmStatusTest() throws Exception {
        String response = getWebTarget()
                .path("alarms")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(new AlarmReport()), String.class);

        assertEquals(500, getReturnCode(response));
    }

    @Test
    @OperateOnDeployment("movement")
    public void getAlarmReportByGuidTest() throws Exception {
        AlarmReport alarmReport = getBasicAlarmReport();
        alarmDao.save(alarmReport);

        String response = getWebTarget()
                .path("alarms/" + alarmReport.getGuid())
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        AlarmReport responseAlarmReportType = deserializeResponseDto(response, AlarmReport.class);
        assertNotNull(responseAlarmReportType);
        assertEquals(alarmReport.getGuid(), responseAlarmReportType.getGuid());

        alarmDao.removeAlarmReportAfterTests(alarmReport);
    }

    @Test
    @OperateOnDeployment("movement")
    public void negativeGetAlarmReportByGuidTest() throws Exception{
        String response = getWebTarget()
                .path("alarms/" + "test guid")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertEquals(500, getReturnCode(response));
    }

    @Test
    @OperateOnDeployment("movement")
    public void reprocessAlarmTest() throws Exception {
        String response = getWebTarget()
                .path("alarms/reprocess")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Collections.singletonList("NULL_GUID")), String.class);

        String responseString = deserializeResponseDto(response, String.class);
        assertThat(responseString, is("OK"));

        AlarmReport alarmReport = getBasicAlarmReport();
        IncomingMovement incomingMovement = new IncomingMovement();
        incomingMovement.setUpdated(new Date());
        incomingMovement.setUpdatedBy("Test User");
        incomingMovement.setActive(true);
        incomingMovement.setAlarmReport(alarmReport);
        alarmReport.setIncomingMovement(incomingMovement);
        alarmDao.save(alarmReport);

        response = getWebTarget()
                .path("alarms/reprocess")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(Collections.singletonList(alarmReport.getGuid())), String.class);

        responseString = deserializeResponseDto(response, String.class);
        assertThat(responseString, is("OK"));

        response = getWebTarget()
                .path("alarms/" + alarmReport.getGuid())
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        AlarmReport responseAlarmReportType = deserializeResponseDto(response, AlarmReport.class);
        assertNotNull(responseAlarmReportType);
        assertEquals(AlarmStatusType.REPROCESSED, responseAlarmReportType.getStatus());

        alarmDao.removeAlarmReportAfterTests(alarmReport);
    }

    @Test
    @OperateOnDeployment("movement")
    public void getNumberOfOpenAlarmReportsTest() throws Exception {
        String response = getWebTarget()
                .path("alarms/countopen")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        Integer openAlarmReports = deserializeResponseDto(response, Integer.class);
        assertThat(openAlarmReports, is(notNullValue()));

        int prevNumberOfReports = openAlarmReports;

        AlarmReport alarmReport = getBasicAlarmReport();
        alarmDao.save(alarmReport);

        //hmm, is it a good idea to have tests that depend on there not being crap in teh DB?
        response = getWebTarget()
                .path("alarms/countopen")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        openAlarmReports = deserializeResponseDto(response, Integer.class);
        assertThat(openAlarmReports, is(prevNumberOfReports + 1 ));

        AlarmReport alarmReport2 = getBasicAlarmReport();
        alarmDao.save(alarmReport2);

        response = getWebTarget()
                .path("alarms/countopen")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        openAlarmReports = deserializeResponseDto(response, Integer.class);
        assertThat(openAlarmReports, is(prevNumberOfReports + 2 ));

        alarmDao.removeAlarmReportAfterTests(alarmReport);
        alarmDao.removeAlarmReportAfterTests(alarmReport2);
    }

    private ObjectMapper objectMapper = new ObjectMapper();
    private int getReturnCode(String responsDto) throws Exception{
        return objectMapper.readValue(responsDto, ObjectNode.class).get("code").asInt();
    }

    private static <T> T deserializeResponseDto(String responseDto, Class<T> clazz) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.readValue(responseDto, ObjectNode.class);
        JsonNode jsonNode = node.get("data");
        return objectMapper.readValue(objectMapper.writeValueAsString(jsonNode), clazz);
    }

}
