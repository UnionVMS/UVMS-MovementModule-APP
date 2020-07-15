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
package eu.europa.ec.fisheries.uvms.movement.service.dao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import eu.europa.ec.fisheries.schema.movement.area.v1.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.*;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Movementarea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceRuntimeException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchValue;
import eu.europa.ec.fisheries.uvms.movement.service.util.WKTUtil;

@Stateless
public class MovementDao {

    private static final Logger LOG = LoggerFactory.getLogger(MovementDao.class);

    @PersistenceContext
    private EntityManager em;

    @Inject
    private AreaDao areaDao;

    public List<Geometry> getPointsFromTrack(Track track) {
        TypedQuery<Geometry> query = em.createNamedQuery(Movement.FIND_ALL_LOCATIONS_BY_TRACK, Geometry.class);
        query.setParameter("track", track);
        return query.getResultList();
    }

    public List<Movement> getMovementListByMovementConnect(MovementConnect movementConnect) {
        TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_ALL_BY_MOVEMENTCONNECT, Movement.class);
        query.setParameter("movementConnect", movementConnect);
        return query.getResultList();
    }

    public List<Movement> getMovementsByTrack(Track track) {
        TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_ALL_BY_TRACK, Movement.class);
        query.setParameter("track", track);
        return query.getResultList();
    }

    public List<Segment> getSegmentsByTrack(Track track) {
        TypedQuery<Segment> query = em.createNamedQuery(Segment.FIND_ALL_BY_TRACK, Segment.class);
        query.setParameter("track", track);
        return query.getResultList();
    }

    public Track getTrackById(Long id){
        return em.find(Track.class, id);
    }

    public Movement getMovementByGUID(String guid) {
        try {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_BY_GUID, Movement.class);
            query.setParameter("guid", guid);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("No result when retrieving movements by GUID: {}", guid);
            return null;
        }
    }

    public List<Movement> findMovementsByGUIDList(List<String> guidList) {
        TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_BY_GUID_LIST, Movement.class);
        query.setParameter("guidList", guidList);
        return query.getResultList();
    }

    public Movement getMovementById(Long id) {
        return em.find(Movement.class, id);
    }

    public List<Movement> getLatestMovementsByConnectIdList(List<String> connectIds) {
        List<Movement> resultList = new ArrayList<>();
        if (connectIds == null || connectIds.isEmpty()) {
            return resultList;
        }
        TypedQuery<LatestMovement> latestMovementQuery =
                em.createNamedQuery(LatestMovement.FIND_LATEST_BY_MOVEMENT_CONNECT_LIST, LatestMovement.class);
        latestMovementQuery.setParameter("connectId", connectIds);
        for (LatestMovement lm : latestMovementQuery.getResultList()) {
            resultList.add(lm.getMovement());
        }
        return resultList;
    }

    public List<Movement> getLatestMovementsByConnectId(String connectId, Integer amount) {
        if(amount < 1) {
            throw new MovementServiceRuntimeException("Amount can't have 0 or negative value.", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        } else if (amount == 1) {
            LatestMovement latestMovement = getLatestMovement(connectId);
            if(latestMovement != null && latestMovement.getMovement() != null)
                return Collections.singletonList(latestMovement.getMovement());
            else
                return Collections.emptyList();
        } else {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_LATEST_BY_MOVEMENT_CONNECT, Movement.class);
            query.setParameter("connectId", connectId);
            query.setMaxResults(amount);
            return query.getResultList();
        }
    }

    public List<Movement> isDateAlreadyInserted(String id, Instant date) {
        TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_EXISTING_DATE, Movement.class);
        query.setParameter("date", date);
        query.setParameter("id", id);
        return query.getResultList();
    }

    public List<LatestMovement> getLatestMovements(Integer numberOfMovements) {
        TypedQuery<LatestMovement> latestMovementQuery = em.createNamedQuery(LatestMovement.FIND_LATEST, LatestMovement.class);
        latestMovementQuery.setMaxResults(numberOfMovements);
        return latestMovementQuery.getResultList();
    }

    public Movement getPreviousMovement(String id, Instant date) {
        Movement singleResult = null;
        try {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_PREVIOUS, Movement.class);
            query.setParameter("id", id);
            query.setParameter("date", date);
            singleResult = query.getSingleResult();
        }catch (NoResultException e){
            LOG.debug("No previous movement found for date: {} and connectedId: {}", date, id);
        }
        return singleResult;
    }

    public void upsertLatestMovement(Movement movement, MovementConnect movementConnect) {
        LatestMovement latestMovement = getLatestMovement(movementConnect.getValue());
        if (latestMovement == null) {
            latestMovement = new LatestMovement();
            latestMovement.setMovementConnect(movementConnect);
            latestMovement.setMovement(movement);
            latestMovement.setTimestamp(movement.getTimestamp());
            em.persist(latestMovement);
        } else if (latestMovement.getTimestamp().isBefore(movement.getTimestamp())) {
            latestMovement.setMovement(movement);
            latestMovement.setTimestamp(movement.getTimestamp());
        }
    }

    private LatestMovement getLatestMovement(String connectId) {
        try {
            TypedQuery<LatestMovement> latestMovementQuery = em.createNamedQuery(LatestMovement.FIND_LATEST_BY_MOVEMENT_CONNECT, LatestMovement.class);
            latestMovementQuery.setParameter("connectId", connectId);
            return latestMovementQuery.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public Movement getFirstMovement(String movementConnectValue) {
        try {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_FIRST, Movement.class);
            query.setParameter("id", movementConnectValue);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("Could not get first position, no result of id: {}", movementConnectValue);
            return null;
        }
    }

    public long countNrOfMovementsForAssetBetween(String asset, Instant from, Instant to){
        try{
            Query query = em.createNamedQuery(Movement.NR_OF_MOVEMENTS_FOR_ASSET_IN_TIMESPAN);
            query.setParameter("asset", asset);
            query.setParameter("fromDate", from);
            query.setParameter("toDate", to);

            Long count = (Long)query.getSingleResult();
            return count;
        }catch (NoResultException e) {
            LOG.debug("No valid position in DB for {}, between {} and {}", asset, from, to);
            return 0;
        }
    }

    public <T> List<T> getMovementListPaginated(Integer page, Integer listSize, String sql, List<SearchValue> searchKeyValues, Class<T> clazz) throws MovementServiceException {
        try {
            TypedQuery<T> query = getMovementQuery(sql, searchKeyValues, clazz);
            query.setFirstResult(listSize * (page - 1));
            query.setMaxResults(listSize);
            return query.getResultList();
        } catch (IllegalArgumentException e) {
            throw new MovementServiceRuntimeException("Error when getting paginated list", e, ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        } catch (Exception e) {
            throw new MovementServiceException("Error when getting paginated list", e, ErrorCode.DATA_RETRIEVING_ERROR);
        }
    }

    private <T> TypedQuery<T> getMovementQuery(String sql, List<SearchValue> searchKeyValues, Class<T> clazz) throws ParseException {
        TypedQuery<T> query = em.createQuery(sql, clazz);
        setTypedQueryMovementParams(searchKeyValues, query);
        return query;
    }

    public Long getMovementListSearchCount(String countSql, List<SearchValue> searchKeyValues) throws ParseException {
        TypedQuery<Long> query = em.createQuery(countSql, Long.class);
        setTypedQueryMovementParams(searchKeyValues, query);
        return query.getSingleResult();
    }

    public List<Long> findMovementAreaIdsByAreaRemoteIdAndNameList(List<AreaType> areaTypes)  {

        StringBuilder queryBuilder = new StringBuilder("SELECT ma.movareaAreaId.areaId FROM Movementarea ma WHERE ");
        for(int i=0; i < areaTypes.size(); i++){
            queryBuilder.append("(ma.movareaAreaId.remoteId=:remoteId").append(i).append(" AND ma.movareaAreaId.areaType.name=:areaName").append(i).append(") OR ");
        }
        TypedQuery<Long> typedQuery = em.createQuery(queryBuilder.substring(0,queryBuilder.lastIndexOf(" OR ")), Long.class);

        for(int i=0; i < areaTypes.size(); i++){
            AreaType areaType = areaTypes.get(i);
            typedQuery.setParameter("remoteId"+i, String.valueOf(areaType.getAreaId()));
            typedQuery.setParameter("areaName"+i, areaType.getAreaName());
        }

        return typedQuery.getResultList();
    }

    public boolean checkMovementExistence(String connectId, Date startDate, Date endDate, List<Long> movementAreaIds)  {

        TypedQuery<Movement> typedQuery = em.createNamedQuery(Movement.FIND_BY_CONNECTID_FOR_AREAS_IN_A_PERIOD, Movement.class).setMaxResults(1);
        typedQuery.setParameter("connectId", connectId);
        typedQuery.setParameter("startDate", startDate.toInstant());
        typedQuery.setParameter("endDate", endDate.toInstant());
        typedQuery.setParameter("movementAreaIds", movementAreaIds);
        try{
            return typedQuery.getSingleResult() != null;
        }
        catch (NoResultException e){
            LOG.debug("On checkMovementExistence [{}]",e.getMessage());
            return false;
        }
    }

    private void setTypedQueryMovementParams(List<SearchValue> searchKeyValues, Query query) throws ParseException {
        for (SearchValue searchValue : searchKeyValues) {
            if (searchValue.isRange()) {
                if (searchValue.getField().equals(SearchField.DATE)) {
                    query.setParameter("fromDate", DateUtil.convertDateTimeInUTC(searchValue.getFromValue()));
                    query.setParameter("toDate", DateUtil.convertDateTimeInUTC(searchValue.getToValue()));
                }
            } else {
                if (searchValue.getField().equals(SearchField.AREA)) {
                    query.setParameter("wkt", WKTUtil.getGeometryFromWKTSrring(searchValue.getValue()));
                }
            }
        }
    }

    public List<Movement> getMovementList(String sql, List<SearchValue> searchKeyValues) throws MovementServiceException {
        try {
            LOG.debug("SQL QUERY IN LIST PAGINATED: {}", sql);
            TypedQuery<Movement> query = getMovementQuery(sql, searchKeyValues, Movement.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new MovementServiceException("Error when getting list", e, ErrorCode.DATA_RETRIEVING_ERROR);
        }
    }

    public List<Movement> getMovementList(String sql, List<SearchValue> searchKeyValues, int numberOfReports) throws MovementServiceException {
        try {
            List<Movement> movements = new ArrayList<>();
            if (searchKeyValues == null || searchKeyValues.isEmpty()) {
                LOG.debug("searchValues empty or null, getting all vessels and the latest reports for them");
                TypedQuery<MovementConnect> connectQuery = em.createNamedQuery(MovementConnect.MOVEMENT_CONNECT_GET_ALL, MovementConnect.class);
                List<MovementConnect> movementConnects = connectQuery.getResultList();

                for (MovementConnect movementConnect : movementConnects) {
                    List<Movement> latestMovementsByConnectId = getLatestMovementsByConnectId(movementConnect.getValue(), numberOfReports);
                    movements.addAll(latestMovementsByConnectId);
                }
            } else {
                LOG.debug("Searchvalues is NOT empty, getting latest reports for the query ( TOP( {} ) )", numberOfReports);
                TypedQuery<Movement> query = getMovementQuery(sql, searchKeyValues, Movement.class);
                query.setMaxResults(numberOfReports);
                movements = query.getResultList();
            }
            return movements;
        } catch (ParseException e) {
            throw new MovementServiceException("Error when getting list", e, ErrorCode.DATA_RETRIEVING_ERROR);
        }
    }

    public MovementConnect getMovementConnectByConnectId(String id) {
        try {
            TypedQuery<MovementConnect> query = em.createNamedQuery(MovementConnect.MOVEMENT_CONNECT_BY_CONNECT_ID, MovementConnect.class);
            query.setParameter("value", id);
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public Movement createMovement(Movement entity) {
        em.persist(entity);
        return entity;
    }

    public Segment createSegment(Segment segment) {
        em.persist(segment);
        return segment;
    }

    public Segment updateSegment(Segment segment) {
        Segment updated = em.merge(segment);
        em.flush();
        return updated;
    }

    public List<Movement> getMovementListByAreaAndTimeInterval(MovementAreaAndTimeIntervalCriteria criteria) {
        List<Movement> resultList = new ArrayList<>();
        Area areaResult = areaDao.getAreaByCode(criteria.getAreaCode());
        if(areaResult!=null) {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.LIST_BY_AREA_TIME_INTERVAL, Movement.class);
            query.setParameter("fromDate", DateUtil.convertDateTimeInUTC(criteria.getFromDate()));
            query.setParameter("toDate", DateUtil.convertDateTimeInUTC(criteria.getToDate()));
            query.setParameter("areaId", areaResult.getAreaId());
            resultList = query.getResultList();
        }
        return resultList;
    }

    public MovementConnect createMovementConnect(MovementConnect movementConnect) {
        em.persist(movementConnect);
        return movementConnect;
    }

    public List<MicroMovementDto> getMicroMovementsAfterDate(Instant date) {
        try {
            TypedQuery<MicroMovementDto> query = em.createNamedQuery(MicroMovementDto.FIND_ALL_AFTER_DATE, MicroMovementDto.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (NoResultException e) {
            LOG.debug("No positions found after date: {}", date);
            return new ArrayList<>();
        }
    }

    public List<MicroMovement> getLastMicroMovementForAllAssets() {
        try {
            TypedQuery<MicroMovement> query = em.createQuery("FROM MicroMovement",MicroMovement.class);
            return query.getResultList();
        } catch (NoResultException e) {
            LOG.debug("No positions found while searching for last position of all assets");
            return new ArrayList<>();
        }
    }
}
