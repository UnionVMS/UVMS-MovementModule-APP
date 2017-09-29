package eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util;

import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementComChannelType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Movementarea;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by andreasw on 2017-03-09.
 */
public class TestUtil {


    public MovementType createMovementType(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId) {
        return createMovementType(longitude,latitude,altitude,segmentCategoryType,connectId,0d);
    }


        public MovementType createMovementType(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId, double reportedCourse) {

        MovementActivityType activityType = new MovementActivityType();
        activityType.setCallback("TEST");
        activityType.setMessageId("TEST");
        activityType.setMessageType(MovementActivityTypeType.AUT);

        AssetId assetId = new AssetId();
        assetId.setAssetType(AssetType.VESSEL);
        assetId.setIdType(AssetIdType.GUID);
        assetId.setValue("TEST");

        MovementPoint movementPoint = new MovementPoint();
        movementPoint.setLongitude(longitude);
        movementPoint.setLatitude(latitude);
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
        movementType.setReportedCourse(reportedCourse);
        movementType.setReportedSpeed(0d);
        movementType.setSource(MovementSourceType.NAF);
        movementType.setStatus("TEST");
        movementType.setPositionTime(Calendar.getInstance().getTime());
        movementType.setTripNumber(0d);

        movementType.setCalculatedCourse(0d);
        movementType.setCalculatedSpeed(0d);
        movementType.setComChannelType(MovementComChannelType.NAF);
        movementType.setMetaData(movementMetaData);

        return movementType;
    }

    public static Areatransition getAreaTransition(String code, MovementTypeType transitionType) {
        Areatransition transition = new Areatransition();
        transition.setAreatranAreaId(getArea(code));
        return transition;
    }

    public static Area getArea(String areaCode) {
        Area area = new Area();
        area.setAreaCode(areaCode);
        area.setAreaName(areaCode);
        area.setAreaType(getAreaType(areaCode));
        return area;
    }

    public static AreaType getAreaType(String name) {
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

    public static MovementMetaDataAreaType getMovementMetadataType(String areaCode) {
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
        currentArea.setAreaId(Long.valueOf(areaId));
        currentMoveArea.setMovareaAreaId(currentArea);
        List<Movementarea> currentMoveAreaList = Arrays.asList(currentMoveArea);
        currentMovement.setMovementareaList(currentMoveAreaList);
        return currentMovement;
    }

    public static Movement getPreviousMovement(int areaId, MovementTypeType movementType) {
        Movement previousMovement = new Movement();
        Areatransition priviousTransition = new Areatransition();
        Area previousArea = new Area();
        previousArea.setAreaId(Long.valueOf(areaId));
        priviousTransition.setAreatranAreaId(previousArea);
        priviousTransition.setMovementType(movementType);
        List<Areatransition> previousMoveAreaList = Arrays.asList(priviousTransition);
        previousMovement.setAreatransitionList(previousMoveAreaList);
        return previousMovement;
    }
}
