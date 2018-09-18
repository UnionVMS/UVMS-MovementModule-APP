package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.ejb.EJB;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.v1.MovementComChannelType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementDomainException;

/**
 * Created by thofan on 2017-02-23.
 */

@RunWith(Arquillian.class)
public class MovementBatchModelBeanIntTest extends TransactionalTests {

    private Random rnd = new Random();

    private final static String TEST_USER_NAME = "Arquillian";

    @EJB
    private MovementBatchModelBean movementBatchModelBean;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    /******************************************************************************************************************
     *   TEST FUNCTIONS
     ******************************************************************************************************************/

    @Test
    public void getMovementConnect() {

        // Note getMovementConnectByConnectId CREATES one if it does not exists  (probably to force a batchimport to succeed)
        String randomUUID = UUID.randomUUID().toString();
        MovementConnect fetchedMovementConnect = movementBatchModelBean.getMovementConnectByConnectId(randomUUID);
        assertTrue(fetchedMovementConnect != null);
        assertTrue(fetchedMovementConnect.getValue().equals(randomUUID));
    }


    @Test
    public void getMovementConnect_ZEROISH_GUID() {

        String guid = "100000-0000-0000-0000-000000000000";
        // Note getMovementConnectByConnectId CREATES one if it does not exists  (probably to force a batchimport to succeed)
        MovementConnect fetchedMovementConnect = movementBatchModelBean.getMovementConnectByConnectId(guid);
        assertTrue(fetchedMovementConnect != null);
        assertTrue(fetchedMovementConnect.getValue().equals(guid));
    }

    @Test
    public void getMovementConnect_NULL_GUID() {
        assertNull(movementBatchModelBean.getMovementConnectByConnectId(null));
    }

    @Test
    public void createMovement() throws MovementDomainException {

        Instant now = DateUtil.nowUTC();
        double longitude = rnd.nextDouble();
        double latitude = rnd.nextDouble();

        String randomUUID = UUID.randomUUID().toString();
        MovementType movementType = createMovementTypeHelper(now, longitude, latitude);
        movementType.setConnectId(randomUUID);

        movementBatchModelBean.createMovement(movementType, TEST_USER_NAME);
        movementBatchModelBean.flush();
    }

    @Test
    public void enrichAreasSameArea() {

        // this test is migrated from the old testsuite
        Instant now = DateUtil.nowUTC();
        double longitude = rnd.nextDouble();
        double latitude = rnd.nextDouble();

        MovementType movementType = createMovementTypeHelper(now, longitude, latitude);

        MovementMetaData metaData = getMappedMovementHelper(2);
        movementType.setMetaData(metaData);

        List<Areatransition> areaTransitionList = new ArrayList<>();
        areaTransitionList.add(getAreaTransition("AREA1", MovementTypeType.ENT));
        movementBatchModelBean.enrichAreas(movementType, areaTransitionList);
        assertEquals(" AreaSize should be 2", 2, movementType.getMetaData().getAreas().size());
    }

    @Test
    public void enrichAreasNotSameArea() {

        // this test is migrated from the old testsuite
        Instant now = DateUtil.nowUTC();
        double longitude = rnd.nextDouble();
        double latitude = rnd.nextDouble();

        MovementType movementType = createMovementTypeHelper(now, longitude, latitude);

        MovementMetaData metaData = getMappedMovementHelper(2);
        movementType.setMetaData(metaData);

        List<Areatransition> areaTransitionList = new ArrayList<>();
        areaTransitionList.add(getAreaTransition("AREA3", MovementTypeType.ENT));
        movementBatchModelBean.enrichAreas(movementType, areaTransitionList);
        assertEquals(" AreaSize should be 3", 3, movementType.getMetaData().getAreas().size());
    }


    @Test
    public void mapToMovementMetaDataAreaType() {

        // TODO  maybe like this ?
        Areatransition areaTransition = getAreaTransition("AREA51", MovementTypeType.ENT);
        areaTransition.setMovementType(MovementTypeType.MAN);
        areaTransition.setAreatranAreaId(areaTransition.getAreatranAreaId());
        MovementMetaDataAreaType movementMetaDataAreaType = movementBatchModelBean.MapToMovementMetaDataAreaType(areaTransition);
        assertNotNull(movementMetaDataAreaType);
    }

    @Test
    public void getAreaType() throws MovementDomainException {

        Areatransition areaTransition = getAreaTransition("AREA51", MovementTypeType.ENT);
        areaTransition.setMovementType(MovementTypeType.MAN);
        areaTransition.setAreatranAreaId(areaTransition.getAreatranAreaId());
        MovementMetaDataAreaType movementMetaDataAreaType = movementBatchModelBean.MapToMovementMetaDataAreaType(areaTransition);
        AreaType areaType = movementBatchModelBean.getAreaType(movementMetaDataAreaType);
        assertNotNull(areaType);
    }

    @Test
    public void getAreaType_AREATYPE_AS_NULL() throws MovementDomainException {

        expectedException.expect(Exception.class);

        Areatransition areaTransition = getAreaTransition("AREA51", MovementTypeType.ENT);
        areaTransition.setMovementType(MovementTypeType.MAN);
        areaTransition.setAreatranAreaId(areaTransition.getAreatranAreaId());
        MovementMetaDataAreaType movementMetaDataAreaType = movementBatchModelBean.MapToMovementMetaDataAreaType(areaTransition);
        assertNotNull(movementMetaDataAreaType);
        movementBatchModelBean.getAreaType(null);
    }


    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/

    private Areatransition getAreaTransition(String code, MovementTypeType transitionType) {
        Areatransition transition = new Areatransition();
        transition.setMovementType(transitionType);
        transition.setAreatranAreaId(getAreaHelper(code));
        return transition;
    }

    private Area getAreaHelper(String areaCode) {
        Area area = new Area();
        area.setAreaCode(areaCode);
        area.setAreaName(areaCode);
        area.setAreaType(getAraTypeHelper(areaCode));
        return area;
    }

    private AreaType getAraTypeHelper(String name) {
        AreaType areaType = new AreaType();
        areaType.setName(name);
        return areaType;
    }

    private MovementType createMovementTypeHelper(Instant timeStamp, double longitude, double latitude) {
        MovementType movementType = new MovementType();
        movementType.setPositionTime(Date.from(timeStamp));
        MovementPoint point = new MovementPoint();
        point.setLatitude(latitude);
        point.setLongitude(longitude);

        movementType.setPosition(point);
        movementType.setComChannelType(MovementComChannelType.MANUAL);
        //movementType.setInternalReferenceNumber( );
        movementType.setTripNumber(rnd.nextDouble());
        movementType.setMovementType(MovementTypeType.POS);

        return movementType;
    }

    public static MovementMetaData getMappedMovementHelper(int numberOfAreas) {
        MovementMetaData metaData = new MovementMetaData();
        for (int i = 0; i < numberOfAreas; i++) {
            metaData.getAreas().add(getMovementMetadataTypeHelper("AREA" + i));
        }
        return metaData;
    }

    public static MovementMetaDataAreaType getMovementMetadataTypeHelper(String areaCode) {
        MovementMetaDataAreaType area = new MovementMetaDataAreaType();
        area.setCode(areaCode);
        area.setName(areaCode);
        area.setAreaType(areaCode);
        return area;
    }
}