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

import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

public class MovementModelToEntityMapper {

    private static final Logger LOG = LoggerFactory.getLogger(MovementModelToEntityMapper.class);

    public static Movement mapNewMovementEntity(MovementType movement, String username) {
        Movement entity = new Movement();

        if (movement.getReportedSpeed() != null) {
            entity.setSpeed(movement.getReportedSpeed().floatValue());
        }

        if (movement.getReportedCourse() != null) {
            entity.setHeading(movement.getReportedCourse().floatValue());
        }

        entity.setStatus(movement.getStatus());

        if (movement.getPosition() != null) {
            Coordinate coordinate = new Coordinate(movement.getPosition().getLongitude(), movement.getPosition().getLatitude());
            GeometryFactory factory = new GeometryFactory();
            Point point = factory.createPoint(coordinate);
            point.setSRID(4326);
            entity.setLocation(point);
        }

        entity.setUpdated(Instant.now());
        entity.setUpdatedBy(username);

        if (movement.getSource() != null) {
            entity.setSource(movement.getSource());
        } else {
            entity.setSource(MovementSourceType.INMARSAT_C);
        }

        if (movement.getMovementType() != null) {
            entity.setMovementType(movement.getMovementType());
        } else {
            entity.setMovementType(MovementTypeType.POS);
        }

        if (movement.getPositionTime() != null) {
            entity.setTimestamp(movement.getPositionTime().toInstant());
        } else {
            entity.setTimestamp(Instant.now());
        }

        return entity;

    }
    
    public static Movement mapNewMovementEntity(MovementBaseType movement, String username) {
        Movement entity = new Movement();

        if (movement.getReportedSpeed() != null) {
            entity.setSpeed(movement.getReportedSpeed().floatValue());
        }

        if (movement.getReportedCourse() != null) {
            entity.setHeading(movement.getReportedCourse().floatValue());
        }

        entity.setStatus(movement.getStatus());

        if (movement.getPosition() != null) {
            Coordinate coordinate = new Coordinate(movement.getPosition().getLongitude(), movement.getPosition().getLatitude());
            GeometryFactory factory = new GeometryFactory();
            Point point = factory.createPoint(coordinate);
            point.setSRID(4326);
            entity.setLocation(point);

        }

        entity.setUpdated(Instant.now());
        entity.setUpdatedBy(username);

        if (movement.getSource() != null) {
            entity.setSource(movement.getSource());
        } else {
            entity.setSource(MovementSourceType.INMARSAT_C);
        }

        if (movement.getMovementType() != null) {
            entity.setMovementType(movement.getMovementType());
        } else {
            entity.setMovementType(MovementTypeType.POS);
        }

        if (movement.getPositionTime() != null) {
            entity.setTimestamp(movement.getPositionTime().toInstant());
        } else {
            entity.setTimestamp(Instant.now());
        }

        // TODO find a better solution to transfer connectid
        MovementConnect movementConnect = new MovementConnect();
        movementConnect.setId(UUID.fromString(movement.getConnectId()));
        entity.setMovementConnect(movementConnect);

        return entity;
    }
}
