package eu.europa.ec.fisheries.uvms.movement.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListPagination;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.LatestMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

@RunWith(Arquillian.class)
public class MovementServiceIntTest extends TransactionalTests {

    private Random rnd = new Random();
    private static int NumberOfMovements = 3;

    @EJB
    MovementService movementService;


    @Test
    @OperateOnDeployment("movementservice")
    public void createMovement() {

        Instant now = DateUtil.nowUTC();
        double longitude = 9.140625D;
        double latitude = 57.683804D;

        // create a MovementConnect
        UUID connectId = UUID.randomUUID();
        Movement movementType = MockData.createMovement(longitude, latitude, connectId);
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

        List<UUID> connectionIds = new ArrayList<>();
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
        Movement movementType = MockData.createMovement(longitude, latitude, connectId);
        try {
            createdMovement = movementService.createMovement(movementType);
            assertNotNull(createdMovement);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        MovementQuery query = createMovementQuery(true);
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(createdMovement.getMovementConnect().getValue().toString());
        query.getMovementSearchCriteria().add(criteria);

        try {
            GetMovementListByQueryResponse list = movementService.getList(query);
            assertNotNull(list);
            List<MovementType> movementList = list.getMovement();
            assertNotNull(movementList);
            assertTrue(!movementList.isEmpty());
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

        double longitude = rnd.nextDouble();
        double latitude = rnd.nextDouble();
        List<Movement> movementTypeList = new ArrayList<>();
        for(int i = 0 ; i < NumberOfMovements ; i++){
            movementTypeList.add(MockData.createMovement(longitude, latitude, UUID.randomUUID()));
            longitude = longitude  + 0.05;
            latitude = latitude +  0.05;
        }

        List<Movement> movementBatch = movementService.createMovementBatch(movementTypeList);
        assertNotNull(movementBatch);
        assertTrue(!movementBatch.isEmpty());
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void getById() {

        try {
            Instant now = DateUtil.nowUTC();
            double longitude = 9.140625D;
            double latitude = 57.683804D;

            // create a MovementConnect
            UUID connectId = UUID.randomUUID();
            Movement movementType = MockData.createMovement(longitude, latitude, connectId);
            assertNotNull(movementService);
            Movement createdMovementType = movementService.createMovement(movementType);
            em.flush();
            assertNotNull(createdMovementType);

            UUID guid = createdMovementType.getGuid();
            assertNotNull(guid);

            Movement fetchedMovement = movementService.getById(guid);
            assertNotNull(fetchedMovement);
            UUID fetchedGuid = fetchedMovement.getGuid();
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
        UUID connectId = null;
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
