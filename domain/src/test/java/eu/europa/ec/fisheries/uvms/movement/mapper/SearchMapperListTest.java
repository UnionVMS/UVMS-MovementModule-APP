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

import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.exception.SearchMapperException;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchFieldMapper;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchValue;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 **/
public class SearchMapperListTest {

    private static final String GLOBAL_ID = "1";
    private static final String DATE_FROM = "2015-06-08 16:50:00 +02:00";
    private static final String DATE_TO = "2015-06-10 16:50:00 +02:00";

    private static final String DATE_FROM_JPA = ":fromDate ";
    private static final String DATE_TO_JPA = ":toDate ";

    private static final String INITIAL_SELECT = "SELECT DISTINCT m FROM Movement m ";
    private static final String ORDER_BY = " order by m.timestamp desc";
    private static final String WHERE = "WHERE ";
    private static final String AND = " AND ";
    private static final String OR = "OR ";

    private static final String METADATA_JOIN = " LEFT JOIN m.metadata mmd ";
    private static final String ACTIVITY_JOIN = " INNER JOIN m.activity act ";
    private static final String MOVEMENT_CONECT_JOIN = " INNER JOIN m.movementConnect mc ";
    private static final String MOVEMENT_TRACK_JOIN = " INNER JOIN m.track tra ";
    private static final String MOVEMENT_SEGMENT1_JOIN = " LEFT JOIN m.fromSegment seg1 ";
    private static final String MOVEMENT_SEGMENT2_JOIN = " LEFT JOIN m.toSegment seg2 ";

    private static final String MOVEMENT_ID = "m.id = ";
    private static final String SEGMENT_ID = "seg.id = ";
    private static final String TRACK_ID = "tra.id = ";
    private static final String MOVEMENT_CONNECT_ID = "mc.value = ";
    private static final String MOVEMENT_TYPE = "m.movementType = ";
    private static final String ACTIVITY_TYPE = "act.activityType = ";
    private static final String FROM_DATE = "m.timestamp >= ";
    private static final String TO_DATE = "m.timestamp <= ";
    private static final String AREA = "todo = ";
    private static final String SPEED_TYPE = "todo = ";
    private static final String SPEED_MIN = "m.speed >= ";
    private static final String SPEED_MAX = "m.speed <= ";
    private static final String STATUS = "m.status = ";
    private static final String SOURCE = "m.movementSource = ";
    private static final String CATEGORY = "seg.segmentCategory = ";

    /**
     * Helper method
     *
     * @param value
     * @param field
     * @return
     */
    private SearchValue getSearchValue(final String value, final SearchField field) {
        return new SearchValue(field, value);
    }

    /**
     * Helper method
     *
     * @param data
     * @param params
     * @return
     */
    private String revertToInStatement(final String data, final String params) {
        return data.replace("=", "").trim() + getInStatement(params);
    }

    /**
     * Helper method
     *
     * @param where
     * @param joins
     * @return
     */
    private String getSelectString(final String where, final String... joins) {
        final StringBuilder builder = new StringBuilder();
        builder.append(INITIAL_SELECT);

        if (joins != null) {
            for (final String data : joins) {
                builder.append(data);
            }
        }

        builder
                .append(WHERE)
                .append(where)
                .append(ORDER_BY);

        return builder.toString();
    }

    /**
     * Helper method
     *
     * @param values
     * @return
     */
    private String getInStatement(final String values) {
        return " IN ( " + values + " )";
    }

    /**
     * Theese must mach the order in buildJoin method in Search field mapper
     *
     * @return
     */
    private String getAllJoins() {
        return MOVEMENT_CONECT_JOIN + MOVEMENT_TRACK_JOIN + MOVEMENT_SEGMENT1_JOIN + MOVEMENT_SEGMENT2_JOIN + METADATA_JOIN;
    }

    @Ignore
    @Test
    public void testCreateSearchSql() throws ParseException, SearchMapperException {
        final String data = SearchFieldMapper.createSelectSearchSql(null, true);
        Assert.assertEquals(INITIAL_SELECT + ORDER_BY, data);
    }

    @Ignore
    @Test
    public void testMovementId() throws ParseException, SearchMapperException {
        final List<SearchValue> list = new ArrayList<>();
        list.add(getSearchValue(GLOBAL_ID, SearchField.MOVEMENT_ID));
        final String data = SearchFieldMapper.createSelectSearchSql(list, true);
        Assert.assertEquals(getSelectString(MOVEMENT_ID + GLOBAL_ID, METADATA_JOIN), data);
        //Assert.assertEquals(getSelectString(MOVEMENT_ID + GLOBAL_ID, getAllJoins()), data);
    }

    @Ignore
    @Test
    public void testSegmentId() throws ParseException, SearchMapperException {
        final List<SearchValue> list = new ArrayList<>();
        list.add(getSearchValue(GLOBAL_ID, SearchField.SEGMENT_ID));
        final String data = SearchFieldMapper.createSelectSearchSql(list, true);
        //TODO  
        //Assert.assertEquals(getSelectString(SEGMENT_ID + GLOBAL_ID, MOVEMENT_SEGMENT1_JOIN, MOVEMENT_SEGMENT2_JOIN, METADATA_JOIN), data);
        //Assert.assertEquals(getSelectString(SEGMENT_ID + GLOBAL_ID, getAllJoins()), data);
    }

    @Ignore
    @Test
    public void testTrackId() throws ParseException, SearchMapperException {
        final List<SearchValue> list = new ArrayList<>();
        list.add(getSearchValue(GLOBAL_ID, SearchField.TRACK_ID));
        final String data = SearchFieldMapper.createSelectSearchSql(list, true);
        Assert.assertEquals(getSelectString(TRACK_ID + GLOBAL_ID, MOVEMENT_TRACK_JOIN, METADATA_JOIN), data);
        //Assert.assertEquals(getSelectString(TRACK_ID + GLOBAL_ID, getAllJoins()), data);
    }

    @Ignore
    @Test
    public void testConnectId() throws ParseException, SearchMapperException {
        final List<SearchValue> list = new ArrayList<>();
        list.add(getSearchValue(GLOBAL_ID, SearchField.CONNECT_ID));
        final String data = SearchFieldMapper.createSelectSearchSql(list, true);
        //Assert.assertEquals(getSelectString(MOVEMENT_CONNECT_ID + GLOBAL_ID, getAllJoins()), data);
        //Assert.assertEquals(getSelectString(MOVEMENT_CONNECT_ID + GLOBAL_ID, MOVEMENT_CONECT_JOIN, METADATA_JOIN), data);
    }

    @Ignore
    @Test
    public void testGetOrdinalValueFromEnum() throws ParseException, SearchMapperException {

        for (final MovementTypeType mt : MovementTypeType.values()) {
            final Integer data = SearchFieldMapper.getOrdinalValueFromEnum(getSearchValue(mt.name(), SearchField.MOVMENT_TYPE));
            Assert.assertTrue(mt.ordinal() == data);
        }

        for (final MovementActivityTypeType mat : MovementActivityTypeType.values()) {
            final Integer data = SearchFieldMapper.getOrdinalValueFromEnum(getSearchValue(mat.name(), SearchField.ACTIVITY_TYPE));
            Assert.assertTrue(mat.ordinal() == data);
        }

        for (final MovementSourceType mst : MovementSourceType.values()) {
            final Integer data = SearchFieldMapper.getOrdinalValueFromEnum(getSearchValue(mst.name(), SearchField.SOURCE));
            Assert.assertTrue(mst.ordinal() == data);
        }

        for (final SegmentCategoryType sct : SegmentCategoryType.values()) {
            final Integer data = SearchFieldMapper.getOrdinalValueFromEnum(getSearchValue(sct.name(), SearchField.CATEGORY));
            Assert.assertTrue(sct.ordinal() == data);
        }

    }

    @Ignore
    @Test
    public void testMovementType() throws ParseException, SearchMapperException {
        for (final MovementTypeType mt : MovementTypeType.values()) {
            final List<SearchValue> list = new ArrayList<>();
            list.add(getSearchValue(mt.name(), SearchField.MOVMENT_TYPE));
            final String data = SearchFieldMapper.createSelectSearchSql(list, true);
            Assert.assertEquals(getSelectString(MOVEMENT_TYPE + mt.ordinal(), METADATA_JOIN), data);
            //Assert.assertEquals(getSelectString(MOVEMENT_TYPE + mt.ordinal(), getAllJoins()), data);
        }
    }

    @Ignore
    @Test
    public void testActivityType() throws ParseException, SearchMapperException {
        for (final MovementActivityTypeType mt : MovementActivityTypeType.values()) {
            final List<SearchValue> list = new ArrayList<>();
            list.add(getSearchValue(mt.name(), SearchField.ACTIVITY_TYPE));
            final String data = SearchFieldMapper.createSelectSearchSql(list, true);
            Assert.assertEquals(getSelectString(ACTIVITY_TYPE + mt.ordinal(), ACTIVITY_JOIN, METADATA_JOIN), data);
            //Assert.assertEquals(getSelectString(ACTIVITY_TYPE + mt.ordinal(), getAllJoins()), data);
        }
    }

    //TODO FIX AREA IN MAPPER
    @Ignore
    @Test
    public void testArea() throws ParseException, SearchMapperException {
        final List<SearchValue> list = new ArrayList<>();
        list.add(getSearchValue(AREA, SearchField.AREA));
        final String data = SearchFieldMapper.createSelectSearchSql(list, true);
        //Assert.assertEquals(getSelectString(AREA, getAllJoins()), data);
        Assert.assertEquals(getSelectString(AREA, METADATA_JOIN), data);
    }


    @Ignore
    @Test
    public void testSource() throws ParseException, SearchMapperException {
        for (final MovementSourceType mt : MovementSourceType.values()) {
            final List<SearchValue> list = new ArrayList<>();
            list.add(getSearchValue(mt.name(), SearchField.SOURCE));
            final String data = SearchFieldMapper.createSelectSearchSql(list, true);
            Assert.assertEquals(getSelectString(SOURCE + mt.ordinal(), METADATA_JOIN), data);
            //Assert.assertEquals(getSelectString(SOURCE + mt.ordinal(), getAllJoins()), data);
        }
    }

    @Ignore
    @Test
    public void testCategory() throws ParseException, SearchMapperException {
        for (final SegmentCategoryType mt : SegmentCategoryType.values()) {
            final List<SearchValue> list = new ArrayList<>();
            list.add(getSearchValue(mt.name(), SearchField.CATEGORY));
            final String data = SearchFieldMapper.createSelectSearchSql(list, true);

            //TODO  
            //Assert.assertEquals(getSelectString(CATEGORY + mt.ordinal(), getAllJoins()), data);
            //Assert.assertEquals(getSelectString(CATEGORY + mt.ordinal(), METADATA_JOIN), data);
        }
    }

    @Ignore
    @Test
    public void testSingleQueryAllCategory() throws ParseException, SearchMapperException {

        final List<SearchValue> list = new ArrayList<>();
        list.add(getSearchValue(GLOBAL_ID, SearchField.MOVEMENT_ID));
        list.add(getSearchValue(GLOBAL_ID, SearchField.SEGMENT_ID));
        list.add(getSearchValue(GLOBAL_ID, SearchField.TRACK_ID));
        list.add(getSearchValue(GLOBAL_ID, SearchField.CONNECT_ID));
        list.add(getSearchValue(MovementTypeType.ENT.name(), SearchField.MOVMENT_TYPE));
        list.add(getSearchValue(MovementActivityTypeType.ANC.name(), SearchField.ACTIVITY_TYPE));
        //TODO
        //list.add(getSearchValue(AREA, SearchField.AREA));
        list.add(getSearchValue(MovementSourceType.INMARSAT_C.name(), SearchField.SOURCE));
        list.add(getSearchValue(SegmentCategoryType.ANCHORED.name(), SearchField.CATEGORY));

        final String data = SearchFieldMapper.createSelectSearchSql(list, true);

        Assert.assertTrue(StringUtils.countMatches(data, MOVEMENT_ID + GLOBAL_ID) == 1);
        //TODO
        //Assert.assertTrue(StringUtils.countMatches(data, SEGMENT_ID + GLOBAL_ID) == 1);
        Assert.assertTrue(StringUtils.countMatches(data, TRACK_ID + GLOBAL_ID) == 1);
        Assert.assertTrue(StringUtils.countMatches(data, MOVEMENT_CONNECT_ID + "'" + GLOBAL_ID + "'") == 1);
        Assert.assertTrue(StringUtils.countMatches(data, MOVEMENT_TYPE + MovementTypeType.ENT.ordinal()) == 1);
        Assert.assertTrue(StringUtils.countMatches(data, ACTIVITY_TYPE + MovementActivityTypeType.ANC.ordinal()) == 1);
        Assert.assertTrue(StringUtils.countMatches(data, SOURCE + MovementSourceType.INMARSAT_C.ordinal()) == 1);
        //TODO
        //Assert.assertTrue(StringUtils.countMatches(data, CATEGORY + SegmentCategoryType.ANCHORED.ordinal()) == 1);

        Assert.assertTrue(StringUtils.countMatches(data, MOVEMENT_CONECT_JOIN) == 1);
        Assert.assertTrue(StringUtils.countMatches(data, MOVEMENT_TRACK_JOIN) == 1);
        //TODO
        //Assert.assertTrue(StringUtils.countMatches(data, MOVEMENT_SEGMENT1_JOIN) == 1);
        //Assert.assertTrue(StringUtils.countMatches(data, MOVEMENT_SEGMENT2_JOIN) == 1);
        Assert.assertTrue(StringUtils.countMatches(data, METADATA_JOIN) == 1);
        //TODO
        //Assert.assertTrue(StringUtils.countMatches(data, AND) == 11);

    }

    /**
     * If there are several keys of the same type the query should build IN
     * statements
     *
     * @throws ParseException
     * @throws SearchMapperException
     */
    @Ignore
    @Test
    public void testQueryAllCategoryTwoTimes() throws ParseException, SearchMapperException {

        final String ID = "1";
        final String ID2 = "2";

        final List<SearchValue> list = new ArrayList<>();

        //FIRST ROUND
        list.add(getSearchValue(ID, SearchField.MOVEMENT_ID));
        list.add(getSearchValue(ID, SearchField.SEGMENT_ID));
        list.add(getSearchValue(ID, SearchField.TRACK_ID));
        list.add(getSearchValue(ID, SearchField.CONNECT_ID));
        list.add(getSearchValue(MovementTypeType.ENT.name(), SearchField.MOVMENT_TYPE));
        list.add(getSearchValue(MovementActivityTypeType.ANC.name(), SearchField.ACTIVITY_TYPE));
        //TODO
        //list.add(getSearchValue(AREA, SearchField.AREA));
        //list.add(getSearchValue(SPEED_TYPE, SearchField.SPEED_TYPE));
        list.add(getSearchValue(MovementSourceType.INMARSAT_C.name(), SearchField.SOURCE));
        list.add(getSearchValue(SegmentCategoryType.ANCHORED.name(), SearchField.CATEGORY));

        //SECOND ROUND ( DATE_TO, DATE_FROM MAX_SPEED, MIN_SPEED are not added because they can only occur once )
        list.add(getSearchValue(ID2, SearchField.MOVEMENT_ID));
        list.add(getSearchValue(ID2, SearchField.SEGMENT_ID));
        list.add(getSearchValue(ID2, SearchField.TRACK_ID));
        list.add(getSearchValue(ID2, SearchField.CONNECT_ID));
        list.add(getSearchValue(MovementTypeType.EXI.name(), SearchField.MOVMENT_TYPE));
        list.add(getSearchValue(MovementActivityTypeType.CAN.name(), SearchField.ACTIVITY_TYPE));
        //TODO
        //list.add(getSearchValue(AREA, SearchField.AREA));
        //list.add(getSearchValue(SPEED_TYPE, SearchField.SPEED_TYPE));
        list.add(getSearchValue(MovementSourceType.INMARSAT_C.name(), SearchField.SOURCE));
        list.add(getSearchValue(SegmentCategoryType.EXIT_PORT.name(), SearchField.CATEGORY));

        //GET THE LIST
        final String data = SearchFieldMapper.createSelectSearchSql(list, true);

        //ASSERT THE DATA
        final String movement = revertToInStatement(MOVEMENT_ID, ID + ", " + ID2);
        Assert.assertTrue(StringUtils.countMatches(data, movement) == 1);

        final String segment = revertToInStatement(SEGMENT_ID, ID + ", " + ID2);
        /*//System.out.println(data);
         //System.out.println(segment);*/
        //TODO FIX!
        //Assert.assertTrue(StringUtils.countMatches(data, segment) == 1);

        final String track = revertToInStatement(TRACK_ID, ID + ", " + ID2);
        Assert.assertTrue(StringUtils.countMatches(data, track) == 1);

        //String connect = revertToInStatement(MOVEMENT_CONNECT_ID, ID + ", " + ID2);
        //Assert.assertTrue(StringUtils.countMatches(data, connect) == 1);
        final String movementType = revertToInStatement(MOVEMENT_TYPE, MovementTypeType.ENT.ordinal() + ", " + MovementTypeType.EXI.ordinal());
        Assert.assertTrue(StringUtils.countMatches(data, movementType) == 1);

        final String activityType = revertToInStatement(ACTIVITY_TYPE, MovementActivityTypeType.ANC.ordinal() + ", " + MovementActivityTypeType.CAN.ordinal());
        Assert.assertTrue(StringUtils.countMatches(data, activityType) == 1);

        final String movementSource = revertToInStatement(SOURCE, MovementSourceType.INMARSAT_C.ordinal() + ", " + MovementSourceType.INMARSAT_C.ordinal());
        Assert.assertTrue(StringUtils.countMatches(data, movementSource) == 1);

        final String segmentCategory = revertToInStatement(CATEGORY, SegmentCategoryType.ANCHORED.ordinal() + ", " + SegmentCategoryType.EXIT_PORT.ordinal());
        //TODO FIX!
        //Assert.assertTrue(StringUtils.countMatches(data, movementSource) == 1);

        Assert.assertTrue(StringUtils.countMatches(data, MOVEMENT_CONECT_JOIN) == 1);
        Assert.assertTrue(StringUtils.countMatches(data, MOVEMENT_TRACK_JOIN) == 1);
        //TODO        
        //Assert.assertTrue(StringUtils.countMatches(data, MOVEMENT_SEGMENT1_JOIN) == 1);
        //Assert.assertTrue(StringUtils.countMatches(data, MOVEMENT_SEGMENT2_JOIN) == 1);
        Assert.assertTrue(StringUtils.countMatches(data, METADATA_JOIN) == 1);
        //Assert.assertTrue(StringUtils.countMatches(data, AND) == 11);
    }

    @Ignore
    @Test
    public void testSearchFieldSegmentId() throws MovementDaoMappingException, ParseException, SearchMapperException {
        final List<ListCriteria> listCriterias = new ArrayList<>();

        final ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.SEGMENT_ID);
        criteria.setValue(GLOBAL_ID);
        listCriterias.add(criteria);

        final List<SearchValue> mapSearchField = SearchFieldMapper.mapListCriteriaToSearchValue(listCriterias);

        Assert.assertTrue(mapSearchField.size() == 1);

        final String data = SearchFieldMapper.createSelectSearchSql(mapSearchField, true);

    }

    @Ignore
    @Test
    public void testSearchFieldCategory() throws MovementDaoMappingException, ParseException, SearchMapperException {
        final List<ListCriteria> listCriterias = new ArrayList<>();

        final ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CATEGORY);
        criteria.setValue(SegmentCategoryType.ANCHORED.name());
        listCriterias.add(criteria);

        final List<SearchValue> mapSearchField = SearchFieldMapper.mapListCriteriaToSearchValue(listCriterias);

        Assert.assertTrue(mapSearchField.size() == 1);

        final String data = SearchFieldMapper.createSelectSearchSql(mapSearchField, true);
    }
    
    

}