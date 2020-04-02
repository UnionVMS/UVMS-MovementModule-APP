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
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movementmetadata;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaTransition;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Movementarea;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class MockData {

    public static AreaTransition getAreaTransition(Area area, MovementTypeType transitionType) {
        AreaTransition transition = new AreaTransition();
        transition.setAreaId(area);
        return transition;
    }

    public static AreaType createAreaType() {
        AreaType areaType = new AreaType();
        String input = "TestAreaType";
        areaType.setName(input);
        areaType.setUpdatedTime(Instant.now());
        areaType.setUpdatedUser("TestUser");
        return areaType;
    }

    public static Area createArea() {
        AreaType areaType = createAreaType();
        return createArea(areaType);
    }
    
    public static Area createArea(String code) {
        AreaType areaType = createAreaType();
        Area area = createArea(areaType);
        area.setAreaCode(code);
        return area;
    }
    
    public static Area createArea(AreaType areaType) {
        Area area = new Area();
        area.setAreaName("TestArea");
        area.setAreaCode("AreaCode" + MovementHelpers.getRandomIntegers(10));
        area.setRemoteId("TestRemoteId");
        area.setAreaUpdattim(Instant.now());
        area.setAreaUpuser("TestUser");
        area.setAreaType(areaType);
        return area;
    }
    
    public static Movementarea getMovementArea(Area area, Movement movement) {
        Movementarea movementArea = new Movementarea();
        movementArea.setMovareaAreaId(area);
        movementArea.setMovareaMoveId(movement);
        movementArea.setMovareaUpdattim(Instant.now());
        movementArea.setMovareaUpuser("Test");
        return movementArea;
    }

    /**
     * Get a movement type with an added metadata and areas in the metadata
     * depending on how many areas you want ( numberOfAreas )
     *
     * @param numberOfAreas
     * @return
     */
    public static MovementType getMappedMovement(int numberOfAreas) {
        MovementType type = new MovementType();
        MovementMetaData metaData = new MovementMetaData();

        for (int i = 0; i < numberOfAreas; i++) {
            metaData.getAreas().add(getMovementMetadataType("AREA" + i));
        }

        type.setMetaData(metaData);
        return type;
    }

    private static MovementMetaDataAreaType getMovementMetadataType(String areaCode) {
        MovementMetaDataAreaType area = new MovementMetaDataAreaType();
        area.setCode(areaCode);
        area.setName(areaCode);
        area.setAreaType(areaCode);
        return area;
    }

    public static Movement getCurrentMovement(int areaId) {
        Movement currentMovement = new Movement();
        Movementarea currentMoveArea = new Movementarea();
        Area currentArea = new Area();
        currentArea.setAreaId((long) areaId);
        currentMoveArea.setMovareaAreaId(currentArea);
        List<Movementarea> currentMoveAreaList = Collections.singletonList(currentMoveArea);
        currentMovement.setMovementareaList(currentMoveAreaList);
        return currentMovement;
    }

    public static Movement getPreviousMovement(int areaId, MovementTypeType movementType) {
        Movement previousMovement = new Movement();
        AreaTransition priviousTransition = new AreaTransition();
        Area previousArea = new Area();
        previousArea.setAreaId((long) areaId);
        priviousTransition.setAreaId(previousArea);
        priviousTransition.setMovementType(movementType);
        List<AreaTransition> previousMoveAreaList = Collections.singletonList(priviousTransition);
        previousMovement.setAreaTransitionList(previousMoveAreaList);
        return previousMovement;
    }

    public static Movement createMovement(double longitude, double latitude, String connectId) {
        return createMovement(longitude, latitude, connectId, 0d, "Test");
    }
    
    public static Movement createMovement(double longitude, double latitude, String connectId, double reportedCourse, String username) {

        LatLong latLong = new LatLong(latitude, longitude, DateUtil.nowUTC());
        latLong.bearing = reportedCourse;
        return createMovement(latLong, connectId, username);
    }

    public static Movement createMovement(LatLong latlong, String connectId, String username) {

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
        movementConnect.setValue(connectId);
        movement.setMovementConnect(movementConnect);
        movement.setInternalReferenceNumber("TEST");
        Coordinate coordinate = new Coordinate(latlong.longitude, latlong.latitude);
        GeometryFactory factory = new GeometryFactory();
        Point point = factory.createPoint(coordinate);
        point.setSRID(4326);
        movement.setLocation(point);
        movement.setAltitude(0);
        movement.setHeading(latlong.bearing);
        movement.setSpeed(latlong.speed);
        movement.setMovementSource(MovementSourceType.NAF);
        movement.setStatus("TEST");
        movement.setUpdated(Instant.now());
        movement.setUpdatedBy(username);
        
        Movementmetadata metadata = new Movementmetadata();
        metadata.setMovemetUpdattim(Instant.now());
        metadata.setMovemetUpuser("Test");
        movement.setMetadata(metadata);

        movement.setTimestamp(latlong.positionTime);
        movement.setTripNumber(0d);
        
        movement.setMovementareaList(new ArrayList<Movementarea>());
        
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

        MovementMetaData movementMetaData = new MovementMetaData();
        movementMetaData.setFromSegmentType(segmentCategoryType);

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
        movementType.setMetaData(movementMetaData);

        return movementType;
    }
}
