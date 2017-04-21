package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.schema.movement.v1.MovementComChannelType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.BuildMovementTestDeployment;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;

/**
 * Created by thofan on 2017-02-23.
 */

@RunWith(Arquillian.class)
public class MovementBatchModelBeanIntTest extends BuildMovementTestDeployment {

    Random rnd = new Random();

    private final static String TEST_USER_NAME = "Arquillian";

    @Inject
    UserTransaction userTransaction;

    @EJB
    private MovementBatchModelBean movementBatchModelBean;


    /******************************************************************************************************************
     *   SETUP FUNCTIONS
     ******************************************************************************************************************/

    @Before
    public void before() throws SystemException, NotSupportedException {
        userTransaction.begin();
    }

    @After
    public void after() throws SystemException {
            userTransaction.rollback();
    }

    /******************************************************************************************************************
     *   TEST FUNCTIONS
     ******************************************************************************************************************/

    @Test
    @OperateOnDeployment("normal")
    public void getMovementConnect() {

        try {
            // Note getMovementConnect CREATES one if it does not exists  (probably to force a batchimport to succeed)
            String randomUUID = UUID.randomUUID().toString();
            MovementConnect fetchedMovementConnect = movementBatchModelBean.getMovementConnect(randomUUID);
            Assert.assertTrue(fetchedMovementConnect != null);
            Assert.assertTrue(fetchedMovementConnect.getValue().equals(randomUUID));
        } catch (MovementModelException e) {
            Assert.fail(e.toString());
        }
    }


    @Test
    @OperateOnDeployment("normal")
    public void getMovementConnect_ZEROISH_GUID() {

        String guid = "100000-0000-0000-0000-000000000000";

        try {
            // Note getMovementConnect CREATES one if it does not exists  (probably to force a batchimport to succeed)
            MovementConnect fetchedMovementConnect = movementBatchModelBean.getMovementConnect(guid);
            Assert.assertTrue(fetchedMovementConnect != null);
            Assert.assertTrue(fetchedMovementConnect.getValue().equals(guid));
        } catch (MovementModelException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementConnect_NULL_GUID() {

        try {
            movementBatchModelBean.getMovementConnect(null);
            Assert.fail("Should not reach this point");
        } catch (MovementModelException e) {
            String msg = e.toString().toUpperCase();
            int i = msg.indexOf("MOVEMENT CONNECTID IS NULL");
            Assert.assertTrue(i >= 0);
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void createMovement() {

        // TODO  denna skapar en post TROTS att vi har automatisk rollback  både i MovementConnect OCH Movement
        try {
            Date now = DateUtil.nowUTC();
            double longitude = rnd.nextDouble();
            double latitude = rnd.nextDouble();

            String randomUUID = UUID.randomUUID().toString();
            MovementType movementType = createMovementTypeHelper(now, longitude, latitude);
            movementType.setConnectId(randomUUID);

            movementBatchModelBean.createMovement(movementType, TEST_USER_NAME);
            movementBatchModelBean.flush();
            Assert.assertTrue(true);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
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

        List<Areatransition> areatransitionList = new ArrayList<>();
        areatransitionList.add(getAreaTransition("AREA1", MovementTypeType.ENT));
        movementBatchModelBean.enrichAreas(movementType, areatransitionList);
        Assert.assertTrue(" AreaSize should be 2", movementType.getMetaData().getAreas().size() == 2);
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

        List<Areatransition> areatransitionList = new ArrayList<>();
        areatransitionList.add(getAreaTransition("AREA3", MovementTypeType.ENT));
        movementBatchModelBean.enrichAreas(movementType, areatransitionList);
        Assert.assertTrue(" AreaSize should be 3", movementType.getMetaData().getAreas().size() == 3);
    }


    @Test
    @OperateOnDeployment("normal")
    public void mapToMovementMetaDataAreaType() {

        // TODO  maybe like this ?

        try {

            Areatransition areaTransition = getAreaTransition("AREA51", MovementTypeType.ENT);
            areaTransition.setMovementType(MovementTypeType.MAN);
            areaTransition.setAreatranAreaId(areaTransition.getAreatranAreaId());
            MovementMetaDataAreaType movementMetaDataAreaType = movementBatchModelBean.MapToMovementMetaDataAreaType(areaTransition);
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.fail(toString());
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getAreaType() {

        try {
            Areatransition areaTransition = getAreaTransition("AREA51", MovementTypeType.ENT);
            areaTransition.setMovementType(MovementTypeType.MAN);
            areaTransition.setAreatranAreaId(areaTransition.getAreatranAreaId());
            MovementMetaDataAreaType movementMetaDataAreaType = movementBatchModelBean.MapToMovementMetaDataAreaType(areaTransition);
            AreaType areaType = movementBatchModelBean.getAreaType(movementMetaDataAreaType);
            Assert.assertTrue(areaType != null);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getAreaType_AREATYPE_AS_NULL() {

        // TODO crashes on NULL input
        // public AreaType getAreaType(MovementMetaDataAreaType type) throws MovementDaoException {


        try {
            Areatransition areaTransition = getAreaTransition("AREA51", MovementTypeType.ENT);
            areaTransition.setMovementType(MovementTypeType.MAN);
            areaTransition.setAreatranAreaId(areaTransition.getAreatranAreaId());
            MovementMetaDataAreaType movementMetaDataAreaType = movementBatchModelBean.MapToMovementMetaDataAreaType(areaTransition);
            AreaType areaType = movementBatchModelBean.getAreaType(null);
            Assert.assertTrue(areaType != null);
        } catch (NullPointerException e) {
            Assert.assertTrue(e != null);
        } catch (MovementDaoException e) {
            Assert.assertTrue(e != null);
        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }
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
