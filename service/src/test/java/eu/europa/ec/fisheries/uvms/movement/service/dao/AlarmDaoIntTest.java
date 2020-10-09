package eu.europa.ec.fisheries.uvms.movement.service.dao;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dto.AlarmStatusType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmItem;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmReport;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.time.Instant;
import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class AlarmDaoIntTest extends TransactionalTests {

    @EJB
    private AlarmDAO alarmDAO;

    @Test
    @OperateOnDeployment("movementservice")
    public void testCreateAlarmReport() {
        IncomingMovement im = new IncomingMovement();
        im.setAssetGuid("test");
        im.setDateReceived(Instant.now());
        im.setUpdated(Instant.now());
        im.setUpdatedBy("TEST");
        alarmDAO.save(im);
        assertNotNull(im.getId());


        AlarmReport alarmReport = new AlarmReport();
        alarmReport.setUpdatedBy("TEST");
        alarmReport.setUpdated(Instant.now());
        alarmReport.setStatus(AlarmStatusType.OPEN);
        alarmReport.setUpdated(Instant.now());
        alarmReport.setUpdatedBy("test");
        alarmReport.setIncomingMovement(im);
        alarmReport.setRecipient("");
        alarmReport.setPluginType(PluginType.SATELLITE_RECEIVER);
        alarmReport.setAlarmItemList(new ArrayList<>());
        alarmDAO.save(alarmReport);
        assertNotNull(alarmReport.getId());

        im.setAlarmReport(alarmReport);

        AlarmItem alarmItem = new AlarmItem();
        alarmItem.setRuleName("TEST");
        alarmItem.setRuleGuid("TEST");
        alarmItem.setAlarmReport(alarmReport);
        alarmItem.setUpdated(Instant.now());
        alarmItem.setUpdatedBy("TEST");
        alarmDAO.save(alarmItem);
        alarmReport.getAlarmItemList().add(alarmItem);
        assertNotNull(alarmItem.getId());
        em.flush();


        AlarmReport theReport = alarmDAO.getOpenAlarmReportByMovementGuid(im.getId());
        assertNotNull(theReport);
        Assert.assertEquals(alarmReport.getId(), theReport.getId());
    }

}
