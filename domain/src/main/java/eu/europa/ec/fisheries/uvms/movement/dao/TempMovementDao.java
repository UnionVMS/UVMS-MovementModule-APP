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
package eu.europa.ec.fisheries.uvms.movement.dao;

import java.util.List;

import javax.ejb.Local;

import eu.europa.ec.fisheries.uvms.movement.entity.temp.TempMovement;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;

@Local
public interface TempMovementDao {

    /**
     * @param tempMovement
     * @return
     */
    TempMovement createTempMovementEntity(TempMovement tempMovement) throws MovementDomainException;

    /**
     * @param guid
     * @return The temp movement
     * @throws MovementDomainException
     */
    TempMovement getTempMovementByGuid(String guid) throws MovementDomainException;

    /**
     * @param page
     * @param listSize
     * @return All active temp movements
     * @throws MovementDomainException
     */
    List<TempMovement> getTempMovementListPaginated(Integer page, Integer listSize) throws MovementDomainException;

    /**
     * @return Number of active temp movements
     * @throws MovementDomainException
     */
    Long getTempMovementListCount() throws MovementDomainException;
}
