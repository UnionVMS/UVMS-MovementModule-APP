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
import com.vividsolutions.jts.geom.Point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Activity;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Movementmetadata;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;

public class MovementModelToEntityMapper {

    final static Logger LOG = LoggerFactory.getLogger(MovementModelToEntityMapper.class);

    public static Movement mapNewMovementEntity(MovementType movement, String username) throws MovementDaoMappingException {
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
                entity.setTimestamp(movement.getPositionTime());
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
            throw new MovementDaoMappingException("Error when mapping to Movement Entity ", e);
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

    public static Activity createActivity(MovementBaseType movement) throws MovementDaoMappingException {
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
            throw new MovementDaoMappingException("ERROR when mapping to Activity entity ", e);
        }
    }

    public static AreaType mapToAreaType(MovementMetaDataAreaType type) {
        AreaType newAreaType = new AreaType();
        newAreaType.setName(type.getAreaType());
        newAreaType.setUpdatedUser("UVMS");
        newAreaType.setUpdatedTime(DateUtil.nowUTC());
        return newAreaType;
    }

    public static Area maptoArea(MovementMetaDataAreaType area, AreaType areaType) {
        Area newArea = new Area();
        newArea.setAreaCode(area.getCode());
        newArea.setAreaName(area.getName());
        newArea.setRemoteId(area.getRemoteId());
        newArea.setAreaType(areaType);
        newArea.setAreaUpuser("UVMS");
        newArea.setAreaUpdattim(DateUtil.nowUTC());
        return newArea;
    }

}