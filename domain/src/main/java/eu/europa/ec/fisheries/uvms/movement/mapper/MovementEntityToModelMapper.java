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
package eu.europa.ec.fisheries.uvms.movement.mapper;

import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.v1.ClosestLocationType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityType;

import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSegment;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTrack;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.entity.*;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.util.MovementComparator;
import eu.europa.ec.fisheries.uvms.movement.util.WKTUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovementEntityToModelMapper {

    static Logger LOG = LoggerFactory.getLogger(MovementEntityToModelMapper.class);

    public static MovementBaseType mapToMovementBaseType(Movement movement) {

        MovementBaseType model = new MovementBaseType();

        if (movement.getSpeed() != null) {
            model.setReportedSpeed(movement.getSpeed());
        }

        if (movement.getHeading() != null) {
            model.setReportedCourse(movement.getHeading());
        }

        if (movement.getGuid() != null) {
            model.setGuid(movement.getGuid());
        }

        if (movement.getTimestamp() != null) {
            model.setPositionTime(movement.getTimestamp());
        }

        model.setStatus(movement.getStatus());
        model.setSource(movement.getMovementSource());
        model.setMovementType(movement.getMovementType());
        model.setActivity(model.getActivity());

        MovementPoint movementPoint = new MovementPoint();
        Point point = (Point) movement.getLocation();
        movementPoint.setLatitude(point.getY());
        movementPoint.setLongitude(point.getX());
        model.setPosition(movementPoint);

        try {
            model.setConnectId(mapToConnectId(movement.getMovementConnect()));
        } catch (MovementDaoException ex) {
            LOG.debug("Error when mapping to carrier {} ", ex.getMessage());
        }

        return model;
    }


    public static MovementType mapToMovementType(MinimalMovement movement) {

        //Previous movement ID is mapped in MovementBatchModelBean
        MovementType model = new MovementType();

        if (movement.getSpeed() != null) {
            model.setReportedSpeed(movement.getSpeed());
        }

        if (movement.getGuid() != null) {
            model.setGuid(movement.getGuid());
        }

        //TODO Fix this to double or int
        if (movement.getHeading() != null) {
            model.setReportedCourse(movement.getHeading());
        }

        if (movement.getTimestamp() != null) {
            model.setPositionTime(movement.getTimestamp());
        }

        model.setStatus(movement.getStatus());
        model.setSource(movement.getMovementSource());
        model.setMovementType(movement.getMovementType());

        MovementPoint movementPoint = new MovementPoint();
        Point point = movement.getLocation();

        movementPoint.setLatitude(point.getY());
        movementPoint.setLongitude(point.getX());
        model.setPosition(movementPoint);

        try {
            model.setConnectId(mapToConnectId(movement.getMovementConnect()));
        } catch (MovementDaoException ex) {
            LOG.debug("Error when mapping to carrier {} ", ex.getMessage());
        }

        if (movement.getFromSegment() != null) {
        	model.setCalculatedSpeed(movement.getFromSegment().getSpeedOverGround());
        	model.setCalculatedCourse(movement.getFromSegment().getCourseOverGround());
        }
        
        model.setWkt(WKTUtil.getWktPointFromMovement(movement));

        return model;
    }

    public static MovementType mapToMovementType(Movement movement) {

        //Previous movement ID is mapped in MovementBatchModelBean
        MovementType model = new MovementType();

        if (movement.getSpeed() != null) {
            model.setReportedSpeed(movement.getSpeed());
        }

        if (movement.getGuid() != null) {
            model.setGuid(movement.getGuid());
        }

        //TODO Fix this to double or int
        if (movement.getHeading() != null) {
            model.setReportedCourse(movement.getHeading());
        }

        if (movement.getTimestamp() != null) {
            model.setPositionTime(movement.getTimestamp());
        }

        model.setActivity(mapToActivityType(movement.getActivity()));

        model.setStatus(movement.getStatus());
        model.setSource(movement.getMovementSource());
        model.setMovementType(movement.getMovementType());

        MovementPoint movementPoint = new MovementPoint();
        Point point = (Point) movement.getLocation();

        movementPoint.setLatitude(point.getY());
        movementPoint.setLongitude(point.getX());
        model.setPosition(movementPoint);

        try {
            model.setConnectId(mapToConnectId(movement.getMovementConnect()));
        } catch (MovementDaoException ex) {
            LOG.debug("Error when mapping to carrier {} ", ex.getMessage());
        }

        model.setWkt(WKTUtil.getWktPointFromMovement(movement));

        if (movement.getFromSegment() != null) {
            model.getSegmentIds().add(movement.getFromSegment().getId().toString());
            model.setCalculatedCourse(movement.getFromSegment().getCourseOverGround());
            model.setCalculatedSpeed(movement.getFromSegment().getSpeedOverGround());
        }

        if (movement.getToSegment() != null) {
            model.getSegmentIds().add(movement.getToSegment().getId().toString());
        }

        if (movement.getMetadata() != null) {
            model.setMetaData(mapToMovementMetaData(movement.getMetadata()));
        }

        //Duplicated code from 20 lines above?
        try {
            model.setConnectId(mapToConnectId(movement.getMovementConnect()));
        } catch (MovementDaoException ex) {
            LOG.debug("Error when mapping to carrier {} ", ex.getMessage());
        }

        if (model.getMetaData() != null) {
            model.getMetaData().getAreas().addAll(mapToMovementMetaDataAreaTypeList(movement.getMovementareaList()));
        }

        model.setProcessed(movement.getProcessed());
        if (movement.getDuplicate() != null) {
            model.setDuplicate(movement.getDuplicate());
        } else {
            model.setDuplicate(false);
        }
        model.setDuplicates(movement.getDuplicateId());
        
        model.setInternalReferenceNumber(movement.getInternalReferenceNumber());

        return model;
    }

    private static List<MovementMetaDataAreaType> mapToMovementMetaDataAreaTypeList(List<Movementarea> areas) {
        List<MovementMetaDataAreaType> areaList = new ArrayList<>();
        for (Movementarea area : areas) {
            MovementMetaDataAreaType type = new MovementMetaDataAreaType();
            type.setAreaType(area.getMovareaAreaId().getAreaType().getName());
            type.setRemoteId(area.getMovareaAreaId().getRemoteId());
            type.setCode(area.getMovareaAreaId().getAreaCode());
            type.setName(area.getMovareaAreaId().getAreaName());
            type.setTransitionType(MovementTypeType.POS);
            areaList.add(type);
        }
        return areaList;
    }

    public static MovementActivityType mapToActivityType(Activity activity) {
        MovementActivityType actType = new MovementActivityType();
        if (activity != null) {
            actType.setCallback(activity.getCallback());
            actType.setMessageId(activity.getMessageId());
            actType.setMessageType(activity.getActivityType());
        }
        return actType;
    }

    public static MovementMetaData mapToMovementMetaData(Movementmetadata metaData) {
        MovementMetaData meta = new MovementMetaData();

        ClosestLocationType country = new ClosestLocationType();
        country.setCode(metaData.getClosestCountryCode());
        country.setDistance(metaData.getClosestCountryDistance());
        country.setRemoteId(metaData.getClosestCountryRemoteId());
        meta.setClosestCountry(country);

        ClosestLocationType port = new ClosestLocationType();
        port.setCode(metaData.getClosestPortCode());
        port.setDistance(metaData.getClosestPortDistance());
        port.setRemoteId(metaData.getClosestPortRemoteId());
        meta.setClosestPort(port);

        return meta;
    }

    public static List<MovementType> mapToMovementType(List<Movement> movements) {
        List<MovementType> mappedMovements = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (Movement movement : movements) {
            mappedMovements.add(mapToMovementType(movement));
        }
        long diff = System.currentTimeMillis() - start;
        LOG.debug("mapToMovementType: " + " ---- TIME ---- " + diff +"ms" );
        return mappedMovements;
    }

    public static List<MovementType> mapToMovementTypeFromLatestMovement(List<LatestMovement> movements) {
        List<MovementType> mappedMovements = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (LatestMovement movement : movements) {
            mappedMovements.add(mapToMovementType(movement.getMovement()));
        }
        long diff = System.currentTimeMillis() - start;
        LOG.debug("mapToMovementType: " + " ---- TIME ---- " + diff +"ms" );
        return mappedMovements;
    }

    public static String mapToConnectId(MovementConnect connect) throws MovementDaoException {
        if (connect != null) {
            return connect.getValue();
        }
        return null;
    }

    public static List<MovementSegment> mapToMovementSegment(List<Segment> segments) {
        long start = System.currentTimeMillis();
        List<MovementSegment> mappedSegments = new ArrayList<>();
        for (Segment segment : segments) {
            mappedSegments.add(mapToMovementSegment(segment));
        }
        long diff = System.currentTimeMillis() - start;
        LOG.debug("mapToMovementSegment: " + " ---- TIME ---- " + diff +"ms" );
        return mappedSegments;
    }

    public static MovementSegment mapToMovementSegment(Segment segment) {
        MovementSegment movSegment = new MovementSegment();
        movSegment.setCategory(segment.getSegmentCategory());
        movSegment.setId(segment.getId().toString());
        movSegment.setTrackId(segment.getTrack().getId().toString());
        movSegment.setWkt(WKTUtil.getWktLineStringFromSegment(segment));
        movSegment.setCourseOverGround(segment.getCourseOverGround());
        movSegment.setSpeedOverGround(segment.getSpeedOverGround());
        movSegment.setDuration(segment.getDuration());
        movSegment.setDistance(segment.getDistance());
        return movSegment;
    }

    public static MovementTrack mapToMovementTrack(Track track) {
        MovementTrack movementTrack = new MovementTrack();
        movementTrack.setDistance(track.getDistance());
        movementTrack.setDuration(track.getDuration());
        movementTrack.setTotalTimeAtSea(track.getTotalTimeAtSea());
        movementTrack.setWkt(WKTUtil.getWktLineStringFromTrack(track));
        movementTrack.setId(track.getId().toString());
        return movementTrack;
    }

    public static Map<String, List<Movement>> orderMovementsByConnectId(List<Movement> movements) {
        Map<String, List<Movement>> orderedMovements = new HashMap<>();
        long start = System.currentTimeMillis();
        for (Movement movement : movements) {
            if (orderedMovements.get(movement.getMovementConnect().getValue()) == null) {
                orderedMovements.put(movement.getMovementConnect().getValue(), new ArrayList<>(Arrays.asList(movement)));
            } else {
                orderedMovements.get(movement.getMovementConnect().getValue()).add(movement);
            }
        }
        long diff = System.currentTimeMillis() - start;
        LOG.debug("orderMovementByConnectID: " + " ---- TIME ---- " + diff + "ms");
        return orderedMovements;
    }

    public static List<MovementTrack> extractTracks(List<Segment> segments) {
        long start = System.currentTimeMillis();
        Set<Track> tracks = new HashSet<>();
        for (Segment segment : segments) {
            tracks.add(segment.getTrack());
        }
        List<MovementTrack> movementTracks = new ArrayList<>();
        for (Track track : tracks) {
            movementTracks.add(mapToMovementTrack(track));
        }

        long diff = System.currentTimeMillis() - start;
        LOG.debug("extractTracks: " + " ---- TIME ---- " + diff +"ms" );
        return movementTracks;
    }

    public static ArrayList<Segment> extractSegments(ArrayList<Movement> movements, boolean excludeFirstLastSegment) {
        Set<Segment> segments = new HashSet<>();
        long start = System.currentTimeMillis();
        if (movements.size() == 1 && excludeFirstLastSegment) {
            return new ArrayList<>(segments);
        }

        Collections.sort(movements, MovementComparator.MOVEMENT);

        for (int i = 0; i < movements.size(); i++) {

            if (excludeFirstLastSegment) {
                if (i == 0) {
                    if (movements.get(i).getToSegment() != null) {
                        segments.add(movements.get(i).getToSegment());
                    }
                } else if (i == movements.size() - 1) {
                    if (movements.get(i).getFromSegment() != null) {
                        segments.add(movements.get(i).getFromSegment());
                    }
                } else {
                    if (movements.get(i).getFromSegment() != null) {
                        segments.add(movements.get(i).getFromSegment());
                    }
                    if (movements.get(i).getToSegment() != null) {
                        segments.add(movements.get(i).getToSegment());
                    }
                }
            } else {
                if (movements.get(i).getFromSegment() != null) {
                    segments.add(movements.get(i).getFromSegment());
                }
                if (movements.get(i).getToSegment() != null) {
                    segments.add(movements.get(i).getToSegment());
                }
            }
        }

        long diff = System.currentTimeMillis() - start;
        LOG.debug("extractSegments: " + " ---- TIME ---- " + diff +"ms" );
        return new ArrayList<>(segments);
    }
    

}