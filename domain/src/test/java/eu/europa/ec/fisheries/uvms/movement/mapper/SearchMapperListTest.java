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
package eu.europa.ec.fisheries.uvms.movement.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;

import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.exception.SearchMapperException;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchFieldMapper;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchValue;

/**
 **/
@RunWith(Arquillian.class)
public class SearchMapperListTest extends TransactionalTests {

    private static final String GLOBAL_ID = "1";
    private static final String INITIAL_SELECT = "SELECT DISTINCT  m FROM Movement m ";
    private static final String ORDER_BY = " ORDER BY m.timestamp DESC ";
    private static final String NO_DUPLICATE = "WHERE  m.duplicate = false ";

    /**
     * Helper method
     *
     * @param value
     * @param field
     * @return
     */
    private SearchValue getSearchValue(String value, SearchField field) {
        return new SearchValue(field, value);
    }


    @Test
    public void testCreateSearchSql() throws ParseException, SearchMapperException {
        String data = SearchFieldMapper.createSelectSearchSql(null, true);
        assertEquals(INITIAL_SELECT +NO_DUPLICATE + ORDER_BY, data);
    }

    
    @Test
    public void testGetOrdinalValueFromEnum() throws ParseException, SearchMapperException {

        for (MovementTypeType mt : MovementTypeType.values()) {
            Integer data = SearchFieldMapper.getOrdinalValueFromEnum(getSearchValue(mt.name(), SearchField.MOVMENT_TYPE));
            assertTrue(mt.ordinal() == data);
        }

        for (MovementActivityTypeType mat : MovementActivityTypeType.values()) {
            Integer data = SearchFieldMapper.getOrdinalValueFromEnum(getSearchValue(mat.name(), SearchField.ACTIVITY_TYPE));
            assertTrue(mat.ordinal() == data);
        }

        for (MovementSourceType mst : MovementSourceType.values()) {
            Integer data = SearchFieldMapper.getOrdinalValueFromEnum(getSearchValue(mst.name(), SearchField.SOURCE));
            assertTrue(mst.ordinal() == data);
        }

        for (SegmentCategoryType sct : SegmentCategoryType.values()) {
            Integer data = SearchFieldMapper.getOrdinalValueFromEnum(getSearchValue(sct.name(), SearchField.CATEGORY));
            assertTrue(sct.ordinal() == data);
        }

    }

    
    @Test
    public void testSearchFieldSegmentId() throws MovementDaoMappingException, ParseException, SearchMapperException {
        List<ListCriteria> listCriterias = new ArrayList<>();

        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.SEGMENT_ID);
        criteria.setValue(GLOBAL_ID);
        listCriterias.add(criteria);

        List<SearchValue> mapSearchField = SearchFieldMapper.mapListCriteriaToSearchValue(listCriterias);

        assertTrue(mapSearchField.size() == 1);

        String data = SearchFieldMapper.createSelectSearchSql(mapSearchField, true);
        assertEquals("SELECT DISTINCT  m FROM Movement m INNER JOIN FETCH m.movementConnect mc  LEFT JOIN FETCH m.activity act  LEFT JOIN FETCH m.track tra  LEFT JOIN FETCH m.fromSegment fromSeg  LEFT JOIN FETCH m.toSegment toSeg  LEFT JOIN FETCH m.metadata mmd  LEFT JOIN FETCH m.movementareaList marea  LEFT JOIN FETCH marea.movareaAreaId area  LEFT JOIN FETCH area.areaType mareatype  WHERE  ( toSeg.id = 1 OR fromSeg.id = 1 )  AND  m.duplicate = false  ORDER BY m.timestamp DESC ",data);

    }

    
    @Test
    public void testSearchFieldCategory() throws MovementDaoMappingException, ParseException, SearchMapperException {
        List<ListCriteria> listCriterias = new ArrayList<>();

        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CATEGORY);
        criteria.setValue(SegmentCategoryType.ANCHORED.name());
        listCriterias.add(criteria);

        List<SearchValue> mapSearchField = SearchFieldMapper.mapListCriteriaToSearchValue(listCriterias);

        assertTrue(mapSearchField.size() == 1);

        String data = SearchFieldMapper.createSelectSearchSql(mapSearchField, true);
        assertEquals("SELECT DISTINCT  m FROM Movement m INNER JOIN FETCH m.movementConnect mc  LEFT JOIN FETCH m.activity act  LEFT JOIN FETCH m.track tra  LEFT JOIN FETCH m.fromSegment fromSeg  LEFT JOIN FETCH m.toSegment toSeg  LEFT JOIN FETCH m.metadata mmd  LEFT JOIN FETCH m.movementareaList marea  LEFT JOIN FETCH marea.movareaAreaId area  LEFT JOIN FETCH area.areaType mareatype  WHERE  ( toSeg.segmentCategory = 6 OR fromSeg.segmentCategory = 6 )  AND  m.duplicate = false  ORDER BY m.timestamp DESC ",data); 
    }
    
    
    @Test
    public void testCreateMinimalSelectSearchSql() throws MovementDaoMappingException, SearchMapperException, ParseException {
    	List<ListCriteria> listCriterias = new ArrayList<>();

        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CATEGORY);
        criteria.setValue(SegmentCategoryType.ANCHORED.name());
        listCriterias.add(criteria);

        List<SearchValue> mapSearchField = SearchFieldMapper.mapListCriteriaToSearchValue(listCriterias);

        assertTrue(mapSearchField.size() == 1);

        String data = SearchFieldMapper.createMinimalSelectSearchSql(mapSearchField, true);
        assertEquals("SELECT DISTINCT  m FROM MinimalMovement m INNER JOIN FETCH m.movementConnect mc  WHERE  ( toSeg.segmentCategory = 6 OR fromSeg.segmentCategory = 6 )  AND  m.duplicate = false  ORDER BY m.timestamp DESC ", data);
    }
    
    
    @Test
    public void testMultipleSearchFieldCategorys() throws MovementDaoMappingException, SearchMapperException, ParseException {
    	List<ListCriteria> listCriterias = new ArrayList<>();

        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CATEGORY);
        criteria.setValue(SegmentCategoryType.ANCHORED.name());
        listCriterias.add(criteria);
        
        criteria = new ListCriteria();
        criteria.setKey(SearchKey.SOURCE);
        criteria.setValue(MovementSourceType.MANUAL.name());
        listCriterias.add(criteria);

        List<SearchValue> mapSearchField = SearchFieldMapper.mapListCriteriaToSearchValue(listCriterias);

        assertTrue(mapSearchField.size() == 2);

        String data = SearchFieldMapper.createSelectSearchSql(mapSearchField, false);
        System.out.println(data);
        String correctOutput = "SELECT DISTINCT  m FROM Movement m INNER JOIN FETCH m.movementConnect mc  LEFT JOIN FETCH m.activity act  LEFT JOIN FETCH m.track tra "
        		+ " LEFT JOIN FETCH m.fromSegment fromSeg  LEFT JOIN FETCH m.toSegment toSeg  LEFT JOIN FETCH m.metadata mmd  LEFT JOIN FETCH m.movementareaList marea "
        		+ " LEFT JOIN FETCH marea.movareaAreaId area  LEFT JOIN FETCH area.areaType mareatype  WHERE m.movementSource = 3 OR  ( toSeg.segmentCategory = 6 OR"
        		+ " fromSeg.segmentCategory = 6 )  AND  m.duplicate = false  ORDER BY m.timestamp DESC "; 
        assertEquals(correctOutput, data);
    }
    
    @Test
    public void testCreateCountSearchSql() throws MovementDaoMappingException, SearchMapperException, ParseException {
    	List<ListCriteria> listCriterias = new ArrayList<>();

        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.SOURCE);
        criteria.setValue(MovementSourceType.MANUAL.name());
        listCriterias.add(criteria);

        List<SearchValue> mapSearchField = SearchFieldMapper.mapListCriteriaToSearchValue(listCriterias);

        assertTrue(mapSearchField.size() == 1);

        String data = SearchFieldMapper.createCountSearchSql(mapSearchField, true);
        String correctOutput = "SELECT COUNT(DISTINCT m) FROM Movement m  INNER JOIN m.movementConnect mc  LEFT JOIN m.activity act  LEFT JOIN m.track tra "
        		+ " LEFT JOIN m.fromSegment fromSeg  LEFT JOIN m.toSegment toSeg  LEFT JOIN m.metadata mmd  LEFT JOIN m.movementareaList marea "
        		+ " LEFT JOIN marea.movareaAreaId area  LEFT JOIN area.areaType mareatype  WHERE m.movementSource = 3 AND  m.duplicate = false ";
        assertEquals(correctOutput, data);
    }

}