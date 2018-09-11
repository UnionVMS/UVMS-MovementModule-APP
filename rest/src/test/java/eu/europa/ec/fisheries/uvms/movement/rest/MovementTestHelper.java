package eu.europa.ec.fisheries.uvms.movement.rest;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetType;
import eu.europa.ec.fisheries.schema.movement.asset.v1.VesselType;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementStateEnum;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;

public class MovementTestHelper {

    public static MovementBaseType createMovementBaseType() {
        return MovementTestHelper.createMovementBaseType(56d,11d);
    }

    public static MovementBaseType createMovementBaseType(Double longitude , Double latitude) {

        MovementBaseType movementBaseType = new MovementBaseType();
        movementBaseType.setMovementType(MovementTypeType.POS);
        movementBaseType.setConnectId(UUID.randomUUID().toString());

        AssetId assetId = new AssetId();
        assetId.setAssetType(AssetType.VESSEL);
        assetId.setIdType(AssetIdType.GUID);
        assetId.setValue(UUID.randomUUID().toString());
        movementBaseType.setAssetId(assetId);

        MovementPoint movementPoint = new MovementPoint();
        movementPoint.setLongitude(longitude);
        movementPoint.setLatitude(latitude);
        movementPoint.setAltitude(2D);
        movementBaseType.setPosition(movementPoint);

        MovementActivityType activityType = new MovementActivityType();
        activityType.setCallback("TEST");
        activityType.setMessageId("TEST");
        activityType.setMessageType(MovementActivityTypeType.AUT);

        movementBaseType.setActivity(activityType);

        movementBaseType.setDuplicates("false");
        movementBaseType.setInternalReferenceNumber("TEST");
        movementBaseType.setReportedCourse(0d);
        movementBaseType.setReportedSpeed(0d);
        movementBaseType.setSource(MovementSourceType.NAF);
        movementBaseType.setStatus("TEST");
        movementBaseType.setPositionTime(Calendar.getInstance().getTime());
        movementBaseType.setTripNumber(0d);

        return movementBaseType;
    }
    
    public static MovementSearchGroup createBasicMovementSearchGroup() {
        MovementSearchGroup movementSearchGroup = new MovementSearchGroup();
        movementSearchGroup.setName("Test Group " + getRandomIntegers(5));
        movementSearchGroup.setUser("TEST");
        return movementSearchGroup;
    }

    public static MovementQuery createMovementQuery() {
        MovementQuery movementQuery = new MovementQuery();
        movementQuery.setExcludeFirstAndLastSegment(true);

        ListPagination listPagination = new ListPagination();
        listPagination.setPage(BigInteger.ONE);
        listPagination.setListSize(BigInteger.valueOf(1000));
        movementQuery.setPagination(listPagination);
        
        return movementQuery;
    }
    
    public static TempMovementType createTempMovementType() {
        TempMovementType tempMovement = new TempMovementType();
        VesselType asset = new VesselType();
        asset.setCfr("CFR" + getRandomIntegers(7));
        asset.setExtMarking("EXT" + getRandomIntegers(2));
        asset.setIrcs("IRCS" + getRandomIntegers(5));
        asset.setFlagState("SWE");
        asset.setName("Ship" + getRandomIntegers(8));
        tempMovement.setAsset(asset);
        MovementPoint movementPoint = new MovementPoint();
        movementPoint.setLongitude(1d);
        movementPoint.setLatitude(2d);
        tempMovement.setPosition(movementPoint);
        tempMovement.setCourse(45d);
        tempMovement.setState(TempMovementStateEnum.DRAFT);
        return tempMovement;
    }
    
    public static String getRandomIntegers(int length) {
        return new Random()
                .ints(0,9)
                .mapToObj(i -> String.valueOf(i))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
