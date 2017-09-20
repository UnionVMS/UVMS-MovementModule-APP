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

import eu.europa.ec.fisheries.uvms.movement.dto.SegmentCalculations;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Movementmetadata;
import org.junit.Assert;
import org.junit.Test;

/**
 **/
public class SegmentCalculationUtilTest {

    /**
     * IN_PORT = From-point and To-point < 1.5 NM from port
     */
    @Test
    public void testInPort() {

        double distanceToClosestPortTo = 1.4;
        double distanceToClosestPortFrom = 1.4;

        final double duration = 11;
        final double avgSpeed = 51;

        final SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);

        final Movement toMovement = new Movement();
        final Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        final Movement fromMovement = new Movement();
        final Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        //Success
        SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        Assert.assertEquals(SegmentCategoryType.IN_PORT, segmentCategory);

        //Fail
        distanceToClosestPortTo = 1.5;
        distanceToClosestPortFrom = 1.5;
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        Assert.assertNotSame(SegmentCategoryType.IN_PORT, segmentCategory);

        distanceToClosestPortTo = 1.5;
        distanceToClosestPortFrom = 1.4;
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        Assert.assertNotSame(SegmentCategoryType.IN_PORT, segmentCategory);

        distanceToClosestPortTo = 1.4;
        distanceToClosestPortFrom = 1.5;
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        Assert.assertNotSame(SegmentCategoryType.IN_PORT, segmentCategory);

    }

    /**
     * JUMP = AGV_SPEED > 50 OR ( DISTANCE > 250 AND DURATION > 12 )
     */
    @Test
    public void testJump() {

        double duration = 13;
        double avgSpeed = 51;
        double distance = 251;

        final SegmentCalculations segCalc = new SegmentCalculations();
        segCalc.setDurationBetweenPoints(duration);
        segCalc.setAvgSpeed(avgSpeed);
        segCalc.setDistanceBetweenPoints(distance);
        SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCalc, null, null);
        Assert.assertEquals(SegmentCategoryType.JUMP, segmentCategory);

        duration = 11;
        avgSpeed = 49;
        distance = 249;
        segCalc.setDurationBetweenPoints(duration);
        segCalc.setAvgSpeed(avgSpeed);
        segCalc.setDistanceBetweenPoints(distance);
        segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCalc, null, null);
        Assert.assertNotSame(SegmentCategoryType.JUMP, segmentCategory);

        duration = 13;
        avgSpeed = 49;
        distance = 251;
        segCalc.setDurationBetweenPoints(duration);
        segCalc.setAvgSpeed(avgSpeed);
        segCalc.setDistanceBetweenPoints(distance);
        segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCalc, null, null);
        Assert.assertEquals(SegmentCategoryType.JUMP, segmentCategory);

        duration = 13;
        avgSpeed = 49;
        distance = 249;
        segCalc.setDurationBetweenPoints(duration);
        segCalc.setAvgSpeed(avgSpeed);
        segCalc.setDistanceBetweenPoints(distance);
        segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCalc, null, null);
        Assert.assertNotSame(SegmentCategoryType.JUMP, segmentCategory);

        duration = 11;
        avgSpeed = 49;
        distance = 251;
        segCalc.setDurationBetweenPoints(duration);
        segCalc.setAvgSpeed(avgSpeed);
        segCalc.setDistanceBetweenPoints(distance);
        segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCalc, null, null);
        Assert.assertNotSame(SegmentCategoryType.JUMP, segmentCategory);

    }

    /**
     * GAP = ( Duration > 12 ) AND ( From/To points = NOT_IN_PORT )
     */
    @Test
    public void testGap() {

        final double distanceToClosestPortTo = 1.6;
        final double distanceToClosestPortFrom = 1.6;

        final double duration = 13;
        final double avgSpeed = 49;
        final double distance = 249;

        final SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);

        final Movement toMovement = new Movement();
        final Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        final Movement fromMovement = new Movement();
        final Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        final SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);

        Assert.assertEquals(SegmentCategoryType.GAP, segmentCategory);

    }

    /**
     * EXIT_PORT = ( From-point < 1.5 from Port ) AND  ( To-point > 1.5 from port
     * )
     *
     */
    @Test
    public void testExitPort() {

        double distanceToClosestPortTo = 1.6;
        double distanceToClosestPortFrom = 1.4;

        final double duration = 13;
        final double avgSpeed = 49;
        final double distance = 249;

        final SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);

        final Movement toMovement = new Movement();
        final Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        final Movement fromMovement = new Movement();
        final Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        final SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);
        Assert.assertEquals(SegmentCategoryType.EXIT_PORT, segmentCategory);

        distanceToClosestPortTo = 1.4;
        distanceToClosestPortFrom = 1.6;
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        fromMovement.getMetadata().setClosestPortDistance(distanceToClosestPortFrom);
        toMovement.getMetadata().setClosestPortDistance(distanceToClosestPortTo);

    }

    @Test
    public void testExitPortFailure() {

        final double distanceToClosestPortTo = 1.4;
        final double distanceToClosestPortFrom = 1.6;

        final double duration = 13;
        final double avgSpeed = 49;
        final double distance = 249;

        final SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);

        final Movement toMovement = new Movement();
        final Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        final Movement fromMovement = new Movement();
        final Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        final SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);

        Assert.assertNotSame(SegmentCategoryType.EXIT_PORT, segmentCategory);

    }

    /**
     * ENTER_PORT = ( From-point > 1.5 from Port ) AND ( To-point < 1.5 from
     * port )
     *
     */
    @Test
    public void testEnterPort() {

        final double distanceToClosestPortTo = 1.4;
        final double distanceToClosestPortFrom = 1.6;

        final double duration = 13;
        final double avgSpeed = 49;
        final double distance = 249;

        final SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);

        final Movement toMovement = new Movement();
        final Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        final Movement fromMovement = new Movement();
        final Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        final SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);

        Assert.assertEquals(SegmentCategoryType.ENTER_PORT, segmentCategory);

    }

    /**
     * NULL_DURATION = duration = 0
     */
    @Test
    public void testNullDuration() {

        final double distanceToClosestPortTo = 1.6;
        final double distanceToClosestPortFrom = 1.6;

        final double duration = 0;
        final double avgSpeed = 49;
        final double distance = 249;

        final SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);

        final Movement toMovement = new Movement();
        final Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        final Movement fromMovement = new Movement();
        final Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        final SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);

        Assert.assertEquals(SegmentCategoryType.NULL_DUR, segmentCategory);

    }

    /**
     * ANCGHORED = avg speed = 0
     */
    @Test
    public void testAnchored() {

        final double distanceToClosestPortTo = 1.6;
        final double distanceToClosestPortFrom = 1.6;

        final double duration = 0;
        final double avgSpeed = 0;
        final double distance = 249;

        final SegmentCalculations segCat = new SegmentCalculations();
        segCat.setDurationBetweenPoints(duration);
        segCat.setAvgSpeed(avgSpeed);

        final Movement toMovement = new Movement();
        final Movementmetadata toMeta = new Movementmetadata();
        toMeta.setClosestPortDistance(distanceToClosestPortTo);
        toMovement.setMetadata(toMeta);

        final Movement fromMovement = new Movement();
        final Movementmetadata fromMeta = new Movementmetadata();
        fromMeta.setClosestPortDistance(distanceToClosestPortFrom);
        fromMovement.setMetadata(fromMeta);

        final SegmentCategoryType segmentCategory = SegmentCalculationUtil.getSegmentCategoryType(segCat, fromMovement, toMovement);

        Assert.assertEquals(SegmentCategoryType.ANCHORED, segmentCategory);

    }

    @Test
    public void testLowSpeed() {

    }

}