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

import eu.europa.ec.fisheries.schema.movement.area.v1.AreaType;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelRuntimeException;
import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Movementarea;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class MovementFiltererBeanTest extends TransactionalTests {

    private static final String GUID_1 = UUID.randomUUID().toString();
    private static final String GUID_2 = UUID.randomUUID().toString();

    @Inject
    private MovementFiltererBean sut;

    @Inject
    private MovementService movementService;

    @EJB
    private AreaDao areaDao;

    @EJB
    private MovementBatchModelBean movementBatchModelBean;

    @Test(expected = MovementModelRuntimeException.class)
    public void testFilterGuidListWithInvalidArguments(){
        sut.filterGuidListForPeriodAndAreaTypesByArea(Arrays.asList(GUID_1,GUID_2),null,null,null);
    }

    @Test(expected = MovementModelRuntimeException.class)
    public void testFilterGuidListWithValidArgumentsAndExpectMovementModelRuntimeException(){
        final LocalDate todayLD = LocalDate.now();
        final Instant twoMonthsAgo = todayLD.minusMonths(2).atStartOfDay(ZoneId.systemDefault()).toInstant();
        final Instant today = todayLD.atStartOfDay(ZoneId.systemDefault()).toInstant();
        List<AreaType> areaTypes = new ArrayList<>();
        AreaType areaType1 = new AreaType();
        areaType1.setAreaName("a1");
        areaType1.setAreaId(1L);
        AreaType areaType2 = new AreaType();
        areaType2.setAreaName("a2");
        areaType2.setAreaId(2L);
        areaTypes.add(areaType1);
        areaTypes.add(areaType2);

        sut.filterGuidListForPeriodAndAreaTypesByArea(Arrays.asList(GUID_1,GUID_2), Date.from(twoMonthsAgo),Date.from(today),areaTypes);
    }

    @Test
    public void testFilterGuidListWithValidArgumentsAndExpectEmptyList(){
        final LocalDate todayLD = LocalDate.now();
        final Instant twoMonthsAgo = todayLD.minusMonths(2).atStartOfDay(ZoneId.systemDefault()).toInstant();
        final Instant lastMonth = todayLD.minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        final Instant today = todayLD.atStartOfDay(ZoneId.systemDefault()).toInstant();
        final String AREA_TYPE_1 = "AreaType1";
        final String AREA_TYPE_2 = "AreaType2";
        final long REMOTE_ID_1 = 1L;
        final long REMOTE_ID_2 = 2L;
        List<AreaType> areaTypes = new ArrayList<>();
        AreaType areaType1 = new AreaType();
        areaType1.setAreaName(AREA_TYPE_1);
        areaType1.setAreaId(REMOTE_ID_1);
        AreaType areaType2 = new AreaType();
        areaType2.setAreaName(AREA_TYPE_2);
        areaType2.setAreaId(REMOTE_ID_2);
        areaTypes.add(areaType1);
        areaTypes.add(areaType2);

        eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType a1 = MockData.createAreaType(AREA_TYPE_1);
        Area areaA = areaDao.createMovementArea(MockData.createArea(a1,String.valueOf(REMOTE_ID_1)));
        Movement firstMovement = MockData.createMovement(0d, 1d, GUID_1, 0, "TEST");
        firstMovement.setTimestamp(today);
        Movementarea movementArea1 = MockData.getMovementArea(areaA, firstMovement);
        firstMovement.setMovementareaList(singletonList(movementArea1));
        movementBatchModelBean.createMovement(firstMovement);

        assertEquals(0,sut.filterGuidListForPeriodAndAreaTypesByArea(Arrays.asList(GUID_1,GUID_2), Date.from(twoMonthsAgo),Date.from(lastMonth),areaTypes).size());
    }
}
