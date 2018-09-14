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
package eu.europa.ec.fisheries.uvms.movement.bean;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.dao.TempMovementDao;
import eu.europa.ec.fisheries.uvms.movement.entity.temp.TempMovement;
import eu.europa.ec.fisheries.uvms.movement.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainRuntimeException;
import eu.europa.ec.fisheries.uvms.movement.mapper.TempMovementMapper;
import eu.europa.ec.fisheries.uvms.movement.model.constants.TempMovementStateEnum;
import eu.europa.ec.fisheries.uvms.movement.model.dto.TempMovementsListResponseDto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
public class TempMovementDomainModelBean {

    private static final Logger LOG = LoggerFactory.getLogger(TempMovementDomainModelBean.class);

    @EJB
    private TempMovementDao dao;

    public TempMovementType createTempMovement(TempMovementType tempMovementType, String username) throws MovementDomainException {
        try {
            LOG.debug("Create temp movement.");
            TempMovement tempMovement = TempMovementMapper.toTempMovementEntity(tempMovementType, username);
            tempMovement = dao.createTempMovementEntity(tempMovement);
            return TempMovementMapper.toTempMovement(tempMovement);
        } catch (Exception e) {
            LOG.error("[ Error when creating new temp movement. ] {}", e.getMessage());
            throw new MovementDomainException("Could not create temp movement", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    public TempMovementType archiveTempMovement(String guid, String username) throws MovementDomainException {
        LOG.debug("Archiving temp movement.");
        return setTempMovementState(guid, TempMovementStateEnum.DELETED, username);
    }

    public TempMovementType sendTempMovement(String guid, String username) throws MovementDomainException {
        LOG.debug("Archiving temp movement.");
        return setTempMovementState(guid, TempMovementStateEnum.SENT, username);
    }

    public TempMovementType setTempMovementState(String guid, TempMovementStateEnum state, String username) throws MovementDomainException {
        try {
            LOG.debug("Set temp movement state.");
            if (guid == null) {
                throw new MovementDomainRuntimeException("Non valid id of temp movement to update", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
            }

            TempMovement tempMovement = dao.getTempMovementByGuid(guid);
            tempMovement.setState(state);
            tempMovement.setUpdated(DateUtil.nowUTC());
            tempMovement.setUpdatedBy(username);
            return TempMovementMapper.toTempMovement(tempMovement);
        } catch (Exception e) {
            LOG.error("[ Error when set temp movement state. ] {}", e.getMessage());
            throw new MovementDomainException("Could not set temp movement state.", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    public TempMovementType updateTempMovement(TempMovementType tempMovementType, String username) throws MovementDomainException {
        try {
            LOG.debug("Update temp movement.");

            if (tempMovementType == null) {
                throw new MovementDomainRuntimeException("No temp movement to update", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
            }
            if (tempMovementType.getGuid() == null) {
                throw new MovementDomainRuntimeException("Non valid id of temp movement to update", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
            }

            TempMovement tempMovement = dao.getTempMovementByGuid(tempMovementType.getGuid());
            tempMovement = TempMovementMapper.toExistingTempMovementEntity(tempMovement, tempMovementType, username);
            return TempMovementMapper.toTempMovement(tempMovement);
        } catch (Exception e) {
            LOG.error("[ Error when updating temp movement. ] {}", e.getMessage());
            throw new MovementDomainException("Could not update temp movement.", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }

    public TempMovementsListResponseDto getTempMovementList(MovementQuery query) throws MovementDomainException {

        try {
            if (query == null || query.getPagination() == null || query.getPagination().getPage() == null) {
                throw new MovementDomainRuntimeException("No valid query", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
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

            return response;
        } catch (Exception e) {
            LOG.error("[ Error when updating temp movement. ] {}", e.getMessage());
            throw new MovementDomainException("Could not list active temp movements.", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }

    }

    public TempMovementType getTempMovement(String guid) throws MovementDomainException {
        LOG.debug("Getting temp movement.");
        if (guid == null) {
            throw new MovementDomainRuntimeException("TempMovement GUID cannot be null.", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }

        try {
            return TempMovementMapper.toTempMovement(dao.getTempMovementByGuid(guid));	
        }
        catch (Exception e) {
        	LOG.error("[ Error when gettin temp movement by GUID. ] {}", e.getMessage());
        	throw new MovementDomainException("Could not get temp movement by GUID.", e, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }
}
