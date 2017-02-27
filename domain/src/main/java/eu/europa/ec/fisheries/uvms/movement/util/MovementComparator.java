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

import eu.europa.ec.fisheries.uvms.movement.entity.LatestMovement;
import eu.europa.ec.fisheries.uvms.movement.entity.MinimalMovement;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import java.util.Comparator;

/**
 **/
public class MovementComparator {

    public static Comparator<Movement> MOVEMENT = new Comparator<Movement>() {
        @Override
        public int compare(Movement m1, Movement m2) {
            return m1.getTimestamp().compareTo(m2.getTimestamp());
        }
    };

    public static Comparator<LatestMovement> LATEST_MOVEMENT = new Comparator<LatestMovement>() {
        @Override
        public int compare(LatestMovement m1, LatestMovement m2) {
            return m1.getMovementConnect().getId().compareTo(m2.getMovementConnect().getId());
        }
    };

    public static Comparator<MinimalMovement> MINIMAL_MOVEMENT = new Comparator<MinimalMovement>() {
        @Override
        public int compare(MinimalMovement m1, MinimalMovement m2) {
            return m1.getMovementConnect().getId().compareTo(m2.getMovementConnect().getId());
        }
    };
}