package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.movement.common.v1.SimpleResponse;
import eu.europa.ec.fisheries.schema.movement.search.v1.*;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByAreaAndTimeIntervalResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.*;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementComChannelType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.message.producer.bean.MessageProducerBean;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.jms.JMSException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by thofan on 2017-03-02.
 */

@RunWith(Arquillian.class)
public class MovementServiceIntTest extends TransactionalTests {

    Random rnd = new Random();
    private static int NumberOfMovements = 3;

    private final static String TEST_USER_NAME = "MovementServiceIntTestTestUser";

    @EJB
    MovementService movementService;



    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementListByAreaAndTimeInterval_EmptyCriteria() {
        MovementAreaAndTimeIntervalCriteria criteria = new MovementAreaAndTimeIntervalCriteria();
        try {
            GetMovementListByAreaAndTimeIntervalResponse list = movementService.getMovementListByAreaAndTimeInterval(criteria);
            Assert.assertTrue(list != null);
        } catch (MovementServiceException e) {
            Assert.fail();
        } catch (MovementDuplicateException e) {
            Assert.fail();
        }
    }


    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementListByAreaAndTimeInterval_NoResult_But_RunsTheCode() {
        MovementAreaAndTimeIntervalCriteria criteria = new MovementAreaAndTimeIntervalCriteria();

        Date curDate = DateUtil.nowUTC();
        String fmt = "yyyy-MM-dd HH:mm:ss Z";

        SimpleDateFormat format = new SimpleDateFormat(fmt);
       String formattedDate = format.format(curDate);


        // areaCode
        criteria.setAreaCode("AREA0");
        // fromDate
        criteria.setFromDate(formattedDate);
        // toDate
        criteria.setToDate(formattedDate);

        try {
            GetMovementListByAreaAndTimeIntervalResponse list = movementService.getMovementListByAreaAndTimeInterval(criteria);
            Assert.assertTrue(list != null);
        } catch (MovementServiceException e) {
            Assert.fail();
        } catch (MovementDuplicateException e) {
            Assert.fail();
        }
    }


    @Test
    @OperateOnDeployment("movementservice")
    public void getMovementListByAreaAndTimeInterval_NoArea_ButDateAdded_NoResult_But_RunsTheCode() {
        MovementAreaAndTimeIntervalCriteria criteria = new MovementAreaAndTimeIntervalCriteria();

        Date curDate = DateUtil.nowUTC();
        String fmt = "yyyy-MM-dd HH:mm:ss Z";

        SimpleDateFormat format = new SimpleDateFormat(fmt);
        String formattedDate = format.format(curDate);


        // NO areaCode  shpuld make the dates NOT be used
        //criteria.setAreaCode("AREA0");
        // fromDate
        criteria.setFromDate(formattedDate);
        // toDate
        criteria.setToDate(formattedDate);

        try {
            GetMovementListByAreaAndTimeIntervalResponse list = movementService.getMovementListByAreaAndTimeInterval(criteria);
            Assert.assertTrue(list != null);
        } catch (MovementServiceException e) {
            Assert.fail();
        } catch (MovementDuplicateException e) {
            Assert.fail();
        }
    }



    @Test
    @OperateOnDeployment("movementservice")
    public void createMovement() {

        Date now = DateUtil.nowUTC();
        double longitude = 9.140625D;
        double latitude = 57.683804D;

        // create a MovementConnect
        String connectId = UUID.randomUUID().toString();
        MovementType movementType = createMovementTypeHelper(now, longitude, latitude);
        movementType.setConnectId(connectId);
        try {
            MovementType createdMovementType = movementService.createMovement(movementType, "TEST");
            Assert.assertTrue(createdMovementType != null);
        } catch (Exception e) {
            Assert.fail();
        }
    }


    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovementsByConnectIds_EmptyList() {

        List<String> connectionIds = new ArrayList<>();
        try {
            List<MovementDto> movements =  movementService.getLatestMovementsByConnectIds(connectionIds);
            Assert.fail();
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        } catch (MovementDuplicateException e) {
            Assert.assertTrue(e != null);
        }
    }


    @Test
    @OperateOnDeployment("movementservice")
    public void getList() {

        MovementQuery query = createMovementQuery(true);
        try {
            GetMovementListByQueryResponse getMovementListByQueryResponse = movementService.getList(query);
            //Assert.assertTrue(getMovementListByQueryResponse != null); //changes to the error handling a few functions down means that the above call will throw an exception
            Assert.fail("The above call should throw an exception since query is incomplete");
        } catch (MovementServiceException e) {
            //Assert.fail();
        	Assert.assertTrue(true);
        } catch (MovementDuplicateException e) {
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
        } catch (MovementDuplicateException e) {
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
            GetMovementMapByQueryResponse response = movementService.getMapByQuery(query);
            Assert.fail();
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        } catch (MovementDuplicateException e) {
            Assert.assertTrue(e != null);
        }
    }


    @Test
    @OperateOnDeployment("movementservice")
    public void createMovementBatch() {

        List<MovementBaseType> query = createBaseTypeList();
        SimpleResponse response = movementService.createMovementBatch(query);
        Assert.assertTrue(response != null);
        Assert.assertTrue(response == SimpleResponse.OK);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createBatch() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");
        Double longitude = rnd.nextDouble();
        Double latitude = rnd.nextDouble();
        List<MovementBaseType> movementTypeList = new ArrayList<>();
        for(int i = 0 ; i < NumberOfMovements ; i++){
            movementTypeList.add(MovementEventTestHelper.createMovementBaseType(longitude, latitude));
            longitude = longitude  + 0.05;
            latitude = latitude +  0.05;
        }

        SimpleResponse simpleResponse = movementService.createMovementBatch(movementTypeList);
        Assert.assertNotNull(simpleResponse);
        Assert.assertEquals(SimpleResponse.OK, simpleResponse);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void triggerBatchEventWithBrokenJMS() throws JMSException, ModelMarshallException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "true");
        Double longitude = rnd.nextDouble();
        Double latitude = rnd.nextDouble();
        List<MovementBaseType> movementTypeList = new ArrayList<>();
        for(int i = 0 ; i < NumberOfMovements ; i++){
            movementTypeList.add(MovementEventTestHelper.createMovementBaseType(longitude, latitude));
            longitude += 0.05;
            latitude += 0.05;
        }
        try {
            movementService.createMovementBatch(movementTypeList);
            Assert.fail("This should produce an EJBException and trigger rollback");
        } catch (EJBException ignore) {}
    }



    @Test
    @OperateOnDeployment("movementservice")
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
    @OperateOnDeployment("movementservice")
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
            // TODO  check this it is suspect
            //Assert.fail();
        }
    }


    @Test
    @OperateOnDeployment("movementservice")
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
    @OperateOnDeployment("movementservice")
    public void getLatestMovements() {

        try {
            List<MovementDto> listMovementDto = movementService.getLatestMovements(5);
            Assert.assertTrue(listMovementDto != null);
        } catch (MovementDuplicateException e) {
            Assert.fail();
        } catch (MovementServiceException e) {
            Assert.fail();
        }

    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovements_NumberNULL() {

        try {
            List<MovementDto> listMovementDto = movementService.getLatestMovements(null);
            Assert.fail();
        } catch  (MovementDuplicateException e) {
            Assert.assertTrue(e != null);
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        } catch (Throwable e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovements_NegativeNumber() {

        try {
            List<MovementDto> listMovementDto = movementService.getLatestMovements(-3);
            Assert.fail();
        } catch  (MovementDuplicateException e) {
            Assert.assertTrue(e != null);
        } catch (MovementServiceException e) {
            Assert.assertTrue(e != null);
        } catch (Throwable e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
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


    private List<MovementBaseType> createBaseTypeList() {
        List<MovementBaseType> query = new ArrayList<>();
        String connectId = UUID.randomUUID().toString();
        Integer n = rnd.nextInt(10);
        for (int i = 0; i < n; i++) {
            query.add(createMovementBaseType(i, connectId));
        }
        return query;
    }

    private MovementBaseType createMovementBaseType(Integer i, String connectId) {
        MovementBaseType movementBaseType = new MovementBaseType();
        movementBaseType.setConnectId(connectId);
        movementBaseType.setSource(MovementSourceType.AIS);
        movementBaseType.setMovementType(MovementTypeType.MAN);
        movementBaseType.setPosition(position());
        return movementBaseType;
    }

    private MovementPoint position() {

        Double longitude = rnd.nextDouble();
        Double latitude = rnd.nextDouble();
        MovementPoint movementPoint = new MovementPoint();
        movementPoint.setLongitude(longitude);
        movementPoint.setLatitude(latitude);
        return movementPoint;
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
