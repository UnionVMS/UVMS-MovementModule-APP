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

import java.util.Comparator;
import org.apache.commons.lang3.ObjectUtils;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MinimalMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;

public class MovementComparator {
    
    private MovementComparator() {}

    public static final Comparator<Movement> MOVEMENT = new Comparator<Movement>() {
        @Override
        public int compare(Movement m1, Movement m2) {
        	if (m1 == null || m2 ==null) {
        		return ObjectUtils.compare(m1, m2);
        	} else {
        		return ObjectUtils.compare(m1.getTimestamp(),m2.getTimestamp());
        	}
        }
    };

    public static final Comparator<MovementConnect> MOVEMENT_CONNECT = new Comparator<MovementConnect>() {
        @Override
        public int compare(MovementConnect m1, MovementConnect m2) {
        	if (m1 == null || m2 ==null) {
        		return ObjectUtils.compare(m1, m2);
        	} else {
        		return ObjectUtils.compare(m1.getId(),m2.getId());
        	}
        }
    };

    public static final Comparator<MinimalMovement> MINIMAL_MOVEMENT = new Comparator<MinimalMovement>() {
        @Override
        public int compare(MinimalMovement m1, MinimalMovement m2) {
        	if (m1 == null || m2 ==null) {
        		return ObjectUtils.compare(m1, m2);
        	} else {
        		return MOVEMENT_CONNECT.compare(m1.getMovementConnect(), m2.getMovementConnect());
        	}
        }
    };
}