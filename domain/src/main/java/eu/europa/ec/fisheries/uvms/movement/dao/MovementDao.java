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

import com.vividsolutions.jts.io.ParseException;
import java.util.List;

import javax.ejb.Local;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.uvms.movement.entity.*;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchValue;
import java.util.Date;

@Local
public interface MovementDao {

    /**
     *
     * @param <T>
     * @param entity
     * @return
     * @throws MovementDomainException
     */
    public <T> T create(T entity) throws MovementDomainException;

    /**
     *
     * @param <T>
     * @param entity
     * @return
     * @throws MovementDomainException
     */
    public <T> T merge(T entity) throws MovementDomainException;

    /**
     *
     * @param <T>
     * @param entity
     * @return
     * @throws MovementDomainException
     */
    <T> T persist(T entity) throws MovementDomainException;

    /**
     * Get entity by internal entity id
     *
     * @param id
     * @return
     * @throws MovementDomainException
     */
    @Deprecated
    public Movement getEntityById(String id) throws MovementDomainException;

    /**
     * Get all entities (FIND_ALL)
     *
     * @return
     * @throws MovementDomainException
     */
    public List<Movement> getListAll() throws MovementDomainException;

    /**
     *
     * Gets all movements based on a prebuild PSQL query. The prebuild
     * parameters are set in the method
     *
     * @param page
     * @param listSize
     * @param sql
     * @param searchKeyValues
     * @return
     * @throws MovementDomainException
     */
    public List getMovementListPaginated(Integer page, Integer listSize, String sql, List<SearchValue> searchKeyValues) throws MovementDomainException;

    /**
     *
     * Gets all movements with minimal data based on a prebuild PSQL query. The prebuild
     * parameters are set in the method
     *
     * @param page
     * @param listSize
     * @param sql
     * @param searchKeyValues
     * @return
     * @throws MovementDomainException
     */
    public List<MinimalMovement> getMinimalMovementListPaginated(Integer page, Integer listSize, String sql, List<SearchValue> searchKeyValues) throws MovementDomainException;

    /**
     *
     * Gets all movements based on a prebuild PSQL query. The prebuild
     * parameters are set in the method
     *
     * @param sql
     * @param searchKeyValues
     * @return
     * @throws MovementDomainException
     */
    public List<Movement> getMovementList(String sql, List<SearchValue> searchKeyValues) throws MovementDomainException;

    public List<Movement> getMovementList(String sql, List<SearchValue> searchKeyValues, int numberOfReports) throws MovementDomainException;

    /**
     *
     * Gets the count of all movements based on a prebuild PSQL query. The
     * prebuild parameters are set in the method
     *
     * @param countSql
     * @param searchKeyValues
     * @return
     * @throws MovementDomainException
     * @throws com.vividsolutions.jts.io.ParseException
     */
    public Long getMovementListSearchCount(String countSql, List<SearchValue> searchKeyValues) throws MovementDomainException, ParseException;

    /**
     *
     * @param movement
     * @param movementConnect
     * @return
     * @throws MovementDomainException
     */
    public void upsertLatestMovement(Movement movement, MovementConnect movementConnect) throws MovementDomainException;

    /**
     *
     * @param movementConnectValue
     * @return
     * @throws MovementDomainException
     */

    public Movement getFirstMovement(String movementConnectValue) throws MovementDomainException;


    /**
     *
     * @param id
     * @return
     * @throws MovementDomainException
     */
    public MovementConnect getMovementConnectByConnectId(String id) throws MovementDomainException;

    /**
     *
     * @param criteria
     * @return
     * @throws MovementDomainException
     */
    List<Movement> getMovementListByAreaAndTimeInterval(MovementAreaAndTimeIntervalCriteria criteria) throws MovementDomainException;

    List<LatestMovement> getLatestMovements(Integer numberOfMovements) throws MovementDomainException;

    List<Movement> getUnprocessedMovements();

    List<Long> getUnprocessedMovementIds();

    Movement getLatestMovement(String id, Date date);

    Movement getMovementById(Long id);

    void flush() throws MovementDomainException;

    public MovementConnect createMovementConnect(MovementConnect movementConnect) throws MovementDomainException ;
}
