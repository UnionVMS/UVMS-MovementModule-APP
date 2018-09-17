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
package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetType;
import eu.europa.ec.fisheries.schema.movement.v1.*;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;

import java.sql.Date;
import java.util.Collections;
import java.util.List;

public class MockData {

    public static Areatransition getAreaTransition(String code, MovementTypeType transitionType) {
        Areatransition transition = new Areatransition();
        transition.setAreatranAreaId(getArea(code));
        return transition;
    }

    private static Area getArea(String areaCode) {
        Area area = new Area();
        area.setAreaCode(areaCode);
        area.setAreaName(areaCode);
        area.setAreaType(getAreaType(areaCode));
        return area;
    }

    private static AreaType getAreaType(String name) {
        AreaType areaType = new AreaType();
        areaType.setName(name);
        return areaType;
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
        Areatransition priviousTransition = new Areatransition();
        Area previousArea = new Area();
        previousArea.setAreaId((long) areaId);
        priviousTransition.setAreatranAreaId(previousArea);
        priviousTransition.setMovementType(movementType);
        List<Areatransition> previousMoveAreaList = Collections.singletonList(priviousTransition);
        previousMovement.setAreatransitionList(previousMoveAreaList);
        return previousMovement;
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
