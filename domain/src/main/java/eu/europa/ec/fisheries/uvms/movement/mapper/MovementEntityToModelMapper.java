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
import eu.europa.ec.fisheries.uvms.movement.entity.*;
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

    public static MovementBaseType mapToMovementBaseType(final Movement movement) {

        final MovementBaseType model = new MovementBaseType();

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

        final MovementPoint movementPoint = new MovementPoint();
        final Point point = movement.getLocation();
        movementPoint.setLatitude(point.getY());
        movementPoint.setLongitude(point.getX());
        model.setPosition(movementPoint);

        try {
            model.setConnectId(mapToConnectId(movement.getMovementConnect()));
        } catch (final MovementDaoException ex) {
            LOG.debug("Error when mapping to carrier {} ", ex.getMessage());
        }

        return model;
    }


    public static MovementType mapToMovementType(final MinimalMovement movement) {

        //Previous movement ID is mapped in MovementBatchModelBean
        final MovementType model = new MovementType();

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

        final MovementPoint movementPoint = new MovementPoint();
        final Point point = movement.getLocation();

        movementPoint.setLatitude(point.getY());
        movementPoint.setLongitude(point.getX());
        model.setPosition(movementPoint);

        try {
            model.setConnectId(mapToConnectId(movement.getMovementConnect()));
        } catch (final MovementDaoException ex) {
            LOG.debug("Error when mapping to carrier {} ", ex.getMessage());
        }

        model.setWkt(WKTUtil.getWktPointFromMovement(movement));

        try {
            model.setConnectId(mapToConnectId(movement.getMovementConnect()));
        } catch (final MovementDaoException ex) {
            LOG.debug("Error when mapping to carrier {} ", ex.getMessage());
        }

        return model;
    }

    public static MovementType mapToMovementType(final Movement movement) {

        //Previous movement ID is mapped in MovementBatchModelBean
        final MovementType model = new MovementType();

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

        final MovementPoint movementPoint = new MovementPoint();
        final Point point = movement.getLocation();

        movementPoint.setLatitude(point.getY());
        movementPoint.setLongitude(point.getX());
        model.setPosition(movementPoint);

        try {
            model.setConnectId(mapToConnectId(movement.getMovementConnect()));
        } catch (final MovementDaoException ex) {
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

        try {
            model.setConnectId(mapToConnectId(movement.getMovementConnect()));
        } catch (final MovementDaoException ex) {
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

        return model;
    }

    private static List<MovementMetaDataAreaType> mapToMovementMetaDataAreaTypeList(final List<Movementarea> areas) {
        final List<MovementMetaDataAreaType> areaList = new ArrayList<>();
        for (final Movementarea area : areas) {
            final MovementMetaDataAreaType type = new MovementMetaDataAreaType();
            type.setAreaType(area.getMovareaAreaId().getAreaType().getName());
            type.setRemoteId(area.getMovareaAreaId().getRemoteId());
            type.setCode(area.getMovareaAreaId().getAreaCode());
            type.setName(area.getMovareaAreaId().getAreaName());
            areaList.add(type);
        }
        return areaList;
    }

    public static MovementActivityType mapToActivityType(final Activity activity) {
        final MovementActivityType actType = new MovementActivityType();
        if (activity != null) {
            actType.setCallback(activity.getCallback());
            actType.setMessageId(activity.getMessageId());
            actType.setMessageType(activity.getActivityType());
        }
        return actType;
    }

    public static MovementMetaData mapToMovementMetaData(final Movementmetadata metaData) {
        final MovementMetaData meta = new MovementMetaData();

        final ClosestLocationType country = new ClosestLocationType();
        country.setCode(metaData.getClosestCountryCode());
        country.setDistance(metaData.getClosestCountryDistance());
        country.setRemoteId(metaData.getClosestCountryRemoteId());
        meta.setClosestCountry(country);

        final ClosestLocationType port = new ClosestLocationType();
        port.setCode(metaData.getClosestPortCode());
        port.setDistance(metaData.getClosestPortDistance());
        port.setRemoteId(metaData.getClosestPortRemoteId());
        meta.setClosestPort(port);

        return meta;
    }

    public static List<MovementType> mapToMovementType(final List<Movement> movements) {
        final List<MovementType> mappedMovements = new ArrayList<>();
        final long start = System.currentTimeMillis();
        for (final Movement movement : movements) {
            mappedMovements.add(mapToMovementType(movement));
        }
        final long diff = System.currentTimeMillis() - start;
        LOG.debug("mapToMovementType: " + " ---- TIME ---- " + diff +"ms" );
        return mappedMovements;
    }

    public static List<MovementType> mapToMovementTypeFromLatestMovement(final List<LatestMovement> movements) {
        final List<MovementType> mappedMovements = new ArrayList<>();
        final long start = System.currentTimeMillis();
        for (final LatestMovement movement : movements) {
            mappedMovements.add(mapToMovementType(movement.getMovement()));
        }
        final long diff = System.currentTimeMillis() - start;
        LOG.debug("mapToMovementType: " + " ---- TIME ---- " + diff +"ms" );
        return mappedMovements;
    }

    public static String mapToConnectId(final MovementConnect connect) throws MovementDaoException {
        if (connect != null) {
            return connect.getValue();
        }
        return null;
    }

    public static List<MovementSegment> mapToMovementSegment(final List<Segment> segments) {
        final long start = System.currentTimeMillis();
        final List<MovementSegment> mappedSegments = new ArrayList<>();
        for (final Segment segment : segments) {
            mappedSegments.add(mapToMovementSegment(segment));
        }
        final long diff = System.currentTimeMillis() - start;
        LOG.debug("mapToMovementSegment: " + " ---- TIME ---- " + diff +"ms" );
        return mappedSegments;
    }

    public static MovementSegment mapToMovementSegment(final Segment segment) {
        final MovementSegment movSegment = new MovementSegment();
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

    public static MovementTrack mapToMovementTrack(final Track track) {
        final MovementTrack movementTrack = new MovementTrack();
        movementTrack.setDistance(track.getDistance());
        movementTrack.setDuration(track.getDuration());
        movementTrack.setTotalTimeAtSea(track.getTotalTimeAtSea());
        movementTrack.setWkt(WKTUtil.getWktLineStringFromTrack(track));
        movementTrack.setId(track.getId().toString());
        return movementTrack;
    }

    public static Map<String, List<Movement>> orderMovementsByConnectId(final List<Movement> movements) {
        final Map<String, List<Movement>> orderedMovements = new HashMap<>();
        final long start = System.currentTimeMillis();
        for (final Movement movement : movements) {
            if (orderedMovements.get(movement.getMovementConnect().getValue()) == null) {
                orderedMovements.put(movement.getMovementConnect().getValue(), new ArrayList<>(Arrays.asList(movement)));
            } else {
                orderedMovements.get(movement.getMovementConnect().getValue()).add(movement);
            }
        }
        final long diff = System.currentTimeMillis() - start;
        LOG.debug("orderMovementByConnectID: " + " ---- TIME ---- " + diff + "ms");
        return orderedMovements;
    }

    public static List<MovementTrack> extractTracks(final List<Segment> segments) {
        final long start = System.currentTimeMillis();
        final Set<Track> tracks = new HashSet<>();
        for (final Segment segment : segments) {
            tracks.add(segment.getTrack());
        }
        final List<MovementTrack> movementTracks = new ArrayList<>();
        for (final Track track : tracks) {
            movementTracks.add(mapToMovementTrack(track));
        }

        final long diff = System.currentTimeMillis() - start;
        LOG.debug("extractTracks: " + " ---- TIME ---- " + diff +"ms" );
        return movementTracks;
    }

    public static ArrayList<Segment> extractSegments(final ArrayList<Movement> movements, final boolean excludeFirstLastSegment) {
        final Set<Segment> segments = new HashSet<>();
        final long start = System.currentTimeMillis();
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

        final long diff = System.currentTimeMillis() - start;
        LOG.debug("extractSegments: " + " ---- TIME ---- " + diff +"ms" );
        return new ArrayList<>(segments);
    }
    

}