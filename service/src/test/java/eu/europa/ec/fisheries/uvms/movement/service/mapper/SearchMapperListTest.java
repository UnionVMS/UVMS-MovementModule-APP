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
package eu.europa.ec.fisheries.uvms.movement.service.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchFieldMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.search.SearchValue;

/**
 **/
@RunWith(Arquillian.class)
public class SearchMapperListTest extends TransactionalTests {

    private static final String INITIAL_SELECT = "SELECT  m FROM Movement m ";
    private static final String ORDER_BY = "ORDER BY m.timestamp DESC ";
    private static final String NO_DUPLICATE = "";

    @Test
    @OperateOnDeployment("movementservice")
    public void testCreateSearchSql() throws Exception {
        String data = SearchFieldMapper.createSelectSearchSql(null, true);
        assertEquals(INITIAL_SELECT +NO_DUPLICATE + ORDER_BY, data);
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testGetOrdinalValueFromEnum() {

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
    @OperateOnDeployment("movementservice")
    public void testCreateMinimalSelectSearchSql() throws Exception {
    	List<ListCriteria> listCriterias = new ArrayList<>();

        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CATEGORY);
        criteria.setValue(SegmentCategoryType.ANCHORED.name());
        listCriterias.add(criteria);

        List<SearchValue> mapSearchField = SearchFieldMapper.mapListCriteriaToSearchValue(listCriterias);

        assertTrue(mapSearchField.size() == 1);

        String data = SearchFieldMapper.createMinimalSelectSearchSql(mapSearchField, true);
        assertEquals("SELECT  m FROM MinimalMovement m INNER JOIN FETCH m.movementConnect mc  WHERE  ( toSeg.segmentCategory = 6 OR fromSeg.segmentCategory = 6 )  ORDER BY m.timestamp DESC ", data);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void testMultipleSearchFieldCategorys() throws Exception {
    	List<ListCriteria> listCriterias = new ArrayList<>();

        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.STATUS);
        criteria.setValue("11");
        listCriterias.add(criteria);
        
        criteria = new ListCriteria();
        criteria.setKey(SearchKey.SOURCE);
        criteria.setValue(MovementSourceType.MANUAL.name());
        listCriterias.add(criteria);

        List<SearchValue> mapSearchField = SearchFieldMapper.mapListCriteriaToSearchValue(listCriterias);

        assertTrue(mapSearchField.size() == 2);

        String data = SearchFieldMapper.createSelectSearchSql(mapSearchField, false);
        System.out.println(data);
        String correctOutput = "SELECT  m FROM Movement m INNER JOIN FETCH m.movementConnect mc  LEFT JOIN FETCH m.activity act  LEFT JOIN FETCH m.track tra "
        		+ " WHERE m.movementSource = 3 OR m.status = '11'"
        		+ " ORDER BY m.timestamp DESC ";
        assertEquals(correctOutput, data);
    }
    
    @Test
    @OperateOnDeployment("movementservice")
    public void testCreateCountSearchSql() throws Exception {
    	List<ListCriteria> listCriterias = new ArrayList<>();

        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.SOURCE);
        criteria.setValue(MovementSourceType.MANUAL.name());
        listCriterias.add(criteria);

        List<SearchValue> mapSearchField = SearchFieldMapper.mapListCriteriaToSearchValue(listCriterias);

        assertTrue(mapSearchField.size() == 1);

        String data = SearchFieldMapper.createCountSearchSql(mapSearchField, true);
        String correctOutput = "SELECT COUNT( m) FROM Movement m  INNER JOIN m.movementConnect mc  LEFT JOIN m.activity act  LEFT JOIN m.track tra "
        		+ " WHERE m.movementSource = 3";
        assertEquals(correctOutput, data);
    }

    private SearchValue getSearchValue(String value, SearchField field) {
        return new SearchValue(field, value);
    }
}
