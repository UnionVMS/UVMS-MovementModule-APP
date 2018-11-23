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
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import eu.europa.ec.fisheries.uvms.movement.service.message.MovementMessageProducerBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetTempMovementListResponse;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.longpolling.notifications.NotificationMessage;
import eu.europa.ec.fisheries.uvms.movement.model.constants.TempMovementStateEnum;
import eu.europa.ec.fisheries.uvms.movement.model.dto.TempMovementsListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.dao.TempMovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.temp.TempMovement;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedManualMovement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementDataSourceResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.TempMovementMapper;
import eu.europa.ec.fisheries.uvms.movement.service.message.ModuleQueue;

@Stateless
public class TempMovementService {

    private static final Logger LOG = LoggerFactory.getLogger(TempMovementService.class);

    @EJB
    private MovementMessageProducerBean producer;
    
    @Inject
    private TempMovementDao dao;
    
    @Inject
    private AuditService auditService;

    @Inject
    @CreatedManualMovement
    private Event<NotificationMessage> createdManualMovement;

    
    public TempMovement createTempMovement(TempMovement tempMovement, String username) {
        checkUsernameProvided(username);
        validatePosition(tempMovement.getLatitude(), tempMovement.getLongitude());
        try {
            tempMovement = dao.createTempMovementEntity(tempMovement);
            fireMovementEvent(tempMovement);
            auditService.sendTempMovementCreatedAudit(tempMovement, username);
            return tempMovement;
        } catch (Exception e) {
            throw new EJBException("Error when creating temp movement", e);
        }
    }

    public TempMovement archiveTempMovement(UUID guid, String username) {
        checkUsernameProvided(username);
        return setTempMovementState(guid, TempMovementStateEnum.DELETED, username);
    }
    
    public TempMovement updateTempMovement(TempMovement newTempMovement, String username) {
        checkUsernameProvided(username);
        if (newTempMovement == null) {
            throw new IllegalArgumentException("No temp movement to update");
        }
        if (newTempMovement.getId() == null) {
            throw new IllegalArgumentException("Non valid id of temp movement to update");
        }

        TempMovement tempMovement = dao.getTempMovementById(newTempMovement.getId());
        tempMovement = TempMovementMapper.toExistingTempMovementEntity(tempMovement, newTempMovement, username);
//            return TempMovementMapper.toTempMovement(tempMovement);
        return tempMovement;

    }

    
    public GetTempMovementListResponse getTempMovements(MovementQuery query) {
        if (query == null || query.getPagination() == null || query.getPagination().getPage() == null) {
            throw new IllegalArgumentException("No valid query");
        }

        TempMovementsListResponseDto response = new TempMovementsListResponseDto();
        List<TempMovementType> tempMovementList = new ArrayList<>();

        Integer page = query.getPagination().getPage().intValue();
        Integer listSize = query.getPagination().getListSize().intValue();

        List<TempMovement> tempMovementEntityList = dao.getTempMovementListPaginated(page, listSize);
        for (TempMovement entity : tempMovementEntityList) {
            tempMovementList.add(TempMovementMapper.toTempMovement(entity));
        }

        Long numberMatches = dao.getTempMovementListCount();
        int numberOfPages = (int) (numberMatches / listSize);
        if (numberMatches % listSize != 0) {
            numberOfPages += 1;
        }

        response.setTotalNumberOfPages(new BigInteger("" + numberOfPages));
        response.setCurrentPage(query.getPagination().getPage());
        response.setTempMovementList(tempMovementList);

        return MovementDataSourceResponseMapper.tempMovementListResponse(response);
    }

    
    public TempMovement sendTempMovement(UUID guid, String username) {
        checkUsernameProvided(username);
        try {
            TempMovement movement = setTempMovementState(guid, TempMovementStateEnum.SENT, username);
            SetReportMovementType report = MovementMapper.mapToSetReportMovementType(movement);
            String exchangeRequest = ExchangeModuleRequestMapper.createSetMovementReportRequest(report, username, null,
                    Date.from(DateUtil.nowUTC()), null, PluginType.MANUAL, username, null);
            producer.sendModuleMessage(exchangeRequest, ModuleQueue.EXCHANGE);
            return movement;
        } catch (ExchangeModelMarshallException ex) {
            throw new RuntimeException("Error when marshaling exchange request.", ex);
        }
    }
    
    public TempMovement getTempMovement(UUID guid) {
        if (guid == null) {
            throw new IllegalArgumentException("TempMovement GUID cannot be null.");
        }
        return dao.getTempMovementById(guid);

    }
    
    private TempMovement setTempMovementState(UUID guid, TempMovementStateEnum state, String username) {
        if (guid == null) {
            throw new IllegalArgumentException("Non valid id of temp movement to update");
        }
        TempMovement tempMovement = dao.getTempMovementById(guid);
        tempMovement.setState(state);
        tempMovement.setUpdated(Instant.now());
        tempMovement.setUpdatedBy(username);
        return tempMovement;
    }

    private void fireMovementEvent(TempMovement createdMovement) {
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
