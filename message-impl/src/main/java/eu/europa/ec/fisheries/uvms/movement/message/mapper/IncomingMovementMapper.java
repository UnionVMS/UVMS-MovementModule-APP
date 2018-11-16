package eu.europa.ec.fisheries.uvms.movement.message.mapper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.v1.*;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetMTEnrichmentResponse;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.entity.*;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.MovementDetails;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public abstract class IncomingMovementMapper {


    public static Movement mapNewMovementEntity(IncomingMovement ic, String username) {
        Movement entity = new Movement();

        entity.setSpeed(ic.getReportedSpeed());
        entity.setHeading(ic.getReportedCourse());
        entity.setInternalReferenceNumber(ic.getInternalReferenceNumber());
        entity.setTripNumber(ic.getTripNumber());
        entity.setStatus(ic.getStatus());
        //entity.setAltitude(ic.getAltitude());
        //entity.setMoveAltitude(ic.getAltitude());
        MovementConnect movementConnect = new MovementConnect();
        movementConnect.setValue(UUID.fromString(ic.getAssetHistoryId()));
        movementConnect.setUpdated(Instant.now());
        movementConnect.setUpdatedBy(username);
        entity.setMovementConnect(movementConnect);

        Coordinate coordinate = new Coordinate(ic.getLongitude(), ic.getLatitude());
        GeometryFactory factory = new GeometryFactory();
        Point point = factory.createPoint(coordinate);
        point.setSRID(4326);
        entity.setLocation(point);
        entity.setAltitude(ic.getAltitude());

        entity.setUpdated(DateUtil.nowUTC());
        entity.setUpdatedBy(username);

        if (ic.getMovementSourceType() != null) {
            entity.setMovementSource(MovementSourceType.fromValue(ic.getMovementSourceType()));
        } else {
            entity.setMovementSource(MovementSourceType.INMARSAT_C);
        }

        if (ic.getMovementType() != null) {
            entity.setMovementType(MovementTypeType.fromValue(ic.getMovementType()));
        } else {
            entity.setMovementType(MovementTypeType.POS);
        }

        if (ic.getPositionTime() != null) {
            entity.setTimestamp(ic.getPositionTime());
        } else {
            entity.setTimestamp(DateUtil.nowUTC());
        }

        Activity activity = createActivity(ic);
        entity.setActivity(activity);

        entity.setProcessed(false);

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
        md.setMovementGuid(movement.getGuid().toString());
        md.setLongitude(movement.getLocation().getX());
        md.setLatitude(movement.getLocation().getY());
        md.setAltitude(movement.getAltitude());
        md.setMovementType(movement.getMovementType().value());
        if(movement.getFromSegment() != null) {
            md.setCalculatedCourse(movement.getFromSegment().getCourseOverGround());
            md.setCalculatedSpeed(movement.getFromSegment().getSpeedOverGround());
            md.setSegmentType(movement.getFromSegment().getSegmentCategory().value());
        }
        md.setReportedCourse(movement.getHeading());
        md.setReportedSpeed(movement.getSpeed());
        md.setPositionTime(Date.from(movement.getTimestamp()));
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
        md.setConnectId(response.getAssetHistoryId());
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

        /*
    private Double previousLatitude;
    private Double previousLongitude;
    private List<String> vicinityOf;
    private Integer sumPositionReport;
    private Long timeDiffPositionReport;
    private List<String> areaCodes;
    private List<String> areaTypes;
    private List<String> entAreaCodes;
    private List<String> entAreaTypes;
    private List<String> extAreaCodes;
    private List<String> extAreaTypes;
    private String closestCountryCode;
    private String closestPortCode;
    private Double closestPortDistance;
    private Double closestCountryDistance;
         */

        return md;
    }
}
