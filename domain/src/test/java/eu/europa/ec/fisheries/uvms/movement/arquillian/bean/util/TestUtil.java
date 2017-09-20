package eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util;

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

    public MovementType createMovementType(final double longitude, final double latitude, final double altitude, final SegmentCategoryType segmentCategoryType, final String connectId) {

        final MovementActivityType activityType = new MovementActivityType();
        activityType.setCallback("TEST");
        activityType.setMessageId("TEST");
        activityType.setMessageType(MovementActivityTypeType.AUT);

        final AssetId assetId = new AssetId();
        assetId.setAssetType(AssetType.VESSEL);
        assetId.setIdType(AssetIdType.GUID);
        assetId.setValue("TEST");

        final MovementPoint movementPoint = new MovementPoint();
        movementPoint.setLongitude(longitude);
        movementPoint.setLatitude(latitude);
        movementPoint.setAltitude(altitude);

        final MovementMetaData movementMetaData = new MovementMetaData();
        movementMetaData.setFromSegmentType(segmentCategoryType);

        final MovementType movementType = new MovementType();

        movementType.setMovementType(MovementTypeType.POS);
        movementType.setActivity(activityType);
        movementType.setConnectId(connectId);
        movementType.setAssetId(assetId);
        movementType.setDuplicates("false");
        movementType.setInternalReferenceNumber("TEST");
        movementType.setPosition(movementPoint);
        movementType.setReportedCourse(0d);
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

    public static Areatransition getAreaTransition(final String code, final MovementTypeType transitionType) {
        final Areatransition transition = new Areatransition();
        transition.setAreatranAreaId(getArea(code));
        return transition;
    }

    public static Area getArea(final String areaCode) {
        final Area area = new Area();
        area.setAreaCode(areaCode);
        area.setAreaName(areaCode);
        area.setAreaType(getAreaType(areaCode));
        return area;
    }

    public static AreaType getAreaType(final String name) {
        final AreaType areaType = new AreaType();
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
    public static MovementType getMappedMovement(final int numberOfAreas) {
        final MovementType type = new MovementType();
        final MovementMetaData metaData = new MovementMetaData();

        for (int i = 0; i < numberOfAreas; i++) {
            metaData.getAreas().add(getMovementMetadataType("AREA" + i));
        }

        type.setMetaData(metaData);
        return type;
    }

    public static MovementMetaDataAreaType getMovementMetadataType(final String areaCode) {
        final MovementMetaDataAreaType area = new MovementMetaDataAreaType();
        area.setCode(areaCode);
        area.setName(areaCode);
        area.setAreaType(areaCode);
        return area;
    }

    public static Movement getCurrentMovement(final int areaId) {
        final Movement currentMovement = new Movement();
        final Movementarea currentMoveArea = new Movementarea();
        final Area currentArea = new Area();
        currentArea.setAreaId(Long.valueOf(areaId));
        currentMoveArea.setMovareaAreaId(currentArea);
        final List<Movementarea> currentMoveAreaList = Arrays.asList(currentMoveArea);
        currentMovement.setMovementareaList(currentMoveAreaList);
        return currentMovement;
    }

    public static Movement getPreviousMovement(final int areaId, final MovementTypeType movementType) {
        final Movement previousMovement = new Movement();
        final Areatransition priviousTransition = new Areatransition();
        final Area previousArea = new Area();
        previousArea.setAreaId(Long.valueOf(areaId));
        priviousTransition.setAreatranAreaId(previousArea);
        priviousTransition.setMovementType(movementType);
        final List<Areatransition> previousMoveAreaList = Arrays.asList(priviousTransition);
        previousMovement.setAreatransitionList(previousMoveAreaList);
        return previousMovement;
    }
}
