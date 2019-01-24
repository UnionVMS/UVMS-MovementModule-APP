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
package eu.europa.ec.fisheries.uvms.movement.service;

import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetType;
import eu.europa.ec.fisheries.schema.movement.v1.*;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Activity;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;

import java.sql.Date;
import java.time.Instant;
import java.util.UUID;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class MockData {

    public static Movement createMovement(double longitude, double latitude, UUID connectId) {
        return createMovement(longitude, latitude, connectId, 0d, "Test");
    }
    
    public static Movement createMovement(double longitude, double latitude, UUID connectId, double reportedCourse, String username) {

        LatLong latLong = new LatLong(latitude, longitude, DateUtil.nowUTC());
        latLong.bearing = reportedCourse;
        return createMovement(latLong, connectId, username);
    }

    public static Movement createMovement(LatLong latlong, UUID connectId, String username) {

        Activity activityType = new Activity();
        activityType.setCallback("TEST");
        activityType.setMessageId("TEST");
        activityType.setActivityType(MovementActivityTypeType.AUT);
        activityType.setUpdated(Instant.now());
        activityType.setUpdatedBy("TEST");

        Movement movement = new Movement();

        movement.setMovementType(MovementTypeType.POS);
        movement.setActivity(activityType);
        MovementConnect movementConnect = new MovementConnect();
        movementConnect.setId(connectId);
        movementConnect.setUpdated(Instant.now());
        movementConnect.setUpdatedBy("Mock Data");
        movement.setMovementConnect(movementConnect);
        movement.setInternalReferenceNumber("TEST");
        Coordinate coordinate = new Coordinate(latlong.longitude, latlong.latitude);
        GeometryFactory factory = new GeometryFactory();
        Point point = factory.createPoint(coordinate);
        point.setSRID(4326);
        movement.setLocation(point);
        movement.setAltitude(0d);
        movement.setHeading(latlong.bearing);
        movement.setSpeed(latlong.speed);
        movement.setMovementSource(MovementSourceType.NAF);
        movement.setStatus("TEST");
        movement.setUpdated(Instant.now());
        movement.setUpdatedBy(username);

        movement.setTimestamp(latlong.positionTime);
        movement.setTripNumber(0d);
        
        movement.setDuplicate(false);
        movement.setProcessed(false);
        
        return movement;
    }
    
    public static MovementType createMovementType(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId, double reportedCourse) {

        LatLong latLong = new LatLong(latitude, longitude, DateUtil.nowUTC());
        latLong.bearing = reportedCourse;
        return createMovementType(latLong, segmentCategoryType, connectId, altitude);
    }

    public static MovementType createMovementType(LatLong latlong, SegmentCategoryType segmentCategoryType, String connectId, double altitude) {

        MovementActivityType activityType = new MovementActivityType();
        activityType.setCallback("TEST");
        activityType.setMessageId("TEST");
        activityType.setMessageType(MovementActivityTypeType.AUT);

        AssetId assetId = new AssetId();
        assetId.setAssetType(AssetType.VESSEL);
        assetId.setIdType(AssetIdType.GUID);
        assetId.setValue("TEST");

        MovementPoint movementPoint = new MovementPoint();
        movementPoint.setLongitude(latlong.longitude);
        movementPoint.setLatitude(latlong.latitude);
        movementPoint.setAltitude(altitude);


        MovementType movementType = new MovementType();

        movementType.setMovementType(MovementTypeType.POS);
        movementType.setActivity(activityType);
        movementType.setConnectId(connectId);
        movementType.setAssetId(assetId);
        movementType.setDuplicates("false");
        movementType.setInternalReferenceNumber("TEST");
        movementType.setPosition(movementPoint);
        movementType.setReportedCourse(latlong.bearing);
        movementType.setReportedSpeed(latlong.speed);
        movementType.setSource(MovementSourceType.NAF);
        movementType.setStatus("TEST");

        movementType.setPositionTime(Date.from(latlong.positionTime));
        movementType.setTripNumber(0d);

        movementType.setCalculatedCourse(0d);
        movementType.setCalculatedSpeed(0d);
        movementType.setComChannelType(MovementComChannelType.NAF);

        return movementType;
    }
}
