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
package eu.europa.ec.fisheries.uvms.movement.service;

import javax.ejb.Local;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetTempMovementListResponse;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

@Local
public interface TempMovementService {

    TempMovementType createTempMovement(TempMovementType tempMovementType, String username) throws MovementServiceException, MovementDuplicateException;

    TempMovementType getTempMovement(String guid) throws MovementServiceException, MovementDuplicateException;

    TempMovementType setStatusTempMovement(String guid, String username) throws MovementServiceException, MovementDuplicateException;

    TempMovementType updateTempMovement(TempMovementType tempMovementType, String username) throws MovementServiceException, MovementDuplicateException;

    GetTempMovementListResponse getTempMovements(MovementQuery query) throws MovementServiceException, MovementDuplicateException;

    TempMovementType sendTempMovement(String guid, String username) throws MovementServiceException, MovementDuplicateException;

}