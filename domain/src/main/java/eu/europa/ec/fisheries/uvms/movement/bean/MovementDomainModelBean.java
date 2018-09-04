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
package eu.europa.ec.fisheries.uvms.movement.bean;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.europa.ec.fisheries.schema.movement.area.v1.AreaType;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementMapResponseType;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSegment;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTrack;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.dao.bean.AreaDaoBean;
import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDaoBean;
import eu.europa.ec.fisheries.uvms.movement.entity.LatestMovement;
import eu.europa.ec.fisheries.uvms.movement.entity.MinimalMovement;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainRuntimeException;
import eu.europa.ec.fisheries.uvms.movement.mapper.AreaMapper;
import eu.europa.ec.fisheries.uvms.movement.mapper.MovementEntityToModelMapper;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchFieldMapper;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchValue;
import eu.europa.ec.fisheries.uvms.movement.model.dto.ListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LocalBean
@Stateless
public class MovementDomainModelBean {

    @EJB
    private MovementDaoBean dao;

    @EJB
    private AreaDaoBean areaDao;

    private static final Logger LOG = LoggerFactory.getLogger(MovementDomainModelBean.class);

    public ListResponseDto getMovementListByQuery(MovementQuery query) throws MovementDomainException, ParseException {

        LOG.debug("Get list of movement from query.");

        if (query == null) {
            throw new MovementDomainRuntimeException("Movement list query is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (query.getPagination() == null || query.getPagination().getListSize() == null || query.getPagination().getPage() == null) {
            throw new MovementDomainRuntimeException("Pagination in movementlist query is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (query.getMovementSearchCriteria().isEmpty()) {
            throw new MovementDomainRuntimeException("No search criterias in MovementList query", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
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

            return response;
        } catch (MovementDomainException | com.vividsolutions.jts.io.ParseException ex) {
            LOG.error("[ Error when getting movement by query ] {} ", ex);
            throw new MovementDomainException("Error when getting movement by query", ex, ErrorCode.PARSING_ERROR);
        }
    }

    public ListResponseDto getMinimalMovementListByQuery(MovementQuery query) throws MovementModelException, MovementDomainException {

        LOG.debug("Get list of movement from query.");

        if (query == null) {
            throw new MovementDomainRuntimeException("Movement list query is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (query.getPagination() == null || query.getPagination().getListSize() == null || query.getPagination().getPage() == null) {
            throw new MovementDomainRuntimeException("Pagination in movementList query is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (query.getMovementSearchCriteria() == null) {
            throw new MovementDomainRuntimeException("No search criterias in MovementList query", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
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
            List<MinimalMovement> movementEntityList = dao.getMinimalMovementListPaginated(page, listSize, sql, searchKeyValues);
            LOG.debug("Get got {} matches", movementEntityList.size());

            for (MinimalMovement move : movementEntityList){
                movementList.add(MovementEntityToModelMapper.mapToMovementType(move));
            }

            response.setCurrentPage(BigInteger.valueOf(page));
            response.setMovementList(movementList);
            response.setTotalNumberOfPages(BigInteger.valueOf(getNumberOfPages(numberMatches, listSize)));

            return response;
        } catch (ParseException ex) {
            LOG.error("[ Error when getting movement by query ] {} ", ex.getMessage());
            throw new MovementDomainException("Error when getting movement by query, ParseException", ex, ErrorCode.PARSING_ERROR);
        } catch (com.vividsolutions.jts.io.ParseException ex) {
            LOG.error("[ Error when getting movement by query, ParseException ] {} ", ex.getMessage());
            throw new MovementDomainException("Error when getting movement by query, ParseException", ex, ErrorCode.PARSING_ERROR);
        }
    }

    private int getNumberOfPages(Long numberOfMovements, int listSize){
        int numberOfPages = (int) (numberOfMovements / listSize);
        if (numberOfMovements % listSize != 0) {
            numberOfPages += 1;
        }
        return numberOfPages;
    }

    public List<MovementMapResponseType> getMovementMapByQuery(MovementQuery query) throws MovementDomainException {
        if (query == null) {
            throw new MovementDomainRuntimeException("Movement list query is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (query.getMovementSearchCriteria() == null) {
            throw new MovementDomainRuntimeException("No search criterias in MovementList query", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (query.getPagination() != null) {
            throw new MovementDomainRuntimeException("Pagination not supported in get movement map by query", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }

        boolean getLatestReports = SearchFieldMapper.containsCriteria(query.getMovementSearchCriteria(), SearchKey.NR_OF_LATEST_REPORTS);

        Integer numberOfLatestReports = 0;
        if (getLatestReports) {
            String value = SearchFieldMapper.getCriteriaValue(query.getMovementSearchCriteria(), SearchKey.NR_OF_LATEST_REPORTS);
            if (value != null) {
                numberOfLatestReports = Integer.valueOf(value);
            } else {
                throw new MovementDomainRuntimeException(SearchKey.NR_OF_LATEST_REPORTS.name()
                        + " Is in the query but no value could be found!, VALUE = null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
            }
        }

        try {

            List<MovementMapResponseType> response = new ArrayList<>();

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

                response.add(responseType);

            }
            return response;
        } catch (ParseException ex) {
            LOG.error("[ Error when getting movement by query ] {} ", ex.getMessage());
            throw new MovementDomainException("Error when getting movement by query", ex, ErrorCode.PARSING_ERROR);
        }
    }

    private void getMovementsByConnectedIds(Integer numberOfLatestReports, List<SearchValue> searchKeys,
                                            List<Movement> movementEntityList, List<SearchValue> connectedIdsFromSearchKeyValues)
            throws ParseException, MovementDomainException {

        String sql;
        List<SearchValue> searchValuesWithoutConnectedIds = removeConnectedIdsFromSearchKeyValues(searchKeys);
        for(SearchValue connectedId : connectedIdsFromSearchKeyValues){
            List<SearchValue> searchValues = new ArrayList<>();
            searchValues.addAll(searchValuesWithoutConnectedIds);
            searchValues.add(connectedId);
            sql = SearchFieldMapper.createSelectSearchSql(searchValues, true);
            movementEntityList.addAll(dao.getMovementList(sql, searchKeys, numberOfLatestReports));
        }
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

    private List<SearchValue> removeConnectedIdsFromSearchKeyValues(List<SearchValue> searchKeyValues){
        List<SearchValue> searchKeyValuesWithoutConnectedId = new ArrayList<>();
        for(SearchValue searchValue : searchKeyValues){
            if(!(SearchField.CONNECT_ID.getFieldName().equals(searchValue.getField().getFieldName()))){
                searchKeyValuesWithoutConnectedId.add(searchValue);
            }
        }
        return searchKeyValuesWithoutConnectedId;
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
            throw new MovementDomainRuntimeException("MovementTrack list or Movement list is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
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

    public List<MovementType> getLatestMovementsByConnectIds(List<String> connectIds) {
        List<Movement> movements = dao.getLatestMovementsByConnectIdList(connectIds);
        return MovementEntityToModelMapper.mapToMovementType(movements);
    }

    public List<MovementType> getLatestMovements(Integer numberOfMovements) {
        List<LatestMovement> movements = dao.getLatestMovements(numberOfMovements);
        return MovementEntityToModelMapper.mapToMovementTypeFromLatestMovement(movements);
    }

    public MovementType getMovementByGUID(String guid)  {
        Movement latestMovements = dao.getMovementByGUID(guid);
        return MovementEntityToModelMapper.mapToMovementType(latestMovements);
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

    public boolean keepSegment(MovementSegment segment, List<SearchValue> searchKeyValuesRange) {

        if (segment == null || searchKeyValuesRange == null) {
            throw new MovementDomainRuntimeException("MovementSegment or SearchValue list is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
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

    /* suggestion for filterimpl . . .
    public boolean keepSegment2(MovementSegment segment, List<SearchValue> searchKeyValuesRange) {

        for (SearchValue searchkey : searchKeyValuesRange) {

            if (searchkey.isRange()) {

                double fromValue = Double.valueOf(searchkey.getFromValue()).doubleValue();
                double toValue = Double.valueOf(searchkey.getFromValue()).doubleValue();

                double value = Double.MIN_VALUE ;
                switch (searchkey.getField()) {
                    case SEGMENT_DURATION:
                        value = segment.getDuration().doubleValue();
                        break;
                    case SEGMENT_LENGTH:
                        value = segment.getDistance().doubleValue();
                        break;
                    case SEGMENT_SPEED:
                        value = segment.getSpeedOverGround().doubleValue();
                        break;
                }

                if (value < fromValue || value > toValue) {
                    return false;
                }
            }
        }
        return true;
    }
    */

    public List<MovementType> getMovementListByAreaAndTimeInterval(MovementAreaAndTimeIntervalCriteria criteria) {
        List<Movement> movementListByAreaAndTimeInterval = dao.getMovementListByAreaAndTimeInterval(criteria);
        return movementListByAreaAndTimeInterval != null ? MovementEntityToModelMapper.mapToMovementType(movementListByAreaAndTimeInterval) : null;
    }

    public List<AreaType> getAreas() {
        List<Area> areas = areaDao.getAreas();
        return AreaMapper.mapToAreaTypes(areas);
    }
}
