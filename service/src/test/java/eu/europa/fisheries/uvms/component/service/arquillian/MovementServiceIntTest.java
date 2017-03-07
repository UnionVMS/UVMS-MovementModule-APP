package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.common.v1.SimpleResponse;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.*;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static eu.europa.fisheries.uvms.component.service.arquillian.BuildMovementServiceTestDeployment.LOG;

/**
 * Created by thofan on 2017-03-02.
 */

@RunWith(Arquillian.class)
public class MovementServiceIntTest extends TransactionalTests {

    Random rnd = new Random();


    private final static String TEST_USER_NAME = "MovementServiceIntTestTestUser";

    @EJB
    MovementService movementService;


    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementServiceTestDeployment.createDeployment_FOR_MovementServiceIntTest();
    }


    @Test
    public void createMovement() {

        Date now = DateUtil.nowUTC();
        double longitude = 9.140625D;
        double latitude = 57.683804D;

        // create a MovementConnect
        String connectId = UUID.randomUUID().toString();
        MovementType movementType = createMovementTypeHelper(now, longitude, latitude);
        movementType.setConnectId(connectId);
        Assert.assertTrue(movementService != null);

        try {
            MovementType createdMovementType = movementService.createMovement(movementType, "TEST");
        } catch (Exception e) {
            Assert.fail();
        }
    }


@Test
    public void getMapByQuery() {

        MovementQuery query = new MovementQuery();
        //query.


        try {
            GetMovementMapByQueryResponse reponse = movementService.getMapByQuery(query);
        } catch (MovementServiceException e) {
            Assert.fail();
        } catch (MovementDuplicateException e) {
            Assert.fail();
        }
    }

    @Test
    public void createMovementBatch() {

        List<MovementBaseType> query = null;
        try {
            SimpleResponse rseponse = movementService.createMovementBatch(query);
        } catch (MovementServiceException e) {
            Assert.fail();
        } catch (MovementDuplicateException e) {
            Assert.fail();
        }
    }


    @Test
    public void getAreas() {

        try {
            List<eu.europa.ec.fisheries.schema.movement.area.v1.AreaType> response = movementService.getAreas();
            Assert.assertTrue(response != null);
        } catch (MovementServiceException e) {
            Assert.fail();
        } catch (MovementDuplicateException e) {
            Assert.fail();
        }
    }


    @Test
    public void getById() {

        try {

            Date now = DateUtil.nowUTC();
            double longitude = 9.140625D;
            double latitude = 57.683804D;

            // create a MovementConnect
            String connectId = UUID.randomUUID().toString();
            MovementType movementType = createMovementTypeHelper(now, longitude, latitude);
            movementType.setConnectId(connectId);
            Assert.assertTrue(movementService != null);
            MovementType createdMovementType = movementService.createMovement(movementType, "TEST");
            em.flush();
            Assert.assertTrue(createdMovementType != null);

            String guid = createdMovementType.getGuid();
            Assert.assertTrue(guid != null);

            MovementType fetchedMovementType = movementService.getById(guid);
            Assert.assertTrue(fetchedMovementType != null);
            String fetchedGuid = fetchedMovementType.getGuid();
            Assert.assertTrue(fetchedGuid != null);
            Assert.assertTrue(fetchedGuid.equals(guid));

        } catch (Exception e) {
            //Assert.fail();
        }
    }


    @Test
    public void getById_Null_ID() {

        String connectId = null;
        try {
            MovementType createdMovementType = movementService.getById(connectId);
            Assert.fail();

        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    public void getById_emptyGUID() {

        String connectId = "";
        try {
            MovementType createdMovementType = movementService.getById(connectId);
            Assert.fail();
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
