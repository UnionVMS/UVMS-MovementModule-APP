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
package eu.europa.ec.fisheries.uvms.movement.dao.bean;

import com.vividsolutions.jts.io.ParseException;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.uvms.movement.constant.UvmsConstants;
import eu.europa.ec.fisheries.uvms.movement.dao.Dao;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.entity.*;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchValue;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.util.WKTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 **/
@LocalBean
@Stateless
public class MovementDaoBean extends Dao implements MovementDao {

    final static Logger LOG = LoggerFactory.getLogger(MovementDaoBean.class);

    public Movement getMovementsByGUID(String guid) throws MovementDaoException {
        try {
            TypedQuery<Movement> query = em.createNamedQuery("Movement.findByGUID", Movement.class);
            query.setParameter("guid", guid);
            Movement singleResult = query.getSingleResult();
            return singleResult;
        } catch (javax.persistence.NoResultException e) {
            LOG.debug("No result when retrieveing movements by GUID");
            return null;
        } catch (Exception e) {
            LOG.error("[ Error when getting movement by GUID ] {} ", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting Movement by GUID. ]", e);
        }
    }

    public Movement getMovementById(Long id) {
        return em.find(Movement.class, id);
    }

    public List<Movement> getLatestMovementsByConnectIdList(List<String> connectIds) throws MovementDaoException {
        try {
            List<Movement> resultList = new ArrayList<>();
            TypedQuery<LatestMovement> latestMovementQuery = em.createNamedQuery("LatestMovement.findLatestByMovementConnectList", LatestMovement.class);
            latestMovementQuery.setParameter("connectId", connectIds);
            for (LatestMovement lm : latestMovementQuery.getResultList()) {
                resultList.add(lm.getMovement());
            }
            return resultList;
        /*} catch (javax.persistence.NoResultException e) {
            LOG.debug("No result when retrieving movements by GUID");
            // TODO why return null for a empty resultset ????
            // TODO even more so, what in the above code throws an NoResultException??????? Commenting out to see if something explodes
            return null;*/
        } catch (Exception e) {
            LOG.error("[ Error when getting movement by GUID ] {} ", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting Movement by GUID. ]", e);
        }
    }

    public List<Movement> getLatestMovementsByConnectId(String connectId, Integer amount) throws MovementDaoException {
        try {
            if (amount == 1) {
                // TODO  not stable
                LatestMovement latestMovement = getLatestMovement(connectId);
                Movement movement = latestMovement.getMovement();
                return Collections.singletonList(movement);
            }
            TypedQuery<Movement> query = em.createNamedQuery("Movement.findLatestByMovementConnect", Movement.class);
            query.setParameter("connectId", connectId);
            query.setMaxResults(amount);
            List<Movement> resultList = query.getResultList();
            return resultList;
        } catch (javax.persistence.NoResultException e) {
            LOG.debug("No result when retrieveing movements by GUID");
            return null;
        } catch (Exception e) {
            LOG.error("[ Error when getting movement by GUID ] {} ", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting Movement by GUID. ]", e);
        }
    }

    // TODO this method removes records in DB if it gets exception at a getOperation
    // TODO this state should not occur in the first place
    public AreaType getAreaTypeByCode(String code) throws MovementDaoException {
        try {
            long start = System.currentTimeMillis();
            TypedQuery<AreaType> query = em.createNamedQuery("AreaType.findByCode", AreaType.class);
            query.setParameter("code", code);
            AreaType singleResult = query.getSingleResult();
            long diff = System.currentTimeMillis() - start;
            LOG.debug("getAreaTypeByCode: " + " ---- TIME ----" + diff +"ms" );
            return singleResult;
        } catch (javax.persistence.NoResultException e) {
            LOG.debug("No result when retrieveing AreaType By code:" + code);
            return null;
        } catch (javax.persistence.NonUniqueResultException e) {
            LOG.error("Duplicate area type in DB for code: {}. Cleaning up duplicates.", code);
            TypedQuery<AreaType> query = em.createNamedQuery("AreaType.findByCode", AreaType.class);
            query.setParameter("code", code);
            List<AreaType> areaList = query.getResultList();
            // TODO  a get method that changes state in DB ???
            // TODO correct the error at correct place
            for (int i = 1; i < areaList.size(); i++) {
                em.remove(areaList.get(i));
            }
            return areaList.get(0);
        } catch (Exception e) {
            LOG.error("[ Error when getting AreaType. ] {} CODE: {} ", e.getMessage(), code);
            throw new MovementDaoException(6, "[ Error when getting AreaType. ]", e);
        }
    }

    /**
     *
     * @param code
     * @param remoteId
     * @return
     * @throws MovementDaoException
     */
    public Area getAreaByRemoteIdAndCode(String code, String remoteId) throws MovementDaoException {
        try {
            long start = System.currentTimeMillis();
            TypedQuery<Area> query;
            if (code != null && !code.isEmpty()) {
                LOG.debug("Code present in GetAreaQuery: CODE: {}", code);
                query = em.createNamedQuery("Area.findByCode", Area.class);
                query.setParameter("code", code);
            } else {
                throw new NoResultException("No valid input parameters to method getAreaByRemoteIdAndCode");
            }
            Area singleResult = query.getSingleResult();
            long diff = System.currentTimeMillis() - start;
            LOG.debug("getAreaByRemoteIdAndCode: " +  " ---- TIME ----" + diff +"ms");
            return singleResult;
        } catch (javax.persistence.NoResultException e) {
            LOG.debug("Could not get AreaType By code: {} and remoteId: {}", code, remoteId);
            return null;
        } catch (Exception e) {
            LOG.error("[ Error when getting AreaType. ] {} CODE: {}, REMOTEID: {} ", e.getMessage(), code, remoteId);
            throw new MovementDaoException(7, "[ Error when getting AreaType. ]", e);
        }
    }

    public List<Movement> isDateAlreadyInserted(String id, Date date) {
        try {
            long start = System.currentTimeMillis();
            //ToDo: The named query findExistingDate in the Movement class assumes that the duplicate field is false.
            //ToDo: Need to check if a null check is needed here since e.g. postgres defaults to null if no value is actively set.
            TypedQuery<Movement> query = em.createNamedQuery("Movement.findExistingDate", Movement.class);
            query.setParameter("date", date);
            query.setParameter("id", id);
            List<Movement> resultList = query.getResultList();
            LOG.debug("Check for existing movement time: {}", (System.currentTimeMillis() - start));
            return resultList;
        } catch (javax.persistence.NoResultException e) {
            LOG.debug("Could not get previous position, No result of id:");
        } catch (Exception e) {
            LOG.error("[ Error isDateAlreadyInserted. ] {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public List<LatestMovement> getLatestMovements(Integer numberOfMovements) throws MovementDaoException {
        try {
            TypedQuery<LatestMovement> latestMovementQuery = em.createNamedQuery("LatestMovement.findLatest", LatestMovement.class);
            latestMovementQuery.setMaxResults(numberOfMovements);
            List<LatestMovement> rs = latestMovementQuery.getResultList();
            return rs;
        } catch (javax.persistence.NoResultException e) {
            // TODO why exception at empty result
            LOG.debug("Could not get latest movements");
            return null;
        } catch (Exception e) {
            LOG.error("[ Error when getting latest movement. ]");
            throw new MovementDaoException(6, "[ Error when getting latest movement. ]", e);
        }
    }

    @Override
    public Movement getLatestMovement(String id, Date date) {
        Movement singleResult = null;
        try {
            TypedQuery<Movement> query = em.createNamedQuery("Movement.findLatest", Movement.class);
            query.setParameter("id", id);
            query.setParameter("date", date, TemporalType.TIMESTAMP);
            singleResult = query.getSingleResult();
        }catch (NoResultException e){
            LOG.debug("No previous movement found for date: " + date.toString() + " and connectedId: " + id );
        }
        return singleResult;
    }

    public void upsertLatestMovement(Movement movement, MovementConnect movementConnect) throws MovementDaoException {
        // TODO db update in catchblock ?
        // TODO no nullcheck on latestMovement ???
        LatestMovement latestMovement = null;
        try {
            latestMovement = getLatestMovement(movementConnect.getValue());     //get the latest movement in the table LatestMovement
            if (latestMovement.getTimestamp().after(movement.getTimestamp())) { //if it is in the middle of things
                LOG.debug("CURRENT MOVEMENT BEFORE LATEST MOVEMENT. NO CHANGE.");
                return;
            }
        } catch (javax.persistence.NoResultException e) {       //if there is no latestMovement connected to this connectID, create one. Should this really be in a catch statement?
            latestMovement = new LatestMovement();
            latestMovement.setMovementConnect(movementConnect);
            latestMovement.setMovement(movement);
            latestMovement.setTimestamp(movement.getTimestamp());
            em.persist(latestMovement);
            return;
        }
        latestMovement.setMovement(movement);
        latestMovement.setTimestamp(movement.getTimestamp());
        //dont you need a create or persist here?
    }

    private LatestMovement getLatestMovement(String connectId) throws javax.persistence.NoResultException {
        TypedQuery<LatestMovement> latestMovementQuery = em.createNamedQuery("LatestMovement.findLatestByMovementConnect", LatestMovement.class);
        latestMovementQuery.setParameter("connectId", connectId);
        return latestMovementQuery.getSingleResult();
    }

    public Segment findByFromMovement(Movement movement) throws MovementDaoException {
        try {
            TypedQuery<Segment> query = em.createNamedQuery("Segment.findByFromMovement", Segment.class);
            query.setParameter("movement", movement);
            Segment retVal = query.getSingleResult();
            return retVal;
        } catch (javax.persistence.NoResultException e) {
            LOG.debug("Could not get Segment by fromMovement, No result of movementId:" + movement.getId());
            return null;
        } catch (Exception e) {
            LOG.error("[ Error when getting segment by fromMovement. ] {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting segment by fromMovement. ]", e);
        }
    }

    public Boolean hasMovementToOrFromSegment(Movement movement) throws MovementDaoException {
        try {
            TypedQuery<Segment> query = em.createNamedQuery("Segment.findByMovement", Segment.class);
            query.setParameter("movement", movement);
            List<Segment> resultList = query.getResultList();
            return !resultList.isEmpty();
        } catch (javax.persistence.NoResultException e) {
            LOG.debug("Could not get Segment by fromMovement, No result of movementId:" + movement.getId());
            return null;
        } catch (Exception e) {
            LOG.error("[ Error when getting latest movement. ] {}", e.getMessage());
            throw new MovementDaoException(2, "[ Error when getting latest movement. ]", e);
        }
    }

    public Segment findByToMovement(Movement movement) throws MovementDaoException {
        try {
            long start = System.currentTimeMillis();
            TypedQuery<Segment> query = em.createNamedQuery("Segment.findByToMovement", Segment.class);
            query.setParameter("movement", movement);
            //query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            Segment retVal = query.getSingleResult();
            long diff = System.currentTimeMillis() - start;
            LOG.debug("findByToMovement: " + " ---- TIME ---- " + diff +"ms" );
            return retVal;
        } catch (javax.persistence.NoResultException e) {
            LOG.debug("Could not get Segment by fromMovement, No result of movementId:" + movement.getId());
            return null;
        } catch (Exception e) {
            LOG.error("[ Error when getting latest movement. ] {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting latest movement. ]", e);
        }
    }

    @Override
    public Movement getFirstMovement(String movementConnectValue) throws MovementDaoException {
        Movement firstMovememnt;
        try {
            TypedQuery<Movement> query = em.createNamedQuery("Movement.findFirst", Movement.class);
            query.setParameter("id", movementConnectValue);
            firstMovememnt = query.getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            LOG.debug("Could not get first position, No result of id:" + movementConnectValue);
            return null;
        } catch (Exception e) {
            LOG.error("[ Error when getting first movement. ] {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting first movement. ]", e);
        }
        return firstMovememnt;
    }

    
    @Override
    @Deprecated
    public Movement getEntityById(String id){
    	return null;
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

    @Override
    public List<Movement> getListAll() throws MovementDaoException {
        try {
            Query query = em.createNamedQuery(UvmsConstants.MOVEMENT_FIND_ALL);
            List resultList = query.getResultList();
            return resultList;
        } catch (IllegalArgumentException e) {
            LOG.error("[ Error when updating entity ] {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting list ] ", e);
        } catch (Exception e) {
            LOG.error("[ Error when updating entity ] {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting list ] ", e);
        }
    }

    @Override
    public List<Movement> getMovementListPaginated(Integer page, Integer listSize, String sql, List<SearchValue> searchKeyValues) throws MovementDaoException {
        try {
            Query query = getMovementQuery(sql, searchKeyValues);
            query.setFirstResult(listSize * (page - 1));
            query.setMaxResults(listSize);
            List resultList = query.getResultList();
            return resultList;
        } catch (IllegalArgumentException e) {
            LOG.error("[ Error getting movement list paginated ] {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting list ] ", e);
        } catch (Exception e) {
            LOG.error("[ Error getting movement list paginated ]  {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting list ] ", e);
        }
    }

    @Override
    public List<MinimalMovement> getMinimalMovementListPaginated(Integer page, Integer listSize, String sql, List<SearchValue> searchKeyValues) throws MovementDaoException {
        try {
            Query query = getMovementQuery(sql, searchKeyValues);
            query.setFirstResult(listSize * (page - 1));
            query.setMaxResults(listSize);
            List resultList = query.getResultList();
            return resultList;
        } catch (IllegalArgumentException e) {
            LOG.error("[ Error getting movement list paginated ] {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting list ] ", e);
        } catch (Exception e) {
            LOG.error("[ Error getting movement list paginated ]  {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting list ] ", e);
        }
    }

    private Query getMovementQuery(String sql, List<SearchValue> searchKeyValues) throws ParseException {
        Query query = em.createQuery(sql);
        setTypedQueryMovementParams(searchKeyValues, query);
        return query;
    }

    @Override
    public Long getMovementListSearchCount(String countSql, List<SearchValue> searchKeyValues) throws MovementDaoException, ParseException {
        TypedQuery<Long> query = em.createQuery(countSql, Long.class);
        setTypedQueryMovementParams(searchKeyValues, query);
        Long singleResult = query.getSingleResult();
        return singleResult;
    }


    private void setTypedQueryMovementParams(List<SearchValue> searchKeyValues, Query query) throws ParseException, IllegalArgumentException {
        for (SearchValue searchValue : searchKeyValues) {
            if (searchValue.isRange()) {
                switch (searchValue.getField()) {
                    case DATE:

                        //query.setParameter("fromDate", DateUtil.parseToUTCDate(searchValue.getFromValue()));
                        query.setParameter("fromDate", DateUtil.convertDateTimeInUTC(searchValue.getFromValue()));
                        query.setParameter("toDate", DateUtil.convertDateTimeInUTC(searchValue.getToValue()));
                        //query.setParameter("toDate", DateUtil.parseToUTCDate(searchValue.getToValue()));
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

    @Override
    public List<Movement> getMovementList(String sql, List<SearchValue> searchKeyValues) throws MovementDaoException {
        try {
            LOG.debug("SQL QUERY IN LIST PAGINATED: " + sql);
            Query query = getMovementQuery(sql, searchKeyValues);
            List resultList = query.getResultList();
            return resultList;
        } catch (IllegalArgumentException e) {
            LOG.error("[ Error getting movement list paginated ] {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting list ] ", e);
        } catch (Exception e) {
            LOG.error("[ Error getting movement list paginated ]  {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting list ] ", e);
        }
    }

    @Override
    public List<Movement> getMovementList(String sql, List<SearchValue> searchKeyValues, int numberOfReports) throws MovementDaoException {
        try {
            List<Movement> movements = new ArrayList<>();
            // long start = System.currentTimeMillis();
            if (searchKeyValues == null || searchKeyValues.isEmpty()) {
                LOG.debug("searchValues empty or null, getting all vessels and the latest reports for them");
                TypedQuery<MovementConnect> connectQuery = em.createNamedQuery(UvmsConstants.MOVEMENT_CONNECT_GET_ALL, MovementConnect.class);
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
        } catch (IllegalArgumentException e) {
            LOG.error("[ Error getting movement list paginated ] {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting list ] ", e);
        } catch (Exception e) {
            LOG.error("[ Error getting movement list paginated ]  {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting list ] ", e);
        }
    }

    @Override
    public MovementConnect getMovementConnectByConnectId(String id) throws MovementDaoException {
        try {
            TypedQuery<MovementConnect> query = em.createNamedQuery(UvmsConstants.MOVEMENT_CONNECT_BY_CONNECT_ID, MovementConnect.class);
            query.setParameter("value", id);
            MovementConnect singleResult = query.getSingleResult();
            long endTime = System.currentTimeMillis();
            return singleResult;
        } catch (NoResultException ex) {
            // Not logged as an error, since this will be the case for the first position for every asset.
            LOG.info("[ No result when retrieving MovementConnect from DAO {}]",id);
            return null;
        } catch (IllegalArgumentException e) {
            LOG.error("[ Error when getting MovementConnect ] {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting list ] ", e);
        } catch (Exception e) {
            LOG.error("[ Error when getting MovementConnect ] {}", e.getMessage());
            throw new MovementDaoException(6, "[ Error when getting list ] ", e);
        }
    }

    @Override
    public <T> T merge(T entity) throws MovementDaoException {
        try {
            em.merge(entity);
            return entity;
        } catch (Exception e) {
            LOG.error("[ Error when merging ] {}", e.getMessage());
            throw new MovementDaoException(10, "[ Error when merging ] ", e);
        }

    }

    @Override
    public <T> T persist(T entity) throws MovementDaoException {
        try {
            em.persist(entity);
            return entity;
        } catch (Exception e) {
            LOG.error("[ Error when persisting ] {}", e.getMessage());
            throw new MovementDaoException(12, "[ Error when persisting ] ", e);
        }

    }

    @Override
    public <T> T create(T entity) throws MovementDaoException {
        try {
            em.persist(entity);
            return entity;
        } catch (Exception e) {
            LOG.error("[ Error when creating ] {}", e.getMessage());
            throw new MovementDaoException(12, "[ Error when creating ] ", e);
        }
    }

    @Override
    public void flush() throws MovementDaoException {
        try {
            em.flush();
        } catch (Exception e) {
            LOG.error("[ Error when creating ] {}", e.getMessage());
            throw new MovementDaoException(12, "[ Error when creating ] ", e);
        }
    }

    @Override
    public List<Movement> getMovementListByAreaAndTimeInterval(MovementAreaAndTimeIntervalCriteria criteria) throws MovementDaoException {
        List<Movement> resultList = new ArrayList();
        Area areaResult = getAreaByRemoteIdAndCode(criteria.getAreaCode(), null);
        if(areaResult!=null){
            TypedQuery<Movement> query = em.createNamedQuery(UvmsConstants.MOVEMENT_LIST_BY_AREA_TIME_INTERVAL, Movement.class);
            query.setParameter("fromDate", DateUtil.parseToUTCDate(criteria.getFromDate()));
            query.setParameter("toDate", DateUtil.parseToUTCDate(criteria.getToDate()));
            query.setParameter("areaId", areaResult.getAreaId());
            resultList = query.getResultList();
        }
        return resultList;
    }

    public List<Movement> getUnprocessedMovements() throws javax.persistence.NoResultException {
        TypedQuery<Movement> latestMovementQuery = em.createNamedQuery("Movement.findUnprocessed", Movement.class);
        latestMovementQuery.setMaxResults(100);
        return latestMovementQuery.getResultList();
    }

    public List<Long> getUnprocessedMovementIds() throws javax.persistence.NoResultException {
        TypedQuery<Long> latestMovementQuery = em.createNamedQuery("Movement.findUnprocessedId", Long.class);
        latestMovementQuery.setMaxResults(100);
        return latestMovementQuery.getResultList();
    }


    @Override
    public MovementConnect createMovementConnect(MovementConnect movementConnect) throws MovementDaoException {
        try {
            em.persist(movementConnect);
            return movementConnect;
        } catch (Exception e) {
            LOG.error("[ Error when creating ] {}", e.getMessage());
            throw new MovementDaoException(12, "[ Error when creating ] ", e);
        }
    }

}