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
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Activity;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movementmetadata;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

public class MovementModelToEntityMapper {

    private static final Logger LOG = LoggerFactory.getLogger(MovementModelToEntityMapper.class);

    public static Movement mapNewMovementEntity(MovementType movement, String username) throws MovementServiceException {
        try {
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

            if (movement.getMetaData() != null) {
                Movementmetadata metaData = mapToMovementMetaData(movement.getMetaData());
                entity.setMetadata(metaData);
            }

            entity.setProcessed(false);

            return entity;
        } catch (Exception e) {
            LOG.error("[ ERROR when mapping to Movement entity: < mapNewMovementEntity > ]");
            throw new MovementServiceException("Error when mapping to Movement Entity ", e, ErrorCode.DAO_MAPPING_ERROR);
        }
    }
    
    public static Movement mapNewMovementEntity(MovementBaseType movement, String username) throws MovementServiceException {
        try {
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
                
                if (movement.getPosition().getAltitude() != null) {
                    entity.setAltitude(movement.getPosition().getAltitude());
                }
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
            movementConnect.setValue(UUID.fromString(movement.getConnectId()));
            entity.setMovementConnect(movementConnect);
            
            entity.setProcessed(false);

            return entity;
        } catch (Exception e) {
            LOG.error("[ ERROR when mapping to Movement entity: < mapNewMovementEntity > ]");
            throw new MovementServiceException("Error when mapping to Movement Entity ", e, ErrorCode.DAO_MAPPING_ERROR);
        }
    }

    public static Movementmetadata mapToMovementMetaData(MovementMetaData metaData) {
        Movementmetadata meta = new Movementmetadata();

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

    public static Activity createActivity(MovementBaseType movement) throws MovementServiceException {
        try {
            Activity activity = new Activity();
            activity.setActivityType(movement.getActivity().getMessageType());
            activity.setCallback(movement.getActivity().getCallback());
            activity.setMessageId(movement.getActivity().getMessageId());
            activity.setUpdated(DateUtil.nowUTC());
            activity.setUpdatedBy("UVMS");
            return activity;
        } catch (Exception e) {
            LOG.error("[ ERROR when mapping to Activity entity: < createActivity > ]");
            throw new MovementServiceException("ERROR when mapping to Activity entity ", e, ErrorCode.DAO_MAPPING_ERROR);
        }
    }
}
