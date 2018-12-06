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

import java.math.BigInteger;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementMapResponseType;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSegment;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTrack;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.longpolling.notifications.NotificationMessage;
import eu.europa.ec.fisheries.uvms.movement.model.dto.ListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.LatestMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MinimalMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedMovement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementDataSourceResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementEntityToModelMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchFieldMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchValue;

@Stateless
public class MovementService {

    private static final Logger LOG = LoggerFactory.getLogger(MovementService.class);


    @EJB
    private MovementBatchModelBean movementBatch;
    
    @Inject
    private IncomingMovementBean incomingMovementBean;

    @Inject
    private MovementDao dao;
    
    @Inject
    private AuditService auditService;

    @Inject
    @CreatedMovement
    private Event<NotificationMessage> createdMovementEvent;

    /**
     *
     * @param movement
     */
    public Movement createMovement(Movement movement) {
        movementBatch.createMovement(movement);
        incomingMovementBean.processMovement(movement);
        if(movement != null){
            fireMovementEvent(movement);
            auditService.sendMovementCreatedAudit(movement, movement.getUpdatedBy());
        }
        return movement;
    }

    public List<Movement> createMovementBatch(List<Movement> movements) {
        for (Movement movement : movements) {
            movementBatch.createMovement(movement);
            incomingMovementBean.processMovement(movement);
        }
        return movements;

    }

    public GetMovementMapByQueryResponse getMapByQuery(MovementQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Movement list query is null");
        }
        if (query.getMovementSearchCriteria() == null) {
            throw new IllegalArgumentException("No search criterias in MovementList query");
        }
        if (query.getPagination() != null) {
            throw new IllegalArgumentException("Pagination not supported in get movement map by query");
        }
        boolean getLatestReports = query.getMovementSearchCriteria()
                .stream()
                .anyMatch(criteria -> criteria.getKey().equals(SearchKey.NR_OF_LATEST_REPORTS));

        int numberOfLatestReports = 0;

        if (getLatestReports) {
            Optional<String> first = query.getMovementSearchCriteria()
                    .stream()
                    .filter(criteria -> criteria.getKey().equals(SearchKey.NR_OF_LATEST_REPORTS))
                    .map(ListCriteria::getValue)
                    .findFirst();
            if (first.isPresent()) {
                numberOfLatestReports = Integer.parseInt(first.get());
            } else {
                throw new IllegalArgumentException(SearchKey.NR_OF_LATEST_REPORTS.name()
                        + " is in the query but no value could be found!, VALUE = null");
            }
        }
        try {
            List<MovementMapResponseType> mapResponse = new ArrayList<>();

            List<SearchValue> searchKeys = new ArrayList<>();
            List<SearchValue> searchKeyValuesList = SearchFieldMapper.mapListCriteriaToSearchValue(query.getMovementSearchCriteria());
            List<SearchValue> searchKeyValuesRange = SearchFieldMapper.mapRangeCriteriaToSearchField(query.getMovementRangeSearchCriteria());

            searchKeys.addAll(searchKeyValuesList);
            searchKeys.addAll(searchKeyValuesRange);

            String sql = SearchFieldMapper.createSelectSearchSql(searchKeys, true);
            List<Movement> movementEntityList = new ArrayList<>();

            if ( numberOfLatestReports > 0) {
                List<SearchValue> connectedIdsFromSearchKeyValues = getConnectedIdsFromSearchKeyValues(searchKeyValuesList);
                if(!connectedIdsFromSearchKeyValues.isEmpty() && connectedIdsFromSearchKeyValues.size()>1) {
                    getMovementsByConnectedIds(numberOfLatestReports, searchKeys, movementEntityList, connectedIdsFromSearchKeyValues);
                }else{
                    movementEntityList = dao.getMovementList(sql, searchKeys, numberOfLatestReports);
                }
            } else {
                movementEntityList = dao.getMovementList(sql, searchKeys);
            }

            Map<UUID, List<Movement>> orderMovementsByConnectId = MovementEntityToModelMapper.orderMovementsByConnectId(movementEntityList);

            for (Map.Entry<UUID, List<Movement>> entries : orderMovementsByConnectId.entrySet()) {

                MovementMapResponseType responseType = new MovementMapResponseType();

                responseType.setKey(entries.getKey().toString());

                ArrayList<Segment> extractSegments = MovementEntityToModelMapper.extractSegments(new ArrayList<>(entries.getValue()), query.isExcludeFirstAndLastSegment());
                List<MovementSegment> segmentList = MovementEntityToModelMapper.mapToMovementSegment(extractSegments);
                List<MovementSegment> filteredSegments = filterSegments(segmentList, searchKeyValuesRange);

                responseType.getSegments().addAll(filteredSegments);

                List<MovementType> mapToMovementType = MovementEntityToModelMapper.mapToMovementType(entries.getValue());
                responseType.getMovements().addAll(mapToMovementType);

                List<MovementTrack> extractTracks = MovementEntityToModelMapper.extractTracks(extractSegments);
                // In the rare event of segments that are attached to two different tracks, the track that is not
                //connected to the any relevant Movement should be removed from the search result.
                removeTrackMismatches(extractTracks, entries.getValue());
                responseType.getTracks().addAll(extractTracks);

                mapResponse.add(responseType);

            }
            return MovementDataSourceResponseMapper.createMovementMapResponse(mapResponse);
        } catch (Exception  ex) {
            throw new RuntimeException("Error when getting movement map by query", ex);
        }
    }
    
    /**
     *
     * @return
     */
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

            Long numberMatches = dao.getMovementListSearchCount(countSql, searchKeyValues);
            List<Movement> movementEntityList = dao.getMovementListPaginated(page, listSize, sql, searchKeyValues, Movement.class);
            //List<Movement> movementEntityList = dao.getMovementList(sql, searchKeyValues);
            //int numberMatches = movementEntityList.size();

            movementEntityList.forEach(movement -> movementList.add(MovementEntityToModelMapper.mapToMovementType(movement)));

            response.setCurrentPage(BigInteger.valueOf(page));
            response.setMovementList(movementList);
            response.setTotalNumberOfPages(BigInteger.valueOf(getNumberOfPages(numberMatches, listSize)));

            return MovementDataSourceResponseMapper.createMovementListResponse(response);
        } catch (ParseException e) {
            throw new RuntimeException("Error when getting movement list by query: ParseException", e);
        }
    }

    /**
     *
     * @return
     */
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

            Long numberMatches = dao.getMovementListSearchCount(countSql, searchKeyValues);
            LOG.debug("Count found {} matches", numberMatches);
            List<MinimalMovement> movementEntityList = dao.getMovementListPaginated(page, listSize, sql, searchKeyValues, MinimalMovement.class);
            LOG.debug("Get got {} matches", movementEntityList.size());

            movementEntityList.forEach(movement -> movementList.add(MovementEntityToModelMapper.mapToMovementType(movement)));

            response.setCurrentPage(BigInteger.valueOf(page));
            response.setMovementList(movementList);
            response.setTotalNumberOfPages(BigInteger.valueOf(getNumberOfPages(numberMatches, listSize)));
            return MovementDataSourceResponseMapper.createMovementListResponse(response);
        } catch (ParseException ex) {
            throw new RuntimeException("Error when getting movement list by query", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param id
     * @return
     */
    public Movement getById(UUID id) {
        return dao.getMovementByGUID(id);
    }


    private void fireMovementEvent(Movement createdMovement) {
        try {
            createdMovementEvent.fire(new NotificationMessage("movementGuid", createdMovement.getId()));
        } catch (Exception e) {
            LOG.error("[ Error when firing notification of created temp movement. ] {}", e.getMessage());
        }
    }

    public List<Movement> getLatestMovementsByConnectIds(List<UUID> connectIds) {
        return dao.getLatestMovementsByConnectIdList(connectIds);
    }

    public List<LatestMovement> getLatestMovements(Integer numberOfMovements) {
        return dao.getLatestMovements(numberOfMovements);
    }
	
	private int getNumberOfPages(Long numberOfMovements, int listSize){
        int numberOfPages = (int) (numberOfMovements / listSize);
        if (numberOfMovements % listSize != 0) {
            numberOfPages += 1;
        }
        return numberOfPages;
    }
	
    private List<SearchValue> getConnectedIdsFromSearchKeyValues(List<SearchValue> searchKeyValues){
        return searchKeyValues.stream()
                .filter(searchValue -> SearchField.CONNECT_ID.getFieldName().equals(searchValue.getField().getFieldName()))
                .collect(Collectors.toList());
    }
    
    /**
     * This method removes track mismatches. These can occur during movement creation but are easier to remove on
     * read than write.
     * 
     * In the rare event of segments that are attached to two different tracks, the track that is not
       connected to the any relevant Movement should be removed from the input list.
     * @param tracks list of tracks to purge
     * @param movements list of movements to look for correct tracks in
     */
    public void removeTrackMismatches(List<MovementTrack> tracks, List<Movement> movements) {
        if(tracks == null || movements == null) {
            throw new IllegalArgumentException("MovementTrack list or Movement list is null");
        }
        Set<String> trackIds = movements.stream()
                .filter(movement -> movement.getTrack() != null)
                .map(movement -> movement.getTrack().getId().toString())
                .collect(Collectors.toSet());

        Set<MovementTrack> tracksToSave = tracks.stream()
                .filter(track -> trackIds.contains(track.getId()))
                .collect(Collectors.toSet());

        tracks.removeIf(track -> !tracksToSave.contains(track));
    }
    
    public List<MovementSegment> filterSegments(List<MovementSegment> movementSegments, List<SearchValue> searchKeyValuesRange) {
        Set<MovementSegment> segments = new HashSet<>();
        if (movementSegments != null) {
            segments = movementSegments.stream()
                    .filter(segment -> keepSegment(segment, searchKeyValuesRange))
                    .collect(Collectors.toSet());
        }
        return new ArrayList<>(segments);
    }
	
    private void getMovementsByConnectedIds(Integer numberOfLatestReports, List<SearchValue> searchKeys,
            List<Movement> movementEntityList, List<SearchValue> connectedIdsFromSearchKeyValues) {

        String sql;
        List<SearchValue> searchValuesWithoutConnectedIds = removeConnectedIdsFromSearchKeyValues(searchKeys);
        for (SearchValue connectedId : connectedIdsFromSearchKeyValues) {
            List<SearchValue> searchValues = new ArrayList<>(searchValuesWithoutConnectedIds);
            searchValues.add(connectedId);
            sql = SearchFieldMapper.createSelectSearchSql(searchValues, true);
            movementEntityList.addAll(dao.getMovementList(sql, searchKeys, numberOfLatestReports));
        }
    }
    
    private List<SearchValue> removeConnectedIdsFromSearchKeyValues(List<SearchValue> searchKeyValues){
        return searchKeyValues
                .stream()
                .filter(searchValue -> !(SearchField.CONNECT_ID.getFieldName().equals(searchValue.getField().getFieldName())))
                .collect(Collectors.toList());
    }
	
	public boolean keepSegment(MovementSegment segment, List<SearchValue> searchKeyValuesRange) {

        if (segment == null || searchKeyValuesRange == null) {
            throw new IllegalArgumentException("MovementSegment or SearchValue list is null");
        }

        for (SearchValue searchValue : searchKeyValuesRange) {

            if (searchValue.isRange() && searchValue.getField().equals(SearchField.SEGMENT_DURATION)) {
                if (segment.getDuration() < Double.valueOf(searchValue.getFromValue())) {
                    return false;
                }
                if (segment.getDuration() > Double.valueOf(searchValue.getToValue())) {
                    return false;
                }
            }

            if (searchValue.isRange() && searchValue.getField().equals(SearchField.SEGMENT_LENGTH)) {
                if (segment.getDistance() < Double.valueOf(searchValue.getFromValue())) {
                    return false;
                }
                if (segment.getDistance() > Double.valueOf(searchValue.getToValue())) {
                    return false;
                }
            }

            if (searchValue.isRange() && searchValue.getField().equals(SearchField.SEGMENT_SPEED)) {
                if (segment.getSpeedOverGround() < Double.valueOf(searchValue.getFromValue())) {
                    return false;
                }
                if (segment.getSpeedOverGround() > Double.valueOf(searchValue.getToValue())) {
                    return false;
                }
            }
        }
        return true;
    }
	
    public int countNrOfMovementsLastDayForAsset(String asset, Instant positionTime) {
        return (int) dao.countNrOfMovementsForAssetBetween(UUID.fromString(asset), positionTime.minus(1,
                ChronoUnit.DAYS), positionTime);
    }
}
