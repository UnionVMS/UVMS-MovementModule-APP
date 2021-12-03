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

import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.CursorPagination;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchValue;
import eu.europa.ec.fisheries.uvms.movement.service.util.WKTUtil;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.VicinityInfoDTO;
import org.hibernate.HibernateException;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.*;
import javax.persistence.criteria.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Stateless
public class MovementDao {

    private static final Logger LOG = LoggerFactory.getLogger(MovementDao.class);
    private static final String CONNECT_ID = "connectId";

    @PersistenceContext
    private EntityManager em;

    public List<Geometry> getPointsFromTrack(Track track, int maxResults) {
        TypedQuery<Geometry> query = em.createNamedQuery(Movement.FIND_ALL_LOCATIONS_BY_TRACK, Geometry.class);
        query.setMaxResults(maxResults);
        query.setParameter("track", track);
        return query.getResultList();
    }

    public List<Movement> getMovementListByMovementConnect(MovementConnect movementConnect) {
        TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_ALL_BY_MOVEMENTCONNECT, Movement.class);
        query.setParameter("movementConnect", movementConnect);
        return query.getResultList();
    }

    public int updateToNewMovementConnect(UUID oldMovementConnect, UUID newMovementConnect, int limit) {
        Query query = em.createNamedQuery(Movement.UPDATE_TO_NEW_MOVEMENTCONNECT);
        query.setParameter("oldMC", oldMovementConnect);
        query.setParameter("newMC", newMovementConnect);
        query.setParameter("limit", limit);
        return query.executeUpdate();
    }

    public List<Movement> getMovementsByTrack(Track track, int maxResults) {
        TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_ALL_BY_TRACK, Movement.class);
        query.setMaxResults(maxResults);
        query.setParameter("track", track);
        return query.getResultList();
    }

    public Track getTrackById(UUID id){
        return em.find(Track.class, id);
    }

    public Movement getMovementById(UUID id) {
        return em.find(Movement.class, id);
    }

    public List<Movement> getLatestMovementsByConnectIdList(List<UUID> connectIds) {
        if (connectIds == null || connectIds.isEmpty()) {
            return new ArrayList<>();
        }
        TypedQuery<Movement> latestMovementQuery =
                em.createNamedQuery(Movement.FIND_LATESTMOVEMENT_BY_MOVEMENT_CONNECT_LIST, Movement.class);
        latestMovementQuery.setParameter(CONNECT_ID, connectIds);
        return latestMovementQuery.getResultList();
    }

    public List<Movement> getLatestMovementsByConnectId(UUID connectId, Integer amount) {
        if(amount < 1) {
            throw new IllegalArgumentException("Amount can't have 0 or negative value.");
        } else if (amount == 1) {
            Movement latestMovement = getLatestMovement(connectId);
            if(latestMovement != null)
                return Collections.singletonList(latestMovement);
            else
                return Collections.emptyList();
        } else {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_LATEST_BY_MOVEMENT_CONNECT, Movement.class);
            query.setParameter(CONNECT_ID, connectId);
            query.setMaxResults(amount);
            return query.getResultList();
        }
    }

    public List<Movement> isDateAlreadyInserted(UUID id, Instant date) {
        TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_EXISTING_DATE, Movement.class);
        query.setParameter("date", date);
        query.setParameter("id", id);
        return query.getResultList();
    }

    public List<Movement> getLatestMovements(Integer numberOfMovements) {
        TypedQuery<Movement> latestMovementQuery = em.createNamedQuery(Movement.FIND_LATEST, Movement.class);
        latestMovementQuery.setMaxResults(numberOfMovements);
        return latestMovementQuery.getResultList();
    }

    public Movement getPreviousMovement(UUID id, Instant date) {
        return getPreviousMovement(id, date, Arrays.asList(MovementSourceType.values()));
    }

    public Movement getPreviousMovement(UUID id, Instant date, List<MovementSourceType> sources) {
        Movement singleResult = null;
        try {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_PREVIOUS, Movement.class);
            query.setParameter("id", id);
            query.setParameter("date", date);
            query.setParameter("sources", sources);
            query.setMaxResults(1);
            singleResult = query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("No previous movement found for date: {} and connectedId: {}", date, id);
        }
        return singleResult;
    }

    public Movement getNextMovementByMcAndDate(UUID id, Instant date) {
        return getNextMovementByMcAndDate(id, date, Arrays.asList(MovementSourceType.values()));
    }

    public Movement getNextMovementByMcAndDate(UUID id, Instant date, List<MovementSourceType> sources) {
        Movement singleResult = null;
        try {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_NEXT, Movement.class);
            query.setParameter("id", id);
            query.setParameter("date", date);
            query.setParameter("sources", sources);
            singleResult = query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("No previous movement found for date: {} and connectedId: {}", date, id);
        }
        return singleResult;
    }

    public Movement getLatestMovement(UUID connectId) {
        try {
            TypedQuery<Movement> latestMovementQuery = em.createNamedQuery(Movement.FIND_LATESTMOVEMENT_BY_MOVEMENT_CONNECT, Movement.class);
            latestMovementQuery.setParameter(CONNECT_ID, connectId);
            return latestMovementQuery.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public List<VicinityInfoDTO> getVicinityOfMovement(Movement move, double maxDistance){
        TypedQuery<VicinityInfoDTO> latestMovementQuery = em.createNamedQuery(MovementConnect.FIND_NEAREST_AFTER, VicinityInfoDTO.class);
        latestMovementQuery.setParameter("excludedID", move.getMovementConnect().getId());
        latestMovementQuery.setParameter("point", move.getLocation());
        latestMovementQuery.setParameter("maxDistance", maxDistance);
        latestMovementQuery.setParameter("time", Instant.now().minus(8, ChronoUnit.HOURS));
        return latestMovementQuery.getResultList();
    }

    public Movement getFirstMovement(UUID movementConnectValue, UUID excludedMovement) {
        try {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_FIRST, Movement.class);
            query.setParameter("id", movementConnectValue);
            query.setParameter("excludedMovement", excludedMovement);
            query.setMaxResults(1);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("Could not get first position, no result of id: {}", movementConnectValue);
            return null;
        }
    }

    public long countNrOfMovementsForAssetBetween(UUID asset, Instant from, Instant to){
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

    public List<Movement> getMovementListPaginated(Integer page, Integer listSize, String sql, List<SearchValue> searchKeyValues){
        TypedQuery<Movement> query = getMovementQuery(sql, searchKeyValues);
        query.setFirstResult(listSize * (page - 1));
        query.setMaxResults(listSize);
        return query.getResultList();
    }

    private TypedQuery<Movement> getMovementQuery(String sql, List<SearchValue> searchKeyValues) {
        TypedQuery<Movement> query = em.createQuery(sql, Movement.class);
        setTypedQueryMovementParams(searchKeyValues, query);
        return query;
    }

    public Long getMovementListSearchCount(String countSql, List<SearchValue> searchKeyValues) {
        TypedQuery<Long> query = em.createQuery(countSql, Long.class);
        setTypedQueryMovementParams(searchKeyValues, query);
        return query.getSingleResult();
    }


    private void setTypedQueryMovementParams(List<SearchValue> searchKeyValues, Query query) {
        for (SearchValue searchValue : searchKeyValues) {
            if (searchValue.isRange()) {
                if (searchValue.getField().equals(SearchField.DATE)) {
                    query.setParameter("fromDate", DateUtils.stringToDate(searchValue.getFromValue()));
                    query.setParameter("toDate", DateUtils.stringToDate(searchValue.getToValue()));
                }
            } else {
                if (searchValue.getField().equals(SearchField.AREA)) {
                    query.setParameter("wkt", WKTUtil.getGeometryFromWKTSrring(searchValue.getValue()));
                }
            }
        }
    }

    public List<Movement> getMovementList(String sql, List<SearchValue> searchKeyValues){
        try {
            LOG.debug("SQL QUERY IN LIST PAGINATED: {}", sql);
            TypedQuery<Movement> query = getMovementQuery(sql, searchKeyValues);
            return query.getResultList();
        } catch (Exception e) {
            throw new HibernateException("Error when getting list", e);
        }
    }

    public List<Movement> getMovementList(String sql, List<SearchValue> searchKeyValues, int numberOfReports) {
        List<Movement> movements = new ArrayList<>();
        if (searchKeyValues == null || searchKeyValues.isEmpty()) {
            LOG.debug("searchValues empty or null, getting all vessels and the latest reports for them");
            TypedQuery<MovementConnect> connectQuery = em.createNamedQuery(MovementConnect.MOVEMENT_CONNECT_GET_ALL, MovementConnect.class);
            List<MovementConnect> movementConnects = connectQuery.getResultList();

            for (MovementConnect movementConnect : movementConnects) {
                List<Movement> latestMovementsByConnectId = getLatestMovementsByConnectId(movementConnect.getId(), numberOfReports);
                movements.addAll(latestMovementsByConnectId);
            }
        } else {
            LOG.debug("Searchvalues is NOT empty, getting latest reports for the query ( TOP( {} ) )", numberOfReports);
            TypedQuery<Movement> query = getMovementQuery(sql, searchKeyValues);
            query.setMaxResults(numberOfReports);
            movements = query.getResultList();
        }
        return movements;
    }

    public List<Movement> getCursorBasedList(CursorPagination cursorPagination) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Movement> cq = criteriaBuilder.createQuery(Movement.class);
        Root<Movement> movement = cq.from(Movement.class);

        List<Predicate> predicates = new ArrayList<>();

        if (cursorPagination.getTimestampCursor() != null && cursorPagination.getIdCursor() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(movement.get("timestamp"), cursorPagination.getTimestampCursor()));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(movement.get("timestamp"), cursorPagination.getTo()));
            predicates.add(criteriaBuilder.not(
                                criteriaBuilder.and(
                                        criteriaBuilder.equal(movement.get("timestamp"), cursorPagination.getTimestampCursor()),
                                        criteriaBuilder.lessThan(movement.get("id"), cursorPagination.getIdCursor())
                                        )
                                ));
        } else {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(movement.get("timestamp"), cursorPagination.getFrom()));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(movement.get("timestamp"), cursorPagination.getTo()));
        }

        if (cursorPagination.getConnectIds() != null) {
            Join<Movement, MovementConnect> movementConnect = movement.join("movementConnect");
            predicates.add(criteriaBuilder.in(movementConnect.get("id")).value(cursorPagination.getConnectIds()));
        }

        if (cursorPagination.getSources() != null) {
            predicates.add(criteriaBuilder.in(movement.get("source")).value(cursorPagination.getSources()));
        }

        cq.where(criteriaBuilder.and(predicates.stream().toArray(Predicate[]::new)));

        cq.orderBy(criteriaBuilder.asc(movement.get("timestamp")), criteriaBuilder.asc(movement.get("id")));
        TypedQuery<Movement> query = em.createQuery(cq);
        if (cursorPagination.getLimit() != null) {
            query.setMaxResults(cursorPagination.getLimit()); // limit
        }

        return query.getResultList();
    }

    public MovementConnect getMovementConnectByConnectId(UUID id) {
        return em.find(MovementConnect.class, id);
    }

    public Movement createMovement(Movement entity) {
        em.persist(entity);
        return entity;
    }

    public MovementConnect createMovementConnect(MovementConnect movementConnect) {
        em.persist(movementConnect);
        return movementConnect;
    }

    public void flush(){
        em.flush();
    }

    public void deleteMovementConnect(MovementConnect movementConnect){
        em.remove(movementConnect);
    }


    public List<Movement> getMovementsForAssetAfterDate(UUID id, Instant startDate, Instant endDate, List<MovementSourceType> sources){
        try {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_ALL_FOR_ASSET_BETWEEN_DATES, Movement.class);
            query.setParameter("id", id);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            query.setParameter("sources", sources);
            return query.getResultList();
        } catch (NoResultException e) {
            LOG.debug("No positions found for asset after date: {}", startDate);
            return new ArrayList<>();
        }
    }

    public List<Movement> getLatestNumberOfMovementsForAsset(UUID id, int number, List<MovementSourceType> sources){
        try {
            TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_LATEST_X_NUMBER_FOR_ASSET, Movement.class);
            query.setParameter("id", id);
            query.setParameter("sources", sources);
            query.setMaxResults(number);
            return query.getResultList();
        } catch (NoResultException e) {
            LOG.debug("No positions found for asset {}");
            return new ArrayList<>();
        }
    }

    public List<MovementDto> getLatestWithLimit(Instant date, List<MovementSourceType> sources) {
        TypedQuery<MovementDto> query = em.createNamedQuery(Movement.FIND_LATEST_SINCE, MovementDto.class);
        query.setParameter("date", date);
        query.setParameter("sources", sources);
        return query.getResultList();
    }

    public List<Movement> getMovementsForConnectIdsBetweenDates(List<UUID> connectIds, Instant fromDate, Instant toDate, List<MovementSourceType> sources) {
        TypedQuery<Movement> query = em.createNamedQuery(Movement.FIND_ALL_FOR_CONNECT_IDS_BETWEEN_DATES, Movement.class);
        query.setParameter("connectIds", connectIds);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("sources", sources);
        return query.getResultList();
    }
    
    public List<Movement> getMovementsByMoveIdList(List<UUID> moveIds) {
        if (moveIds == null || moveIds.isEmpty()) {
            return new ArrayList<>();
        }
        TypedQuery<Movement> latestMovementQuery =
                em.createNamedQuery(Movement.FIND_MOVEMENT_BY_ID_LIST, Movement.class);
        latestMovementQuery.setParameter("moveIds", moveIds);
        return latestMovementQuery.getResultList();
    }
}
