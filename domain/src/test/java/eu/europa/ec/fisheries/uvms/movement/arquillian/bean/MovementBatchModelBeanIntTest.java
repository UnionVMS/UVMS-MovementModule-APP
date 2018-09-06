package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import eu.europa.ec.fisheries.schema.movement.v1.*;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MissingMovementConnectException;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by thofan on 2017-02-23.
 */

@RunWith(Arquillian.class)
public class MovementBatchModelBeanIntTest extends TransactionalTests {

    private Random rnd = new Random();

    private final static String TEST_USER_NAME = "Arquillian";

    @Inject
    private UserTransaction userTransaction;

    @EJB
    private MovementBatchModelBean movementBatchModelBean;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    /******************************************************************************************************************
     *   TEST FUNCTIONS
     ******************************************************************************************************************/

    @Test
    @OperateOnDeployment("normal")
    public void getMovementConnect() {

        // Note getMovementConnect CREATES one if it does not exists  (probably to force a batchimport to succeed)
        String randomUUID = UUID.randomUUID().toString();
        MovementConnect fetchedMovementConnect = movementBatchModelBean.getMovementConnect(randomUUID);
        assertTrue(fetchedMovementConnect != null);
        assertTrue(fetchedMovementConnect.getValue().equals(randomUUID));
    }


    @Test
    @OperateOnDeployment("normal")
    public void getMovementConnect_ZEROISH_GUID() {

        String guid = "100000-0000-0000-0000-000000000000";
        // Note getMovementConnect CREATES one if it does not exists  (probably to force a batchimport to succeed)
        MovementConnect fetchedMovementConnect = movementBatchModelBean.getMovementConnect(guid);
        assertTrue(fetchedMovementConnect != null);
        assertTrue(fetchedMovementConnect.getValue().equals(guid));
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementConnect_NULL_GUID() {
        assertNull(movementBatchModelBean.getMovementConnect(null));
    }

    @Test
    @OperateOnDeployment("normal")
    public void createMovement() throws MovementDaoException, MissingMovementConnectException {

        Date now = DateUtil.nowUTC();
        double longitude = rnd.nextDouble();
        double latitude = rnd.nextDouble();

        String randomUUID = UUID.randomUUID().toString();
        MovementType movementType = createMovementTypeHelper(now, longitude, latitude);
        movementType.setConnectId(randomUUID);

        movementBatchModelBean.createMovement(movementType, TEST_USER_NAME);
        movementBatchModelBean.flush();
    }

    @Test
    @OperateOnDeployment("normal")
    public void enrichAreasSameArea() {

        // this test is migrated from the old testsuite
        Date now = DateUtil.nowUTC();
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
    @OperateOnDeployment("normal")
    public void enrichAreasNotSameArea() {

        // this test is migrated from the old testsuite
        Date now = DateUtil.nowUTC();
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
    @OperateOnDeployment("normal")
    public void mapToMovementMetaDataAreaType() {

        // TODO  maybe like this ?
        Areatransition areaTransition = getAreaTransition("AREA51", MovementTypeType.ENT);
        areaTransition.setMovementType(MovementTypeType.MAN);
        areaTransition.setAreatranAreaId(areaTransition.getAreatranAreaId());
        MovementMetaDataAreaType movementMetaDataAreaType = movementBatchModelBean.MapToMovementMetaDataAreaType(areaTransition);
        assertNotNull(movementMetaDataAreaType);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getAreaType() throws MovementDaoException {

        Areatransition areaTransition = getAreaTransition("AREA51", MovementTypeType.ENT);
        areaTransition.setMovementType(MovementTypeType.MAN);
        areaTransition.setAreatranAreaId(areaTransition.getAreatranAreaId());
        MovementMetaDataAreaType movementMetaDataAreaType = movementBatchModelBean.MapToMovementMetaDataAreaType(areaTransition);
        AreaType areaType = movementBatchModelBean.getAreaType(movementMetaDataAreaType);
        assertNotNull(areaType);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getAreaType_AREATYPE_AS_NULL() throws MovementDaoException {

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

    private MovementType createMovementTypeHelper(Date timeStamp, double longitude, double latitude) {
        MovementType movementType = new MovementType();
        movementType.setPositionTime(timeStamp);
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
