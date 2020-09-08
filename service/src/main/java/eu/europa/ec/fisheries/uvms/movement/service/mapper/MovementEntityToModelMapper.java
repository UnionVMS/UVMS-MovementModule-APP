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
package eu.europa.ec.fisheries.uvms.movement.service.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSegment;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTrack;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.service.dto.SegmentCalculations;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.service.util.CalculationUtil;
import eu.europa.ec.fisheries.uvms.movement.service.util.WKTUtil;

public class MovementEntityToModelMapper {
    
    private MovementEntityToModelMapper() {}

    public static MovementBaseType mapToMovementBaseType(Movement movement) {
        MovementBaseType model = new MovementBaseType();
        model.setReportedSpeed((double)movement.getSpeed());
        model.setReportedCourse((double)movement.getHeading());
        model.setGuid(movement.getId().toString());
        model.setPositionTime(Date.from(movement.getTimestamp()));
        model.setLesReportTime( Date.from(movement.getLesReportTime() ));
        model.setStatus(movement.getStatus());
        model.setSource(movement.getSource());
        model.setMovementType(movement.getMovementType());
        model.setActivity(model.getActivity());
        MovementPoint movementPoint = new MovementPoint();
        Point point = movement.getLocation();
        movementPoint.setLatitude(point.getY());
        movementPoint.setLongitude(point.getX());
        model.setPosition(movementPoint);
        model.setConnectId(mapToConnectId(movement.getMovementConnect()));
        return model;
    }

    public static MovementType mapToMovementType(Movement movement) {

        if (movement == null) {
            return null;
        }

        //Previous movement ID is mapped in MovementBatchModelBean
        MovementType model = new MovementType();
        if (movement.getSpeed() != null) {
            model.setReportedSpeed((double)movement.getSpeed());
        }
        model.setGuid(movement.getId().toString());
        if (movement.getHeading() != null) {
            model.setReportedCourse((double)movement.getHeading());
        }
        model.setPositionTime(Date.from(movement.getTimestamp()));
        if ( movement.getLesReportTime() != null) {
        	model.setLesReportTime( Date.from(movement.getLesReportTime() ));
        }
        model.setStatus(movement.getStatus());
        model.setSource(movement.getSource());
        model.setMovementType(movement.getMovementType());

        MovementPoint movementPoint = new MovementPoint();
        Point point = movement.getLocation();

        movementPoint.setLatitude(point.getY());
        movementPoint.setLongitude(point.getX());
        model.setPosition(movementPoint);

        model.setConnectId(mapToConnectId(movement.getMovementConnect()));

        model.setWkt(WKTUtil.getWktPointFromMovement(movement));
        Movement previousMovement = movement.getPreviousMovement();
        if (previousMovement != null) {
            SegmentCalculations positionCalculations = CalculationUtil.getPositionCalculations(previousMovement, movement);
            model.setCalculatedCourse(positionCalculations.getCourse());
            Double calculatedSpeed = positionCalculations.getAvgSpeed();
            model.setCalculatedSpeed(calculatedSpeed != null && !calculatedSpeed.isInfinite() && !calculatedSpeed.isNaN() ? calculatedSpeed : null);
        }
        if (movement.getTrack() != null) {
            // Investigate if segmentsIds can be removed
            model.getSegmentIds().add(movement.getTrack().getId().toString());
        }
        model.setProcessed(true);

        model.setInternalReferenceNumber(movement.getInternalReferenceNumber());
        model.setTripNumber(movement.getTripNumber());

        
        return model;
    }


    public static List<MovementType> mapToMovementType(List<Movement> movements) {
        List<MovementType> mappedMovements = new ArrayList<>();
        for (Movement movement : movements) {
            mappedMovements.add(mapToMovementType(movement));
        }
        return mappedMovements;
    }

    private static String mapToConnectId(MovementConnect connect) {
        if (connect != null) {
            return connect.getId().toString();
        }
        return null;
    }
    
    public static List<MovementSegment> mapToMovementSegment(List<Movement> movements, boolean excludeFirstAndLast) {
        List<MovementSegment> mappedSegments = new ArrayList<>();
        Collections.sort(movements, (m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
        for (Movement movement : movements) {
            Movement previousMovement = movement.getPreviousMovement();
            if (previousMovement != null) {
                SegmentCalculations positionCalculations = CalculationUtil.getPositionCalculations(previousMovement, movement);
                MovementSegment movSegment = new MovementSegment();
                movSegment.setId(movement.getId().toString());
//                movSegment.setCategory(SegmentCategoryType.OTHER); // TODO
                movSegment.setTrackId(movement.getTrack().getId().toString());
                movSegment.setWkt(WKTUtil.getWktLineStringFromMovements(previousMovement, movement));
                movSegment.setCourseOverGround(positionCalculations.getCourse());
                movSegment.setSpeedOverGround(positionCalculations.getAvgSpeed());
                movSegment.setDuration(positionCalculations.getDurationBetweenPoints());
                movSegment.setDistance(positionCalculations.getDistanceBetweenPoints());
                mappedSegments.add(movSegment);
            }
        }
        if (excludeFirstAndLast && mappedSegments.size() >= movements.size()) {
            return mappedSegments.subList(1, mappedSegments.size());
        }
        return mappedSegments;
    }

    public static MovementTrack mapToMovementTrack(Track track, List<Geometry> points) {
        MovementTrack movementTrack = new MovementTrack();
        if(track == null){
            return movementTrack;
        }
        movementTrack.setDistance(track.getDistance());
        movementTrack.setDuration(track.getDuration());
        movementTrack.setTotalTimeAtSea(track.getTotalTimeAtSea());
        if (points.size() > 1) {
            movementTrack.setWkt(WKTUtil.getWktLineString(points));
        }
        movementTrack.setId(track.getId().toString());
        return movementTrack;
    }

    public static Map<UUID, List<Movement>> orderMovementsByConnectId(List<Movement> movements) {
        Map<UUID, List<Movement>> orderedMovements = new HashMap<>();
        for (Movement movement : movements) {
            if (orderedMovements.get(movement.getMovementConnect().getId()) == null) {
                orderedMovements.put(movement.getMovementConnect().getId(), new ArrayList<>(Collections.singletonList(movement)));
            } else {
                orderedMovements.get(movement.getMovementConnect().getId()).add(movement);
            }
        }
        return orderedMovements;
    }

    public static List<Track> extractTracks(List<Movement> movements) {
        Set<Track> tracks = new HashSet<>();
        for (Movement movement : movements) {
            tracks.add(movement.getTrack());
        }
        return new ArrayList<>(tracks);
    }
}
