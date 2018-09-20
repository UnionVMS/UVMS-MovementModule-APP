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
package eu.europa.ec.fisheries.uvms.movement.service.util;

import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.uvms.movement.service.dto.SegmentCalculations;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceRuntimeException;

public class CalculationUtil {
    
    private CalculationUtil() {}

    private static final double NAUTICAL_MILE_ONE_METER = 0.000539956803;
    private static final int EARTH_RADIUS_METER = 6371000;
    private static final double FACTOR_METER_PER_SECOND_TO_KNOTS = 1.9438444924574;

    /**
     * Calculated the distance between 2 points and returns the distance in meters
     */
    public static double calculateDistance(Double prevLat, Double prevLon, Double currentLat, Double currentLon) {
        return distanceMeter(prevLat, prevLon, currentLat, currentLon);
    }

    /**
     *
     * Calculates the course between 2 Points
     */
    public static Double calculateCourse(double prevLat, double prevLon, double currentLat, double currentLon) {
        if(prevLat == 0.0 && prevLon == 0.0 && currentLat == 0.0 && currentLon == 0.0)
            return null;
        return bearing(prevLat, prevLon, currentLat, currentLon);
    }

    public static SegmentCalculations getPositionCalculations(Movement previousPosition, Movement currentPosition) throws MovementServiceException {
        // TODO no null checks on incoming

        SegmentCalculations calculations = new SegmentCalculations();

        if (currentPosition.getLocation() == null) {
            throw new MovementServiceRuntimeException("CalculationUtil.getPositionCalculations. CurrentPosition is null! ",
                    ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }

        if (previousPosition.getLocation() == null) {
            throw new MovementServiceRuntimeException("CalculationUtil.getPositionCalculations. PreviousPosition is null! ",
                    ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }

        Point pointThisPosition = currentPosition.getLocation();
        Point pointPreviousPosition = previousPosition.getLocation();

        double distanceInMeters = 0;
        double durationInSeconds = 0;
        double speedOverGround = 0;
        double courseOverGround = 0;
        double distanceBetweenPointsInNauticalMiles = 0;

        if ((pointThisPosition.getX() != pointPreviousPosition.getX()) ||  (pointThisPosition.getY() != pointPreviousPosition.getY())) {

            distanceInMeters = calculateDistance(pointPreviousPosition.getY(), pointPreviousPosition.getX(), pointThisPosition.getY(), pointThisPosition.getX());
            durationInSeconds = (currentPosition.getTimestamp().getEpochSecond() - previousPosition.getTimestamp().getEpochSecond());

            courseOverGround = calculateCourse(pointPreviousPosition.getY(), pointPreviousPosition.getX(), pointThisPosition.getY(), pointThisPosition.getX());
            distanceBetweenPointsInNauticalMiles = CalculationUtil.getNauticalMilesFromMeter(distanceInMeters);

            speedOverGround = (distanceInMeters / durationInSeconds) * FACTOR_METER_PER_SECOND_TO_KNOTS;
        }

        calculations.setAvgSpeed(speedOverGround);
        calculations.setDistanceBetweenPoints(distanceBetweenPointsInNauticalMiles);
        calculations.setDurationBetweenPoints(durationInSeconds);
        calculations.setCourse(courseOverGround);

        return calculations;
    }

    public static Double getNauticalMilesFromMeter(Double meters) {
        return NAUTICAL_MILE_ONE_METER * meters;
    }

    /**
     * Computes the bearing in degrees between two points on Earth.
     *
     * @param prevLat Latitude of the first point
     * @param prevLon Longitude of the first point
     * @param currentLat Latitude of the second point
     * @param currentLon Longitude of the second point
     * @return Bearing between the two points in degrees. A value of 0 means due
     * north.
     */
    private static double bearing(double prevLat, double prevLon, double currentLat, double currentLon) {
        double lat1Rad = Math.toRadians(prevLat);
        double lat2Rad = Math.toRadians(currentLat);
        double deltaLonRad = Math.toRadians(currentLon - prevLon);

        double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad)
                * Math.cos(deltaLonRad);
        return radToDegrees(Math.atan2(y, x));
    }

    /**
     * Converts an angle in radians to degrees
     */
    private static double radToDegrees(double rad) {
        return (Math.toDegrees(rad) + 360) % 360;
    }

    /**
     * Calculate the distance between two points (Latitude, Longitude)
     */
    private static double distanceMeter(double prevLat, double prevLon, double currentLat, double currentLon) {
        double lat1Rad = Math.toRadians(prevLat);
        double lat2Rad = Math.toRadians(currentLat);
        double deltaLonRad = Math.toRadians(currentLon - prevLon);

        return Math.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.cos(deltaLonRad))
                * EARTH_RADIUS_METER;
    }
}
