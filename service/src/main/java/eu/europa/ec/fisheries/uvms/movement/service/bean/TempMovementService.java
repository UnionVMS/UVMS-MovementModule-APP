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
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetTempMovementListResponse;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.exchange.model.util.DateUtils;
import eu.europa.ec.fisheries.uvms.longpolling.notifications.NotificationMessage;
import eu.europa.ec.fisheries.uvms.movement.message.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.movement.message.consumer.MessageConsumer;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.constants.TempMovementStateEnum;
import eu.europa.ec.fisheries.uvms.movement.model.dto.TempMovementsListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.dao.TempMovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.temp.TempMovement;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedManualMovement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceRuntimeException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementDataSourceResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.TempMovementMapper;
import un.unece.uncefact.data.standard.fluxvesselpositionmessage._4.FLUXVesselPositionMessage;

@Stateless
public class TempMovementService {

    private static final Logger LOG = LoggerFactory.getLogger(TempMovementService.class);

    private static final String FR = "FR";
    private static final String AD = "XEU";
    private static final String TO = "60";
    private static final Long CREATE_TEMP_MOVEMENT_TIMEOUT = 30000L;

    @EJB
    private MessageConsumer consumer;

    @EJB
    private MessageProducer producer;
    
    @Inject
    private TempMovementDao dao;
    
    @Inject
    private AuditService auditService;

    @Inject
    @CreatedManualMovement
    private Event<NotificationMessage> createdManualMovement;

    
    public TempMovement createTempMovement(TempMovement tempMovement, String username) throws MovementServiceException {
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

    public TempMovement archiveTempMovement(String guid, String username) throws MovementServiceException {
        checkUsernameProvided(username);
        return setTempMovementState(guid, TempMovementStateEnum.DELETED, username);
    }
    
    public TempMovement updateTempMovement(TempMovement newTempMovement, String username) throws MovementServiceException {
        checkUsernameProvided(username);
        try {
            if (newTempMovement == null) {
                throw new MovementServiceRuntimeException("No temp movement to update", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
            }
            if (newTempMovement.getId() == null) {
                throw new MovementServiceRuntimeException("Non valid id of temp movement to update", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
            }

            TempMovement tempMovement = dao.getTempMovementById(newTempMovement.getId());
            tempMovement = TempMovementMapper.toExistingTempMovementEntity(tempMovement, newTempMovement, username);
//            return TempMovementMapper.toTempMovement(tempMovement);
            return tempMovement;
        } catch (MovementServiceException e) {
            throw new MovementServiceException("Error when updating temp movement", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    
    public GetTempMovementListResponse getTempMovements(MovementQuery query) throws MovementServiceException {
        try {
            if (query == null || query.getPagination() == null || query.getPagination().getPage() == null) {
                throw new MovementServiceRuntimeException("No valid query", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
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
        } catch (Exception e) {
            throw new MovementServiceException("Error when updating temp movement", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    
    public TempMovement sendTempMovement(String guid, String username) throws MovementServiceException {
        checkUsernameProvided(username);
        try {
            TempMovement movement = setTempMovementState(guid, TempMovementStateEnum.SENT, username);
            FLUXVesselPositionMessage fluxVesselPositionMessage = MovementMapper.mapToFLUXVesselPositionMessage(movement,guid);
            String setFLUXMovementReportRequest = ExchangeModuleRequestMapper.createSetFLUXMovementReportRequest(JAXBUtils.marshallJaxBObjectToString(fluxVesselPositionMessage), username, "urn:un:unece:uncefact:fisheries:FLUX:FA:XNE:1",
                    DateUtils.nowUTC().toDate(), guid, PluginType.MANUAL,
                    FR, null, guid, "eu.europa.ec.fisheries.uvms.plugins.flux.movement",
                    AD, TO, (new Date()).toString());

            producer.sendModuleMessage(setFLUXMovementReportRequest, ModuleQueue.EXCHANGE);
            //Because we were waiting for a response by the consumer, after the fix, the message goes through Rules and we receive a timeout.
            //The delay is greater than 30 sec and that causes the to UI receive an error. Thus we just return the movement back.
            return movement;
        }catch (MovementMessageException | JAXBException e) {
            throw new MovementServiceException("Error when sending temp movement", e, ErrorCode.JMS_SENDING_ERROR);
        }
    }
    
    public TempMovement getTempMovement(String guid) throws MovementServiceException {
        try {
            if (guid == null) {
                throw new MovementServiceRuntimeException("TempMovement GUID cannot be null.", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
            }
            return dao.getTempMovementById(guid);
        } catch (MovementServiceException e) {
            throw new MovementServiceException("Error when getting temp movement", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }
    
    private TempMovement setTempMovementState(String guid, TempMovementStateEnum state, String username) throws MovementServiceException {
        try {
            if (guid == null) {
                throw new IllegalArgumentException("Non valid id of temp movement to update");
            }
            TempMovement tempMovement = dao.getTempMovementById(guid);
            tempMovement.setState(state);
            tempMovement.setUpdated(Instant.now());
            tempMovement.setUpdatedBy(username);
            return tempMovement;
        } catch (MovementServiceException e) {
            throw new MovementServiceException("Could not set temp movement state.", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
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
            throw new MovementServiceRuntimeException("Username in TempMovementRequest cannot be empty", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
    }

    private void validatePosition(Double lat, Double lon) throws MovementServiceException {
        if (lat == null || lon == null) {
            throw new MovementServiceRuntimeException("Longitude and/or latitude is missing.", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (Math.abs(lat) > 90) {
            throw new MovementServiceException("Latitude is outside range.", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (Math.abs(lon) > 180) {
            throw new MovementServiceException("Longitude is outside range.", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
    }
}
