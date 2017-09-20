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


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.movement.dao.TempMovementDao;
import eu.europa.ec.fisheries.uvms.movement.entity.temp.TempMovement;
import eu.europa.ec.fisheries.uvms.movement.mapper.TempMovementMapper;

@Stateless
@LocalBean
public class TempMovementDomainModelBean {


    private static final Logger LOG = LoggerFactory.getLogger(TempMovementDomainModelBean.class);

    @EJB
    TempMovementDao dao;

    public TempMovementType createTempMovement(final TempMovementType tempMovementType, final String username) throws MovementModelException {
        try {
            LOG.debug("Create temp movement.");
            TempMovement tempMovement = TempMovementMapper.toTempMovementEntity(tempMovementType, username);
            tempMovement = dao.createTempMovementEntity(tempMovement);
            return TempMovementMapper.toTempMovement(tempMovement);
        } catch (final MovementDaoException e) {
            LOG.error("[ Error when creating new temp movement. ] {}", e.getMessage());
            throw new MovementModelException("Could not create temp movement. " + e.getMessage(), e);
        }
    }

    public TempMovementType archiveTempMovement(final String guid, final String username) throws MovementModelException {
        LOG.debug("Archiving temp movement.");
        return setTempMovementState(guid, TempMovementStateEnum.DELETED, username);
    }

    public TempMovementType sendTempMovement(final String guid, final String username) throws MovementModelException {
        LOG.debug("Archiving temp movement.");
        return setTempMovementState(guid, TempMovementStateEnum.SENT, username);
    }

    public TempMovementType setTempMovementState(final String guid, final TempMovementStateEnum state, final String username) throws MovementModelException {
        try {
            LOG.debug("Set temp movement state.");
            if (guid == null) {
                throw new InputArgumentException("Non valid id of temp movement to update");
            }

            final TempMovement tempMovement = dao.getTempMovementByGuid(guid);
            tempMovement.setState(state);
            tempMovement.setUpdated(DateUtil.nowUTC());
            tempMovement.setUpdatedBy(username);
            return TempMovementMapper.toTempMovement(tempMovement);
        } catch (final MovementDaoException e) {
            LOG.error("[ Error when set temp movement state. ] {}", e.getMessage());
            throw new MovementModelException("Could not set temp movement state.", e);
        }
    }

    public TempMovementType updateTempMovement(final TempMovementType tempMovementType, final String username) throws MovementModelException {
        try {
            LOG.debug("Update temp movement.");

            if (tempMovementType == null) {
                throw new InputArgumentException("No temp movement to update");
            }
            if (tempMovementType.getGuid() == null) {
                throw new InputArgumentException("Non valid id of temp movement to update");
            }

            TempMovement tempMovement = dao.getTempMovementByGuid(tempMovementType.getGuid());
            tempMovement = TempMovementMapper.toExistingTempMovementEntity(tempMovement, tempMovementType, username);
            return TempMovementMapper.toTempMovement(tempMovement);
        } catch (final MovementDaoException e) {
            LOG.error("[ Error when updating temp movement. ] {}", e.getMessage());
            throw new MovementModelException("Could not update temp movement.", e);
        }
    }

    public TempMovementsListResponseDto getTempMovementList(final MovementQuery query) throws MovementModelException {

        try {
            if (query == null || query.getPagination() == null || query.getPagination().getPage() == null) {
                throw new InputArgumentException("No valid query");
            }

        	
            final TempMovementsListResponseDto response = new TempMovementsListResponseDto();
            final List<TempMovementType> tempMovementList = new ArrayList<>();

            final Integer page = query.getPagination().getPage().intValue();
            final Integer listSize = query.getPagination().getListSize().intValue();

            final List<TempMovement> tempMovementEntityList = dao.getTempMovementListPaginated(page, listSize);
            for (final TempMovement entity : tempMovementEntityList) {
                tempMovementList.add(TempMovementMapper.toTempMovement(entity));
            }

            final Long numberMatches = dao.getTempMovementListCount();
            int numberOfPages = (int) (numberMatches / listSize);
            if (numberMatches % listSize != 0) {
                numberOfPages += 1;
            }

            response.setTotalNumberOfPages(new BigInteger("" + numberOfPages));
            response.setCurrentPage(query.getPagination().getPage());
            response.setTempMovementList(tempMovementList);

            return response;
        } catch (final MovementDaoException e) {
            LOG.error("[ Error when updating temp movement. ] {}", e.getMessage());
            throw new MovementModelException("Could not list active temp movements.", e);
        }

    }

    public TempMovementType getTempMovement(final String guid) throws MovementModelException {
        LOG.debug("Getting temp movement.");
        if (guid == null) {
            throw new InputArgumentException("TempMovement GUID cannot be null.");
        }

        try {
            return TempMovementMapper.toTempMovement(dao.getTempMovementByGuid(guid));	
        }
        catch (final MovementDaoException e) {
        	LOG.error("[ Error when gettin temp movement by GUID. ] {}", e.getMessage());
        	throw new MovementModelException("Could not get temp movement by GUID.", e);
        }
    }

}