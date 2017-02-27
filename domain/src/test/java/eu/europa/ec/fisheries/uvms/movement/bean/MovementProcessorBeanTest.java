/*
 Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
 © European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
 redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
 the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
 copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.movement.bean;

import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.MockData;
import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDaoBean;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

/**
 * Created by osdjup on 2016-12-19.
 */
public class MovementProcessorBeanTest {

    @InjectMocks
    MovementProcessorBean bean;

    @Mock
    MovementDaoBean dao;

    public MovementProcessorBeanTest() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Same area exists in previous movement and is set as an Entry. This should
     * be a simple Position
     */
    @Test
    public void testPopulateTransitions_SAME_ENT() {

        Movement current = MockData.getCurrentMovement(1);
        Movement previous = MockData.getPreviousMovement(1, MovementTypeType.ENT);

        List<Areatransition> transitions = bean.populateTransitions(current, previous);

        Assert.assertNotNull(transitions);
        Assert.assertEquals(1, transitions.size());
        Assert.assertEquals(MovementTypeType.POS, transitions.get(0).getMovementType());

    }

    /**
     * Same area exists in previous movement and is set as an Position. This
     * should be a simple Position
     */
    @Test
    public void testPopulateTransitions_SAME_POS() {

        Movement current = MockData.getCurrentMovement(1);
        Movement previous = MockData.getPreviousMovement(1, MovementTypeType.POS);

        List<Areatransition> transitions = bean.populateTransitions(current, previous);

        Assert.assertNotNull(transitions);
        Assert.assertEquals(1, transitions.size());
        Assert.assertEquals(MovementTypeType.POS, transitions.get(0).getMovementType());

    }

    /**
     * new Area does not exist in the previous movments areatransitions. Previos
     * area should be added as an exit in the current areatransitions
     */
    @Test
    public void testPopulateTransitions_NOT_SAME_ENT() {

        Movement current = MockData.getCurrentMovement(1);
        Movement previous = MockData.getPreviousMovement(2, MovementTypeType.ENT);

        List<Areatransition> transitions = bean.populateTransitions(current, previous);

        Assert.assertNotNull(transitions);
        Assert.assertEquals(2, transitions.size());

        Assert.assertEquals(MovementTypeType.ENT, transitions.get(0).getMovementType());

        Assert.assertEquals(MovementTypeType.EXI, transitions.get(1).getMovementType());
        Assert.assertTrue(transitions.get(1).getAreatranAreaId().getAreaId() == 2);

    }

    /**
     * new Area does not exist in the previous movments areatransitions. Previos
     * area should be added as an exit in the current areatransitions
     */
    @Test
    public void testPopulateTransitions_NOT_SAME_POS() {

        Movement current = MockData.getCurrentMovement(1);
        Movement previous = MockData.getPreviousMovement(2, MovementTypeType.POS);

        List<Areatransition> transitions = bean.populateTransitions(current, previous);

        Assert.assertNotNull(transitions);
        Assert.assertEquals(2, transitions.size());

        Assert.assertEquals(MovementTypeType.ENT, transitions.get(0).getMovementType());

        Assert.assertEquals(MovementTypeType.EXI, transitions.get(1).getMovementType());
        Assert.assertTrue(transitions.get(1).getAreatranAreaId().getAreaId() == 2);

    }

    /**
     * If there are no previois transitions all areaTransitions shall be created
     * as new entitites and set to TransitionType ENT
     */
    @Test
    public void testPopulateTransitionsNoPrevMovement() {

        Movement current = MockData.getCurrentMovement(1);

        List<Areatransition> transitions = bean.populateTransitions(current, null);

        Assert.assertNotNull(transitions);
        Assert.assertEquals(1, transitions.size());

        Assert.assertEquals(MovementTypeType.ENT, transitions.get(0).getMovementType());
        Assert.assertTrue(transitions.get(0).getAreatranAreaId().getAreaId() == 1);

    }
}
