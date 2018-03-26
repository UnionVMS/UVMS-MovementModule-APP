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

import eu.europa.ec.fisheries.uvms.movement.util.CalculationUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MovementDomainModelBeanBeanTest {

    @Test
    public void north() {
        Double expected = 0.0;

        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = 0.0;
        Double thisLat = 1.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);

        assertEquals(expected, actual);
    }

    @Test
    public void northEast() {
        Double expected = 44.99563645534488;

        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = 1.0;
        Double thisLat = 1.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);

        assertEquals(expected, actual);
    }

    @Test
    public void east() {
        Double expected = 90.0;

        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = 1.0;
        Double thisLat = 0.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);

        assertEquals(expected, actual);
    }

    @Test
    public void southEast() {
        Double expected = 135.00436354465512;

        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = 1.0;
        Double thisLat = -1.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);

        assertEquals(expected, actual);
    }

    @Test
    public void south() {
        Double expected = 180.0;

        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = 0.0;
        Double thisLat = -1.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);
        assertEquals(expected, actual);
    }

    @Test
    public void southWest() {
        Double expected = 224.99563645534485;

        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = -1.0;
        Double thisLat = -1.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);
        assertEquals(expected, actual);
    }

    @Test
    public void west() {
        Double expected = 270.0;

        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = -1.0;
        Double thisLat = 0.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);
        assertEquals(expected, actual);
    }

    @Test
    public void noMovement() {
        Double prevLon = 0.0;
        Double prevLat = 0.0;
        Double thisLon = 0.0;
        Double thisLat = 0.0;

        Double actual = CalculationUtil.calculateCourse(prevLat, prevLon, thisLat, thisLon);
        assertNull(actual);
    }
}
