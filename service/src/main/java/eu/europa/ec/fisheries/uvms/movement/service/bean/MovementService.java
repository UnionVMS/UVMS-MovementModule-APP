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
package eu.europa.ec.fisheries.uvms.movement.service.bean;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.model.dto.ListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.LatestMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MinimalMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedMovement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementEntityToModelMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchFieldMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Stateless
public class MovementService {

    private static final Logger LOG = LoggerFactory.getLogger(MovementService.class);
    
    @Inject
    private IncomingMovementBean incomingMovementBean;

    @Inject
    private MovementDao movementDao;
    
    @Inject
    private AuditService auditService;

    @Inject
    private MovementMapResponseHelper movementMapResponseHelper;

    @Inject
    @CreatedMovement
    private Event<Movement> createdMovementEvent;

    public Movement createAndProcessMovement(Movement movement) {
        createMovement(movement);
        incomingMovementBean.processMovement(movement);
        if(movement != null){
            fireMovementEvent(movement);
            auditService.sendMovementCreatedAudit(movement, movement.getUpdatedBy());
        }
        return movement;
    }

    public List<Movement> createMovementBatch(List<Movement> movements) {
        for (Movement movement : movements) {
            createMovement(movement);
            incomingMovementBean.processMovement(movement);
        }
        return movements;

    }

    public Movement createMovement(Movement movement) {
        if(movement.getMovementConnect().getId() == null) {
            throw new IllegalArgumentException("No movementConnect ID");
        }
        try {
            MovementConnect moveConnect = getOrCreateMovementConnectByConnectId(movement.getMovementConnect());

            movement.setMovementConnect(moveConnect);
            return movementDao.createMovement(movement);
        } catch (Exception e) {
            throw new EJBException("Could not create movement.", e);
        }
    }

    public MovementConnect getOrCreateMovementConnectByConnectId(MovementConnect connect) {
        MovementConnect movementConnect;

        if (connect == null) {
            return null;
        }
        movementConnect = movementDao.getMovementConnectByConnectId(connect.getId());

        if (movementConnect == null) {
            LOG.info("Creating new MovementConnect");
            return movementDao.createMovementConnect(connect);
        }
        return movementConnect;
    }

    public GetMovementMapByQueryResponse getMapByQuery(MovementQuery query) {
        return movementMapResponseHelper.getMapByQuery(query);
    }

    public GetMovementListByQueryResponse getList(MovementQuery query){
        if (query == null) {
            throw new IllegalArgumentException("Movement list query is null");
        }
        if (query.getPagination() == null || query.getPagination().getListSize() == null || query.getPagination().getPage() == null) {
            throw new IllegalArgumentException("Pagination in movementlist query is null");
        }
        if (query.getMovementSearchCriteria().isEmpty()) {
            throw new IllegalArgumentException("No search criterias in MovementList query");
        }
        try {
            ListResponseDto response = new ListResponseDto();
            List<MovementType> movementList = new ArrayList<>();

            int page = query.getPagination().getPage().intValue();
            int listSize = query.getPagination().getListSize().intValue();

            List<SearchValue> searchKeyValues = new ArrayList<>();
            List<SearchValue> searchKeyValuesList = SearchFieldMapper.mapListCriteriaToSearchValue(query.getMovementSearchCriteria());
            List<SearchValue> searchKeyValuesRange = SearchFieldMapper.mapRangeCriteriaToSearchField(query.getMovementRangeSearchCriteria());

            searchKeyValues.addAll(searchKeyValuesList);
            searchKeyValues.addAll(searchKeyValuesRange);

            String countSql = SearchFieldMapper.createCountSearchSql(searchKeyValues, true);
            String sql = SearchFieldMapper.createSelectSearchSql(searchKeyValues, true);

            Long numberMatches = movementDao.getMovementListSearchCount(countSql, searchKeyValues);
            List<Movement> movementEntityList = movementDao.getMovementListPaginated(page, listSize, sql, searchKeyValues, Movement.class);

            movementEntityList.forEach(movement -> movementList.add(MovementEntityToModelMapper.mapToMovementType(movement)));

            response.setCurrentPage(BigInteger.valueOf(page));
            response.setMovementList(movementList);
            response.setTotalNumberOfPages(BigInteger.valueOf(getNumberOfPages(numberMatches, listSize)));

            return MovementResponseMapper.createMovementListResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("Error when getting movement list by query: ParseException", e);
        }
    }

    public GetMovementListByQueryResponse getMinimalList(MovementQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Movement list query is null");
        }
        if (query.getPagination() == null || query.getPagination().getListSize() == null || query.getPagination().getPage() == null) {
            throw new IllegalArgumentException("Pagination in movementList query is null");
        }
        if (query.getMovementSearchCriteria() == null) {
            throw new IllegalArgumentException("No search criterias in MovementList query");
        }
        try {
            ListResponseDto response = new ListResponseDto();
            List<MovementType> movementList = new ArrayList<>();

            int page = query.getPagination().getPage().intValue();
            int listSize = query.getPagination().getListSize().intValue();

            List<SearchValue> searchKeyValues = new ArrayList<>();
            List<SearchValue> searchKeyValuesList = SearchFieldMapper.mapListCriteriaToSearchValue(query.getMovementSearchCriteria());
            List<SearchValue> searchKeyValuesRange = SearchFieldMapper.mapRangeCriteriaToSearchField(query.getMovementRangeSearchCriteria());

            searchKeyValues.addAll(searchKeyValuesList);
            searchKeyValues.addAll(searchKeyValuesRange);

            String countSql = SearchFieldMapper.createCountSearchSql(searchKeyValues, true);
            String sql = SearchFieldMapper.createMinimalSelectSearchSql(searchKeyValues, true);

            Long numberMatches = movementDao.getMovementListSearchCount(countSql, searchKeyValues);
            LOG.debug("Count found {} matches", numberMatches);
            List<MinimalMovement> movementEntityList = movementDao.getMovementListPaginated(page, listSize, sql, searchKeyValues, MinimalMovement.class);
            LOG.debug("Get got {} matches", movementEntityList.size());

            movementEntityList.forEach(movement -> movementList.add(MovementEntityToModelMapper.mapToMovementType(movement)));

            response.setCurrentPage(BigInteger.valueOf(page));
            response.setMovementList(movementList);
            response.setTotalNumberOfPages(BigInteger.valueOf(getNumberOfPages(numberMatches, listSize)));
            return MovementResponseMapper.createMovementListResponse(response);
        } catch (Exception ex) {
            throw new RuntimeException("Error when getting movement list by query", ex);
        }
    }

    public Movement getById(UUID id) {
        return movementDao.getMovementByGUID(id);
    }


    private void fireMovementEvent(Movement createdMovement) {
        try {
            createdMovementEvent.fire(createdMovement);
        } catch (Exception e) {
            LOG.error("[ Error when firing notification of created temp movement. ] {}", e.getMessage());
        }
    }

    public List<Movement> getLatestMovementsByConnectIds(List<UUID> connectIds) {
        return movementDao.getLatestMovementsByConnectIdList(connectIds);
    }

    public List<LatestMovement> getLatestMovements(Integer numberOfMovements) {
        return movementDao.getLatestMovements(numberOfMovements);
    }
	
	private int getNumberOfPages(Long numberOfMovements, int listSize){
        int numberOfPages = (int) (numberOfMovements / listSize);
        if (numberOfMovements % listSize != 0) {
            numberOfPages += 1;
        }
        return numberOfPages;
    }
	
    public int countNrOfMovementsLastDayForAsset(String asset, Instant positionTime) {
        return (int) movementDao.countNrOfMovementsForAssetBetween(UUID.fromString(asset), positionTime.minus(1,
                ChronoUnit.DAYS), positionTime);
    }

    public List<Movement> getLatestMovementsLast8Hours() {
        return getLatestMovementsAfter(Instant.now().minus(8, ChronoUnit.HOURS));
    }

    public List<Movement> getLatestMovementsAfter(Instant date) {
        return movementDao.getLatestWithLimit(date);
    }
}
