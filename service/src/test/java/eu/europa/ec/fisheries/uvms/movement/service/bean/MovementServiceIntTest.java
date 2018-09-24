package eu.europa.ec.fisheries.uvms.movement.service.bean;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;

import eu.europa.ec.fisheries.uvms.movement.service.entity.LatestMovement;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.common.v1.SimpleResponse;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByAreaAndTimeIntervalResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.message.producer.bean.MessageProducerBean;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class MovementServiceIntTest extends TransactionalTests {

    private Random rnd = new Random();
    private static int NumberOfMovements = 3;

    @EJB
    MovementService movementService;

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void getMovementListByAreaAndTimeInterval_EmptyCriteria() {
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
        assertNotNull(list);
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @OperateOnDeployment("movementservice")
    public void getMovementListByAreaAndTimeIntervalTimeIntervalNoCode() {
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
            assertNotNull(createdMovementType);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovementsByConnectIds_EmptyList() {

        List<String> connectionIds = new ArrayList<>();
        List<Movement> movements =  movementService.getLatestMovementsByConnectIds(connectionIds);
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
        	assertTrue(true);
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
            assertNotNull(response);
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
            assertNotNull(e);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createBatch() {

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
        assertNotNull(simpleResponse);
        assertEquals(SimpleResponse.OK, simpleResponse);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void triggerBatchEventWithBrokenJMS() {

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
        assertNotNull(response);
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
            assertNotNull(movementService);
            Movement createdMovementType = movementService.createMovement(movementType, "TEST");
            em.flush();
            assertNotNull(createdMovementType);

            String guid = createdMovementType.getGuid();
            assertNotNull(guid);

            Movement fetchedMovement = movementService.getById(guid);
            assertNotNull(fetchedMovement);
            String fetchedGuid = fetchedMovement.getGuid();
            assertNotNull(fetchedGuid);
            assertEquals(fetchedGuid, guid);

        } catch (Exception e) {
            // TODO  check this it is suspect
            //Assert.fail();
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getById_Null_ID() {
        String connectId = null;
        Movement byId = movementService.getById(connectId);
        assertNull(byId);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getLatestMovements() {
        List<LatestMovement> movements = movementService.getLatestMovements(5);
        assertNotNull(movements);
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
            assertNotNull(e);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getById_emptyGUID() {
        String connectId = "";
        Movement byId = movementService.getById(connectId);
        assertNull(byId);
    }

    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/
 
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
