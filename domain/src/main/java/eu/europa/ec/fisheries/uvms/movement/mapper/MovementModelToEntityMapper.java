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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.dto.SegmentCalculations;
import eu.europa.ec.fisheries.uvms.movement.entity.Activity;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.entity.Movementmetadata;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.util.CalculationUtil;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.util.GeometryUtil;
import eu.europa.ec.fisheries.uvms.movement.util.SegmentCalculationUtil;

//TODO: AW: Rewrite this, This class is NOT ModelToEntity Mapper, contains a lot of business logic and handles mostly
// entities..
public class MovementModelToEntityMapper {

    final static Logger LOG = LoggerFactory.getLogger(MovementModelToEntityMapper.class);

    public static Movement mapNewMovementEntity(final MovementType movement, final String username) throws MovementDaoMappingException {
        try {
            final Movement entity = new Movement();

            if (movement.getReportedSpeed() != null) {
                entity.setSpeed(movement.getReportedSpeed());
            }

            if (movement.getReportedCourse() != null) {
                entity.setHeading(movement.getReportedCourse());
            }
            
            entity.setInternalReferenceNumber(movement.getInternalReferenceNumber());
            entity.setTripNumber(movement.getTripNumber());

            entity.setStatus(movement.getStatus());

            if (movement.getPosition() != null) {
                final Coordinate coordinate = new Coordinate(movement.getPosition().getLongitude(), movement.getPosition().getLatitude());
                final GeometryFactory factory = new GeometryFactory();
                final Point point = factory.createPoint(coordinate);
                point.setSRID(4326);
                entity.setLocation(point);
            }

            entity.setUpdated(DateUtil.nowUTC());
            entity.setUpdatedBy(username);

            if (movement.getSource() != null) {
                entity.setMovementSource(movement.getSource());
            } else {
                entity.setMovementSource(MovementSourceType.INMARSAT_C);
            }

            if (movement.getMovementType() != null) {
                entity.setMovementType(movement.getMovementType());
            } else {
                entity.setMovementType(MovementTypeType.POS);
            }

            if (movement.getPositionTime() != null) {
                entity.setTimestamp(movement.getPositionTime());
            } else {
                entity.setTimestamp(DateUtil.nowUTC());
            }

            if (movement.getActivity() != null) {
                final Activity activity = createActivity(movement);
                entity.setActivity(activity);
            }

            if (movement.getMetaData() != null) {
                final Movementmetadata metaData = mapToMovementMetaData(movement.getMetaData());
                entity.setMetadata(metaData);
            }

            entity.setProcessed(false);

            return entity;
        } catch (final Exception e) {
            LOG.error("[ ERROR when mapping to Movement entity: < mapNewMovementEntity > ]");
            throw new MovementDaoMappingException("Error when mapping to Movement Entity ", e);
        }
    }

    public static Movementmetadata mapToMovementMetaData(final MovementMetaData metaData) {
        final Movementmetadata meta = new Movementmetadata();

        meta.setMovemetUpdattim(DateUtil.nowUTC());
        meta.setMovemetUpuser("UVMS");

        if (metaData.getClosestPort() != null) {
            meta.setClosestPortCode(metaData.getClosestPort().getCode());
            meta.setClosestPortDistance(metaData.getClosestPort().getDistance());
            meta.setClosestPortRemoteId(metaData.getClosestPort().getRemoteId());
            meta.setClosestPortName(metaData.getClosestPort().getName());
        }

        if (metaData.getClosestCountry() != null) {
            meta.setClosestCountryCode(metaData.getClosestCountry().getCode());
            meta.setClosestCountryDistance(metaData.getClosestCountry().getDistance());
            meta.setClosestCountryRemoteId(metaData.getClosestCountry().getRemoteId());
            meta.setClosestCountryName(metaData.getClosestCountry().getName());
        }

        return meta;
    }

    //ToDo: This method does not actually map anything. It only creates a MovementConnect object.
    //ToDo: Should be removed or made to actually map something to something else.
    public static MovementConnect mapToMovementConnenct(final String value, final AssetIdType idType) throws MovementDaoMappingException {
        try {
            final MovementConnect connect = new MovementConnect();
            connect.setUpdated(DateUtil.nowUTC());
            connect.setUpdatedBy("UVMS");
            connect.setValue(value);
            return connect;
        } catch (final Exception e) {
            LOG.error("[ ERROR when mapping to MovementConnect entity: < mapToMovementConnenct > ]");
            throw new MovementDaoMappingException("ERROR when mapping to MovementConnect entity", e);
        }
    }

    public static Activity createActivity(final MovementBaseType movement) throws MovementDaoMappingException {
        try {
            final Activity activity = new Activity();
            activity.setActivityType(movement.getActivity().getMessageType());
            activity.setCallback(movement.getActivity().getCallback());
            activity.setMessageId(movement.getActivity().getMessageId());
            activity.setUpdated(DateUtil.nowUTC());
            activity.setUpdatedBy("UVMS");
            return activity;
        } catch (final Exception e) {
            LOG.error("[ ERROR when mapping to Activity entity: < createActivity > ]");
            throw new MovementDaoMappingException("ERROR when mapping to Activity entity ", e);
        }
    }

    public static Track createTrack(final Segment segment) throws MovementDaoMappingException {
        try {
            final Track track = new Track();
            track.setDistance(segment.getDistance());
            track.setDuration(segment.getDuration());
            track.setUpdated(DateUtil.nowUTC());
            track.setLocation(segment.getLocation());
            track.setUpdatedBy("UVMS");
            // TODO: AW Fixed association when creating track.
            track.setMovementList(new ArrayList<Movement>());
            track.getMovementList().add(segment.getFromMovement());
            track.getMovementList().add(segment.getToMovement());
            // TODO: AW Fixed association for segments
            track.setSegmentList(new ArrayList<Segment>());
            track.getSegmentList().add(segment);

            return track;
        } catch (final Exception e) {
            LOG.error("[ ERROR when mapping to Activity entity: < createTrack > ]");
            throw new MovementDaoMappingException("ERROR when mapping to Activity entity ", e);
        }
    }

    //ToDo: This method does not perform mapping. It updates a Segment and a Track and uses a Movement.
    //ToDo: It could be a good idea to extract this method from the mapper class.
    public static void updateTrack(final Track track, final Movement currentPosition, final Segment segment) throws GeometryUtilException {

        if (track.getMovementList() == null) {
            track.setMovementList(new ArrayList<Movement>());
        }

        track.getMovementList().add(currentPosition);
        segment.setTrack(track);
        currentPosition.setTrack(track);
        track.getSegmentList().add(segment);

        final double calculatedDistance = track.getDistance() + segment.getDistance();
        track.setDistance(calculatedDistance);
        final double calculatedDurationInSeconds = track.getDuration() + segment.getDuration();
        track.setDuration(calculatedDurationInSeconds);

        final LineString updatedTrackLineString = GeometryUtil.getLineStringFromMovments(track.getMovementList());

        if (!segment.getSegmentCategory().equals(SegmentCategoryType.ENTER_PORT) || !segment.getSegmentCategory().equals(SegmentCategoryType.IN_PORT)) {
            final double distance = track.getTotalTimeAtSea();
            track.setTotalTimeAtSea(distance + calculatedDistance);
        }

        track.setLocation(updatedTrackLineString);

    }

    /**
     *
     * @param fromMovement
     * @param toMovement
     * @return
     * @throws GeometryUtilException
     * @throws MovementDaoMappingException
     */
    public static Segment createSegment(final Movement fromMovement, final Movement toMovement) throws GeometryUtilException, MovementDaoMappingException {
        final Segment segment = new Segment();

        if (toMovement == null && fromMovement == null) {
            LOG.error("[ ERROR when mapping to Segment entity: currentPosition AND previous Position cannot be null <createSegment> ]");
            throw new MovementDaoMappingException("ERROR when mapping to Segment entity: currentPosition AND previous Position cannot be null");
        }

        final SegmentCalculations positionCalculations = CalculationUtil.getPositionCalculations(fromMovement, toMovement);

        final SegmentCategoryType segCat = SegmentCalculationUtil.getSegmentCategoryType(positionCalculations, fromMovement, toMovement);
        segment.setSegmentCategory(segCat);

        segment.setDistance(positionCalculations.getDistanceBetweenPoints());
        segment.setSpeedOverGround(positionCalculations.getAvgSpeed());
        segment.setCourseOverGround(positionCalculations.getCourse());
        segment.setDuration(positionCalculations.getDurationBetweenPoints());

        segment.setFromMovement(fromMovement);
        segment.setToMovement(toMovement);

        toMovement.setTempFromSegment(segment);

        segment.setUpdated(DateUtil.nowUTC());
        segment.setUpdatedBy("UVMS");

        final LineString segmentLineString = GeometryUtil.getLineStringFromMovments(fromMovement, toMovement);
        segment.setLocation(segmentLineString);

        return segment;
    }

    //ToDo: This method does not perform any mapping but rather updates an existing Segment.
    //ToDo: It should be extracted out from the mapper class.
    public static void updateSegment(final Segment segment, final Movement fromMovement, final Movement toMovement) throws GeometryUtilException, MovementDaoMappingException {

        if (toMovement == null && fromMovement == null) {
            LOG.error("[ ERROR when updating Segment entity: currentPosition AND previous Position cannot be null <updateSegment> ]");
            throw new MovementDaoMappingException("ERROR when updating Segment entity: currentPosition AND previous Position cannot be null");
        }

        final SegmentCalculations positionCalculations = CalculationUtil.getPositionCalculations(fromMovement, toMovement);

        final SegmentCategoryType segCat = SegmentCalculationUtil.getSegmentCategoryType(positionCalculations, fromMovement, toMovement);
        segment.setSegmentCategory(segCat);

        segment.setDistance(positionCalculations.getDistanceBetweenPoints());
        segment.setSpeedOverGround(positionCalculations.getAvgSpeed());
        segment.setCourseOverGround(positionCalculations.getCourse());
        segment.setDuration(positionCalculations.getDurationBetweenPoints());

        segment.setUpdated(DateUtil.nowUTC());
        segment.setUpdatedBy("UVMS");

        segment.setFromMovement(fromMovement);
        segment.setToMovement(toMovement);

        final LineString segmentLineString = GeometryUtil.getLineStringFromMovments(fromMovement, toMovement);
        segment.setLocation(segmentLineString);

    }

    public static AreaType mapToAreaType(final MovementMetaDataAreaType type) {
        final AreaType newAreaType = new AreaType();
        newAreaType.setName(type.getAreaType());
        newAreaType.setUpdatedUser("UVMS");
        newAreaType.setUpdatedTime(DateUtil.nowUTC());
        return newAreaType;
    }

    public static Area maptoArea(final MovementMetaDataAreaType area, final AreaType areaType) {
        final Area newArea = new Area();
        newArea.setAreaCode(area.getCode());
        newArea.setAreaName(area.getName());
        newArea.setRemoteId(area.getRemoteId());
        newArea.setAreaType(areaType);
        newArea.setAreaUpuser("UVMS");
        newArea.setAreaUpdattim(DateUtil.nowUTC());
        return newArea;
    }

}