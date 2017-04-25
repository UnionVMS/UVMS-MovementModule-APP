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

import eu.europa.ec.fisheries.schema.movement.area.v1.AreaType;
import eu.europa.ec.fisheries.schema.movement.common.v1.SimpleResponse;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByAreaAndTimeIntervalResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import java.util.List;
import javax.ejb.Local;

@Local
public interface MovementService {

    /**
     * Create/Insert data into database
     *
     * @param data
     * @return
     */
    MovementType createMovement(MovementBaseType data, String username);

    /**
     * Get a list with data
     *
     * @param query
     * @return
     * @throws MovementServiceException
     */
    GetMovementListByQueryResponse getList(MovementQuery query) throws MovementServiceException, MovementDuplicateException;

    /**
     * Get a list with data
     *
     * @param query
     * @return
     * @throws MovementServiceException
     */
    GetMovementListByQueryResponse getMinimalList(MovementQuery query) throws MovementServiceException, MovementDuplicateException;

    /**
     * Get a list with data
     *
     * @param query
     * @return
     * @throws MovementServiceException
     */
    GetMovementMapByQueryResponse getMapByQuery(MovementQuery query) throws MovementServiceException, MovementDuplicateException;

    /**
     *
     * @param query
     * @return
     * @throws MovementServiceException
     */
    MovementListResponseDto getListAsRestDto(MovementQuery query) throws MovementServiceException, MovementDuplicateException;

    /**
     *
     * @param query
     * @return
     */
    SimpleResponse createMovementBatch(List<MovementBaseType> query);

    /**
     * Get an object by id
     *
     * @param id
     * @return
     * @throws MovementServiceException
     */
    MovementType getById(String id) throws MovementServiceException, MovementDuplicateException;

    /**
     * Update an object
     *
     * @param data
     * @return
     * @throws MovementServiceException
     */
    Object update(Object data) throws MovementServiceException, MovementDuplicateException;

    /**
     *
     * @param connectIds
     * @return
     * @throws MovementServiceException
     */
    List<MovementDto> getLatestMovementsByConnectIds(List<String> connectIds) throws MovementServiceException, MovementDuplicateException;
    /**
     *
     * @param numberOfMovements
     * @return
     * @throws MovementServiceException
     */
    List<MovementDto> getLatestMovements(Integer numberOfMovements) throws MovementServiceException, MovementDuplicateException;

    /**
     *
     * @param criteria
     * @return GetMovementListByAreaAndTimeIntervalResponse
     * @throws MovementServiceException
     */
    GetMovementListByAreaAndTimeIntervalResponse getMovementListByAreaAndTimeInterval(MovementAreaAndTimeIntervalCriteria criteria) throws MovementServiceException, MovementDuplicateException;

    /**
     * @return list of existing areas
     * @throws MovementServiceException if unsuccessful
     */
    List<AreaType> getAreas() throws MovementServiceException, MovementDuplicateException;

}