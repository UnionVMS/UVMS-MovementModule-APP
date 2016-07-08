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

import eu.europa.ec.fisheries.schema.movement.area.v1.AreaType;
import eu.europa.ec.fisheries.schema.movement.common.v1.SimpleResponse;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByAreaAndTimeIntervalResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.audit.model.exception.AuditModelMarshallException;
import eu.europa.ec.fisheries.uvms.longpolling.notifications.NotificationMessage;
import eu.europa.ec.fisheries.uvms.movement.message.constants.DataSourceQueue;
import eu.europa.ec.fisheries.uvms.movement.message.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.movement.message.consumer.MessageConsumer;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.mapper.AuditModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMapperException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementDataSourceRequestMapper;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementDataSourceResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedMovement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
public class MovementServiceBean implements MovementService {

    final static Logger LOG = LoggerFactory.getLogger(MovementServiceBean.class);

    @EJB
    MessageConsumer consumer;

    @EJB
    MessageProducer producer;

    @EJB
    SpatialServiceBean spatial;

    @Inject
    @CreatedMovement
    Event<NotificationMessage> createdMovementEvent;

    //TODO SET AS PARAMETER
    private static final Long CREATE_MOVEMENT_TIMEOUT = 30000L;
    private static final Long GET_MOVEMENT_MAP_TIMEOUT = 2000000L;
    private static final Long CREATE_MOVEMENT_BATCH_TIMEOUT = 1200000L;

    /**
     * {@inheritDoc}
     *
     * @param data
     * @throws MovementServiceException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public MovementType createMovement(MovementBaseType data, String username) throws MovementServiceException, MovementDuplicateException {
        LOG.info("Create invoked in service layer");
        try {

            MovementType enrichedMovement = spatial.enrichMovementWithSpatialData(data);
            String request = MovementDataSourceRequestMapper.mapCreateMovement(enrichedMovement, username);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class, CREATE_MOVEMENT_TIMEOUT);
            MovementType createdMovement = MovementDataSourceResponseMapper.mapToMovementBaseTypeFromResponse(response);
            fireMovementEvent(createdMovement);

            try {
                String auditData;
                if (MovementTypeType.MAN.equals(enrichedMovement.getMovementType())) {
                    auditData = AuditModuleRequestMapper.mapAuditLogManualMovementCreated(createdMovement.getGuid(), username);
                } else {
                    auditData = AuditModuleRequestMapper.mapAuditLogMovementCreated(createdMovement.getGuid(), username);
                }
                producer.sendModuleMessage(auditData, ModuleQueue.AUDIT);
            } catch (AuditModelMarshallException e) {
                LOG.error("Failed to send audit log message! Movement with guid {} was created ", createdMovement.getGuid());
            }
            return createdMovement;
        } catch (ModelMapperException | MovementMessageException | JMSException ex) {
            throw new MovementServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public GetMovementMapByQueryResponse getMapByQuery(MovementQuery query) throws MovementServiceException, MovementDuplicateException {
        try {
            LOG.info("Get map invoked in service layer");
            String request = MovementDataSourceRequestMapper.mapGetMapByQuery(query);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class, GET_MOVEMENT_MAP_TIMEOUT);
            if (response == null) {
                LOG.error("[ Error when getting map, response from JMS Queue is null ]");
                throw new MovementServiceException("[ Error when getting map, response from JMS Queue is null ]");
            }
            return MovementDataSourceResponseMapper.mapToMovementMapResponse(response);
        } catch (ModelMarshallException | MovementMessageException | JMSException ex) {
            LOG.error("[ Error when getting movement map by query ] {}", ex.getMessage());
            throw new MovementServiceException("[ Error when getting movement map by query ]", ex);
        } catch (ModelMapperException ex) {
            LOG.error("[ Error when getting movement list by query ] {}", ex.getMessage());
            throw new MovementServiceException("[ Error when getting movement map by query ]", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return
     * @throws MovementServiceException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public GetMovementListByQueryResponse getList(MovementQuery query) throws MovementServiceException, MovementDuplicateException {
        try {
            LOG.info("Get list invoked in service layer");
            String request = MovementDataSourceRequestMapper.mapGetListByQuery(query);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class, CREATE_MOVEMENT_TIMEOUT);
            if (response == null) {
                LOG.error("[ Error when getting list, response from JMS Queue is null ]");
                throw new MovementServiceException("[ Error when getting list, response from JMS Queue is null ]");
            }
            return MovementDataSourceResponseMapper.mapToGetMovementListByQueryResponse(response);
        } catch (ModelMarshallException | MovementMessageException | JMSException ex) {
            LOG.error("[ Error when getting movement list by query ] {}", ex.getMessage());
            throw new MovementServiceException("[ Error when getting movement list by query ]", ex);
        } catch (ModelMapperException ex) {
            LOG.error("[ Error when getting movement list by query ] {}", ex.getMessage());
            throw new MovementServiceException("[ Error when getting movement list by query ]", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return
     * @throws MovementServiceException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public MovementListResponseDto getListAsRestDto(MovementQuery query) throws MovementServiceException, MovementDuplicateException {
        try {
            LOG.info("Get list invoked in service layer");
            String request = MovementDataSourceRequestMapper.mapGetListByQuery(query);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class, CREATE_MOVEMENT_TIMEOUT);
            if (response == null) {
                LOG.error("[ Error when getting list, response from JMS Queue is null ]");
                throw new MovementServiceException("[ Error when getting list, response from JMS Queue is null ]");
            }
            GetMovementListByQueryResponse mappedResponse = MovementDataSourceResponseMapper.mapToGetMovementListByQueryResponse(response);
            return MovementMapper.mapToMovementListDto(mappedResponse);
        } catch (ModelMarshallException | MovementMessageException | JMSException ex) {
            LOG.error("[ Error when getting movement list by query ] {}", ex.getMessage());
            throw new MovementServiceException("[ Error when getting movement list by query ]", ex);
        } catch (ModelMapperException ex) {
            LOG.error("[ Error when getting movement list by query ] {}", ex.getMessage());
            throw new MovementServiceException("[ Error when getting movement list by query ]", ex);
        }

    }

    /**
     * {@inheritDoc}
     *
     * @param id
     * @return
     * @throws MovementServiceException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public MovementType getById(String id) throws MovementServiceException, MovementDuplicateException {
        try {
            LOG.info("Get list invoked in service layer");
            String request = MovementDataSourceRequestMapper.mapGetMovementByGUID(id);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class, CREATE_MOVEMENT_TIMEOUT);

            if (response == null) {
                LOG.error("[ Error when getting list, response from JMS Queue is null ]");
                throw new MovementServiceException("[ Error when getting list, response from JMS Queue is null ]");
            }

            return MovementDataSourceResponseMapper.mapToGetMovementByGUIDResponse(response);
        } catch (ModelMarshallException | MovementMessageException | JMSException ex) {
            LOG.error("[ Error when getting movement by guid ] {}", ex.getMessage());
            throw new MovementServiceException("[ Error when getting movement by guid]", ex);
        } catch (ModelMapperException ex) {
            LOG.error("[ Error when getting movement by guid ] {}", ex.getMessage());
            throw new MovementServiceException("[ Error when getting movement by guid]", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param data
     * @return
     * @throws MovementServiceException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Object update(Object data) throws MovementServiceException {
        LOG.info("Update invoked in service layer");
        throw new MovementServiceException("Update not implemented in service layer");
    }

    private void fireMovementEvent(MovementBaseType createdMovement) {
        try {
            createdMovementEvent.fire(new NotificationMessage("movementGuid", createdMovement.getGuid()));
        } catch (Exception e) {
            LOG.error("[ Error when firing notification of created temp movement. ] {}", e.getMessage());
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public SimpleResponse createMovementBatch(List<MovementBaseType> query) throws MovementServiceException, MovementDuplicateException {
        LOG.info("Create invoked in service layer");
        try {

            LOG.debug("ENRICHING MOVEMENTS WITH SPATIAL DATA");

            List<MovementType> enrichedMovements = new ArrayList<>();

            for (MovementBaseType movement : query) {
                MovementType enrichedMovement = spatial.enrichMovementWithSpatialData(movement);
                enrichedMovements.add(enrichedMovement);
            }

            String request = MovementDataSourceRequestMapper.mapCreateMovementBatch(enrichedMovements);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class, CREATE_MOVEMENT_BATCH_TIMEOUT);
            SimpleResponse createdMovement = MovementDataSourceResponseMapper.mapToSimpleResponseFromCreateMovementBatchResponse(response);

            try {
                String auditData = AuditModuleRequestMapper.mapAuditLogMovementCreated(createdMovement.name(), "UVMS batch movement");
                producer.sendModuleMessage(auditData, ModuleQueue.AUDIT);
            } catch (AuditModelMarshallException e) {
                LOG.error("Failed to send audit log message! Movement batch {} was created with outcome: ", createdMovement.name());
            }

            return createdMovement;
        } catch (ModelMapperException | MovementMessageException | JMSException ex) {
            throw new MovementServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<MovementDto> getLatestMovementsByConnectIds(List<String> connectIds) throws MovementServiceException, MovementDuplicateException {
        LOG.info("GetLatestMovementsByConnectIds invoked in service layer");
        try {
            String request = MovementDataSourceRequestMapper.mapGetLatestMovementsByConnectIds(connectIds);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class, CREATE_MOVEMENT_TIMEOUT);
            List<MovementType> latestMovements = MovementDataSourceResponseMapper.mapToLatestMovementsByConnectIdResponse(response);
            return MovementMapper.mapToMovementDtoList(latestMovements);
        } catch (ModelMapperException | MovementMessageException | JMSException ex) {
            throw new MovementServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public GetMovementListByAreaAndTimeIntervalResponse getMovementListByAreaAndTimeInterval(MovementAreaAndTimeIntervalCriteria criteria) throws MovementServiceException, MovementDuplicateException {
        try {
            LOG.info("Get list invoked in service layer");
            String request = MovementDataSourceRequestMapper.mapGetListByAreaAndTimeIntervalRequest(criteria);
            String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
            TextMessage response = consumer.getMessage(messageId, TextMessage.class, CREATE_MOVEMENT_TIMEOUT);
            if (response == null) {
                LOG.error("[ Error when getting list, response from JMS Queue is null ]");
                throw new MovementServiceException("[ Error when getting list, response from JMS Queue is null ]");
            }
            return MovementDataSourceResponseMapper.mapToGetMovementListByAreaAndTimeIntervalResponse(response);
        } catch (ModelMarshallException | MovementMessageException | JMSException ex) {
            LOG.error("[ Error when getting movement list by query ] {}", ex.getMessage());
            throw new MovementServiceException("[ Error when getting movement list by query ]", ex);
        } catch (ModelMapperException ex) {
            LOG.error("[ Error when getting movement list by query ] {}", ex.getMessage());
            throw new MovementServiceException("[ Error when getting movement list by query ]", ex);
        }
    }

	@Override
	public List<AreaType> getAreas() throws MovementServiceException, MovementDuplicateException {
		try {
			String request = MovementDataSourceRequestMapper.mapGetAreasRequest();
			String messageId = producer.sendDataSourceMessage(request, DataSourceQueue.INTERNAL);
			TextMessage response = consumer.getMessage(messageId, TextMessage.class, CREATE_MOVEMENT_TIMEOUT);
			return MovementDataSourceResponseMapper.getAreasFromResponse(response);
		} catch (MovementMessageException | ModelMapperException | JMSException e) {
			LOG.error("[ Error when getting areas. ] {}", e.getMessage());
			throw new MovementServiceException("[ Error when getting areas. ]", e);
		}
	}
}