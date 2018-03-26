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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 **/
public class GeometryUtil {

    final static Logger LOG = LoggerFactory.getLogger(GeometryUtil.class);

    private static final GeometryFactory FACTORY = new GeometryFactory();
    public static final int SRID = 4326;

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

    public static Coordinate[] getCoordinateSequenceFromMovements(Movement previousPosition, Movement currentPosition) throws GeometryUtilException {
        Coordinate[] corSeq = new Coordinate[2];

        if (previousPosition.getLocation() == null) {
            throw new GeometryUtilException(5, "[ GeometryUtil.getCoordinateSequenceFromMovements ] Previous location is null");
        }
        if (currentPosition.getLocation() == null) {
            throw new GeometryUtilException(5, "[ GeometryUtil.getCoordinateSequenceFromMovements ] Current location is null");
        }

        corSeq[0] = previousPosition.getLocation().getCoordinate();
        corSeq[1] = currentPosition.getLocation().getCoordinate();
        return corSeq;
    }

    public static LineString getLineStringFromMovments(Movement previousPosition, Movement currentPosition) throws GeometryUtilException {
        Coordinate[] corSeq = getCoordinateSequenceFromMovements(previousPosition, currentPosition);
        return getLineString(corSeq);
    }

    /**
     * Creates a new Linestring with the points ordered by the dates of the
     * movememnts
     *
     * @param movements
     * @return
     * @throws GeometryUtilException
     */
    public static LineString getLineStringFromMovments(List<Movement> movements) throws GeometryUtilException {

    	
        Collections.sort(movements, MovementComparator.MOVEMENT);

        LinkedList<Coordinate> coordinates = new LinkedList<>();
        for (Movement movement : movements) {
            coordinates.add(movement.getLocation().getCoordinate());
        }

        Coordinate[] coordinateArray = coordinates.toArray(new Coordinate[coordinates.size()]);
        LineString lineString = getLineString(coordinateArray);

        LOG.debug("LINESTERING FROM MOVEMENT LIST {}", WKTUtil.getWktLineStringFromMovementList(movements));

        return lineString;
    }

}