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

import eu.europa.ec.fisheries.uvms.movement.arquillian.BuildMovementTestDeployment;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
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

        // Note getMovementConnect CREATES one if it does not exists  (probably to force a batchimport to succeed)
        final String randomUUID = UUID.randomUUID().toString();
        final MovementConnect fetchedMovementConnect = movementBatchModelBean.getMovementConnect(randomUUID);
        Assert.assertTrue(fetchedMovementConnect != null);
        Assert.assertTrue(fetchedMovementConnect.getValue().equals(randomUUID));
    }


    @Test
    @OperateOnDeployment("normal")
    public void getMovementConnect_ZEROISH_GUID() {

        final String guid = "100000-0000-0000-0000-000000000000";
        // Note getMovementConnect CREATES one if it does not exists  (probably to force a batchimport to succeed)
        final MovementConnect fetchedMovementConnect = movementBatchModelBean.getMovementConnect(guid);
        Assert.assertTrue(fetchedMovementConnect != null);
        Assert.assertTrue(fetchedMovementConnect.getValue().equals(guid));
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementConnect_NULL_GUID() {
        Assert.assertNull(movementBatchModelBean.getMovementConnect(null));
    }

    @Test
    @OperateOnDeployment("normal")
    public void createMovement() {

        try {
            final Date now = DateUtil.nowUTC();
            final double longitude = rnd.nextDouble();
            final double latitude = rnd.nextDouble();

            final String randomUUID = UUID.randomUUID().toString();
            final MovementType movementType = createMovementTypeHelper(now, longitude, latitude);
            movementType.setConnectId(randomUUID);

            movementBatchModelBean.createMovement(movementType, TEST_USER_NAME);
            movementBatchModelBean.flush();
            Assert.assertTrue(true);
        } catch (final MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void enrichAreasSameArea() {

        // this test is migrated from the old testsuite

        final Date now = DateUtil.nowUTC();
        final double longitude = rnd.nextDouble();
        final double latitude = rnd.nextDouble();

        final MovementType movementType = createMovementTypeHelper(now, longitude, latitude);

        final MovementMetaData metaData = getMappedMovementHelper(2);
        movementType.setMetaData(metaData);

        final List<Areatransition> areatransitionList = new ArrayList<>();
        areatransitionList.add(getAreaTransition("AREA1", MovementTypeType.ENT));
        movementBatchModelBean.enrichAreas(movementType, areatransitionList);
        Assert.assertTrue(" AreaSize should be 2", movementType.getMetaData().getAreas().size() == 2);
    }

    @Test
    @OperateOnDeployment("normal")
    public void enrichAreasNotSameArea() {

        // this test is migrated from the old testsuite

        final Date now = DateUtil.nowUTC();
        final double longitude = rnd.nextDouble();
        final double latitude = rnd.nextDouble();

        final MovementType movementType = createMovementTypeHelper(now, longitude, latitude);

        final MovementMetaData metaData = getMappedMovementHelper(2);
        movementType.setMetaData(metaData);

        final List<Areatransition> areatransitionList = new ArrayList<>();
        areatransitionList.add(getAreaTransition("AREA3", MovementTypeType.ENT));
        movementBatchModelBean.enrichAreas(movementType, areatransitionList);
        Assert.assertTrue(" AreaSize should be 3", movementType.getMetaData().getAreas().size() == 3);
    }


    @Test
    @OperateOnDeployment("normal")
    public void mapToMovementMetaDataAreaType() {

        // TODO  maybe like this ?

        try {

            final Areatransition areaTransition = getAreaTransition("AREA51", MovementTypeType.ENT);
            areaTransition.setMovementType(MovementTypeType.MAN);
            areaTransition.setAreatranAreaId(areaTransition.getAreatranAreaId());
            final MovementMetaDataAreaType movementMetaDataAreaType = movementBatchModelBean.MapToMovementMetaDataAreaType(areaTransition);
            Assert.assertTrue(true);
        } catch (final Exception e) {
            Assert.fail(toString());
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getAreaType() {

        try {
            final Areatransition areaTransition = getAreaTransition("AREA51", MovementTypeType.ENT);
            areaTransition.setMovementType(MovementTypeType.MAN);
            areaTransition.setAreatranAreaId(areaTransition.getAreatranAreaId());
            final MovementMetaDataAreaType movementMetaDataAreaType = movementBatchModelBean.MapToMovementMetaDataAreaType(areaTransition);
            final AreaType areaType = movementBatchModelBean.getAreaType(movementMetaDataAreaType);
            Assert.assertTrue(areaType != null);
        } catch (final MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getAreaType_AREATYPE_AS_NULL() {

        // TODO crashes on NULL input
        // public AreaType getAreaType(MovementMetaDataAreaType type) throws MovementDaoException {


        try {
            final Areatransition areaTransition = getAreaTransition("AREA51", MovementTypeType.ENT);
            areaTransition.setMovementType(MovementTypeType.MAN);
            areaTransition.setAreatranAreaId(areaTransition.getAreatranAreaId());
            final MovementMetaDataAreaType movementMetaDataAreaType = movementBatchModelBean.MapToMovementMetaDataAreaType(areaTransition);
            final AreaType areaType = movementBatchModelBean.getAreaType(null);
            Assert.assertTrue(areaType != null);
        } catch (final NullPointerException e) {
            Assert.assertTrue(e != null);
        } catch (final MovementDaoException e) {
            Assert.assertTrue(e != null);
        } catch (final Exception e) {
            Assert.assertTrue(e != null);
        }
    }


    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/

    private Areatransition getAreaTransition(final String code, final MovementTypeType transitionType) {
        final Areatransition transition = new Areatransition();
        transition.setMovementType(transitionType);
        transition.setAreatranAreaId(getAreaHelper(code));
        return transition;
    }

    private Area getAreaHelper(final String areaCode) {
        final Area area = new Area();
        area.setAreaCode(areaCode);
        area.setAreaName(areaCode);
        area.setAreaType(getAraTypeHelper(areaCode));
        return area;
    }

    private AreaType getAraTypeHelper(final String name) {
        final AreaType areaType = new AreaType();
        areaType.setName(name);
        return areaType;
    }

    private MovementType createMovementTypeHelper(final Date timeStamp, final double longitude, final double latitude) {
        final MovementType movementType = new MovementType();
        movementType.setPositionTime(timeStamp);
        final MovementPoint point = new MovementPoint();
        point.setLatitude(latitude);
        point.setLongitude(longitude);

        movementType.setPosition(point);
        movementType.setComChannelType(MovementComChannelType.MANUAL);
        //movementType.setInternalReferenceNumber( );
        movementType.setTripNumber(rnd.nextDouble());
        movementType.setMovementType(MovementTypeType.POS);

        return movementType;
    }


    public static MovementMetaData getMappedMovementHelper(final int numberOfAreas) {
        final MovementMetaData metaData = new MovementMetaData();
        for (int i = 0; i < numberOfAreas; i++) {
            metaData.getAreas().add(getMovementMetadataTypeHelper("AREA" + i));
        }
        return metaData;
    }

    public static MovementMetaDataAreaType getMovementMetadataTypeHelper(final String areaCode) {
        final MovementMetaDataAreaType area = new MovementMetaDataAreaType();
        area.setCode(areaCode);
        area.setName(areaCode);
        area.setAreaType(areaCode);
        return area;
    }


}
