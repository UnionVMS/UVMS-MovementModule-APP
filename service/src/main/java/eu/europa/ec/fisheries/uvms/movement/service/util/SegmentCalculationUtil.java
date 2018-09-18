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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.service.dto.SegmentCalculations;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;

public class SegmentCalculationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SegmentCalculationUtil.class);

    private static final double DISTANCE_TO_PORT_THRESHOLD_IN_NAUTICAL_MILES = 1.5;

    public enum SegmentCalcType {

        AVERAGE_SPEED,
        DURATION_BETWEEN_POINTS,
        DISTANCE_BETWEEN_POINTS,
        DISTANCE_TO_PORT,
        FROM_POINT_IN_PORT,
        TO_POINT_IN_PORT,
        OTHER;
    }

    /**
     *
     * @param calc
     * @param fromMovement
     * @param toMovement
     * @return Returns the category the segment represents
     */
    public static SegmentCategoryType getSegmentCategoryType(SegmentCalculations calc, Movement fromMovement, Movement toMovement) {

        try {
            if (fromMovement != null && toMovement != null) {

                Boolean fromMovementInPort = isVesselInPort(fromMovement);
                Boolean toMovementInPort = isVesselInPort(toMovement);

                if (toMovementInPort != null && fromMovementInPort != null) {

                    if (fromMovementInPort && toMovementInPort) {
                        return SegmentCategoryType.IN_PORT;
                    } else if (fromMovementInPort && !toMovementInPort) {
                        return SegmentCategoryType.EXIT_PORT;
                    } else if (!fromMovementInPort && toMovementInPort) {
                        return SegmentCategoryType.ENTER_PORT;
                    }

                    if (isGap(calc, fromMovement, toMovement)) {
                        return SegmentCategoryType.GAP;
                    }

                }

            }

            if (calc.getAvgSpeed() == 0) {
                return SegmentCategoryType.ANCHORED;
            }

            if (calc.getDurationBetweenPoints() == 0) {
                return SegmentCategoryType.NULL_DUR;
            }

            if (isJump(calc)) {
                return SegmentCategoryType.JUMP;
            }

        } catch (NullPointerException e) {
            LOG.error("[ Got nullpointer exception on line {} when trying to decide SegmentCategoryType, returning SegmentCategoryType.OTHER ]", e.getStackTrace()[0].getLineNumber());
        }

        return SegmentCategoryType.OTHER;

    }

    public static Boolean isGap(SegmentCalculations calc, Movement fromMovement, Movement toMovement) {
        if (calc != null) {
            if ((!isVesselInPort(fromMovement) && !isVesselInPort(toMovement)) && (calc.getDurationBetweenPoints() > 12)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } else {
            LOG.debug("[ SegmentCalculations in movement is null, cannot decide if segments is Gap, returning NULL ]");
            return null;
        }
    }

    public static Boolean isJump(SegmentCalculations calc) {
        if (calc != null) {
            if (calc.getAvgSpeed() > 50 || (calc.getDistanceBetweenPoints() > 250 && calc.getDurationBetweenPoints() > 12)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } else {
            LOG.debug("[SegmentCalculations in movement is null, cannot decide if segments is Jump, returning NULL ]");
            return null;
        }
    }

    public static Boolean isVesselInPort(Movement movement) {
        if (movement.getMetadata() != null) {
            if (movement.getMetadata().getClosestPortDistance() != null &&
                    movement.getMetadata().getClosestPortDistance() < DISTANCE_TO_PORT_THRESHOLD_IN_NAUTICAL_MILES) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } else {
            LOG.debug("[ Metadata not present in movement, cannot decide if vessel is in port, returning NULL ]");
            return null;
        }

    }

}