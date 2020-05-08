package eu.europa.ec.fisheries.uvms.movement.service.bean;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;

import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.LatestMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
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
        double longitude = 9.140625D;
        double latitude = 57.683804D;

        // create a MovementConnect
        UUID connectId = UUID.randomUUID();
        Movement movementType = MockData.createMovement(longitude, latitude, connectId.toString());
        try {
            Movement createdMovementType = movementService.createMovement(movementType);
            assertNotNull(createdMovementType);
        } catch (Exception e) {
            fail();
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
            fail("The above call should throw an exception since query is incomplete");
        } catch (EJBTransactionRolledbackException e) {
            e.printStackTrace();
            //Assert.fail();
        	assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getListByMovementQuery() {

        Movement createdMovement = null;

        double longitude = 9.140625D;
        double latitude = 57.683804D;

        // create a MovementConnect
        UUID connectId = UUID.randomUUID();
        Movement movementType = MockData.createMovement(longitude, latitude, connectId.toString());
        try {
            createdMovement = movementService.createMovement(movementType);
            assertNotNull(createdMovement);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        MovementQuery query = createMovementQuery(true);
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement.getMovementConnect().getValue());
        query.getMovementSearchCriteria().add(criteria);

        try {
            GetMovementListByQueryResponse list = movementService.getList(query);
            assertNotNull(list);
            List<MovementType> movementList = list.getMovement();
            assertNotNull(movementList);
            assertFalse(movementList.isEmpty());
            assertEquals(connectId.toString(), movementList.get(0).getConnectId());
        } catch (Exception e) {
            fail(e.getMessage());
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
        } catch (Exception e) {
            fail();
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
            fail();
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void createBatch() throws MovementServiceException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");
        double longitude = rnd.nextDouble();
        double latitude = rnd.nextDouble();
        List<MovementAndBaseType> movementTypeList = new ArrayList<>();
        for(int i = 0 ; i < NumberOfMovements ; i++){
            Movement movement = MockData.createMovement(longitude, latitude, UUID.randomUUID().toString());
            movementTypeList.add(new MovementAndBaseType(movement, new MovementBaseType()));
            longitude = longitude  + 0.05;
            latitude = latitude +  0.05;
        }

        List<MovementAndBaseType> movementBatch = movementService.createMovementBatch(movementTypeList);
        assertNotNull(movementBatch);
        assertFalse(movementBatch.isEmpty());
        assertTrue(movementBatch.stream().map(MovementAndBaseType::getBaseType).allMatch(Objects::nonNull));
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void triggerBatchEventWithBrokenJMS() throws MovementServiceException {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "true");
        double longitude = rnd.nextDouble();
        double latitude = rnd.nextDouble();
        List<MovementAndBaseType> movementTypeList = new ArrayList<>();
        for(int i = 0 ; i < NumberOfMovements ; i++){
            Movement movement = MockData.createMovement(longitude, latitude, UUID.randomUUID().toString());
            movementTypeList.add(new MovementAndBaseType(movement, new MovementBaseType()));
            longitude += 0.05;
            latitude += 0.05;
        }
        try {
            movementService.createMovementBatch(movementTypeList);
            fail("This should produce an EJBException and trigger rollback");
        } catch (EJBException ignore) {}
        
        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getAreas() {
        List<Area> response = movementService.getAreas();
        assertNotNull(response);
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getById() {
        try {
            double longitude = 9.140625D;
            double latitude = 57.683804D;

            // create a MovementConnect
            UUID connectId = UUID.randomUUID();
            Movement movementType = MockData.createMovement(longitude, latitude, connectId.toString());
            assertNotNull(movementService);
            Movement createdMovementType = movementService.createMovement(movementType);
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
            fail();
        } catch (Exception e) {
            assertNotNull(e);
        }
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
