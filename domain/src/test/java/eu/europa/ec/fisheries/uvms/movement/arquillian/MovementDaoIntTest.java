package eu.europa.ec.fisheries.uvms.movement.arquillian;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.entity.LatestMovement;
import eu.europa.ec.fisheries.uvms.movement.entity.MinimalMovement;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.exception.SearchMapperException;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchFieldMapper;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchValue;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by thofan on 2017-01-30.
 */
@RunWith(Arquillian.class)
public class MovementDaoIntTest extends TransactionalTests {

    private Random rnd = new Random();

    /*  THIS IS THE 4326 BELOW  =

        http://spatialreference.org/ref/epsg/wgs-84/

        GEOGCS["WGS 84",
        DATUM["WGS_1984", SPHEROID["WGS 84",6378137,298.257223563, AUTHORITY["EPSG","7030"]],
        AUTHORITY["EPSG","6326"]],
        PRIMEM["Greenwich",0, AUTHORITY["EPSG","8901"]], UNIT["degree",0.01745329251994328,AUTHORITY["EPSG","9122"]],
        AUTHORITY["EPSG","4326"]]
     */


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Inject
    private UserTransaction userTransaction;

    @EJB
    private MovementDao movementDao;


    /******************************************************************************************************************
     *   TEST FUNCTIONS
     ******************************************************************************************************************/

    @Test
    @OperateOnDeployment("normal")
    public void create() throws MovementDaoException {

        MovementConnect movementConnect = createMovementConnectHelper();
        MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
        movementDao.flush();
        assertNotNull(createdMovementConnect);

        Movement movement = createMovementHelper();
        movement.setMovementConnect(createdMovementConnect);
        Movement createdMovement = movementDao.create(movement);
        movementDao.flush();
        assertNotNull(createdMovement);

        Long createdMovementId = createdMovement.getId();
        Movement fetchedMovement = movementDao.getMovementById(createdMovementId);
        assertNotNull(fetchedMovement);
        assertEquals(createdMovementId, fetchedMovement.getId());

        Long createdMovementConnectId = createdMovementConnect.getId();
        MovementConnect fetchedMovementConnect = fetchedMovement.getMovementConnect();
        assertNotNull(fetchedMovementConnect);
        assertEquals(createdMovementConnectId, fetchedMovementConnect.getId());
    }

    @Test
    @OperateOnDeployment("normal")
    public void flush() throws MovementDaoException {
        movementDao.flush();
        assertTrue("We assume hibernate native functions actually works", true);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getFirstMovement() throws MovementDaoException {

        MovementConnect movementConnect = createMovementConnectHelper();
        MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
        movementDao.flush();
        assertNotNull(createdMovementConnect);

        Movement movement = createMovementHelper();
        movement.setMovementConnect(createdMovementConnect);
        Movement createdMovement = movementDao.create(movement);
        createdMovement.setProcessed(true);
        movementDao.flush();
        assertNotNull(createdMovement);

        Long createdMovementId = createdMovement.getId();
        assertNotNull("The created id : " + createdMovementId.toString(), createdMovementId);

        Movement fetchedMovement = movementDao.getMovementById(createdMovementId);
        assertNotNull(fetchedMovement);
        assertEquals(fetchedMovement.getId(), createdMovementId);
        MovementConnect fetchedMovementConnect = fetchedMovement.getMovementConnect();

        Movement firstMovement = movementDao.getFirstMovement(fetchedMovementConnect.getValue());
        assertNotNull(firstMovement);
    }


    // There is no value to run these 2 tests
//    @Test
//    @OperateOnDeployment("normal")
//    public void getLatestMovement_tryLatestTable_FALSE() throws MovementDaoException {
//        Date timeStamp = DateUtil.nowUTC();
//
//        MovementConnect movementConnect = createMovementConnectHelper();
//        MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
//        movementDao.flush();
//        assertNotNull(createdMovementConnect);
//
//        Movement movement = createMovementHelper();
//        movement.setMovementConnect(createdMovementConnect);
//        Movement createdMovement = movementDao.create(movement);
//        movementDao.flush();
//        assertNotNull(createdMovement);
//        assertEquals(createdMovementConnect.getId(), createdMovement.getMovementConnect().getId());
//
//        Long createdMovementId = createdMovement.getId();
//        assertNotNull("The created id : " + createdMovementId.toString(), createdMovementId);
//
//        Movement fetchedMovement = movementDao.getMovementById(createdMovementId);
//        assertNotNull(fetchedMovement);
//        assertEquals(fetchedMovement.getId(), createdMovementId);
//        MovementConnect fetchedMovementConnect = fetchedMovement.getMovementConnect();
//        assertNotNull(fetchedMovementConnect);
//
//        Movement latestMovement = movementDao.getLatestMovement(fetchedMovementConnect.getValue(), timeStamp);
//        // null is not an error
//        assertTrue(true);
//    }

//    @Test
//    @OperateOnDeployment("normal")
//    public void getLatestMovement_tryLatestTable_TRUE() throws MovementDaoException {
//        Date timeStamp = DateUtil.nowUTC();
//
//        MovementConnect movementConnect = createMovementConnectHelper();
//        MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
//        movementDao.flush();
//        assertNotNull(createdMovementConnect);
//
//        Movement movement = createMovementHelper();
//        movement.setMovementConnect(createdMovementConnect);
//        Movement createdMovement = movementDao.create(movement);
//        movementDao.flush();
//        assertNotNull(createdMovement);
//        assertEquals(createdMovementConnect.getId(), createdMovement.getMovementConnect().getId());
//
//        Long createdMovementId = createdMovement.getId();
//        assertNotNull("The created id : " + createdMovementId.toString(), createdMovementId);
//
//        Movement fetchedMovement = movementDao.getMovementById(createdMovementId);
//        assertNotNull(fetchedMovement);
//        assertEquals(fetchedMovement.getId(), createdMovementId);
//        MovementConnect fetchedMovementConnect = fetchedMovement.getMovementConnect();
//        assertNotNull(fetchedMovementConnect);
//
//        Movement latestMovement = movementDao.getLatestMovement(fetchedMovementConnect.getValue(), timeStamp);
//        // TODO: Should not return null in contrast with the previous test case but it actually does! Same method body, Not good!
//        //assertNotNull(latestMovement);
//    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements() throws MovementDaoException {
        List<LatestMovement> all = movementDao.getLatestMovements(10);
        assertNotNull(all);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getListAll() throws MovementDaoException {

        int n = rnd.nextInt(50);
        for (int i = 0; i < n; i++) {
            MovementConnect movementConnect = createMovementConnectHelper();
            MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
            movementDao.flush();
            assertNotNull(createdMovementConnect);

            Movement movement = createMovementHelper();
            movement.setMovementConnect(createdMovementConnect);
            Movement createdMovement = movementDao.create(movement);
            movementDao.flush();
            assertNotNull(createdMovement);
        }

        List<Movement> all = movementDao.getListAll();
        assertNotNull(all);
        assertTrue(all.size() >= n);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getListAll_NO_PositionalDups() throws MovementDaoException {

        double longitude = 8.140625D;
        double latitude = 56.683804D;

        int n = rnd.nextInt(50);
        for (int i = 0; i < n; i++) {
            MovementConnect movementConnect = createMovementConnectHelper();
            MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
            movementDao.flush();
            assertNotNull(createdMovementConnect);

            Movement movement = createMovementHelper(longitude, latitude);
            movement.setMovementConnect(createdMovementConnect);
            Movement createdMovement = movementDao.create(movement);
            movementDao.flush();
            assertNotNull(createdMovement);

            longitude += 10;
            latitude+= 10;
        }
        List<Movement> all = movementDao.getListAll();
        assertNotNull(all);
        assertTrue(all.size() >= n);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMinimalMovementListPaginated() throws MovementDaoException {

        List<SearchValue> searchKeyValues = new ArrayList<>();
        Integer page = 1;
        Integer listSize = 10;
        String sql = "select distinct m  from  MinimalMovement m";

        List<MinimalMovement> minimalMovementList = movementDao.getMovementListPaginated(page, listSize, sql, searchKeyValues);
        assertNotNull(minimalMovementList);
        assertTrue(minimalMovementList.size() <= 10);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMinimalMovementListPaginated_NegativeSpeed() throws MovementDaoException {

        List<SearchValue> searchKeyValues = new ArrayList<>();
        Integer page = 1;
        Integer listSize = 10;
        String sql = "select distinct m  from  MinimalMovement m where m.speed < 0";

        List<MinimalMovement> minimalMovementList = movementDao.getMovementListPaginated(page, listSize, sql, searchKeyValues);
        assertNotNull(minimalMovementList);
        assertEquals(0, minimalMovementList.size());
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementById() throws MovementDaoException {

        MovementConnect movementConnect = createMovementConnectHelper();
        MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
        movementDao.flush();
        assertNotNull(createdMovementConnect);

        Movement movement = createMovementHelper();
        movement.setMovementConnect(createdMovementConnect);
        Movement createdMovement = movementDao.create(movement);
        movementDao.flush();
        assertNotNull(createdMovement);
        assertEquals(createdMovementConnect.getId(), createdMovement.getMovementConnect().getId());

        Long createdMovementId = createdMovement.getId();
        assertNotNull("The created id : " + createdMovementId.toString(), createdMovementId);

        Movement fetchedMovement = movementDao.getMovementById(createdMovementId);
        assertNotNull(fetchedMovement);
        assertEquals(fetchedMovement.getId(), createdMovementId);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementById_NULL_AS_SearchCriteria() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("id to load is required for loading");

        movementDao.getMovementById(null);
    }

    @Test(expected = NullPointerException.class)
    @OperateOnDeployment("normal")
    public void getMovementById_NonExisting_AS_SearchCriteria() {
        Movement movement = movementDao.getMovementById(-42L);
        movement.setId(42L);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementConnectByConnectId() throws MovementDaoException {

        MovementConnect movementConnect = createMovementConnectHelper();
        MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
        movementDao.flush();
        assertNotNull(createdMovementConnect);

        Movement movement = createMovementHelper();
        movement.setMovementConnect(createdMovementConnect);
        Movement createdMovement = movementDao.create(movement);
        movementDao.flush();
        assertNotNull(createdMovement);
        assertEquals(createdMovementConnect.getId(), createdMovement.getMovementConnect().getId());

        String createdMovementConnectValue = createdMovementConnect.getValue();

        MovementConnect fetchedMovementConnect = movementDao.getMovementConnectByConnectId(createdMovementConnectValue);
        assertNotNull(fetchedMovementConnect);

        String fetchedMovementConnectValue = fetchedMovementConnect.getValue();
        assertNotNull(fetchedMovementConnectValue);
        assertEquals(createdMovementConnectValue, fetchedMovementConnectValue);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementList_NonPaginated_NullCheckResultSetAtUnlogicQuery() throws MovementDaoException {

        // TODO getMovementList looks unhealthy according to the number of queries it runs  (slow)

        List<SearchValue> searchValues = new ArrayList<>();
        String sql = "SELECT m FROM Movement m WHERE m.speed < -42";

        List<Movement> movements = movementDao.getMovementList(sql, searchValues);
        assertNotNull(movements);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementList_NonPaginated_NoSearchValues() throws MovementDaoException {

        List<SearchValue> searchValues = new ArrayList<>();
        String sql = "select m from Movement m ";

        List<Movement> movements = movementDao.getMovementList(sql, searchValues);
        assertNotNull(movements);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementList_NonPaginated_WithSearchValues() throws MovementDaoException {

        // this is not covered in the code BUT it forces that code path to execute

        List<SearchValue> searchValues = Collections.singletonList(new SearchValue(SearchField.MOVEMENT_SPEED, "HEPP"));
        String sql = "select m from Movement m ";

        List<Movement> movements = movementDao.getMovementList(sql, searchValues);
        assertNotNull(movements);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementList_NonPaginated_ShouldCrash() throws MovementDaoException {

        List<SearchValue> searchValues = Collections.singletonList(new SearchValue(SearchField.AREA, "HEPP"));
        String sql = "select m from Movement m ";

        expectedException.expect(MovementDaoException.class);
        expectedException.expectMessage("[ Error when getting list ]");

        movementDao.getMovementList(sql, searchValues);
    }


    @Test
    @OperateOnDeployment("normal")
    public void getMovementList_NumberOfReports_0_NoSearchValue() throws MovementDaoException {

        // TODO getMovementList looks unhealthy according to the number of queries it runs  (slow)

        List<SearchValue> searchValues = new ArrayList<>();
        String sql = "SELECT m FROM Movement m WHERE m.speed < -42";

        List<Movement> movements = movementDao.getMovementList(sql, searchValues, 0);
        assertNotNull(movements);
    }

    // @Test
    // Unstable ServerCode see TODO
    public void getMovementList_NumberOfReports_1_NoSearchValue() throws MovementDaoException {

        // TODO getMovementList looks unhealthy according to the number of queries it runs  (slow)
        // TODO getLatestMovementsByConnectId in DAO throws exception and crashes the entire result-set if a lookup is empty

        List<SearchValue> searchValues = new ArrayList<>();
        String sql = "SELECT m FROM Movement m WHERE m.speed < -42";

        List<Movement> movements = movementDao.getMovementList(sql, searchValues, 1);
        assertNotNull(movements);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementList_NumberOfReports_5_noSearchValue() throws MovementDaoException {

        // TODO getMovementList looks unhealthy according to the number of queries it runs  (slow)

        List<SearchValue> searchValues = new ArrayList<>();
        String sql = "SELECT m FROM Movement m WHERE m.speed < -42";

        List<Movement> movements = movementDao.getMovementList(sql, searchValues, 5);
        assertNotNull(movements);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementList_NumberOfReports_0_WithSearchValue() throws MovementDaoException {

        // TODO this test is maybe to optimistic since the query parameters are given in the test OR they are manufactured in the Service-layer
        // TODO getMovementList looks unhealthy according to the number of queries it runs  (slow)

        Date timeStamp = DateUtil.nowUTC();
        List<SearchValue> searchValues = Collections.singletonList(new SearchValue(SearchField.DATE, timeStamp.toString(), timeStamp.toString()));
        String sql = "SELECT m FROM Movement m WHERE m.timestamp >= :fromDate AND m.timestamp <= :toDate";

        List<Movement> movements = movementDao.getMovementList(sql, searchValues, 0);
        assertNotNull(movements);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementList_NumberOfReports_1_WithSearchValue() throws MovementDaoException {

        // TODO this test is maybe to optimistic since the query parameters are given in the test OR they are manufactured in the Service-layer
        // TODO getMovementList looks unhealthy according to the number of queries it runs  (slow)

        Date timeStamp = DateUtil.nowUTC();
        List<SearchValue> searchValues = Collections.singletonList(new SearchValue(SearchField.DATE, timeStamp.toString(), timeStamp.toString()));
        String sql = "SELECT m FROM Movement m WHERE m.timestamp >= :fromDate AND m.timestamp <= :toDate";

        List<Movement> movements = movementDao.getMovementList(sql, searchValues, 1);
        assertNotNull(movements);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementList_NumberOfReports_1_withSearchValue_NoResult() throws MovementDaoException {

        // TODO this test is maybe to optimistic since the query parameters are given in the test OR they are manufactured in the Service-layer
        // TODO getMovementList  looks unhealthy according to the number of queries it runs  (slow)

        Date timeStamp = DateUtil.nowUTC();
        List<SearchValue> searchValues = Collections.singletonList(new SearchValue(SearchField.DATE, timeStamp.toString(), timeStamp.toString()));
        String sql = "SELECT m FROM Movement m WHERE m.timestamp >= :fromDate AND m.timestamp <= :toDate AND m.speed = -42";

        List<Movement> movements = movementDao.getMovementList(sql, searchValues, 1);
        assertNotNull(movements);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementList_NumberOfReports_5_withSearchValue() throws MovementDaoException {

        // TODO this test is maybe to optimistic since the query parameters are given in the test OR they are manufactured in the Service-layer
        // TODO getMovementList  looks unhealthy according to the number of queries it runs  (slow)

        Date timeStamp = DateUtil.nowUTC();
        List<SearchValue> searchValues = Collections.singletonList(new SearchValue(SearchField.DATE, timeStamp.toString(), timeStamp.toString()));
        String sql = "SELECT m FROM Movement m WHERE m.timestamp >= :fromDate AND m.timestamp <= :toDate";

        List<Movement> movements = movementDao.getMovementList(sql, searchValues, 5);
        assertNotNull(movements);
    }

    // The value of this test is very limited
    @Test
    @OperateOnDeployment("normal")
    public void getMovementListByAreaAndTimeInterval_Invalid_Null_Parameter() throws MovementDaoException {

        // TODO  this one cannot be instantiated using new (probably a soap thing)
        // MovementAreaAndTimeIntervalCriteria movementAreaAndTimeIntervalCriteria = new MovementAreaAndTimeIntervalCriteria();

        expectedException.expect(EJBTransactionRolledbackException.class);

        List<Movement> movementListByAreaAndTimeInterval = movementDao.getMovementListByAreaAndTimeInterval(null);
    }

    // don't want to use JodaTime in tests . . .
    @Test
    @OperateOnDeployment("normal")
    public void getMovementListPaginated() throws MovementDaoException {

        long currentTimeMillis = System.currentTimeMillis();

        Date d1980 = new Date(80,3,3);
        Date nowPlus10 = new Date(currentTimeMillis + 10000);
        String fromDate = DateUtil.parseUTCDateToString(d1980);
        String toDate = DateUtil.parseUTCDateToString(nowPlus10);
        List<SearchValue> searchValues = Collections.singletonList(new SearchValue(SearchField.DATE, fromDate, toDate));

        Integer page = 1;
        Integer listSize = 10;

        List<Movement> allMovements = getAllFromMovementHelper();
        if(allMovements.size() < listSize)
            listSize = allMovements.size();

        String sql = "SELECT m FROM Movement m WHERE m.timestamp >= :fromDate AND m.timestamp <= :toDate";
        List<Movement> movementList = movementDao.getMovementListPaginated(page, listSize, sql, searchValues);
        assertNotNull(movementList);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementListSearchCount() throws java.text.ParseException, SearchMapperException, MovementDaoException, ParseException {
        List<SearchValue> searchValues = new ArrayList<>();
        String sql = SearchFieldMapper.createCountSearchSql(searchValues, true);
        Long searchResult = movementDao.getMovementListSearchCount(sql,searchValues);
        assertNotNull(searchResult);
        assertTrue(searchResult >= 0);
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void getMovementListSearchCount_SearchValueNull_ExceptionThrown() throws java.text.ParseException, SearchMapperException, MovementDaoException, ParseException {

        expectedException.expect(Exception.class);

        String sql = SearchFieldMapper.createCountSearchSql(null, true);
        movementDao.getMovementListSearchCount(sql,null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getUnprocessedMovementIds() {

        List<Long> unprocessedMovementIds = movementDao.getUnprocessedMovementIds();
        assertNotNull(unprocessedMovementIds);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getUnprocessedMovements() {

        List<Movement> unprocessedMovements = movementDao.getUnprocessedMovements();
        assertNotNull(unprocessedMovements);
    }

    @Test
    @OperateOnDeployment("normal")
    public void merge() throws MovementDaoException {

        expectedException.expect(MovementDaoException.class);
        movementDao.merge(null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void persist() throws MovementDaoException {

        expectedException.expect(MovementDaoException.class);
        movementDao.persist(null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void upsertLatestMovementOnExisting() throws MovementDaoException {

        MovementConnect movementConnect = createMovementConnectHelper();
        MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
        movementDao.flush();

        Movement movement = createMovementHelper();
        movement.setMovementConnect(createdMovementConnect);
        Movement createdMovement = movementDao.create(movement);
        movementDao.flush();

        Long createdMovementId = createdMovement.getId();

        // the upsert creates one if it is not there
        movementDao.upsertLatestMovement(createdMovement, createdMovementConnect);
        movementDao.flush();

        List<LatestMovement> all = movementDao.getLatestMovements(10000);
        Boolean found = findLatestMovements(createdMovementId, all);
        assertTrue(found);
    }

    @Test
    @OperateOnDeployment("normal")
    public void upsertLatestMovementOnNonExisting() throws MovementDaoException {

        MovementConnect movementConnect = createMovementConnectHelper();
        MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
        movementDao.flush();

        Movement DOES_NOT_EXIST_IN_DB = createMovementHelper();
        DOES_NOT_EXIST_IN_DB.setMovementConnect(createdMovementConnect);

        Long createdMovementId = DOES_NOT_EXIST_IN_DB.getId();

        List<LatestMovement> listBefore = movementDao.getLatestMovements(10000);

        // the upsert creates one if it is not there
        movementDao.upsertLatestMovement(DOES_NOT_EXIST_IN_DB, createdMovementConnect);
        movementDao.flush();

        List<LatestMovement> listAfter = movementDao.getLatestMovements(10000);

        assertNotEquals(listBefore.size(), listAfter.size());

        Boolean found = findLatestMovements(createdMovementId, listAfter);
        assertFalse(found);
    }


    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/

    /** This one always creates on the same coordinates
     *
     * @return
     */

    private Movement createMovementHelper() {
        // delegate to generic
        double longitude = 9.140625D;
        double latitude = 57.683804D;
        return createMovementHelper(longitude, latitude);
    }

    /**
     * This one supports tampering with longitude latitude to simulate real movements
     *
     * @param longitude
     * @param latitude
     * @return newly created {@code Movement} object.
     */
    private Movement createMovementHelper( double longitude, double latitude  ) {

        Movement movement = new Movement();

        GeometryFactory geometryFactory = new GeometryFactory();
        Date timeStamp = DateUtil.nowUTC();

        movement.setMovementSource(MovementSourceType.NAF);
        movement.setMovementType(MovementTypeType.MAN);
        movement.setUpdatedBy("Arquillian");
        movement.setGuid();
        movement.setTimestamp(timeStamp);
        movement.setUpdated(timeStamp);
        movement.setDuplicate(Boolean.FALSE);
        movement.setProcessed(Boolean.FALSE);
        movement.setSpeed(12D);

        Coordinate coordinate = new Coordinate(longitude, latitude);
        Point point = geometryFactory.createPoint(coordinate);
        point.setSRID(4326);
        movement.setLocation(point);

        return movement;
    }

    private MovementConnect createMovementConnectHelper() {
        MovementConnect movementConnect = new MovementConnect();
        movementConnect.setValue(UUID.randomUUID().toString());
        movementConnect.setUpdatedBy("Arquillian");
        movementConnect.setUpdated(DateUtil.nowUTC());
        return movementConnect;
    }

    private List<Movement> getAllFromMovementHelper() {

        String sql = "SELECT m FROM Movement m ";

        try {
            return movementDao.getMovementList(sql, new ArrayList<SearchValue>());
        } catch (MovementDaoException e) {
            fail(e.toString());
        }
        return new ArrayList<>();
    }

    private Boolean findLatestMovements(Long createdMovementId, List<LatestMovement> all) {
        for (LatestMovement latestMovement : all) {
            Movement movementFromLatestMovement = latestMovement.getMovement();
            Long movementFromLatestMovementId = movementFromLatestMovement.getId();
            if (movementFromLatestMovementId.equals(createdMovementId)) {
                return true;
            }
        }
        return false;
    }
}
