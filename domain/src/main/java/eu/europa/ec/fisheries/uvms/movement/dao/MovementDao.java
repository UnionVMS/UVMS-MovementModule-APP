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
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchValue;
import java.util.Date;

@Local
public interface MovementDao {

    /**
     *
     * @param <T>
     * @param entity
     * @return
     * @throws MovementDaoException
     */
    public <T> T create(T entity) throws MovementDaoException;

    /**
     *
     * @param <T>
     * @param entity
     * @return
     * @throws MovementDaoException
     */
    public <T> T merge(T entity) throws MovementDaoException;

    /**
     *
     * @param <T>
     * @param entity
     * @return
     * @throws MovementDaoException
     */
    <T> T persist(T entity) throws MovementDaoException;

    /**
     * Get entity by internal entity id
     *
     * @param id
     * @param entityId
     * @return
     * @throws MovementDaoException
     */
    @Deprecated
    public Movement getEntityById(String id) throws MovementDaoException;

    /**
     * Get all entities (FIND_ALL)
     *
     * @return
     * @throws MovementDaoException
     */
    public List<Movement> getListAll() throws MovementDaoException;

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
     * @throws MovementDaoException
     */
    public List<Movement> getMovementListPaginated(Integer page, Integer listSize, String sql, List<SearchValue> searchKeyValues) throws MovementDaoException;

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
     * @throws MovementDaoException
     */
    public List<MinimalMovement> getMinimalMovementListPaginated(Integer page, Integer listSize, String sql, List<SearchValue> searchKeyValues) throws MovementDaoException;

    /**
     *
     * Gets all movements based on a prebuild PSQL query. The prebuild
     * parameters are set in the method
     *
     * @param listSize
     * @param sql
     * @param searchKeyValues
     * @return
     * @throws MovementDaoException
     */
    public List<Movement> getMovementList(String sql, List<SearchValue> searchKeyValues) throws MovementDaoException;

    public List<Movement> getMovementList(String sql, List<SearchValue> searchKeyValues, int numberOfReports) throws MovementDaoException;

    /**
     *
     * Gets the count of all movements based on a prebuild PSQL query. The
     * prebuild parameters are set in the method
     *
     * @param countSql
     * @param searchKeyValues
     * @return
     * @throws MovementDaoException
     * @throws com.vividsolutions.jts.io.ParseException
     */
    public Long getMovementListSearchCount(String countSql, List<SearchValue> searchKeyValues) throws MovementDaoException, ParseException;

    /**
     *
     * @param id
     * @return
     * @throws MovementDaoException
     */
    public void upsertLatestMovement(Movement movement, MovementConnect movementConnect) throws MovementDaoException;

    /**
     *
     * @param movementConnectValue
     * @return
     * @throws MovementDaoException
     */

    public Movement getFirstMovement(String movementConnectValue) throws MovementDaoException;


    /**
     *
     * @param id
     * @return
     * @throws MovementDaoException
     */
    public MovementConnect getMovementConnectByConnectId(String id) throws MovementDaoException;

    /**
     *
     * @param criteria
     * @return
     * @throws MovementDaoException
     */
    List<Movement> getMovementListByAreaAndTimeInterval(MovementAreaAndTimeIntervalCriteria criteria) throws MovementDaoException;

    List<LatestMovement> getLatestMovements(Integer numberOfMovements) throws MovementDaoException;

    List<Movement> getUnprocessedMovements() throws javax.persistence.NoResultException;

    List<Long> getUnprocessedMovementIds() throws javax.persistence.NoResultException;

    Movement getLatestMovement(String id, Date date);

    Movement getMovementById(Long id);

    void flush() throws MovementDaoException;

    public MovementConnect createMovementConnect(MovementConnect movementConnect) throws MovementDaoException ;



    }