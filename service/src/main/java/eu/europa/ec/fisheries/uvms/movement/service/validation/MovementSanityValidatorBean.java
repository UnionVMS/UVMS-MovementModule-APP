package eu.europa.ec.fisheries.uvms.movement.service.validation;

import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.uvms.movement.model.constants.AuditObjectTypeEnum;
import eu.europa.ec.fisheries.uvms.movement.model.constants.AuditOperationEnum;
import eu.europa.ec.fisheries.uvms.movement.service.bean.AuditService;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AlarmDAO;
import eu.europa.ec.fisheries.uvms.movement.service.dto.*;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmItem;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmReport;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.AlarmSearchFieldMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.AlarmSearchValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Stateless
public class MovementSanityValidatorBean {

    private static final Logger LOG = LoggerFactory.getLogger(MovementSanityValidatorBean.class);

    @Inject
    private AlarmDAO alarmDAO;

    @Inject
    private AuditService auditService;

    public boolean evaluateSanity(IncomingMovement movement) {

        boolean isOk = true;

        // Evaluate positional data
        if(movement.getPositionTime() == null){  //Time missing
            LOG.info("\t==> Executing RULE 'Sanity rule 1 - Time missing'");
            createAlarmReport("Time missing", movement);
            isOk = false;
        }

        if(movement.getLatitude() == null){  //Lat missing
            LOG.info("\t==> Executing RULE 'Sanity rule 2 - Lat missing'");
            createAlarmReport("Lat missing", movement);
            isOk = false;
        }

        if(movement.getLongitude() == null){  //Long missing
            LOG.info("\t==> Executing RULE 'Sanity rule 3 - Long missing'");
            createAlarmReport("Long missing", movement);
            isOk = false;
        }

        if(movement.getAltitude() == null){  //Altitude must exist
            LOG.info("\t==> Executing RULE 'Sanity check - altitude must exist'");
            createAlarmReport("Sanity check - altitude must exist", movement);
            isOk = false;
        }

        if(movement.getPositionTime() != null && movement.getPositionTime().after(new Date())){  //Time in the future
            LOG.info("\t==> Executing RULE 'Sanity rule 12 - Time in the future'" + "[" + movement.getPositionTime() + " > " + new Date() + "]");
            createAlarmReport("Time in future", movement);
            isOk = false;
        }

        if(movement.getReportedSpeed() == null){  //reportedSpeed must exist
            LOG.info("\t==> Executing RULE 'Sanity check - reportedSpeed must exist'");
            createAlarmReport("Sanity check - reportedSpeed must exist", movement);
            isOk = false;
        }

        if(movement.getReportedCourse() == null){  //reportedCourse must exist
            LOG.info("\t==> Executing RULE 'Sanity check - reportedCourse must exist'");
            createAlarmReport("Sanity check - reportedCourse must exist", movement);
            isOk = false;
        }

        if(movement.getMovementType() == null){  //movementType must
            LOG.info("\t==> Executing RULE 'Sanity check - movementType must exist'");
            createAlarmReport("Sanity check - movementType must exist", movement);
            isOk = false;
        }

        if(movement.getMovementSourceType() == null){  //source must exist
            LOG.info("\t==> Executing RULE 'Sanity check - source must exist'");
            createAlarmReport("Sanity check - source must exist", movement);
            isOk = false;
        }
/*
        if((movement.getMobileTerminalConnectId() == null || movement.getMobileTerminalConnectId().isEmpty()) && movement.getPluginType().equals("SATELLITE_RECEIVER")){  //Transponder not found
            LOG.info("\t==> Executing RULE 'Sanity rule 4 - Transponder not found'");
            createAlarmReport("Transponder not found", movement);
            isOk = false;
        }
*/
        if(movement.getPluginType().equals("SATELLITE_RECEIVER") && (movement.getMobileTerminalConnectId() == null || movement.getMobileTerminalConnectId().isEmpty())){  //Transponder not found
            LOG.info("\t==> Executing RULE 'Sanity rule 4 - Transponder not found'");
            createAlarmReport("Transponder not found", movement);
            isOk = false;
        }

        if(movement.getPluginType().equals("SATELLITE_RECEIVER") && movement.getMovementSourceType().equals("INMARSAT_C") && (movement.getMobileTerminalMemberNumber() == null || movement.getMobileTerminalMemberNumber().isEmpty())){  //Mem No. missing
            LOG.info("\t==> Executing RULE 'Sanity rule 6 - Mem No. missing'");
            createAlarmReport("Mem No. missing", movement);
            isOk = false;
        }

        if(movement.getPluginType().equals("SATELLITE_RECEIVER") && movement.getMovementSourceType().equals("INMARSAT_C") && (movement.getMobileTerminalDNID() == null || movement.getMobileTerminalDNID().isEmpty())){  //DNID missing
            LOG.info("\t==> Executing RULE 'Sanity rule 7 - DNID missing'");
            createAlarmReport("DNID missing", movement);
            isOk = false;
        }

        if(movement.getPluginType().equals("SATELLITE_RECEIVER") && movement.getMovementSourceType().equals("IRIDIUM") && (movement.getMobileTerminalSerialNumber() == null || movement.getMobileTerminalSerialNumber().isEmpty())){  //Serial No. missing
            LOG.info("\t==> Executing RULE 'Sanity rule 8 - Serial No. missing'");
            createAlarmReport("Serial No. missing", movement);
            isOk = false;
        }

        if(movement.getPluginType().equals("SATELLITE_RECEIVER") && (movement.getComChannelType() == null || movement.getComChannelType().isEmpty())){  //ComChannel Type missing
            LOG.info("\t==> Executing RULE 'Sanity rule 9 - ComChannel Type missing'");
            createAlarmReport("ComChannel Type missing", movement);
            isOk = false;
        }

        if(((movement.getAssetCFR() == null || movement.getAssetCFR().isEmpty()) && (movement.getAssetIRCS() == null || movement.getAssetIRCS().isEmpty())) && (movement.getPluginType().equals("FLUX") || movement.getComChannelType().equals("MANUAL"))){  //CFR and IRCS missing
            LOG.info("\t==> Executing RULE 'Sanity rule 10 - CFR and IRCS missing'");
            createAlarmReport("CFR and IRCS missing", movement);
            isOk = false;
        }

        if(movement.getPluginType() == null || movement.getPluginType().isEmpty()){  //Plugin Type missing
            LOG.info("\t==> Executing RULE 'Sanity rule 11 - Plugin Type missing'");
            createAlarmReport("Plugin Type missing", movement);
            isOk = false;
        }

        if(movement.getStatus() == null || movement.getStatus().isEmpty()){  //statusCode must exist
            LOG.info("\t==> Executing RULE 'Sanity check - statusCode must exist'");
            createAlarmReport("Sanity check - statusCode must exist", movement);
            isOk = false;
        }

        if(movement.getActivityCallback() == null || movement.getActivityCallback().isEmpty()){  //activityCallback must exist
            LOG.info("\t==> Executing RULE 'Sanity check - activityCallback must exist'");
            createAlarmReport("Sanity check - activityCallback must exist", movement);
            isOk = false;
        }

        if(movement.getActivityMessageId() == null || movement.getActivityMessageId().isEmpty()){  //activityMessageId must exist
            LOG.info("\t==> Executing RULE 'Sanity check - activityMessageId must exist'");
            createAlarmReport("Sanity check - activityMessageId must exist", movement);
            isOk = false;
        }

        if(movement.getActivityMessageType() == null || movement.getActivityMessageType().isEmpty()){  //activityMessageType must exist
            LOG.info("\t==> Executing RULE 'Sanity check - activityMessageType must exist'");
            createAlarmReport("Sanity check - activityMessageType must exist", movement);
            isOk = false;
        }

        //  movement : RawMovementFact((assetType == null) && (pluginType == "FLUX" || comChannelType == "MANUAL"))
        if(movement.getAssetType() == null && (movement.getPluginType().equals("FLUX") || movement.getComChannelType().equals("MANUAL"))){  //assetType must exist
            LOG.info("\t==> Executing RULE 'Sanity check - assetType must exist'");
            createAlarmReport("Sanity check - assetType must exist", movement);
            isOk = false;
        }

        if(movement.getAssetGuid() == null || movement.getAssetGuid().isEmpty()){  //Asset not found
            LOG.info("\t==> Executing RULE 'Sanity rule 5 - Asset not found'");
            createAlarmReport("Asset not found", movement);
            isOk = false;
        }

        return isOk;
    }

    public void createAlarmReport(String ruleName, IncomingMovement movement) {

        LOG.info("Create alarm invoked in validation service, rule: {}", ruleName);

        AlarmReport alarmReport = alarmDAO.getOpenAlarmReportByMovementGuid(movement.getGuid());
        if(alarmReport == null) {
            alarmReport = new AlarmReport();
            alarmReport.setAssetGuid(movement.getAssetGuid());
            alarmReport.setCreatedDate(new Date());
            alarmReport.setGuid(UUID.randomUUID().toString());
            alarmReport.setPluginType(movement.getPluginType());
            //alarmReport.setRecipient();
            alarmReport.setStatus(AlarmStatusType.OPEN.value());
            alarmReport.setUpdated(new Date());
            alarmReport.setUpdatedBy("UVMS");
            alarmReport.setIncomingMovement(movement);
            alarmReport.setAlarmItemList(new ArrayList<>());
            alarmDAO.save(alarmReport);
        }


        AlarmItem item = new AlarmItem();
        item.setAlarmReport(alarmReport);
        item.setGuid(UUID.randomUUID().toString());
        item.setRuleGuid(ruleName); // WTF?
        item.setRuleName(ruleName);
        item.setUpdated(new Date());
        item.setUpdatedBy("UVMS");
        alarmDAO.save(item);

        alarmReport.getAlarmItemList().add(item);

            /*
            // Notify long-polling clients of the new alarm report
            alarmReportEvent.fire(new NotificationMessage("guid", createdAlarmReport.getGuid()));

            // Notify long-polling clients of the change (no vlaue since FE will need to fetch it)
            alarmReportCountEvent.fire(new NotificationMessage("alarmCount", null));
            */
        auditService.sendAuditMessage(AuditObjectTypeEnum.ALARM, AuditOperationEnum.CREATE, alarmReport.getGuid(), null, alarmReport.getUpdatedBy());
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
        AlarmReport entity = alarmDAO.getAlarmReportByGuid(alarm.getGuid());
        if (entity == null) {
            throw new IllegalArgumentException("Alarm is null", null);
        }

        entity.setStatus(alarm.getStatus());
        entity.setUpdatedBy(alarm.getUpdatedBy());
        entity.setUpdated(new Date());
        /* TODO: WAT isInactivatePosition()
        if (entity.getIncomingMovement() != null) {
            entity.getIncomingMovement().setActive(!alarm.getIncomingMovement().getActive());
        }
        */

        alarmDAO.merge(entity);

        /*
        // Notify long-polling clients of the change
        alarmReportEvent.fire(new NotificationMessage("guid", entity.getGuid()));
        // Notify long-polling clients of the change (no vlaue since FE will need to fetch it)
        alarmReportCountEvent.fire(new NotificationMessage("alarmCount", null));
        */

        auditService.sendAuditMessage(AuditObjectTypeEnum.ALARM, AuditOperationEnum.UPDATE, entity.getGuid(), null, alarm.getUpdatedBy());
        return entity;
    }

    public AlarmReport getAlarmReportByGuid(String guid) {
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
            alarm.setStatus(AlarmStatusType.REPROCESSED.value());
            alarm = updateAlarmStatus(alarm);
            auditService.sendAuditMessage(AuditObjectTypeEnum.ALARM, AuditOperationEnum.UPDATE, alarm.getGuid(), null, username);
            IncomingMovement incomingMovement = alarm.getIncomingMovement();
            String pluginType = alarm.getPluginType();
            // TODO: Implement reprocess of alarms!!
            //movementReportBean.setMovementReportReceived(incomingMovement, pluginType, username);
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
