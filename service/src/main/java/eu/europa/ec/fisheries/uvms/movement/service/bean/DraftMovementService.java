/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.movement.service.bean;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;

import eu.europa.ec.fisheries.schema.exchange.module.v1.ExchangeModuleMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetTempMovementListResponse;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.longpolling.notifications.NotificationMessage;
import eu.europa.ec.fisheries.uvms.movement.model.constants.TempMovementStateEnum;
import eu.europa.ec.fisheries.uvms.movement.model.dto.TempMovementsListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.dao.DraftMovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.temp.DraftMovement;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedManualMovement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.DraftMovementMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.movement.service.message.ExchangeBean;

@Stateless
public class DraftMovementService {

    private static final Logger LOG = LoggerFactory.getLogger(DraftMovementService.class);

    @Inject
    private ExchangeBean exchangeProducer;
    
    @Inject
    private DraftMovementDao dao;
    
    @Inject
    private AuditService auditService;

    @Inject
    @CreatedManualMovement
    private Event<NotificationMessage> createdManualMovement;

    
    public DraftMovement createDraftMovement(DraftMovement draftMovement, String username) {
        checkUsernameProvided(username);
        validatePosition(draftMovement.getLatitude(), draftMovement.getLongitude());
        try {
            draftMovement = dao.createDraftMovementEntity(draftMovement);
            fireMovementEvent(draftMovement);
            auditService.sendTempMovementCreatedAudit(draftMovement, username);
            return draftMovement;
        } catch (Exception e) {
            throw new EJBException("Error when creating temp movement", e);
        }
    }

    public DraftMovement archiveDraftMovement(UUID guid, String username) {
        checkUsernameProvided(username);
        return setDraftMovementState(guid, TempMovementStateEnum.DELETED, username);
    }
    
    public DraftMovement updateDraftMovement(DraftMovement newDraftMovement, String username) {
        checkUsernameProvided(username);
        if (newDraftMovement == null) {
            throw new IllegalArgumentException("No temp movement to update");
        }
        if (newDraftMovement.getId() == null) {
            throw new IllegalArgumentException("Non valid id of temp movement to update");
        }

        DraftMovement draftMovement = dao.getDraftMovementById(newDraftMovement.getId());
        draftMovement = DraftMovementMapper.toExistingTempMovementEntity(draftMovement, newDraftMovement, username);
//            return DraftMovementMapper.toTempMovement(draftMovement);
        return draftMovement;

    }

    
    public GetTempMovementListResponse getDraftMovements(MovementQuery query) {
        if (query == null || query.getPagination() == null || query.getPagination().getPage() == null) {
            throw new IllegalArgumentException("No valid query");
        }

        TempMovementsListResponseDto response = new TempMovementsListResponseDto();
        List<TempMovementType> tempMovementList = new ArrayList<>();

        Integer page = query.getPagination().getPage().intValue();
        Integer listSize = query.getPagination().getListSize().intValue();

        List<DraftMovement> draftMovementEntityList = dao.getDraftMovementListPaginated(page, listSize);
        for (DraftMovement entity : draftMovementEntityList) {
            tempMovementList.add(DraftMovementMapper.toTempMovement(entity));
        }

        Long numberMatches = dao.getDraftMovementListCount();
        int numberOfPages = (int) (numberMatches / listSize);
        if (numberMatches % listSize != 0) {
            numberOfPages += 1;
        }

        response.setTotalNumberOfPages(new BigInteger("" + numberOfPages));
        response.setCurrentPage(query.getPagination().getPage());
        response.setTempMovementList(tempMovementList);

        return MovementResponseMapper.tempMovementListResponse(response);
    }

    
    public DraftMovement sendDraftMovement(UUID guid, String username) {
        checkUsernameProvided(username);
        try {
            DraftMovement movement = setDraftMovementState(guid, TempMovementStateEnum.SENT, username);
            SetReportMovementType report = MovementMapper.mapToSetReportMovementType(movement);
            String exchangeRequest = ExchangeModuleRequestMapper.createSetMovementReportRequest(report, username, null,
                    DateUtil.nowUTC(), PluginType.MANUAL, username, null);
            exchangeProducer.sendModuleMessage(exchangeRequest, ExchangeModuleMethod.SET_MOVEMENT_REPORT.value());
            return movement;
        } catch (JMSException ex) {
            throw new IllegalArgumentException("Error when marshaling exchange request.", ex);
        }
    }
    
    public DraftMovement getDraftMovement(UUID guid) {
        if (guid == null) {
            throw new IllegalArgumentException("DraftMovement GUID cannot be null.");
        }
        return dao.getDraftMovementById(guid);

    }
    
    private DraftMovement setDraftMovementState(UUID guid, TempMovementStateEnum state, String username) {
        if (guid == null) {
            throw new IllegalArgumentException("Non valid id of temp movement to update");
        }
        DraftMovement draftMovement = dao.getDraftMovementById(guid);
        draftMovement.setState(state);
        draftMovement.setUpdated(Instant.now());
        draftMovement.setUpdatedBy(username);
        return draftMovement;
    }

    private void fireMovementEvent(DraftMovement createdMovement) {
        try {
            createdManualMovement.fire(new NotificationMessage("movementGuid", createdMovement.getId()));
        } catch (Exception e) {
            LOG.error("Error when firing notification of created temp movement. {}", e.getMessage());
        }
    }

    private void checkUsernameProvided(String username) {
        if(username == null || username.isEmpty()){
            throw new IllegalArgumentException("Username in TempMovementRequest cannot be empty");
        }
    }

    private void validatePosition(Double lat, Double lon){
        if (lat == null || lon == null) {
            throw new IllegalArgumentException("Longitude and/or latitude is missing.");
        }
        if (Math.abs(lat) > 90) {
            throw new IllegalArgumentException("Latitude is outside range.");
        }
        if (Math.abs(lon) > 180) {
            throw new IllegalArgumentException("Longitude is outside range.");
        }
    }
}
