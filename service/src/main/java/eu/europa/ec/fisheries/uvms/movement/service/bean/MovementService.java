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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import eu.europa.ec.fisheries.uvms.movement.model.exception.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementMapResponseType;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByAreaAndTimeIntervalResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSegment;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTrack;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.longpolling.notifications.NotificationMessage;
import eu.europa.ec.fisheries.uvms.movement.model.dto.ListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.LatestMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedMovement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceRuntimeException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementDataSourceResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementEntityToModelMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchFieldMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchValue;

@Stateless
public class MovementService {

    private static final Logger LOG = LoggerFactory.getLogger(MovementService.class);

    @EJB
    private SpatialService spatial;

    @EJB
    private MovementBatchModelBean movementBatch;
    
    @Inject
    private IncomingMovementBean incomingMovementBean;

    @Inject
    private MovementDao dao;
    
    @Inject
    private AreaDao areaDao;
    
    @Inject
    private AuditService auditService;

    @Inject
    @CreatedMovement
    private Event<NotificationMessage> createdMovementEvent;

    /**
     *
     * @param movement
     * @throws MovementServiceException
     */
    public Movement createMovement(Movement movement) {
        try {
            spatial.enrichMovementWithSpatialData(movement);
            movementBatch.createMovement(movement);
            incomingMovementBean.processMovement(movement);
            if(movement != null){
                fireMovementEvent(movement);
                auditService.sendMovementCreatedAudit(movement, movement.getUpdatedBy());
            }
            return movement;
        } catch (MovementServiceException ex) {
            throw new EJBException(ex);
        }
    }

    public List<MovementAndBaseType> createMovementBatch(List<MovementAndBaseType> movements) throws MovementServiceException {
        try {
            spatial.enrichMovementBatchWithSpatialData(movements.stream().map(MovementAndBaseType::getMovement).collect(Collectors.toList()));
            for (MovementAndBaseType pair : movements) {
                movementBatch.createMovement(pair.getMovement());
                incomingMovementBean.processMovement(pair.getMovement());
            }
            return movements;
        } catch (MovementServiceRuntimeException mdre) {
            throw new MovementServiceException("Didn't find movement connect for the just received movement so NOT going to save anything!", mdre, ErrorCode.MISSING_MOVEMENT_CONNECT_ERROR);
        } catch (MovementServiceException ex) {
            throw new EJBException("createMovementBatch failed", ex);
        }
    }

    public GetMovementMapByQueryResponse getMapByQuery(MovementQuery query) throws MovementServiceException {
        if (query == null) {
            throw new MovementServiceRuntimeException("Movement list query is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (query.getMovementSearchCriteria() == null) {
            throw new MovementServiceRuntimeException("No search criterias in MovementList query", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (query.getPagination() != null) {
            throw new MovementServiceRuntimeException("Pagination not supported in get movement map by query", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
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
                throw new MovementServiceRuntimeException(SearchKey.NR_OF_LATEST_REPORTS.name()
                        + " is in the query but no value could be found!, VALUE = null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
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

            Map<String, List<Movement>> orderMovementsByConnectId = MovementEntityToModelMapper.orderMovementsByConnectId(movementEntityList);

            for (Map.Entry<String, List<Movement>> entries : orderMovementsByConnectId.entrySet()) {

                MovementMapResponseType responseType = new MovementMapResponseType();

                responseType.setKey(entries.getKey());

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
            throw new MovementServiceException("Error when getting movement map by query", ex, ErrorCode.UNSUCCESSFUL_DB_OPERATION);
        }
    }
    
    /**
     *
     * @return
     * @throws MovementServiceException
     */
    public GetMovementListByQueryResponse getList(MovementQuery query) throws MovementServiceException {
        if (query == null) {
            throw new MovementServiceRuntimeException("Movement list query is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (query.getPagination() == null || query.getPagination().getListSize() == null || query.getPagination().getPage() == null) {
            throw new MovementServiceRuntimeException("Pagination in movementlist query is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (query.getMovementSearchCriteria().isEmpty() && query.getMovementRangeSearchCriteria().isEmpty()) {
          throw new MovementServiceRuntimeException("No search criterias in MovementList query", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
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
        } catch (ParseException | com.vividsolutions.jts.io.ParseException e) {
            throw new MovementServiceException("Error when getting movement list by query: ParseException", e, ErrorCode.PARSING_ERROR);
        }
    }

    /**
     *
     * @return
     * @throws MovementServiceException
     */
    public GetMovementListByQueryResponse getMinimalList(MovementQuery query) throws MovementServiceException {
        if (query == null) {
            throw new MovementServiceRuntimeException("Movement list query is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (query.getPagination() == null || query.getPagination().getListSize() == null || query.getPagination().getPage() == null) {
            throw new MovementServiceRuntimeException("Pagination in movementList query is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (query.getMovementSearchCriteria() == null) {
            throw new MovementServiceRuntimeException("No search criterias in MovementList query", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
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
            List<Movement> movementEntityList = dao.getMovementListPaginated(page, listSize, sql, searchKeyValues, Movement.class);
            LOG.debug("Get got {} matches", movementEntityList.size());

            movementEntityList.forEach(movement -> movementList.add(MovementEntityToModelMapper.mapToMinimalMovementType(movement)));

            response.setCurrentPage(BigInteger.valueOf(page));
            response.setMovementList(movementList);
            response.setTotalNumberOfPages(BigInteger.valueOf(getNumberOfPages(numberMatches, listSize)));
            return MovementDataSourceResponseMapper.createMovementListResponse(response);
        } catch (ParseException | com.vividsolutions.jts.io.ParseException ex) {
            throw new MovementServiceException("Error when getting movement list by query", ex, ErrorCode.PARSING_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param id
     * @return
     * @throws MovementServiceException
     */
    public Movement getById(String id) {
        return dao.getMovementByGUID(id);
    }

    public List<Movement> findMovementsByGUIDList(List<String> guidList) {
        List<Movement> movements = dao.findMovementsByGUIDList(guidList);

        return movements;
    }


    private void fireMovementEvent(Movement createdMovement) {
        try {
            createdMovementEvent.fire(new NotificationMessage("movementGuid", createdMovement.getGuid()));
        } catch (Exception e) {
            LOG.error("[ Error when firing notification of created temp movement. ] ", e);
        }
    }

    public List<Movement> getLatestMovementsByConnectIds(List<String> connectIds) {
        return dao.getLatestMovementsByConnectIdList(connectIds);
    }

    public List<LatestMovement> getLatestMovements(Integer numberOfMovements) {
        return dao.getLatestMovements(numberOfMovements);
    }

    public GetMovementListByAreaAndTimeIntervalResponse getMovementListByAreaAndTimeInterval(MovementAreaAndTimeIntervalCriteria criteria) {
        List<Movement> movements = dao.getMovementListByAreaAndTimeInterval(criteria);
        List<MovementType> movementListByAreaAndTimeInterval = movements != null ? MovementEntityToModelMapper.mapToMovementType(movements) : null;
        if (movementListByAreaAndTimeInterval == null) {
            throw new MovementServiceRuntimeException("Error when getting movement list by area and time interval", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        return MovementDataSourceResponseMapper.mapMovementListAreaAndTimeIntervalResponse(movementListByAreaAndTimeInterval);
    }

	public List<Area> getAreas() {
	    return areaDao.getAreas();
	}

    public Area getAreaByCode(String code) {
        return areaDao.getAreaByCode(code);
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
            throw new MovementServiceRuntimeException("MovementTrack list or Movement list is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        Set<Long> trackIds = movements.stream()
                .filter(movement -> movement.getTrack() != null)
                .map(movement -> movement.getTrack().getId())
                .collect(Collectors.toSet());

        Set<MovementTrack> tracksToSave = tracks.stream()
                .filter(track -> trackIds.contains(Long.valueOf(track.getId())))
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
            List<Movement> movementEntityList, List<SearchValue> connectedIdsFromSearchKeyValues) throws ParseException, MovementServiceException {

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
            throw new MovementServiceRuntimeException("MovementSegment or SearchValue list is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
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

    /**
     * Gets list of connect ids (movement connect values) based on arguments
     * @param inList List of connect ids to filter query
     * @param startDate Movement timestamp lower bound
     * @param endDate Movement timestamp upper bound
     * @param areasGeometryUnion An geometry which will be intersected with movement location 
     * @param page Page number
     * @param limit Records limit
     * @return List of movement connect values
     * @throws MovementServiceRuntimeException If arguments are invalid
     */
    public List<String> findConnectIdsByDateAndGeometry(List<String> inList, Date startDate, Date endDate, String areasGeometryUnion,Integer page,Integer limit) throws MovementServiceRuntimeException {
        checkArguments( startDate, endDate, areasGeometryUnion);
        return dao.findConnectIdsByDateAndGeometry(inList,startDate,endDate,areasGeometryUnion,page,limit);
    }
    private void checkArguments( Date startDate, Date endDate, String areasGeometryUnion) {
        if(startDate == null) throw new InvalidArgumentException("No start date provided/or invalid syntax, try UTC");
        if(endDate == null) throw new InvalidArgumentException("No end date provided/or invalid syntax, try UTC");
        if(startDate.toInstant().isAfter(endDate.toInstant())) throw new InvalidArgumentException("Start date cannot be after end date");
        if(areasGeometryUnion == null) throw new InvalidArgumentException("AreasGeometryUnion was null");
    }
}
