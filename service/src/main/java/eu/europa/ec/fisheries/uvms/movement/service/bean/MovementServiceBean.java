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

import javax.ejb.*;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementMapResponseType;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByAreaAndTimeIntervalResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.model.MovementBatchModel;
import eu.europa.ec.fisheries.uvms.movement.model.MovementDomainModel;
import eu.europa.ec.fisheries.uvms.movement.model.dto.ListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.model.exception.*;
import eu.europa.ec.fisheries.uvms.movement.service.SpatialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.audit.model.exception.AuditModelMarshallException;
import eu.europa.ec.fisheries.uvms.longpolling.notifications.NotificationMessage;
import eu.europa.ec.fisheries.uvms.movement.message.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.mapper.AuditModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementDataSourceResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedMovement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import java.util.ArrayList;
import java.util.List;

@LocalBean
@Stateless
public class MovementServiceBean implements MovementService {

    final static Logger LOG = LoggerFactory.getLogger(MovementServiceBean.class);

    @EJB
    MessageProducer producer;

    @EJB
    SpatialService spatial;

    //@EJB(lookup = LookupConstant.BATCH_MODEL_BEAN)
    @EJB
    MovementBatchModel movementBatch;

    //@EJB(lookup = LookupConstant.DOMAIN_MODEL_BEAN)
    @EJB
    MovementDomainModel model;

    @Inject
    @CreatedMovement
    Event<NotificationMessage> createdMovementEvent;

    /**
     * {@inheritDoc}
     *
     * @param data
     * @throws MovementServiceException
     */
    @Override
    public MovementType createMovement(MovementBaseType data, String username) {
        LOG.info("Create invoked in service layer");
        try {

            MovementType enrichedMovement = spatial.enrichMovementWithSpatialData(data);
            MovementType createdMovement = movementBatch.createMovement(enrichedMovement, username);
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
        } catch (MovementServiceException | MovementMessageException  ex) {
            throw new EJBException(ex);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public GetMovementMapByQueryResponse getMapByQuery(MovementQuery query) throws MovementServiceException, MovementDuplicateException {
        try {
            LOG.info("Get map invoked in service layer");
            List<MovementMapResponseType> mapResponse = model.getMovementMapByQuery(query);
            if (mapResponse == null) {
                LOG.error("[ Error when getting map, response from JMS Queue is null ]");
                throw new MovementServiceException("[ Error when getting map, response from JMS Queue is null ]");
            }
            return MovementDataSourceResponseMapper.createMovementMapResponse(mapResponse);
        } catch (ModelMarshallException | MovementModelException ex) {
            LOG.error("[ Error when getting movement map by query ] {}", ex.getMessage());
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
    public GetMovementListByQueryResponse getList(MovementQuery query) throws MovementServiceException, MovementDuplicateException {
        try {
            LOG.info("Get list invoked in service layer");
            ListResponseDto response = model.getMovementListByQuery(query);
            if (response == null) {
                LOG.error("[ Error when getting list, response from JMS Queue is null ]");
                throw new MovementServiceException("[ Error when getting list, response from JMS Queue is null ]");
            }
            return MovementDataSourceResponseMapper.createMovementListResponse(response);
        } catch (MovementModelException | ModelMapperException ex) {
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
    public GetMovementListByQueryResponse getMinimalList(MovementQuery query) throws MovementServiceException, MovementDuplicateException {
        try {
            LOG.info("Get list invoked in service layer");
            ListResponseDto response = model.getMinimalMovementListByQuery(query);
            if (response == null) {
                LOG.error("[ Error when getting list, response from JMS Queue is null ]");
                throw new MovementServiceException("[ Error when getting list, response from JMS Queue is null ]");
            }
            return MovementDataSourceResponseMapper.createMovementListResponse(response);
        } catch (MovementModelException | ModelMapperException ex) {
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
            ListResponseDto response = model.getMovementListByQuery(query);
            if (response == null) {
                LOG.error("[ Error when getting list, response from JMS Queue is null ]");
                throw new MovementServiceException("[ Error when getting list, response from JMS Queue is null ]");
            }
            GetMovementListByQueryResponse mappedResponse = MovementDataSourceResponseMapper.createMovementListResponse(response);
            return MovementMapper.mapToMovementListDto(mappedResponse);
        } catch (MovementModelException ex) {
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
    public MovementType getById(String id) throws MovementServiceException {
        try {
            LOG.info("Get list invoked in service layer");
            MovementType response = model.getMovementByGUID(id);

            if (response == null) {
                LOG.error("[ Error when getting list, response from JMS Queue is null ]");
                throw new MovementServiceException("[ Error when getting list, response from JMS Queue is null ]");
            }

            return response;
        } catch (MovementModelException ex) {
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
    public SimpleResponse createMovementBatch(List<MovementBaseType> query) {
        LOG.debug("Create invoked in service layer");
        try {

            LOG.debug("ENRICHING MOVEMENTS WITH SPATIAL DATA");

            List<MovementType> enrichedMovements = new ArrayList<>();

            for (MovementBaseType movement : query) {
                MovementType enrichedMovement = spatial.enrichMovementWithSpatialData(movement);
                enrichedMovements.add(enrichedMovement);
            }

            SimpleResponse createdMovement = SimpleResponse.OK;
            for (MovementType movement : enrichedMovements) {
                movementBatch.createMovement(movement, "Batch movement");
            }

            String auditData = AuditModuleRequestMapper.mapAuditLogMovementCreated(createdMovement.name(), "UVMS batch movement");
            producer.sendModuleMessage(auditData, ModuleQueue.AUDIT);

            return createdMovement;
        } catch (MovementServiceException | AuditModelMarshallException | MovementMessageException ex) {
            throw new EJBException("createMovementBatch failed", ex);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<MovementDto> getLatestMovementsByConnectIds(List<String> connectIds) throws MovementServiceException, MovementDuplicateException {
        LOG.info("GetLatestMovementsByConnectIds invoked in service layer");
        try {
            List<MovementType> latestMovements = model.getLatestMovementsByConnectIds(connectIds);
            return MovementMapper.mapToMovementDtoList(latestMovements);
        } catch (MovementModelException ex) {
            throw new MovementServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<MovementDto> getLatestMovements(Integer numberOfMovements) throws MovementServiceException, MovementDuplicateException {
        LOG.info("getLatestMovements invoked in service layer");
        try {
            List<MovementType> latestMovements = model.getLatestMovements(numberOfMovements);
            return MovementMapper.mapToMovementDtoList(latestMovements);
        } catch (MovementModelException ex) {
            throw new MovementServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public GetMovementListByAreaAndTimeIntervalResponse getMovementListByAreaAndTimeInterval(MovementAreaAndTimeIntervalCriteria criteria) throws MovementServiceException, MovementDuplicateException {
        try {
            LOG.info("Get list invoked in service layer");
            List<MovementType> movementListByAreaAndTimeInterval = model.getMovementListByAreaAndTimeInterval(criteria);
            if (movementListByAreaAndTimeInterval == null) {
                LOG.error("[ Error when getting list, response from JMS Queue is null ]");
                throw new MovementServiceException("[ Error when getting list, response from JMS Queue is null ]");
            }
            return MovementDataSourceResponseMapper.mapMovementListAreaAndTimeIntervalResponse(movementListByAreaAndTimeInterval);
        } catch (MovementDaoException | ModelMarshallException ex) {
            LOG.error("[ Error when getting movement list by query ] {}", ex.getMessage());
            throw new MovementServiceException("[ Error when getting movement list by query ]", ex);
        }
    }

	@Override
	public List<AreaType> getAreas() throws MovementServiceException, MovementDuplicateException {
		try {
			return model.getAreas();
		} catch (MovementModelException e) {
			LOG.error("[ Error when getting areas. ] {}", e.getMessage());
			throw new MovementServiceException("[ Error when getting areas. ]", e);
		}
	}
}