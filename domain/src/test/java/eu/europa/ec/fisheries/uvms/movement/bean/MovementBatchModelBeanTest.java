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

import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.MockData;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 **/
public class MovementBatchModelBeanTest {

    @InjectMocks
    private MovementBatchModelBean bean;

    public MovementBatchModelBeanTest() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Update movement metadata with added transitions that is not yet added to
     * movement metadata When the area is same no addition should be made to
     * the areas in metadata when areas are equal
     */
    @Test
    public void testEnrichAreasSameArea() {
        MovementType mappedMovement = MockData.getMappedMovement(2);
        List<Areatransition> transitions = new ArrayList<>();
        transitions.add(MockData.getAreaTransition("AREA1", MovementTypeType.ENT));
        bean.enrichAreas(mappedMovement, transitions);
        assertEquals(" AreaSize should be 2", 2, mappedMovement.getMetaData().getAreas().size());
    }

    /**
     * Update movement metadata with added transitions that is not yet added to
     * movement metadata When the area is not same an addition should be made
     * to the areas in metadata when areas are not equal
     */
    @Test
    public void testEnrichAreasNotSameArea() {
        MovementType mappedMovement = MockData.getMappedMovement(2);
        List<Areatransition> transitions = new ArrayList<>();
        transitions.add(MockData.getAreaTransition("AREA3", MovementTypeType.ENT));
        bean.enrichAreas(mappedMovement, transitions);
        assertEquals("AreaSize should be 3", 3,mappedMovement.getMetaData().getAreas().size());
    }

}
