package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertThat;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.common.v1.SimpleResponse;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByAreaAndTimeIntervalResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementComChannelType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.message.producer.bean.MessageProducerBean;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.MovementEventTestHelper;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

@RunWith(Arquillian.class)
public class MovementServiceIntTest extends TransactionalTests {

    private Random rnd = new Random();
    private static int NumberOfMovements = 3;

    @EJB
    MovementService movementService;

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void getMovementListByAreaAndTimeInterval_EmptyCriteria() throws MovementServiceException {
        MovementAreaAndTimeIntervalCriteria criteria = new MovementAreaAndTimeIntervalCriteria();
        movementService.getMovementListByAreaAndTimeInterval(criteria);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementListByAreaAndTimeInterval_NoResult_But_RunsTheCode() {
        MovementAreaAndTimeIntervalCriteria criteria = new MovementAreaAndTimeIntervalCriteria();

        Instant curDate = DateUtil.nowUTC();
        String fmt = "yyyy-MM-dd HH:mm:ss Z";

       String formattedDate = DateUtil.parseDateToString(curDate, fmt);


        // areaCode
        criteria.setAreaCode("AREA0");
        // fromDate
        criteria.setFromDate(formattedDate);
        // toDate
        criteria.setToDate(formattedDate);

        GetMovementListByAreaAndTimeIntervalResponse list = movementService.getMovementListByAreaAndTimeInterval(criteria);
        Assert.assertTrue(list != null);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void getMovementListByAreaAndTimeIntervalTimeIntervalNoCode() throws MovementServiceException {
        MovementAreaAndTimeIntervalCriteria criteria = new MovementAreaAndTimeIntervalCriteria();

        Instant curDate = DateUtil.nowUTC();
        String fmt = "yyyy-MM-dd HH:mm:ss Z";

        String formattedDate = DateUtil.parseDateToString(curDate, fmt);

        //criteria.setAreaCode("AREA0");
        // fromDate
        criteria.setFromDate(formattedDate);
        // toDate
        criteria.setToDate(formattedDate);

        movementService.getMovementListByAreaAndTimeInterval(criteria);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createMovement() {

        Instant now = DateUtil.nowUTC();
        double longitude = 9.140625D;
        double latitude = 57.683804D;

        // create a MovementConnect
        String connectId = UUID.randomUUID().toString();
        Movement movementType = MockData.createMovement(longitude, latitude, connectId);
        try {
            Movement createdMovementType = movementService.createMovement(movementType, "Test");
            Assert.assertTrue(createdMovementType != null);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovementsByConnectIds_EmptyList() {

        List<String> connectionIds = new ArrayList<>();
        List<MovementDto> movements =  movementService.getLatestMovementsByConnectIds(connectionIds);
        assertThat(movements.size(), CoreMatchers.is(0));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getList() {

        MovementQuery query = createMovementQuery(true);
        try {
            movementService.getList(query);
            //Assert.assertTrue(getMovementListByQueryResponse != null); //changes to the error handling a few functions down means that the above call will throw an exception
            Assert.fail("The above call should throw an exception since query is incomplete");
        } catch (EJBTransactionRolledbackException e) {
            //Assert.fail();
        	Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMapByQuery_LATEST() {

        MovementQuery query = createMovementQuery(false);

        ListCriteria listCriteria = new ListCriteria();
        listCriteria.setKey(SearchKey.NR_OF_LATEST_REPORTS);
        listCriteria.setValue("3");

        query.getMovementSearchCriteria().add(listCriteria);
        try {
            GetMovementMapByQueryResponse response = movementService.getMapByQuery(query);
            Assert.assertTrue(response != null);
        } catch (MovementServiceException e) {
            Assert.fail();
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getMapByQuery_LATEST_with_pagination() {

        MovementQuery query = createMovementQuery(true);

        ListCriteria listCriteria = new ListCriteria();
        listCriteria.setKey(SearchKey.NR_OF_LATEST_REPORTS);
        listCriteria.setValue("3");

        query.getMovementSearchCriteria().add(listCriteria);
        try {
            movementService.getMapByQuery(query);
            Assert.fail();
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createBatch() throws MovementServiceException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");
        Double longitude = rnd.nextDouble();
        Double latitude = rnd.nextDouble();
        List<Movement> movementTypeList = new ArrayList<>();
        for(int i = 0 ; i < NumberOfMovements ; i++){
            movementTypeList.add(MockData.createMovement(longitude, latitude, UUID.randomUUID().toString()));
            longitude = longitude  + 0.05;
            latitude = latitude +  0.05;
        }

        SimpleResponse simpleResponse = movementService.createMovementBatch(movementTypeList, "TEST").getResponse();
        Assert.assertNotNull(simpleResponse);
        Assert.assertEquals(SimpleResponse.OK, simpleResponse);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void triggerBatchEventWithBrokenJMS() throws MovementServiceException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "true");
        Double longitude = rnd.nextDouble();
        Double latitude = rnd.nextDouble();
        List<Movement> movementTypeList = new ArrayList<>();
        for(int i = 0 ; i < NumberOfMovements ; i++){
            movementTypeList.add(MockData.createMovement(longitude, latitude, UUID.randomUUID().toString()));
            longitude += 0.05;
            latitude += 0.05;
        }
        try {
            movementService.createMovementBatch(movementTypeList, "TEST").getResponse();
            Assert.fail("This should produce an EJBException and trigger rollback");
        } catch (EJBException ignore) {}
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getAreas() {
        List<eu.europa.ec.fisheries.schema.movement.area.v1.AreaType> response = movementService.getAreas();
        Assert.assertTrue(response != null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getById() {

        try {
            Instant now = DateUtil.nowUTC();
            double longitude = 9.140625D;
            double latitude = 57.683804D;

            // create a MovementConnect
            String connectId = UUID.randomUUID().toString();
            Movement movementType = MockData.createMovement(longitude, latitude, connectId);
            Assert.assertTrue(movementService != null);
            Movement createdMovementType = movementService.createMovement(movementType, "TEST");
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
            // TODO  check this it is suspect
            //Assert.fail();
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getById_Null_ID() {

        String connectId = null;
        try {
            movementService.getById(connectId);
            Assert.fail();

        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovements() {
        List<MovementDto> listMovementDto = movementService.getLatestMovements(5);
        Assert.assertTrue(listMovementDto != null);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void getLatestMovements_NumberNULL() {
        movementService.getLatestMovements(null);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovements_NegativeNumber() {
        try {
            movementService.getLatestMovements(-3);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getById_emptyGUID() {
        String connectId = "";
        try {
            movementService.getById(connectId);
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
    
    private MovementType createMovementTypeHelper(Date timeStamp, double longitude, double latitude, double tripNumber) {
        MovementType movementType = new MovementType();
        movementType.setPositionTime(timeStamp);
        MovementPoint point = new MovementPoint();
        point.setLatitude(latitude);
        point.setLongitude(longitude);

        movementType.setPosition(point);
        movementType.setComChannelType(MovementComChannelType.MANUAL);
        //movementType.setInternalReferenceNumber( );
        movementType.setTripNumber(tripNumber);
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

    private List<Movement> createBaseTypeList() {
        List<Movement> query = new ArrayList<>();
        String connectId = UUID.randomUUID().toString();
        for (int i = 0; i < 5; i++) {
            query.add(createMovementBaseType(i, connectId));
        }
        return query;
    }

    private Movement createMovementBaseType(Integer i, String connectId) {
        Movement movement = new Movement();
        MovementConnect movementConnect = new MovementConnect();
        movementConnect.setValue(connectId);
        movement.setMovementConnect(movementConnect);
        movement.setMovementSource(MovementSourceType.AIS);
        movement.setMovementType(MovementTypeType.MAN);
        movement.setLocation(position());
        return movement;
    }

    private Point position() {
        Double longitude = rnd.nextDouble();
        Double latitude = rnd.nextDouble();
        Coordinate coordinate = new Coordinate(longitude, latitude);
        GeometryFactory factory = new GeometryFactory();
        Point point = factory.createPoint(coordinate);
        point.setSRID(4326);
        return point;
    }

    private MovementQuery createMovementQuery(boolean usePagination) {

        MovementQuery query = new MovementQuery();
        if (usePagination) {
            BigInteger listSize = BigInteger.valueOf(100L);
            BigInteger page = BigInteger.valueOf(1L);
            ListPagination listPagination = new ListPagination();
            listPagination.setListSize(listSize);
            listPagination.setPage(page);
            query.setPagination(listPagination);
        }
        return query;
    }
}
