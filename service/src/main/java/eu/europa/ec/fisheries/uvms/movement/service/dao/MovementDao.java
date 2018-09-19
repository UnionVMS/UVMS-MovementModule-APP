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
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vividsolutions.jts.io.ParseException;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.entity.LatestMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementDomainRuntimeException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchValue;
import eu.europa.ec.fisheries.uvms.movement.service.util.WKTUtil;

@Stateless
public class MovementDao {

    private static final Logger LOG = LoggerFactory.getLogger(MovementDao.class);

    @PersistenceContext
    private EntityManager em;
    
    public Movement getMovementByGUID(String guid) {
        try {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_BY_GUID, Movement.class);
            query.setParameter("guid", guid);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("No result when retrieving movements by GUID");
            return null;
        }
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
            throw new MovementDomainRuntimeException("Amount can't have 0 or negative value.", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        } else if (amount == 1) {
            // TODO  not stable
            LatestMovement latestMovement = getLatestMovement(connectId);
            if(latestMovement == null)
                return null;
            Movement movement = latestMovement.getMovement();
            if(movement != null)
                return Collections.singletonList(movement);
            else
                return null;
        } else {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_LATEST_BY_MOVEMENT_CONNECT, Movement.class);
            query.setParameter("connectId", connectId);
            query.setMaxResults(amount);
            return query.getResultList();
        }
    }

    public AreaType getAreaTypeByCode(String code) {
        try {
            TypedQuery<AreaType> query = em.createNamedQuery(AreaType.FIND_BY_CODE, AreaType.class);
            query.setParameter("code", code);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("No result when retrieving AreaType By code: {}", code);
            return null;
        }
    }

    public Area getAreaByRemoteIdAndCode(String code, String remoteId) {
        try {
            if (code == null || code.isEmpty()) {
                throw new MovementDomainRuntimeException("No valid input parameters to method getAreaByRemoteIdAndCode",
                        ErrorCode.ILLEGAL_ARGUMENT_ERROR);
            }
            TypedQuery<Area> query = em.createNamedQuery(Area.FIND_BY_CODE, Area.class);
            query.setParameter("code", code);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("Could not get AreaType By code: {} and remoteId: {}", code, remoteId);
            return null;
        }
    }

    public List<Movement> isDateAlreadyInserted(String id, Instant date) {
        long start = System.currentTimeMillis();
        //ToDo: The named query findExistingDate in the Movement class assumes that the duplicate field is false.
        //ToDo: Need to check if a null check is needed here since e.g. postgres defaults to null if no value is actively set.
        TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_EXISTING_DATE, Movement.class);
        query.setParameter("date", date);
        query.setParameter("id", id);
        List<Movement> resultList = query.getResultList();
        LOG.debug("Check for existing movement time: {}", (System.currentTimeMillis() - start));
        if(resultList != null && resultList.size() > 0) {
            return resultList;
        }
        else {
            LOG.debug("Could not get previous position, No result of id:");
            return new ArrayList<>();
        }
    }

    public List<LatestMovement> getLatestMovements(Integer numberOfMovements) {
        TypedQuery<LatestMovement> latestMovementQuery = em.createNamedQuery(LatestMovement.FIND_LATEST, LatestMovement.class);
        latestMovementQuery.setMaxResults(numberOfMovements);
        List<LatestMovement> rs = latestMovementQuery.getResultList();
        return rs;
    }

    public Movement getLatestMovement(String id, Instant date) {
        Movement singleResult = null;
        try {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_LATEST, Movement.class);
            query.setParameter("id", id);
            query.setParameter("date", date);
            singleResult = query.getSingleResult();
        }catch (NoResultException e){
            LOG.debug("No previous movement found for date: " + date.toString() + " and connectedId: " + id );
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

    public Segment findByFromMovement(Movement movement) {
        try {
            TypedQuery<Segment> query = em.createNamedQuery(Segment.FIND_FIND_BY_FROM_MOVEMENT, Segment.class);
            query.setParameter("movement", movement);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("Could not get Segment by fromMovement, No result of movementId:" + movement.getId());
            return null;
        }
    }

    public Boolean hasMovementToOrFromSegment(Movement movement) throws MovementDomainException {
        try {
            TypedQuery<Segment> query = em.createNamedQuery(Segment.FIND_BY_MOVEMENT, Segment.class);
            query.setParameter("movement", movement);
            List<Segment> resultList = query.getResultList();
            if(resultList.isEmpty()) {
                LOG.debug("Could not get Segment by fromMovement, No result of movementId:" + movement.getId());
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            LOG.error("[ Error when getting latest movement. ] {}", e.getMessage());
            throw new MovementDomainException("Error when getting latest movement", e, ErrorCode.RETRIEVING_LATEST_MOVEMENT_ERROR);
        }
    }

    public Segment findByToMovement(Movement movement) {
        try {
            TypedQuery<Segment> query = em.createNamedQuery(Segment.FIND_BY_TO_MOVEMENT, Segment.class);
            query.setParameter("movement", movement);
            //query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("Could not get Segment by fromMovement, No result of movementId:" + movement.getId());
            return null;
        }
    }

    public Movement getFirstMovement(String movementConnectValue) {
        try {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_FIRST, Movement.class);
            query.setParameter("id", movementConnectValue);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("Could not get first position, No result of id:" + movementConnectValue);
            return null;
        }
    }

   /* @Override   //removed to see if it blows
    public Movement getEntityById(String id) throws NoEntityFoundException, MovementDaoException {
        try {
            long start = System.currentTimeMillis();
            em.getEntityManagerFactory().getCache().evictAll();
            Movement movement = em.find(Movement.class, new Integer(id));
            long diff = System.currentTimeMillis() - start;
            LOG.debug("getEntityById: " + " ---- TIME ---- " + diff +"ms" );
            return movement;
        } catch (NoResultException e) {
            LOG.error("[ Error when getting entity by ID. ] {}", e.getMessage());
            throw new NoEntityFoundException(6, "[ Error when getting entity by ID. ]", e);
        } catch (Exception e) {
            LOG.error("[ Error when getting entity by ID. ] {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting entity by ID. ] ", e);
        }
    }*/

    public List<Movement> getListAll() {
        Query query = em.createNamedQuery(Movement.FIND_ALL);
        return query.getResultList();
    }

    public <T> T getMovementListPaginated(Integer page, Integer listSize, String sql, List<SearchValue> searchKeyValues) throws MovementDomainException {
        try {
            Query query = getMovementQuery(sql, searchKeyValues);
            query.setFirstResult(listSize * (page - 1));
            query.setMaxResults(listSize);
            T resultList = (T) query.getResultList();
            return resultList;
        } catch (IllegalArgumentException e) {
            LOG.error("[ Error getting movement list paginated ] {}", e.getMessage());
            throw new MovementDomainRuntimeException("Error when getting list", e, ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        } catch (Exception e) {
            LOG.error("[ Error getting movement list paginated ]  {}", e.getMessage());
            throw new MovementDomainException("Error when getting list", e, ErrorCode.DATA_RETRIEVING_ERROR);
        }
    }

    private Query getMovementQuery(String sql, List<SearchValue> searchKeyValues) throws ParseException {
        Query query = em.createQuery(sql);
        setTypedQueryMovementParams(searchKeyValues, query);
        return query;
    }

    public Long getMovementListSearchCount(String countSql, List<SearchValue> searchKeyValues) throws ParseException {
        TypedQuery<Long> query = em.createQuery(countSql, Long.class);
        setTypedQueryMovementParams(searchKeyValues, query);
        return query.getSingleResult();
    }


    private void setTypedQueryMovementParams(List<SearchValue> searchKeyValues, Query query) throws IllegalArgumentException, ParseException {
        for (SearchValue searchValue : searchKeyValues) {
            if (searchValue.isRange()) {
                switch (searchValue.getField()) {
                    case DATE:
                        query.setParameter("fromDate", DateUtil.convertDateTimeInUTC(searchValue.getFromValue()));
                        query.setParameter("toDate", DateUtil.convertDateTimeInUTC(searchValue.getToValue()));
                        break;
                }
            } else {
                switch (searchValue.getField()) {
                    case AREA:
                        query.setParameter("wkt", WKTUtil.getGeometryFromWKTSrring(searchValue.getValue()));
                        break;
                }
            }
        }
    }

    public List<Movement> getMovementList(String sql, List<SearchValue> searchKeyValues) throws MovementDomainException {
        try {
            LOG.debug("SQL QUERY IN LIST PAGINATED: " + sql);
            Query query = getMovementQuery(sql, searchKeyValues);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            LOG.error("[ Error getting movement list paginated ]  {}", e.getMessage());
            throw new MovementDomainException("Error when getting list", e, ErrorCode.DATA_RETRIEVING_ERROR);
        }
    }

    public List<Movement> getMovementList(String sql, List<SearchValue> searchKeyValues, int numberOfReports) throws MovementDomainException {
        try {
            List<Movement> movements = new ArrayList<>();
            // long start = System.currentTimeMillis();
            if (searchKeyValues == null || searchKeyValues.isEmpty()) {
                LOG.debug("searchValues empty or null, getting all vessels and the latest reports for them");
                TypedQuery<MovementConnect> connectQuery = em.createNamedQuery(MovementConnect.MOVEMENT_CONNECT_GET_ALL, MovementConnect.class);
                List<MovementConnect> movementConnects = connectQuery.getResultList();

                for (MovementConnect movementConnect : movementConnects) {
                    List<Movement> latestMovementsByConnectId = getLatestMovementsByConnectId(movementConnect.getValue(), numberOfReports);
                    movements.addAll(latestMovementsByConnectId);
                }
            } else {
                LOG.debug("Searchvalues is NOT empty, getting latest reports for the query ( TOP( " + numberOfReports + " ) )");
                Query query = getMovementQuery(sql, searchKeyValues);
//                query.setFetchSize(numberOfReports);
                query.setMaxResults(numberOfReports);
                movements = query.getResultList();
            }
            return movements;
        } catch (ParseException e) {
            LOG.error("[ Error getting movement list paginated ]  {}", e.getMessage());
            throw new MovementDomainException("Error when getting list", e, ErrorCode.DATA_RETRIEVING_ERROR);
        }
    }

    public MovementConnect getMovementConnectByConnectId(String id) {
        try {
            TypedQuery<MovementConnect> query = em.createNamedQuery(MovementConnect.MOVEMENT_CONNECT_BY_CONNECT_ID, MovementConnect.class);
            query.setParameter("value", id);
            return query.getSingleResult();
        } catch (NoResultException ex) {
            // Not logged as an error, since this will be the case for the first position for every asset.
            LOG.info("[ No result when retrieving MovementConnect from DAO {}]", id);
            return null;
        }
    }

    public <T> T merge(T entity) {
        T updated = em.merge(entity);
        em.flush();
        return updated;
    }

    public <T> T persist(T entity) {
        em.persist(entity);
        return entity;
    }

    public <T> T create(T entity) {
        em.persist(entity);
        return entity;
    }

    public void flush() {
        em.flush();
    }

    public List<Movement> getMovementListByAreaAndTimeInterval(MovementAreaAndTimeIntervalCriteria criteria) {
        List<Movement> resultList = new ArrayList<>();
        Area areaResult = getAreaByRemoteIdAndCode(criteria.getAreaCode(), null);
        if(areaResult!=null) {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.LIST_BY_AREA_TIME_INTERVAL, Movement.class);
            query.setParameter("fromDate", DateUtil.convertDateTimeInUTC(criteria.getFromDate()));
            query.setParameter("toDate", DateUtil.convertDateTimeInUTC(criteria.getToDate()));
            query.setParameter("areaId", areaResult.getAreaId());
            resultList = query.getResultList();
        }
        return resultList;
    }

    public List<Movement> getUnprocessedMovements() {
        TypedQuery<Movement> latestMovementQuery = em.createNamedQuery(Movement.FIND_UNPROCESSED, Movement.class);
        latestMovementQuery.setMaxResults(100);
        return latestMovementQuery.getResultList();
    }

    public List<Long> getUnprocessedMovementIds() {
        TypedQuery<Long> latestMovementQuery = em.createNamedQuery(Movement.FIND_UNPROCESSED_ID, Long.class);
        latestMovementQuery.setMaxResults(100);
        return latestMovementQuery.getResultList();
    }

    public MovementConnect createMovementConnect(MovementConnect movementConnect) {
        em.persist(movementConnect);
        return movementConnect;
    }
}
