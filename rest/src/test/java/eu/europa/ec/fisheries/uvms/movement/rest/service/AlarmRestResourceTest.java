package eu.europa.ec.fisheries.uvms.movement.rest.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AlarmDAO;
import eu.europa.ec.fisheries.uvms.movement.service.dto.AlarmListCriteria;
import eu.europa.ec.fisheries.uvms.movement.service.dto.AlarmListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.AlarmQuery;
import eu.europa.ec.fisheries.uvms.movement.service.dto.AlarmSearchKey;
import eu.europa.ec.fisheries.uvms.movement.service.dto.AlarmStatusType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmReport;

@RunWith(Arquillian.class)
public class AlarmRestResourceTest extends BuildMovementRestDeployment {

    @Inject
    private AlarmDAO alarmDao;


    private static AlarmReport getBasicAlarmReport() {
        AlarmReport alarmReport = new AlarmReport();
        alarmReport.setAssetGuid(UUID.randomUUID().toString());
        alarmReport.setStatus(AlarmStatusType.OPEN.value());
        alarmReport.setUpdated(Instant.now());
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
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(basicAlarmQuery), String.class);

        AlarmListResponseDto alarmList = deserialize(response, AlarmListResponseDto.class);
        assertThat(alarmList.getAlarmList().size(), is(notNullValue()));

        int prevNumberOfReports = alarmList.getAlarmList().size();

        AlarmReport alarmReport = getBasicAlarmReport();
        alarmDao.save(alarmReport);
        criteria.setKey(AlarmSearchKey.ALARM_GUID);
        criteria.setValue(alarmReport.getId().toString());

        response = getWebTarget()
                .path("alarms/list")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(basicAlarmQuery), String.class);

        alarmList = deserialize(response, AlarmListResponseDto.class);
        assertThat(alarmList.getAlarmList().size(), is(prevNumberOfReports + 1));
        assertEquals(alarmReport.getId(), alarmList.getAlarmList().get(0).getId());
        assertEquals(alarmReport.getStatus(), alarmList.getAlarmList().get(0).getStatus());

        alarmDao.removeAlarmReportAfterTests(alarmReport);
    }

    @Test
    @OperateOnDeployment("movement")
    public void negativeGetAlarmListTest() {
        Response response = getWebTarget()
                .path("alarms/list")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(new AlarmQuery()));

        assertEquals(500, response.getStatus());
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
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(alarmReport), String.class);

        assertThat(response, is(notNullValue()));
        AlarmReport output = deserialize(response, AlarmReport.class);
        assertEquals(alarmReport.getId(), output.getId());
        assertEquals(AlarmStatusType.REJECTED.value(), output.getStatus());

        alarmDao.removeAlarmReportAfterTests(alarmReport);
    }

    @Test
    @OperateOnDeployment("movement")
    public void negativeUpdateAlarmStatusTest() {
        Response response = getWebTarget()
                .path("alarms")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(new AlarmReport()));

        assertEquals(500, response.getStatus());
    }

    @Test
    @OperateOnDeployment("movement")
    public void getAlarmReportByGuidTest() throws Exception {
        AlarmReport alarmReport = getBasicAlarmReport();
        alarmDao.save(alarmReport);

        String response = getWebTarget()
                .path("alarms/" + alarmReport.getId())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(String.class);

        AlarmReport responseAlarmReportType = deserialize(response, AlarmReport.class);
        assertNotNull(responseAlarmReportType);
        assertEquals(alarmReport.getId(), responseAlarmReportType.getId());

        alarmDao.removeAlarmReportAfterTests(alarmReport);
    }

    @Test
    @OperateOnDeployment("movement")
    public void negativeGetAlarmReportByGuidTest() {
        Response response = getWebTarget()
                .path("alarms/" + "test guid")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get();

        assertEquals(500, response.getStatus());
    }

    @Test
    @OperateOnDeployment("movement")
    public void reprocessAlarmTest() throws Exception {
        Response response = getWebTarget()
                .path("alarms/reprocess")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(Collections.singletonList(UUID.randomUUID())));      //previsouly "NULL_GUID"

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        AlarmReport alarmReport = getBasicAlarmReport();
        IncomingMovement incomingMovement = new IncomingMovement();
        incomingMovement.setUpdated(Instant.now());
        incomingMovement.setUpdatedBy("Test User");
        incomingMovement.setActive(true);
        incomingMovement.setAlarmReport(alarmReport);
        incomingMovement.setPluginType("NAF");
        incomingMovement.setComChannelType("Test");
        alarmReport.setIncomingMovement(incomingMovement);
        alarmDao.save(alarmReport);

        response = getWebTarget()
                .path("alarms/reprocess")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(Collections.singletonList(alarmReport.getId())));

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        String response2 = getWebTarget()
                .path("alarms/" + alarmReport.getId())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(String.class);

        AlarmReport responseAlarmReportType = deserialize(response2, AlarmReport.class);
        assertNotNull(responseAlarmReportType);
        assertEquals(AlarmStatusType.REPROCESSED.value(), responseAlarmReportType.getStatus());
    }

    @Test
    @OperateOnDeployment("movement")
    public void changeIncomingMovementTest() throws Exception {


        AlarmReport alarmReport = getBasicAlarmReport();
        IncomingMovement incomingMovement = new IncomingMovement();
        incomingMovement.setUpdated(Instant.now());
        incomingMovement.setUpdatedBy("Test User");
        incomingMovement.setActive(true);
        incomingMovement.setAlarmReport(alarmReport);
        incomingMovement.setPluginType("NAF");
        incomingMovement.setComChannelType("Test");
        alarmReport.setIncomingMovement(incomingMovement);
        alarmDao.save(alarmReport);

        assertNotNull(incomingMovement.getId());

        String response2 = getWebTarget()
                .path("alarms/" + alarmReport.getId())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(String.class);

        AlarmReport responseAlarmReportType = deserialize(response2, AlarmReport.class);
        assertNotNull(responseAlarmReportType);
        IncomingMovement savedMovement = responseAlarmReportType.getIncomingMovement();


        savedMovement.setUpdatedBy("Another User");


        Response response = getWebTarget()
                .path("alarms/incomingMovement")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(savedMovement));

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        response2 = getWebTarget()
                .path("alarms/" + alarmReport.getId())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(String.class);

        responseAlarmReportType = deserialize(response2, AlarmReport.class);
        assertNotNull(responseAlarmReportType);
        assertEquals(AlarmStatusType.OPEN.value(), responseAlarmReportType.getStatus());
        assertTrue(responseAlarmReportType.getIncomingMovement().getUpdatedBy().equals("Another User"));

        savedMovement.setAlarmReport(null);
        savedMovement.setUpdatedBy("Yet Another User");

        response = getWebTarget()
                .path("alarms/incomingMovement")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(savedMovement));

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        response2 = getWebTarget()
                .path("alarms/" + alarmReport.getId())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(String.class);

        responseAlarmReportType = deserialize(response2, AlarmReport.class);
        assertNotNull(responseAlarmReportType);
        assertEquals(AlarmStatusType.OPEN.value(), responseAlarmReportType.getStatus());
        assertTrue(responseAlarmReportType.getIncomingMovement().getUpdatedBy().equals("Yet Another User"));
    }

    @Test
    @OperateOnDeployment("movement")
    public void getNumberOfOpenAlarmReportsTest() throws Exception {
        String response = getWebTarget()
                .path("alarms/countopen")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(String.class);

        Integer openAlarmReports = deserialize(response, Integer.class);
        assertThat(openAlarmReports, is(notNullValue()));

        int prevNumberOfReports = openAlarmReports;

        AlarmReport alarmReport = getBasicAlarmReport();
        alarmDao.save(alarmReport);

        response = getWebTarget()
                .path("alarms/countopen")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(String.class);

        openAlarmReports = deserialize(response, Integer.class);
        assertThat(openAlarmReports, is(prevNumberOfReports + 1 ));

        AlarmReport alarmReport2 = getBasicAlarmReport();
        alarmDao.save(alarmReport2);

        response = getWebTarget()
                .path("alarms/countopen")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(String.class);

        openAlarmReports = deserialize(response, Integer.class);
        assertThat(openAlarmReports, is(prevNumberOfReports + 2 ));

        alarmDao.removeAlarmReportAfterTests(alarmReport);
        alarmDao.removeAlarmReportAfterTests(alarmReport2);
    }

    private static <T> T deserialize(String value, Class<T> clazz) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(value, clazz);
    }

}
