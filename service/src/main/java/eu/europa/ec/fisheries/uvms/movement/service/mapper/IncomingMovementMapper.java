package eu.europa.ec.fisheries.uvms.movement.service.mapper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.v1.*;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetMTEnrichmentResponse;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.entity.*;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.MovementDetails;

import java.time.Instant;
import java.util.UUID;

public abstract class IncomingMovementMapper {


    public static Movement mapNewMovementEntity(IncomingMovement ic, String username) {
        Movement entity = new Movement();

        if (ic.getReportedSpeed() != null) {
            entity.setSpeed(ic.getReportedSpeed().floatValue());
        }
        entity.setHeading( ic.getReportedCourse() == null ? -1f : ic.getReportedCourse().floatValue());
        entity.setInternalReferenceNumber(ic.getInternalReferenceNumber());
        entity.setTripNumber(ic.getTripNumber());
        entity.setStatus(ic.getStatus());
        //entity.setAltitude(ic.getAltitude());
        //entity.setMoveAltitude(ic.getAltitude());

        //MovementConnect (aka asset)
        MovementConnect movementConnect = new MovementConnect();
        movementConnect.setId(UUID.fromString(ic.getAssetGuid()));
        movementConnect.setFlagState(ic.getFlagState());
        movementConnect.setName(ic.getAssetName());
        movementConnect.setUpdated(Instant.now());
        movementConnect.setUpdatedBy(username);
        entity.setMovementConnect(movementConnect);

        Coordinate coordinate = new Coordinate(ic.getLongitude(), ic.getLatitude());
        GeometryFactory factory = new GeometryFactory();
        Point point = factory.createPoint(coordinate);
        point.setSRID(4326);
        entity.setLocation(point);

        entity.setUpdated(DateUtil.nowUTC());
        entity.setUpdatedBy(username);

        if (ic.getMovementSourceType() != null) {
            entity.setMovementSource(MovementSourceType.fromValue(ic.getMovementSourceType()));
        } else {
            entity.setMovementSource(MovementSourceType.OTHER);
        }

        entity.setMovementType(MovementTypeType.fromValue(ic.getMovementType()));

        if (ic.getPositionTime() != null) {
            entity.setTimestamp(ic.getPositionTime());
        } else {
            entity.setTimestamp(DateUtil.nowUTC());
        }

        Activity activity = createActivity(ic);
        entity.setActivity(activity);


        return entity;
    }


    private static Activity createActivity(IncomingMovement ic) {
        if (ic.getActivityMessageType() == null) {
            return null;
        }
        Activity activity = new Activity();
        activity.setActivityType(MovementActivityTypeType.fromValue(ic.getActivityMessageType()));
        activity.setCallback(ic.getActivityCallback());
        activity.setMessageId(ic.getActivityMessageId());
        activity.setUpdated(DateUtil.nowUTC());
        activity.setUpdatedBy("UVMS");
        return activity;
    }


    public static MovementDetails mapMovementDetails(IncomingMovement im, Movement movement, AssetMTEnrichmentResponse response) {
        MovementDetails md = new MovementDetails();
        if (movement.getActivity() != null) {
            md.setActivityCallback(movement.getActivity().getCallback());
            md.setActivityMessageId(movement.getActivity().getMessageId());
            md.setActivityMessageType(movement.getActivity().getActivityType().value());
        }
        md.setMovementGuid(movement.getId().toString());
        md.setLongitude(movement.getLocation().getX());
        md.setLatitude(movement.getLocation().getY());
        md.setMovementType(movement.getMovementType().value());
        if(movement.getFromSegment() != null) {
            md.setCalculatedCourse((double)movement.getFromSegment().getCourseOverGround());
            md.setCalculatedSpeed((double)movement.getFromSegment().getSpeedOverGround());
            md.setSegmentType(movement.getFromSegment().getSegmentCategory().value());
        }
        md.setReportedCourse(movement.getHeading() != null ? (double)movement.getHeading() : null);
        md.setReportedSpeed(movement.getSpeed() != null ? (double)movement.getSpeed() : null);
        md.setPositionTime(movement.getTimestamp());
        md.setStatusCode(movement.getStatus());
        md.setTripNumber(movement.getTripNumber());
        md.setWkt("");
        md.setInternalReferenceNumber(movement.getInternalReferenceNumber());

        // Asset
        md.setAssetGuid(response.getAssetUUID());
        md.setAssetIdGearType(response.getGearType());
        md.setExternalMarking(response.getExternalMarking());
        md.setFlagState(response.getFlagstate());
        md.setCfr(response.getCfr());
        md.setIrcs(response.getIrcs());
        md.setAssetName(response.getAssetName());
        md.setAssetStatus(response.getAssetStatus());
        md.setMmsi(response.getMmsi());
        md.setAssetGroups(response.getAssetGroupList());
        md.setConnectId(response.getAssetUUID());
        // TODO: missing
        //md.setAssetType();

        // MobileTerminal
        md.setChannelGuid(response.getChannelGuid());
        md.setMobileTerminalGuid(response.getMobileTerminalGuid());
        md.setComChannelType(im.getComChannelType());
        md.setMobileTerminalType(response.getMobileTerminalType());
        md.setMobileTerminalDnid(response.getDNID());
        md.setMobileTerminalMemberNumber(response.getMemberNumber());
        md.setMobileTerminalSerialNumber(response.getSerialNumber());
        //TODO: missing
        //md.setMobileTerminalStatus();
        md.setSource(movement.getMovementSource().value());

        if (movement.getFromSegment() != null && movement.getFromSegment().getFromMovement() != null) {
            md.setPreviousLatitude(movement.getFromSegment().getFromMovement().getLocation().getY());
            md.setPreviousLongitude(movement.getFromSegment().getFromMovement().getLocation().getX());
        }
        
        /*
    private List<String> vicinityOf;
         */

        return md;
    }
}
