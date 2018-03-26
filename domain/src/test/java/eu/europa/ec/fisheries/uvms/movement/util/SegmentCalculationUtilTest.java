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
package eu.europa.ec.fisheries.uvms.movement.util;

import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;

import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.dto.SegmentCalculations;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Movementmetadata;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.junit.Arquillian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 **/
@RunWith(Arquillian.class)
public class SegmentCalculationUtilTest extends TransactionalTests {

    /**
     * IN_PORT = From-point and To-point < 1.5 NM from port
     */
    @Test
    public void testInPort() {

        double distanceToClosestPortTo = 1.4;
        double distanceToClosestPortFrom = 1.4;

        double duration = 11;
        double avgSpeed = 51;

        SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);

        Movement toMovement = new Movement();
        Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        Movement fromMovement = new Movement();
        Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        //Success
        SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        assertEquals(SegmentCategoryType.IN_PORT, segmentCategory);

        //Fail
        distanceToClosestPortTo = 1.5;
        distanceToClosestPortFrom = 1.5;
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        assertNotSame(SegmentCategoryType.IN_PORT, segmentCategory);

        distanceToClosestPortTo = 1.5;
        distanceToClosestPortFrom = 1.4;
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        assertNotSame(SegmentCategoryType.IN_PORT, segmentCategory);

        distanceToClosestPortTo = 1.4;
        distanceToClosestPortFrom = 1.5;
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        assertNotSame(SegmentCategoryType.IN_PORT, segmentCategory);
        
        //very questionable if this should work this way...... 
        segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(null, fromMovement, toMovement);
        assertEquals(SegmentCategoryType.ENTER_PORT, segmentCategory);
        
        segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(null, null, toMovement);
        assertEquals(SegmentCategoryType.OTHER, segmentCategory);
    }

    /**
     * JUMP = AGV_SPEED > 50 OR ( DISTANCE > 250 AND DURATION > 12 )
     */
    @Test
    public void testJump() {

        double duration = 13;
        double avgSpeed = 51;
        double distance = 251;

        SegmentCalculations segCalc = new SegmentCalculations();
        segCalc.setDurationBetweenPoints(duration);
        segCalc.setAvgSpeed(avgSpeed);
        segCalc.setDistanceBetweenPoints(distance);
        SegmentCategoryType segmentCategoryType = SegmentCalculationUtil.getSegmentCategoryType(segCalc, null, null);
        assertEquals(SegmentCategoryType.JUMP, segmentCategoryType);

        duration = 11;
        avgSpeed = 49;
        distance = 249;

        segCalc.setDurationBetweenPoints(duration);
        segCalc.setAvgSpeed(avgSpeed);
        segCalc.setDistanceBetweenPoints(distance);
        segmentCategoryType = SegmentCalculationUtil.getSegmentCategoryType(segCalc, null, null);
        assertNotSame(SegmentCategoryType.JUMP, segmentCategoryType);

        duration = 13;
        avgSpeed = 49;
        distance = 251;

        segCalc.setDurationBetweenPoints(duration);
        segCalc.setAvgSpeed(avgSpeed);
        segCalc.setDistanceBetweenPoints(distance);
        segmentCategoryType = SegmentCalculationUtil.getSegmentCategoryType(segCalc, null, null);
        assertEquals(SegmentCategoryType.JUMP, segmentCategoryType);

        duration = 13;
        avgSpeed = 49;
        distance = 249;

        segCalc.setDurationBetweenPoints(duration);
        segCalc.setAvgSpeed(avgSpeed);
        segCalc.setDistanceBetweenPoints(distance);
        segmentCategoryType = SegmentCalculationUtil.getSegmentCategoryType(segCalc, null, null);
        assertNotSame(SegmentCategoryType.JUMP, segmentCategoryType);

        duration = 11;
        avgSpeed = 49;
        distance = 251;

        segCalc.setDurationBetweenPoints(duration);
        segCalc.setAvgSpeed(avgSpeed);
        segCalc.setDistanceBetweenPoints(distance);
        segmentCategoryType = SegmentCalculationUtil.getSegmentCategoryType(segCalc, null, null);
        assertNotSame(SegmentCategoryType.JUMP, segmentCategoryType);
        
        segmentCategoryType = SegmentCalculationUtil.getSegmentCategoryType(null, null, null);
        assertEquals(SegmentCategoryType.OTHER, segmentCategoryType);
    }

    /**
     * GAP = ( Duration > 12 ) AND ( From/To points = NOT_IN_PORT )
     */
    @Test
    public void testGap() {

        double distanceToClosestPortTo = 1.6;
        double distanceToClosestPortFrom = 1.6;

        double duration = 13;
        double avgSpeed = 49;
        double distance = 249;

        SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);
        segCat.setDistanceBetweenPoints(distance);

        Movement toMovement = new Movement();
        Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        Movement fromMovement = new Movement();
        Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        assertEquals(SegmentCategoryType.GAP, segmentCategory);
    }

    /**
     * EXIT_PORT = ( From-point < 1.5 from Port ) AND  ( To-point > 1.5 from port )
     */
    @Test
    public void testExitPort() {

        double distanceToClosestPortTo = 1.6;
        double distanceToClosestPortFrom = 1.4;

        double duration = 13;
        double avgSpeed = 49;
        double distance = 249;

        SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);
        segCat.setDistanceBetweenPoints(distance);

        Movement toMovement = new Movement();
        Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        Movement fromMovement = new Movement();
        Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        assertEquals(SegmentCategoryType.EXIT_PORT, segmentCategory);

        distanceToClosestPortTo = 1.4;
        distanceToClosestPortFrom = 1.6;
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        fromMovement.getMetadata().setClosestPortDistance(distanceToClosestPortFrom);
        toMovement.getMetadata().setClosestPortDistance(distanceToClosestPortTo);
        segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        assertEquals(SegmentCategoryType.ENTER_PORT, segmentCategory);

    }

    @Test
    public void testExitPortFailure() {

        double distanceToClosestPortTo = 1.4;
        double distanceToClosestPortFrom = 1.6;

        double duration = 13;
        double avgSpeed = 49;
        double distance = 249;

        SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);
        segCat.setDistanceBetweenPoints(distance);

        Movement toMovement = new Movement();
        Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        Movement fromMovement = new Movement();
        Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        assertNotSame(SegmentCategoryType.EXIT_PORT, segmentCategory);
    }

    /**
     * ENTER_PORT = ( From-point > 1.5 from Port ) AND ( To-point < 1.5 from port )
     */
    @Test
    public void testEnterPort() {

        double distanceToClosestPortTo = 1.4;
        double distanceToClosestPortFrom = 1.6;

        double duration = 13;
        double avgSpeed = 49;
        double distance = 249;

        SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);
        segCat.setDistanceBetweenPoints(distance);

        Movement toMovement = new Movement();
        Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        Movement fromMovement = new Movement();
        Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        assertEquals(SegmentCategoryType.ENTER_PORT, segmentCategory);
    }

    /**
     * NULL_DURATION = (duration = 0)
     */
    @Test
    public void testNullDuration() {

        double distanceToClosestPortTo = 1.6;
        double distanceToClosestPortFrom = 1.6;

        double duration = 0;
        double avgSpeed = 49;
        double distance = 249;

        SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);
        segCat.setDistanceBetweenPoints(distance);

        Movement toMovement = new Movement();
        Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        Movement fromMovement = new Movement();
        Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        assertEquals(SegmentCategoryType.NULL_DUR, segmentCategory);
    }

    /**
     * ANCGHORED = avg speed = 0
     */
    @Test
    public void testAnchored() {

        double distanceToClosestPortTo = 1.6;
        double distanceToClosestPortFrom = 1.6;

        double duration = 0;
        double avgSpeed = 0;
        double distance = 249;

        SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);
        segCat.setDistanceBetweenPoints(distance);

        Movement toMovement = new Movement();
        Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        Movement fromMovement = new Movement();
        Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        assertEquals(SegmentCategoryType.ANCHORED, segmentCategory);
    }

    @Test
    public void testLowSpeed() {

    }
    
    /**
     * Should the system allow for negative speed?
     */
    
    @Test
    public void testNegativeSpeed() {

        double distanceToClosestPortTo = 1.6;
        double distanceToClosestPortFrom = 1.6;

        double duration = 10;
        double avgSpeed = -10;
        double distance = 249;

        SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);

        Movement toMovement = new Movement();
        Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        Movement fromMovement = new Movement();
        Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);

        assertEquals(SegmentCategoryType.OTHER, segmentCategory);

    }

}