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

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Activity;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;

public class MovementModelToEntityMapper {

    private static final Logger LOG = LoggerFactory.getLogger(MovementModelToEntityMapper.class);

    public static Movement mapNewMovementEntity(MovementType movement, String username) {
        Movement entity = new Movement();

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
            Coordinate coordinate = new Coordinate(movement.getPosition().getLongitude(), movement.getPosition().getLatitude());
            GeometryFactory factory = new GeometryFactory();
            Point point = factory.createPoint(coordinate);
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
            entity.setTimestamp(movement.getPositionTime().toInstant());
        } else {
            entity.setTimestamp(DateUtil.nowUTC());
        }

        if (movement.getActivity() != null) {
            Activity activity = createActivity(movement);
            entity.setActivity(activity);
        }


        entity.setProcessed(false);

        return entity;

    }
    
    public static Movement mapNewMovementEntity(MovementBaseType movement, String username) {
        Movement entity = new Movement();

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
            Coordinate coordinate = new Coordinate(movement.getPosition().getLongitude(), movement.getPosition().getLatitude());
            GeometryFactory factory = new GeometryFactory();
            Point point = factory.createPoint(coordinate);
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
            entity.setTimestamp(movement.getPositionTime().toInstant());
        } else {
            entity.setTimestamp(DateUtil.nowUTC());
        }

        if (movement.getActivity() != null) {
            Activity activity = createActivity(movement);
            entity.setActivity(activity);
        }

        // TODO find a better solution to transfer connectid
        MovementConnect movementConnect = new MovementConnect();
        movementConnect.setId(UUID.fromString(movement.getConnectId()));
        entity.setMovementConnect(movementConnect);

        entity.setProcessed(false);

        return entity;
    }

    public static Activity createActivity(MovementBaseType movement){
        Activity activity = new Activity();
        activity.setActivityType(movement.getActivity().getMessageType());
        activity.setCallback(movement.getActivity().getCallback());
        activity.setMessageId(movement.getActivity().getMessageId());
        activity.setUpdated(DateUtil.nowUTC());
        activity.setUpdatedBy("UVMS");
        return activity;
    }
}
