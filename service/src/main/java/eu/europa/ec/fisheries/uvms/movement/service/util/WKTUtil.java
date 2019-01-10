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

import java.util.ArrayList;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MinimalMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;

public class WKTUtil {
    
    private WKTUtil() {}

    public static String getWktLineString(List<Geometry> geometries) {
        if (geometries.size() < 2) {
            return null;
        }
        List<Coordinate> coords = new ArrayList<>();
        for (Geometry geom : geometries) {
        	for(Coordinate verti : geom.getCoordinates()) {
        		coords.add(verti);
        	}
        }
        CoordinateSequence seq = new CoordinateArraySequence(coords.toArray(new Coordinate[0]));
        return WKTWriter.toLineString(seq);
    }

    public static String getWktLineStringFromMovementList(List<Movement> movements) {
        List<Coordinate> coords = new ArrayList<>();
        for (Movement movement : movements) {
            coords.add(movement.getLocation().getCoordinate());
        }
        CoordinateSequence seq = new CoordinateArraySequence(coords.toArray(new Coordinate[0]));
        return WKTWriter.toLineString(seq);
    }

    public static String getWktLineStringFromSegment(Segment segment) {
        return WKTWriter.toLineString(segment.getLocation().getCoordinateSequence());
    }

    public static Geometry getGeometryFromWKTSrring(String wkt) {
        try {
            WKTReader reader = new WKTReader();
            Geometry geom = reader.read(wkt);
            geom.setSRID(GeometryUtil.SRID);
            return geom;
        }catch (ParseException e){
            throw new IllegalArgumentException("Inputstring " + wkt + " causes a parse exception.", e);
        }
    }

    public static String getWktPointString(Geometry geometry) {
        return WKTWriter.toPoint(geometry.getCoordinate());
    }

    public static String getWktPointFromMovement(Movement movement) {
        return getWktPointString(movement.getLocation());
    }

    public static String getWktPointFromMovement(MinimalMovement movement) {
        return getWktPointString(movement.getLocation());
    }

}