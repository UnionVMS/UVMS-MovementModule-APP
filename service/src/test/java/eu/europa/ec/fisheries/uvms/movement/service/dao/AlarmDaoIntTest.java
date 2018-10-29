package eu.europa.ec.fisheries.uvms.movement.service.dao;

import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmItem;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmReport;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

@RunWith(Arquillian.class)
public class AlarmDaoIntTest extends TransactionalTests {

    @EJB
    private AlarmDAO alarmDAO;

    @Test
    public void testCreateAlarmReport() {
        String guid = UUID.randomUUID().toString();
        IncomingMovement im = new IncomingMovement();
        im.setGuid(guid);
        im.setAssetGuid("test");
        im.setDateReceived(new Date());
        im.setUpdated(new Date());
        im.setUpdatedBy("TEST");
        alarmDAO.save(im);


        AlarmReport alarmReport = new AlarmReport();
        alarmReport.setUpdatedBy("TEST");
        alarmReport.setUpdated(new Date());
        alarmReport.setStatus("OPEN");
        alarmReport.setUpdated(new Date());
        alarmReport.setUpdatedBy("test");
        alarmReport.setIncomingMovement(im);
        alarmReport.setRecipient("");
        alarmReport.setPluginType("TEST");
        alarmReport.setAlarmItemList(new ArrayList<>());
        alarmDAO.save(alarmReport);

        im.setAlarmReport(alarmReport);

        AlarmItem alarmItem = new AlarmItem();
        alarmItem.setRuleName("TEST");
        alarmItem.setRuleGuid("TEST");
        alarmItem.setAlarmReport(alarmReport);
        alarmItem.setUpdated(new Date());
        alarmItem.setUpdatedBy("TEST");
        alarmDAO.save(alarmItem);
        alarmReport.getAlarmItemList().add(alarmItem);


        AlarmReport theReport = alarmDAO.getOpenAlarmReportByMovementGuid(guid);
        Assert.assertNotNull(theReport);
        Assert.assertEquals(alarmReport.getId(), theReport.getId());
    }

}
