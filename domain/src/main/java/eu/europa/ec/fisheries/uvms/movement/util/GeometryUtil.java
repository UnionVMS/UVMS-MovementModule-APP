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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import java.util.LinkedList;
import java.util.List;

import eu.europa.ec.fisheries.uvms.movement.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeometryUtil {
    
    private GeometryUtil() {}

    private static final Logger LOG = LoggerFactory.getLogger(GeometryUtil.class);

    private static final GeometryFactory FACTORY = new GeometryFactory();
    static final int SRID = 4326;

    /**
     * Returns a LineString for insertion in database
     *
     * @param sequence
     * @return
     */
    public static LineString getLineString(Coordinate[] sequence) {
        LineString lineString = FACTORY.createLineString(sequence);
        lineString.setSRID(SRID);
        return lineString;
    }

    public static Coordinate[] getCoordinateSequenceFromMovements(Movement previousPosition, Movement currentPosition) {
        Coordinate[] corSeq = new Coordinate[2];

        if (previousPosition.getLocation() == null) {
            throw new MovementDomainRuntimeException("Previous location is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (currentPosition.getLocation() == null) {
            throw new MovementDomainRuntimeException("Current location is null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }

        corSeq[0] = previousPosition.getLocation().getCoordinate();
        corSeq[1] = currentPosition.getLocation().getCoordinate();
        return corSeq;
    }

    public static LineString getLineStringFromMovements(Movement previousPosition, Movement currentPosition) {
        Coordinate[] corSeq = getCoordinateSequenceFromMovements(previousPosition, currentPosition);
        return getLineString(corSeq);
    }

    /**
     * Creates a new Linestring with the points ordered by the dates of the
     * movements
     *
     * @param movements
     * @return LineString instance
     */
    public static LineString getLineStringFromMovements(List<Movement> movements) {

        movements.sort(MovementComparator.MOVEMENT);

        LinkedList<Coordinate> coordinates = new LinkedList<>();
        for (Movement movement : movements) {
            coordinates.add(movement.getLocation().getCoordinate());
        }

        Coordinate[] coordinateArray = coordinates.toArray(new Coordinate[coordinates.size()]);
        LineString lineString = getLineString(coordinateArray);

        LOG.debug("LineString From Movement List {}", WKTUtil.getWktLineStringFromMovementList(movements));

        return lineString;
    }

}
