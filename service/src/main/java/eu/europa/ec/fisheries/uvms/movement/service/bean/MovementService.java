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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.area.v1.AreaType;
import eu.europa.ec.fisheries.schema.movement.common.v1.SimpleResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchResponse;
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
import eu.europa.ec.fisheries.uvms.movement.service.SpatialService;
import eu.europa.ec.fisheries.uvms.movement.service.bean.mapper.MovementDataSourceResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.LatestMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MinimalMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedMovement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceRuntimeException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.AreaMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementEntityToModelMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
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
    public Movement createMovement(Movement movement, String username) {
        try {
            //enrich with closest port, closest country and areas
            Movement enrichedMovement = spatial.enrichMovementWithSpatialData(movement);
            Movement createdMovement = movementBatch.createMovement(enrichedMovement);
            incomingMovementBean.processMovement(createdMovement);
            if(createdMovement != null){
                fireMovementEvent(createdMovement);
                auditService.sendMovementCreatedAudit(createdMovement, username);
            }
            return createdMovement;
        } catch (MovementServiceException ex) {
            throw new EJBException(ex);
        }
    }

    public CreateMovementBatchResponse createMovementBatch(List<Movement> movements, String username) {
        LOG.debug("Create invoked in service layer");
        try {
            LOG.debug("ENRICHING MOVEMENTS BATCH WITH SPATIAL DATA");
            List<Movement> enrichedMovements = spatial.enrichMovementBatchWithSpatialData(movements);
            List<Movement> savedBatchMovements = new ArrayList<>();
            for (Movement enrichedMovement : enrichedMovements) {
                savedBatchMovements.add(movementBatch.createMovement(enrichedMovement));
            }
            SimpleResponse simpleResponse = CollectionUtils.isNotEmpty(savedBatchMovements) ? SimpleResponse.OK : SimpleResponse.NOK;
            auditService.sendMovementBatchCreatedAudit(simpleResponse.name(), username);
            CreateMovementBatchResponse createMovementBatchResponse = new CreateMovementBatchResponse();
            createMovementBatchResponse.setResponse(simpleResponse);
            createMovementBatchResponse.getMovements().addAll(MovementEntityToModelMapper.mapToMovementType(savedBatchMovements));
            return createMovementBatchResponse;
        } catch (MovementServiceRuntimeException mdre) {
            LOG.warn("Didn't find movement connect for the just received movement so NOT going to save anything!");
            CreateMovementBatchResponse createMovementBatchResponse = new CreateMovementBatchResponse();
            createMovementBatchResponse.setResponse(SimpleResponse.NOK);
            return createMovementBatchResponse;
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

        boolean getLatestReports = SearchFieldMapper.containsCriteria(query.getMovementSearchCriteria(), SearchKey.NR_OF_LATEST_REPORTS);

        Integer numberOfLatestReports = 0;
        if (getLatestReports) {
            String value = SearchFieldMapper.getCriteriaValue(query.getMovementSearchCriteria(), SearchKey.NR_OF_LATEST_REPORTS);
            if (value != null) {
                numberOfLatestReports = Integer.valueOf(value);
            } else {
                throw new MovementServiceRuntimeException(SearchKey.NR_OF_LATEST_REPORTS.name()
                        + " Is in the query but no value could be found!, VALUE = null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
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
        if (query.getMovementSearchCriteria().isEmpty()) {
            throw new MovementServiceRuntimeException("No search criterias in MovementList query", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        try {
            ListResponseDto response = new ListResponseDto();
            List<MovementType> movementList = new ArrayList<>();

            Integer page = query.getPagination().getPage().intValue();
            Integer listSize = query.getPagination().getListSize().intValue();

            List<SearchValue> searchKeyValues = new ArrayList<>();
            List<SearchValue> searchKeyValuesList = SearchFieldMapper.mapListCriteriaToSearchValue(query.getMovementSearchCriteria());
            List<SearchValue> searchKeyValuesRange = SearchFieldMapper.mapRangeCriteriaToSearchField(query.getMovementRangeSearchCriteria());

            searchKeyValues.addAll(searchKeyValuesList);
            searchKeyValues.addAll(searchKeyValuesRange);

            String countSql = SearchFieldMapper.createCountSearchSql(searchKeyValues, true);
            String sql = SearchFieldMapper.createSelectSearchSql(searchKeyValues, true);

            Long numberMatches = dao.getMovementListSearchCount(countSql, searchKeyValues);
            List<Movement> movementEntityList = dao.getMovementListPaginated(page, listSize, sql, searchKeyValues);
            //List<Movement> movementEntityList = dao.getMovementList(sql, searchKeyValues);
            //int numberMatches = movementEntityList.size();

            for (Movement move : movementEntityList){
                movementList.add(MovementEntityToModelMapper.mapToMovementType(move));
            }

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

            Integer page = query.getPagination().getPage().intValue();
            Integer listSize = query.getPagination().getListSize().intValue();

            List<SearchValue> searchKeyValues = new ArrayList<>();
            List<SearchValue> searchKeyValuesList = SearchFieldMapper.mapListCriteriaToSearchValue(query.getMovementSearchCriteria());
            List<SearchValue> searchKeyValuesRange = SearchFieldMapper.mapRangeCriteriaToSearchField(query.getMovementRangeSearchCriteria());

            searchKeyValues.addAll(searchKeyValuesList);
            searchKeyValues.addAll(searchKeyValuesRange);

            String countSql = SearchFieldMapper.createCountSearchSql(searchKeyValues, true);
            String sql = SearchFieldMapper.createMinimalSelectSearchSql(searchKeyValues, true);

            Long numberMatches = dao.getMovementListSearchCount(countSql, searchKeyValues);
            LOG.debug("Count found {} matches", numberMatches);
            List<MinimalMovement> movementEntityList = dao.getMovementListPaginated(page, listSize, sql, searchKeyValues);
            LOG.debug("Get got {} matches", movementEntityList.size());

            for (MinimalMovement move : movementEntityList){
                movementList.add(MovementEntityToModelMapper.mapToMovementType(move));
            }

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
    public MovementType getById(String id) {
        Movement latestMovements = dao.getMovementByGUID(id);
        MovementType response = MovementEntityToModelMapper.mapToMovementType(latestMovements);

        if (response == null) {
            throw new MovementServiceRuntimeException("Error when getting movement by id: " + id, ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        return response;
    }


    private void fireMovementEvent(Movement createdMovement) {
        try {
            createdMovementEvent.fire(new NotificationMessage("movementGuid", createdMovement.getGuid()));
        } catch (Exception e) {
            LOG.error("[ Error when firing notification of created temp movement. ] {}", e.getMessage());
        }
    }

    public List<MovementDto> getLatestMovementsByConnectIds(List<String> connectIds) {
        List<Movement> movements = dao.getLatestMovementsByConnectIdList(connectIds);
        List<MovementType> latestMovements = MovementEntityToModelMapper.mapToMovementType(movements);
        return MovementMapper.mapToMovementDtoList(latestMovements);
    }

    public List<MovementDto> getLatestMovements(Integer numberOfMovements) {
        List<LatestMovement> movements = dao.getLatestMovements(numberOfMovements);
        List<MovementType> latestMovements = MovementEntityToModelMapper.mapToMovementTypeFromLatestMovement(movements);
        return MovementMapper.mapToMovementDtoList(latestMovements);
    }

    public GetMovementListByAreaAndTimeIntervalResponse getMovementListByAreaAndTimeInterval(MovementAreaAndTimeIntervalCriteria criteria) {
        List<Movement> movements = dao.getMovementListByAreaAndTimeInterval(criteria);
        List<MovementType> movementListByAreaAndTimeInterval = movements != null ? MovementEntityToModelMapper.mapToMovementType(movements) : null;
        if (movementListByAreaAndTimeInterval == null) {
            throw new MovementServiceRuntimeException("Error when getting movement list by area and time interval", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        return MovementDataSourceResponseMapper.mapMovementListAreaAndTimeIntervalResponse(movementListByAreaAndTimeInterval);
    }

	public List<AreaType> getAreas() {
	    List<Area> areas = areaDao.getAreas();
        return AreaMapper.mapToAreaTypes(areas);
	}
	
	private int getNumberOfPages(Long numberOfMovements, int listSize){
        int numberOfPages = (int) (numberOfMovements / listSize);
        if (numberOfMovements % listSize != 0) {
            numberOfPages += 1;
        }
        return numberOfPages;
    }
	
    private List<SearchValue> getConnectedIdsFromSearchKeyValues(List<SearchValue> searchKeyValues){
        List<SearchValue> connetedIds = new ArrayList<>();
        for(SearchValue searchValue : searchKeyValues){
            if(SearchField.CONNECT_ID.getFieldName().equals(searchValue.getField().getFieldName())){
                connetedIds.add(searchValue);
            }
        }
        return connetedIds;
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
        Set<MovementTrack> tracksToSave = new HashSet<>();
        for (Movement movement : movements) {
            if (movement.getTrack() == null) {
                continue;
            }
            Long allowedTrackId = movement.getTrack().getId();
            for (MovementTrack track : tracks) {
                if (Long.valueOf(track.getId()).equals(allowedTrackId)) {
                    tracksToSave.add(track);
                }
            }
        }
        tracks.removeIf(track -> !tracksToSave.contains(track));
    }
    
    public ArrayList<MovementSegment> filterSegments(List<MovementSegment> movementSegments, List<SearchValue> searchKeyValuesRange) {
        Set<MovementSegment> segments = new HashSet<>();
        if (movementSegments != null) {
            for (MovementSegment segment : movementSegments) {
                if (keepSegment(segment, searchKeyValuesRange)) {
                    segments.add(segment);
                }
            }
        }
        return new ArrayList<>(segments);
    }
	
    private void getMovementsByConnectedIds(Integer numberOfLatestReports, List<SearchValue> searchKeys,
            List<Movement> movementEntityList, List<SearchValue> connectedIdsFromSearchKeyValues) throws ParseException, MovementServiceException {

        String sql;
        List<SearchValue> searchValuesWithoutConnectedIds = removeConnectedIdsFromSearchKeyValues(searchKeys);
        for (SearchValue connectedId : connectedIdsFromSearchKeyValues) {
            List<SearchValue> searchValues = new ArrayList<>();
            searchValues.addAll(searchValuesWithoutConnectedIds);
            searchValues.add(connectedId);
            sql = SearchFieldMapper.createSelectSearchSql(searchValues, true);
            movementEntityList.addAll(dao.getMovementList(sql, searchKeys, numberOfLatestReports));
        }
    }
    
    private List<SearchValue> removeConnectedIdsFromSearchKeyValues(List<SearchValue> searchKeyValues){
        List<SearchValue> searchKeyValuesWithoutConnectedId = new ArrayList<>();
        for(SearchValue searchValue : searchKeyValues){
            if(!(SearchField.CONNECT_ID.getFieldName().equals(searchValue.getField().getFieldName()))){
                searchKeyValuesWithoutConnectedId.add(searchValue);
            }
        }
        return searchKeyValuesWithoutConnectedId;
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
}
