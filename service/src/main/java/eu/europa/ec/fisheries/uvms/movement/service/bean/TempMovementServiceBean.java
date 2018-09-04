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

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetTempMovementListResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.audit.model.exception.AuditModelMarshallException;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.longpolling.notifications.NotificationMessage;
import eu.europa.ec.fisheries.uvms.movement.bean.TempMovementDomainModelBean;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.message.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.movement.message.consumer.MessageConsumer;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.mapper.AuditModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.dto.TempMovementsListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.TempMovementService;
import eu.europa.ec.fisheries.uvms.movement.service.bean.mapper.MovementDataSourceResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedManualMovement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceRuntimeException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class TempMovementServiceBean implements TempMovementService {

    private final static Logger LOG = LoggerFactory.getLogger(TempMovementServiceBean.class);

    @EJB
    private MessageConsumer consumer;

    @EJB
    private MessageProducer producer;

    @Inject
    @CreatedManualMovement
    private Event<NotificationMessage> createdManualMovement;

    @EJB
    private TempMovementDomainModelBean tempMovementModel;

    private static final Long CREATE_TEMP_MOVEMENT_TIMEOUT = 30000L;

    @Override
    public TempMovementType createTempMovement(TempMovementType tempMovementType, String username) throws MovementServiceException {
        LOG.debug("Creating temp movement");
        checkUsernameProvided(username);
        validatePosition(tempMovementType.getPosition());
        try {
            TempMovementType createdMovement = tempMovementModel.createTempMovement(tempMovementType, username);
            fireMovementEvent(createdMovement);
            // this should not roll back,  so we just log it
            try {
                producer.sendModuleMessage(AuditModuleRequestMapper.mapAuditLogTempMovementCreated(createdMovement.getGuid(), username), ModuleQueue.AUDIT);
            } catch (AuditModelMarshallException | MovementMessageException ignore) {
                LOG.error("Failed to send audit log message! Temp Movement with guid {} was created ", createdMovement.getGuid());
            }
            return createdMovement;
        } catch (MovementDomainException e) {
            LOG.error("[ Error when creating temp movement. ] {}", e.getMessage());
            throw new EJBException("Error when creating temp movement: " + e.getMessage(), e);
        }
    }

    @Override
    public TempMovementType archiveTempMovement(String guid, String username) throws MovementServiceException {
        checkUsernameProvided(username);
        try {
            return tempMovementModel.archiveTempMovement(guid, username);
        } catch (MovementDomainException e) {
            LOG.error("[ Error when updating temp movement status. ] {}", e.getMessage());
            throw new MovementServiceException("Error when updating temp movement status", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    @Override
    public TempMovementType updateTempMovement(TempMovementType tempMovementType, String username) throws MovementServiceException {
        checkUsernameProvided(username);
        try {
            return tempMovementModel.updateTempMovement(tempMovementType, username);
        } catch (MovementDomainException e) {
            LOG.error("[ Error when updating temp movement. ] {}", e.getMessage());
            throw new MovementServiceException("Error when updating temp movement", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    @Override
    public GetTempMovementListResponse getTempMovements(MovementQuery query) throws MovementServiceException {
        try {
            TempMovementsListResponseDto tempMovements = tempMovementModel.getTempMovementList(query);
            return MovementDataSourceResponseMapper.tempMovementListResponse(tempMovements);
        } catch (MovementDomainException e) {
            LOG.error("[ Error when updating temp movement. ] {}", e.getMessage());
            throw new MovementServiceException("Error when updating temp movement", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    @Override
    public TempMovementType sendTempMovement(String guid, String username) throws MovementServiceException {
        checkUsernameProvided(username);
        try {
            LOG.debug("Getting tempMovement from db");
            TempMovementType movement = tempMovementModel.sendTempMovement(guid, username);
            LOG.debug("Sending temp movement to Exchange");
            SetReportMovementType report = MovementMapper.mapToSetReportMovementType(movement);
            String exchangeRequest = ExchangeModuleRequestMapper.createSetMovementReportRequest(report, username);
            String exchangeMessageId = producer.sendModuleMessage(exchangeRequest, ModuleQueue.EXCHANGE);
            consumer.getMessage(exchangeMessageId, TextMessage.class, CREATE_TEMP_MOVEMENT_TIMEOUT);
            return movement;
        }catch ( MovementDomainException | MovementMessageException | MessageException e) {
            LOG.error("[ Error when sending temp movement status. ] {}", e.getMessage());
            throw new MovementServiceException("Error when sending temp movement status", e, ErrorCode.JMS_SENDING_ERROR);
        } catch (ExchangeModelMarshallException ex) {
            LOG.error("[ Error when marshaling exchange request. ] {}", ex.getMessage());
            throw new MovementServiceException("Error when marshaling exchange request.", ex, ErrorCode.EXCHANGE_MARSHALLING_ERROR);
        }
    }

    @Override
    public TempMovementType getTempMovement(String guid) throws MovementServiceException {
        try {
            return tempMovementModel.getTempMovement(guid);
        } catch (MovementDomainException e) {
            throw new MovementServiceException("Error when getting temp movement", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    private void fireMovementEvent(TempMovementType createdMovement) {
        try {
            createdManualMovement.fire(new NotificationMessage("movementGuid", createdMovement.getGuid()));
        } catch (Exception e) {
            LOG.error("[ Error when firing notification of created temp movement. ] {}", e.getMessage());
        }
    }

    private void checkUsernameProvided(String username) {
        if(username == null || username.isEmpty()){
            throw new MovementServiceRuntimeException("Username in TempMovementRequest cannot be empty", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
    }

    private void validatePosition(MovementPoint point) throws MovementServiceException {
        if (point.getLongitude() == null || point.getLatitude() == null) {
            throw new MovementServiceRuntimeException("Longitude and/or latitude is missing.", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (Math.abs(point.getLatitude()) > 90) {
            throw new MovementServiceException("Latitude is outside range.", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (Math.abs(point.getLongitude()) > 180) {
            throw new MovementServiceException("Longitude is outside range.", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
    }
}