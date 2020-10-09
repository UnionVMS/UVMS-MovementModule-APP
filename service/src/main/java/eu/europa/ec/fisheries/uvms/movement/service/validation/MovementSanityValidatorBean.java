package eu.europa.ec.fisheries.uvms.movement.service.validation;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.uvms.longpolling.notifications.NotificationMessage;
import eu.europa.ec.fisheries.uvms.movement.model.constants.AuditObjectTypeEnum;
import eu.europa.ec.fisheries.uvms.movement.model.constants.AuditOperationEnum;
import eu.europa.ec.fisheries.uvms.movement.service.bean.AuditService;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementCreateBean;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AlarmDAO;
import eu.europa.ec.fisheries.uvms.movement.service.dto.*;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmItem;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmReport;
import eu.europa.ec.fisheries.uvms.movement.service.event.AlarmReportCountEvent;
import eu.europa.ec.fisheries.uvms.movement.service.event.AlarmReportEvent;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.AlarmSearchFieldMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.AlarmSearchValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Stateless
public class MovementSanityValidatorBean {

    private static final Logger LOG = LoggerFactory.getLogger(MovementSanityValidatorBean.class);

    @Inject
    private AlarmDAO alarmDAO;

    @Inject
    private AuditService auditService;
    
    @Inject
    private MovementCreateBean movementCreate;
    
    
    @Inject
    @AlarmReportEvent
    private Event<NotificationMessage> alarmReportEvent;

    @Inject
    @AlarmReportCountEvent
    private Event<NotificationMessage> alarmReportCountEvent;

    public UUID evaluateSanity(IncomingMovement movement) {
        UUID reportId = null;
        for (SanityRule sanityRule : SanityRule.values()) {
            if (sanityRule.evaluate(movement)) {
                LOG.info("\t==> Executing RULE {}", sanityRule.getRuleName());
                reportId = createAlarmReport(sanityRule.getRuleName(), movement);
            }
        }
        return reportId;
    }

    public UUID createAlarmReport(String ruleName, IncomingMovement movement) {

        LOG.info("Create alarm invoked in validation service, rule: {}", ruleName);

        AlarmReport alarmReport = alarmDAO.getOpenAlarmReportByMovementGuid(movement.getId());
        if(alarmReport == null) {
            alarmReport = new AlarmReport();
            alarmReport.setAssetGuid(movement.getAssetGuid());
            alarmReport.setCreatedDate(Instant.now());
            alarmReport.setPluginType(PluginType.fromValue(movement.getPluginType()));
            //alarmReport.setRecipient();
            alarmReport.setStatus(AlarmStatusType.OPEN);
            alarmReport.setUpdated(Instant.now());
            alarmReport.setUpdatedBy("UVMS");
            alarmReport.setIncomingMovement(movement);
            alarmReport.setAlarmItemList(new ArrayList<>());
            alarmDAO.save(alarmReport);
        }


        AlarmItem item = new AlarmItem();
        item.setAlarmReport(alarmReport);
        item.setRuleGuid(ruleName); // WTF?
        item.setRuleName(ruleName);
        item.setUpdated(Instant.now());
        item.setUpdatedBy("UVMS");
        alarmDAO.save(item);

        alarmReport.getAlarmItemList().add(item);

        // Notify long-polling clients of the new alarm report
        alarmReportEvent.fire(new NotificationMessage("guid", alarmReport.getId()));

        // Notify long-polling clients of the change (no vlaue since FE will need to fetch it)
        alarmReportCountEvent.fire(new NotificationMessage("alarmCount", null));
            
        auditService.sendAuditMessage(AuditObjectTypeEnum.ALARM, AuditOperationEnum.CREATE, alarmReport.getId().toString(), null, alarmReport.getUpdatedBy());

        return alarmReport.getId();
    }

    public AlarmListResponseDto getAlarmList(AlarmQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Alarm list query is null");
        }
        if (query.getPagination() == null) {
            throw new IllegalArgumentException("Pagination in alarm list query is null");
        }

        BigInteger page = query.getPagination().getPage();
        BigInteger listSize = query.getPagination().getListSize();

        List<AlarmSearchValue> searchKeyValues = AlarmSearchFieldMapper.mapSearchField(query.getAlarmSearchCriteria());

        String sql = AlarmSearchFieldMapper.createSelectSearchSql(searchKeyValues, query.isDynamic());
        String countSql = AlarmSearchFieldMapper.createCountSearchSql(searchKeyValues, query.isDynamic());

        Long numberMatches = alarmDAO.getAlarmListSearchCount(countSql);
        List<AlarmReport> alarmEntityList = alarmDAO.getAlarmListPaginated(page.intValue(), listSize.intValue(), sql);

        int numberOfPages = (int) (numberMatches / listSize.longValue());
        if (numberMatches % listSize.longValue() != 0) {
            numberOfPages += 1;
        }

        AlarmListResponseDto response = new AlarmListResponseDto();
        response.setTotalNumberOfPages(numberOfPages);
        response.setCurrentPage(query.getPagination().getPage().intValue());
        response.setAlarmList(alarmEntityList);

        return response;
    }

    public AlarmReport updateAlarmStatus(AlarmReport alarm) {
        AlarmReport entity = alarmDAO.getAlarmReportByGuid(alarm.getId());
        if (entity == null) {
            throw new IllegalArgumentException("Alarm is null", null);
        }

        entity.setStatus(alarm.getStatus());
        entity.setUpdatedBy(alarm.getUpdatedBy());
        entity.setUpdated(Instant.now());
        /* TODO: WAT isInactivatePosition()
        if (entity.getIncomingMovement() != null) {
            entity.getIncomingMovement().setActive(!alarm.getIncomingMovement().getActive());
        }
        */

        alarmDAO.merge(entity);

        auditService.sendAuditMessage(AuditObjectTypeEnum.ALARM, AuditOperationEnum.UPDATE, entity.getId().toString(), null, alarm.getUpdatedBy());
        return entity;
    }

    public IncomingMovement updateIncomingMovement(IncomingMovement movement){
        if(movement == null || movement.getId() == null){
            throw new IllegalArgumentException("IncomingMovement or its ID is null");
        }

        if(movement.getAlarmReport() == null) {
            movement.setAlarmReport(alarmDAO.getOpenAlarmReportByMovementGuid(movement.getId()));
        }
        movement.getAlarmReport().setIncomingMovement(movement); //since these two infinetly recurse we make sure that they recurse into each other
        movement.setUpdated(Instant.now());
        alarmDAO.merge(movement);

        return movement;
    }

    public AlarmReport getAlarmReportByGuid(UUID guid) {
        return alarmDAO.getAlarmReportByGuid(guid);
    }

    public String reprocessAlarm(List<String> alarmGuids, String username) {
        AlarmQuery query = mapToOpenAlarmQuery(alarmGuids);
        AlarmListResponseDto alarms = getAlarmList(query);

        for (AlarmReport alarm : alarms.getAlarmList()) {
            // Cannot reprocess without a movement (i.e. "Asset not sending" alarm)
            if (alarm.getIncomingMovement() == null) {
                continue;
            }

            // Mark the alarm as REPROCESSED before reprocessing. That will create a new alarm (if still wrong) with the items remaining.
            alarm.setStatus(AlarmStatusType.REPROCESSED);
            alarm = updateAlarmStatus(alarm);
            auditService.sendAuditMessage(AuditObjectTypeEnum.ALARM, AuditOperationEnum.UPDATE, alarm.getId().toString(), null, username);
            IncomingMovement incomingMovement = alarm.getIncomingMovement();
            movementCreate.processIncomingMovement(incomingMovement);
        }

        return "OK";
    }

    private AlarmQuery mapToOpenAlarmQuery(List<String> alarmGuids) {
        AlarmQuery query = new AlarmQuery();
        ListPagination pagination = new ListPagination();
        pagination.setListSize(BigInteger.valueOf(alarmGuids.size()));
        pagination.setPage(BigInteger.valueOf(1));
        query.setPagination(pagination);

        for (String alarmGuid : alarmGuids) {
            AlarmListCriteria criteria = new AlarmListCriteria();
            criteria.setKey(AlarmSearchKey.ALARM_GUID);
            criteria.setValue(alarmGuid);
            query.getAlarmSearchCriteria().add(criteria);
        }

        // We only want open alarms
        AlarmListCriteria openCrit = new AlarmListCriteria();
        openCrit.setKey(AlarmSearchKey.STATUS);
        openCrit.setValue(AlarmStatusType.OPEN.name());
        query.getAlarmSearchCriteria().add(openCrit);
        query.setDynamic(true);
        return query;
    }


    public long getNumberOfOpenAlarmReports() {
        LOG.info("Counting open alarms");
        return alarmDAO.getNumberOfOpenAlarms();
    }

}
