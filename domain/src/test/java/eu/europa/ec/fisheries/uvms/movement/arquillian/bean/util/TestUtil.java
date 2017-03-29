package eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementComChannelType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import org.junit.Assert;

import java.util.Calendar;
import java.util.List;

/**
 * Created by andreasw on 2017-03-09.
 */
public class TestUtil {

    public MovementType createMovementType(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId) {

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


    /*
    // old version
    private Movement createMovement(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        return createMovement(longitude, latitude, altitude, segmentCategoryType, connectId, "TEST");
    }

    // added possibility to specify user for easier debug
    private Movement createMovement(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId, String userName) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        MovementType movementType = createMovementType(longitude, latitude, altitude, segmentCategoryType, connectId);
        movementType = movementBatchModelBean.createMovement(movementType, userName);
        em.flush();
        Assert.assertNotNull(movementType.getConnectId());
        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getConnectId());
        List<Movement> movementList = movementConnect.getMovementList();
        Assert.assertNotNull(movementList);
        return movementList.get(movementList.size() - 1);
    }
    */


    public Movement createMovement() {

      Movement movement = new Movement();
        //final Movement currentMovement = MovementModelToEntityMapper.mapNewMovementEntity(movement, username);
      //movement.setAltitude(0);

//      movement.setLocation(point);

        //movement.setAreatransitionList();
    /*
    movement.setDuplicate();
    movement.setDuplicateId();
    movement.setFromSegment();
    movement.setGuid();
    movement.setGuid();
    movement.setInternalReferenceNumber();
    movement.setLocation();
    movement.setHeading();
    movement.setMetadata();
    movement.setMovementConnect();
    movement.setMovementSource();
    movement.setProcessed();
    movement.setMoveAltitude();
    movement.setMovementType();
    movement.setSpeed();
    movement.setStatus();
    movement.setTempFromSegment();
    movement.setToSegment();
    movement.setTimestamp();
    movement.setTrack();
    movement.setTripNumber();
    movement.setUpdated();
    movement.setUpdatedBy();
    //movement.set
    */

    return movement;
    }



}
