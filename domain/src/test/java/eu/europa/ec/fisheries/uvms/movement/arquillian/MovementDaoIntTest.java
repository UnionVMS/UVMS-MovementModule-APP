package eu.europa.ec.fisheries.uvms.movement.arquillian;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.exception.SearchMapperException;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchFieldMapper;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchValue;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.*;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.entity.*;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;

import java.util.*;

import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.transaction.*;

/**
 * Created by thofan on 2017-01-30.
 */
@RunWith(Arquillian.class)
public class MovementDaoIntTest {


    private Random rnd = new Random();


    /*  THIS IS THE 4326 BELOW  =

    http://spatialreference.org/ref/epsg/wgs-84/

    GEOGCS["WGS 84",
       DATUM["WGS_1984", SPHEROID["WGS 84",6378137,298.257223563, AUTHORITY["EPSG","7030"]],
       AUTHORITY["EPSG","6326"]],
       PRIMEM["Greenwich",0, AUTHORITY["EPSG","8901"]], UNIT["degree",0.01745329251994328,AUTHORITY["EPSG","9122"]],
       AUTHORITY["EPSG","4326"]
    ]
     */
    final static Logger LOG = LoggerFactory.getLogger(MovementDaoIntTest.class);

    @Inject
    UserTransaction userTransaction;

    @EJB
    private MovementDao movementDao;


    /******************************************************************************************************************
     *   SETUP FUNCTIONS
     ******************************************************************************************************************/

    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementTestDeployment.createDeployment();
    }


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
    public void create() {
        try {


            MovementConnect movementConnect = createMovementConnectHelper();
            MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
            movementDao.flush();

            Movement movement = createMovementHelper();
            movement.setMovementConnect(createdMovementConnect);
            Movement createdMovement = movementDao.create(movement);
            movementDao.flush();

            Long createdMovementId = createdMovement.getId();
            Long createdMovementConnectId = createdMovementConnect.getId();

            Movement fetchedMovement = movementDao.getMovementById(createdMovementId);
            Assert.assertTrue(fetchedMovement != null);

            MovementConnect fetchedMovementConnect = fetchedMovement.getMovementConnect();
            Assert.assertTrue(fetchedMovementConnect != null);

            Assert.assertTrue(createdMovementId.equals(fetchedMovement.getId()));
            Assert.assertTrue(createdMovementConnectId.equals(fetchedMovementConnect.getId()));

        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void flush() {
        try {
            movementDao.flush();
            Assert.assertTrue("We assume hibernate native functions actually works", true);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void getFirstMovement() {

        /*
        TODO: Date NOT USED in Implementation
         */

        try {

            MovementConnect movementConnect = createMovementConnectHelper();
            MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
            movementDao.flush();

            Movement movement = createMovementHelper();
            movement.setMovementConnect(createdMovementConnect);
            Movement createdMovement = movementDao.create(movement);
            movementDao.flush();
            Assert.assertTrue(createdMovement != null);

            Long createdMovementId = createdMovement.getId();
            Assert.assertTrue("The created id : " + createdMovementId.toString(), createdMovementId != null);

            Movement fetchedMovement = movementDao.getMovementById(createdMovementId);
            Assert.assertTrue(fetchedMovement != null);
            MovementConnect fetchedMovementConnect = fetchedMovement.getMovementConnect();

            Movement firstMovement = movementDao.getFirstMovement(fetchedMovementConnect.getValue(), null);
            Assert.assertTrue(firstMovement != null);
        } catch (RuntimeException e) {
            Assert.fail(e.toString());
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }


    @Test
    public void getLatestMovement_tryLatestTable_FALSE() {
        Date timeStamp = DateUtil.nowUTC();
        try {

            MovementConnect movementConnect = createMovementConnectHelper();
            MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
            movementDao.flush();

            Movement movement = createMovementHelper();
            movement.setMovementConnect(createdMovementConnect);
            Movement createdMovement = movementDao.create(movement);
            movementDao.flush();
            Assert.assertTrue(createdMovement != null);

            Long createdMovementId = createdMovement.getId();
            Assert.assertTrue("The created id : " + createdMovementId.toString(), createdMovementId != null);


            Movement fetchedMovement = movementDao.getMovementById(createdMovementId);
            Assert.assertTrue(fetchedMovement != null);
            MovementConnect fetchedMovementConnect = fetchedMovement.getMovementConnect();


            Movement latestMovement = movementDao.getLatestMovement(fetchedMovementConnect.getValue(), timeStamp, false);
            // null is not an error
            Assert.assertTrue(true);
        } catch (RuntimeException e) {
            Assert.fail(e.toString());
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }


    @Test
    public void getLatestMovement_tryLatestTable_TRUE() {

        Date timeStamp = DateUtil.nowUTC();
        try {

            MovementConnect movementConnect = createMovementConnectHelper();
            MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
            movementDao.flush();

            Movement movement = createMovementHelper();
            movement.setMovementConnect(createdMovementConnect);
            Movement createdMovement = movementDao.create(movement);
            movementDao.flush();
            Assert.assertTrue(createdMovement != null);

            Long createdMovementId = createdMovement.getId();
            Assert.assertTrue("The created id : " + createdMovementId.toString(), createdMovementId != null);


            Movement fetchedMovement = movementDao.getMovementById(createdMovementId);
            Assert.assertTrue(fetchedMovement != null);
            MovementConnect fetchedMovementConnect = fetchedMovement.getMovementConnect();


            Movement latestMovement = movementDao.getLatestMovement(fetchedMovementConnect.getValue(), timeStamp, true);
            // TODO unclear what to expect
            // Assert.assertTrue(latestMovement != null);
        } catch (RuntimeException e) {
            Assert.fail(e.toString());
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void getLatestMovements() {
        try {
            List<LatestMovement> all = movementDao.getLatestMovements(10);
            Assert.assertTrue(all != null);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }


    @Test
    public void getListAll() {
        try {

            int n = rnd.nextInt(50);
            for (int i = 0; i < n; i++) {

                MovementConnect movementConnect = createMovementConnectHelper();
                MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
                movementDao.flush();

                Movement movement = createMovementHelper();
                movement.setMovementConnect(createdMovementConnect);
                Movement createdMovement = movementDao.create(movement);
                movementDao.flush();
            }

            List<Movement> all = movementDao.getListAll();
            Assert.assertTrue(all != null);
            Assert.assertTrue(all.size() >= n);

        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void getListAll_NO_PositionalDups() {
        try {

            double longitude = 8.140625D;
            double latitude = 56.683804D;


            int n = rnd.nextInt(50);
            for (int i = 0; i < n; i++) {

                MovementConnect movementConnect = createMovementConnectHelper();
                MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
                movementDao.flush();

                Movement movement = createMovementHelper(longitude, latitude);
                movement.setMovementConnect(createdMovementConnect);
                Movement createdMovement = movementDao.create(movement);
                movementDao.flush();


                longitude += 10;
                latitude+= 10;

            }

            List<Movement> all = movementDao.getListAll();
            Assert.assertTrue(all != null);
            Assert.assertTrue(all.size() >= n);

        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }



    @Test
    public void getMinimalMovementListPaginated() {

        List<SearchValue> searchKeyValues = new ArrayList<>();
        Integer page = 1;
        Integer listSize = 10;
        String sql = "select distinct m  from  MinimalMovement m";

        try {
            List<MinimalMovement> minimalMovementList = movementDao.getMinimalMovementListPaginated(page, listSize, sql, searchKeyValues);
            Assert.assertTrue(minimalMovementList != null);
            Assert.assertTrue(minimalMovementList.size() >= 0);
            Assert.assertTrue(minimalMovementList.size() <= 10);
        } catch (MovementDaoException e) {
            LOG.error(e.toString());
            Assert.fail();
        }
    }

    @Test
    public void getMinimalMovementListPaginated_NegativeSpeed() {

        List<SearchValue> searchKeyValues = new ArrayList<>();
        Integer page = 1;
        Integer listSize = 10;
        String sql = "select distinct m  from  MinimalMovement m where m.speed < 0";

        try {
            List<MinimalMovement> minimalMovementList = movementDao.getMinimalMovementListPaginated(page, listSize, sql, searchKeyValues);
            Assert.assertTrue(minimalMovementList != null);
            Assert.assertTrue(minimalMovementList.size() == 0);
        } catch (MovementDaoException e) {
            LOG.error(e.toString());
            Assert.fail();
        }
    }


    @Test
    public void getMovementById() {
        try {

            MovementConnect movementConnect = createMovementConnectHelper();
            MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
            movementDao.flush();

            Movement movement = createMovementHelper();
            movement.setMovementConnect(createdMovementConnect);
            Movement createdMovement = movementDao.create(movement);
            movementDao.flush();
            Assert.assertTrue(createdMovement != null);

            Long createdMovementId = createdMovement.getId();
            Assert.assertTrue("The created id : " + createdMovementId.toString(), createdMovementId != null);

            Movement fetchedMovement = movementDao.getMovementById(createdMovementId);
            Assert.assertTrue(fetchedMovement != null);
            Assert.assertTrue(createdMovementId.equals(fetchedMovement.getId()));


        } catch (MovementDaoException e) {
            LOG.info(e.toString());
            Assert.fail(e.toString());
        }
    }

    @Test
    public void getMovementById_NULL_AS_SearchCriteria() {

        try {
            movementDao.getMovementById(null);
            // this is always an error
            Assert.fail();
        } catch (RuntimeException e) {
            String msg = e.toString().toLowerCase();
            int pos = msg.indexOf("id to load is required for loading");
            Assert.assertTrue(pos >= 0);
        }
    }

    @Test
    public void getMovementById_NonExisting_AS_SearchCriteria() {

        try {

            // assume an Id can never be negative
            Movement movement = movementDao.getMovementById(-42L);
            Assert.assertTrue(movement == null);
        } catch (RuntimeException e) {
            Assert.fail(e.toString());
        }
    }


    @Test
    public void getMovementConnectByConnectId() {

        try {


            MovementConnect movementConnect = createMovementConnectHelper();
            MovementConnect createdMovementConnect = movementDao.createMovementConnect(movementConnect);
            movementDao.flush();

            Movement movement = createMovementHelper();
            movement.setMovementConnect(createdMovementConnect);
            movementDao.create(movement);
            movementDao.flush();

            String createdMovementConnectValue = createdMovementConnect.getValue();

            MovementConnect fetchedMovementConnect = movementDao.getMovementConnectByConnectId(createdMovementConnectValue);
            Assert.assertTrue(fetchedMovementConnect != null);

            String fetchedMovementConnectValue = fetchedMovementConnect.getValue();
            Assert.assertTrue(fetchedMovementConnectValue != null);

            Assert.assertTrue(createdMovementConnectValue.equals(fetchedMovementConnectValue));

        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }


    }


    @Test
    public void getMovementList_nonpaginated_NullcheckResultsetAtUnlogicQuery() {

        // TODO getMovementList  looks unhealthy according to the number of queries it runs  (slow)

        List<SearchValue> searchValues = new ArrayList<>();
        // SearchValue searchValue = new SearchValue();
        String sql = "select m from Movement m where m.speed < -42";

        try {
            List<Movement> movements = movementDao.getMovementList(sql, searchValues);
            Assert.assertTrue(movements != null);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void getMovementList_nonpaginated_noSearchValues() {

        List<SearchValue> searchValues = new ArrayList<>();
        // SearchValue searchValue = new SearchValue();
        String sql = "select m from Movement m ";

        try {
            List<Movement> movements = movementDao.getMovementList(sql, searchValues);
            Assert.assertTrue(movements != null);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void getMovementList_nonpaginated_withSearchValues() {

        // this is not covered in the code BUT it forces that codepath to execute
        SearchValue searchValue = new SearchValue(SearchField.MOVEMENT_SPEED, "HEPP");
        List<SearchValue> searchValues = new ArrayList<>();
        searchValues.add(searchValue);

        String sql = "select m from Movement m ";

        try {
            List<Movement> movements = movementDao.getMovementList(sql, searchValues);
            Assert.assertTrue(movements != null);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void getMovementList_nonpaginated_shouldCrash() {

        SearchValue searchValue = new SearchValue(SearchField.AREA, "HEPP");
        List<SearchValue> searchValues = new ArrayList<>();
        searchValues.add(searchValue);

        String sql = "select m from Movement m ";

        try {
            List<Movement> movements = movementDao.getMovementList(sql, searchValues);
            Assert.assertTrue(movements != null);
        } catch (MovementDaoException e) {
            String msg = e.toString().toLowerCase();
            int pos = msg.indexOf("error when getting list");
            Assert.assertTrue(pos >= 0);
        }
    }


    @Test
    public void getMovementList_NumberOfReports_0_noSearchValue() {

        // TODO getMovementList  looks unhealthy according to the number of queries it runs  (slow)

        List<SearchValue> searchValues = new ArrayList<>();
        // SearchValue searchValue = new SearchValue();
        String sql = "select m from Movement m where m.speed < -42";

        try {
            List<Movement> movements = movementDao.getMovementList(sql, searchValues, 0);
            Assert.assertTrue(movements != null);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    //@Test
    // unstable servercode see TODO
    public void getMovementList_NumberOfReports_1_noSearchValue() {

        // TODO getMovementList  looks unhealthy according to the number of queries it runs  (slow)
        // TODO getLatestMovementsByConnectId in DAO  is  throws exception and crash the entire resultset if a lookup is empty


        List<SearchValue> searchValues = new ArrayList<>();
        // SearchValue searchValue = new SearchValue();
        String sql = "select m from Movement m where m.speed < -42";

        try {
            List<Movement> movements = movementDao.getMovementList(sql, searchValues, 1);
            Assert.assertTrue(movements != null);


        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void getMovementList_NumberOfReports_5_noSearchValue() {

        // TODO getMovementList  looks unhealthy according to the number of queries it runs  (slow)

        List<SearchValue> searchValues = new ArrayList<>();
        // SearchValue searchValue = new SearchValue();
        String sql = "select m from Movement m where m.speed < -42";

        try {
            List<Movement> movements = movementDao.getMovementList(sql, searchValues, 5);
            Assert.assertTrue(movements != null);


        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }


    @Test
    public void getMovementList_NumberOfReports_0_withSearchValue() {

        // TODO this test is maybe to optimistic since the queryparameters are given in the test OR they are manufactured in the Service-layer
        // TODO getMovementList  looks unhealthy according to the number of queries it runs  (slow)

        List<SearchValue> searchValues = new ArrayList<>();
        Date timeStamp = DateUtil.nowUTC();
        SearchValue searchValue = new SearchValue(SearchField.DATE, timeStamp.toString(), timeStamp.toString());
        searchValues.add(searchValue);
        String sql = "select m from Movement m where m.timestamp >= :fromDate and m.timestamp <= :toDate";

        try {
            List<Movement> movements = movementDao.getMovementList(sql, searchValues, 0);
            Assert.assertTrue(movements != null);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void getMovementList_NumberOfReports_1_withSearchValue() {

        // TODO this test is maybe to optimistic since the queryparameters are given in the test OR they are manufactured in the Service-layer
        // TODO getMovementList  looks unhealthy according to the number of queries it runs  (slow)

        List<SearchValue> searchValues = new ArrayList<>();
        Date timeStamp = DateUtil.nowUTC();
        SearchValue searchValue = new SearchValue(SearchField.DATE, timeStamp.toString(), timeStamp.toString());
        searchValues.add(searchValue);
        String sql = "select m from Movement m where m.timestamp >= :fromDate and m.timestamp <= :toDate";

        try {
            List<Movement> movements = movementDao.getMovementList(sql, searchValues, 1);
            Assert.assertTrue(movements != null);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void getMovementList_NumberOfReports_1_withSearchValue_Noresult() {

        // TODO this test is maybe to optimistic since the queryparameters are given in the test OR they are manufactured in the Service-layer
        // TODO getMovementList  looks unhealthy according to the number of queries it runs  (slow)

        List<SearchValue> searchValues = new ArrayList<>();
        Date timeStamp = DateUtil.nowUTC();
        SearchValue searchValue = new SearchValue(SearchField.DATE, timeStamp.toString(), timeStamp.toString());
        searchValues.add(searchValue);
        String sql = "select m from Movement m where m.timestamp >= :fromDate and m.timestamp <= :toDate and m.speed = -42";

        try {
            List<Movement> movements = movementDao.getMovementList(sql, searchValues, 1);
            Assert.assertTrue(movements != null);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }


    @Test
    public void getMovementList_NumberOfReports_5_withSearchValue() {

        // TODO this test is maybe to optimistic since the queryparameters are given in the test OR they are manufactured in the Service-layer
        // TODO getMovementList  looks unhealthy according to the number of queries it runs  (slow)

        List<SearchValue> searchValues = new ArrayList<>();
        Date timeStamp = DateUtil.nowUTC();
        SearchValue searchValue = new SearchValue(SearchField.DATE, timeStamp.toString(), timeStamp.toString());
        searchValues.add(searchValue);
        String sql = "select m from Movement m where m.timestamp >= :fromDate and m.timestamp <= :toDate";

        try {
            List<Movement> movements = movementDao.getMovementList(sql, searchValues, 5);
            Assert.assertTrue(movements != null);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }


    // TODO the value of this test is very limited
    @Test
    public void getMovementListByAreaAndTimeIntervall() {

        // TODO  this one cannot be instantiated using new (probably a soap thing)
        //MovementAreaAndTimeIntervalCriteria movementAreaAndTimeIntervalCriteria = new MovementAreaAndTimeIntervalCriteria();
        // TODO getMovementListByAreaAndTimeInterval does not check for null input so it crashes

        try {
            List<Movement> movements = movementDao.getMovementListByAreaAndTimeInterval(null);
            Assert.assertTrue(movements != null);
        } catch (MovementDaoException e) {
            Assert.assertTrue(e != null);
        } catch (Exception e) {
            // TODO OK for now since it crashes on null as input
            Assert.assertTrue(e != null);
        }
    }



    // dont want to use JodaTime in tests . . .
    @Test
    public void getMovementListPaginated() {

        List<SearchValue> searchValues = new ArrayList<>();

        long currentTimeMillis = System.currentTimeMillis();

        Date d1980 = new Date(80,3,3);
        Date nowPlus10 = new Date(currentTimeMillis + 10000);
        String fromDate = DateUtil.parseUTCDateToString(d1980);
        String toDate = DateUtil.parseUTCDateToString(nowPlus10);
        SearchValue searchValue = new SearchValue(SearchField.DATE, fromDate, toDate);
        searchValues.add(searchValue);

        Integer page = 1;
        Integer listSize = 10;

        List<Movement> allMovements = getAllFromMovementHelper();
        if(allMovements.size() < listSize) listSize = allMovements.size();



        String sql = "select m from Movement m where m.timestamp >= :fromDate and m.timestamp <= :toDate";
        try {
            List<Movement> rs = movementDao.getMovementListPaginated(page, listSize, sql, searchValues);
            Assert.assertTrue(rs!= null);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void getMovementListSearchCount() {
        try {
            List<SearchValue> searchValues = new ArrayList<>();
            String sql = SearchFieldMapper.createCountSearchSql(searchValues, true);
            Long searchResult = movementDao.getMovementListSearchCount(sql,searchValues);
            Assert.assertTrue(searchResult != null);
            Assert.assertTrue(searchResult >= 0);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        } catch (ParseException e) {
            Assert.fail(e.toString());
        } catch (java.text.ParseException e) {
            Assert.fail(e.toString());
        } catch (SearchMapperException e) {
            Assert.fail(e.toString());
        }
    }
    @Test
    public void getMovementListSearchCount_searchValueNull() {

        // TODO setTypedQueryMovementParams crashes on null as input
        try {
            String sql = SearchFieldMapper.createCountSearchSql(null, true);
            Long searchResult = movementDao.getMovementListSearchCount(sql,null);
            Assert.assertTrue(searchResult != null);
            Assert.assertTrue(searchResult >= 0);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        } catch (ParseException e) {
            Assert.fail(e.toString());
        } catch (java.text.ParseException e) {
            Assert.fail(e.toString());
        } catch (SearchMapperException e) {
            Assert.fail(e.toString());
        } catch (Exception e) {
            Assert.assertTrue("setTypedQueryMovementParams gives Exception at null input", e != null);
        }
    }

    @Test
    public void getUnprocessedMovementIds() {

        try {
            List<Long> unprocessedMovementIds = movementDao.getUnprocessedMovementIds();
            Assert.assertTrue(unprocessedMovementIds != null);
        } catch (RuntimeException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void getUnprocessedMovements() {

        try {
            List<Movement> unprocessedMovements = movementDao.getUnprocessedMovements();
            Assert.assertTrue(unprocessedMovements != null);
        } catch (RuntimeException e) {
            Assert.fail(e.toString());
        }
    }


    @Test
    public void merge() {

        try {
            movementDao.merge(null);
            Assert.fail("If we reaches this point hibernate does not work properly");
        } catch (MovementDaoException e) {
            Assert.assertTrue("We got an exception and that is excpected", true);
        }
    }

    @Test
    public void persist() {
        try {
            movementDao.persist(null);
            Assert.fail("If we reaches this point hibernate does not work properly");
        } catch (MovementDaoException e) {
            Assert.assertTrue("We got an exception and that is excpected", true);
        }

    }

    @Test
    public void upsertLatestMovementOnExisting() {


        MovementConnect movementConnect = createMovementConnectHelper();
        MovementConnect createdMovementConnect = null;
        try {
            createdMovementConnect = movementDao.createMovementConnect(movementConnect);
            movementDao.flush();

            Movement movement = createMovementHelper();
            movement.setMovementConnect(createdMovementConnect);
            Movement createdMovement = movementDao.create(movement);
            movementDao.flush();

            Long createdMovementId = createdMovement.getId();

            // the upsert creates one if it is not there
            movementDao.upsertLatestMovement(createdMovement, createdMovementConnect);
            movementDao.flush();


            Boolean found = false;
            List<LatestMovement> all = movementDao.getLatestMovements(10000);

            for (LatestMovement latesMovement : all) {
                Movement movementFromLatestMovement = latesMovement.getMovement();
                Long movementFromLatestMovementId = movementFromLatestMovement.getId();
                if (movementFromLatestMovementId.equals(createdMovementId)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(found);
        } catch (MovementDaoException e) {
            LOG.error(e.toString());
            Assert.fail();
        }


    }

    @Test
    public void upsertLatestMovementOnNonExisting() {


        // TODO
        // upsertLatestMovement in the DAO is actually to optimistic on indata
        // since it assumes that an incoming Movement actually exists in DB
        // if it doesnt and there is no latestmovent - it tries to persist an
        // nonExisting movement in latestMovement


        MovementConnect movementConnect = createMovementConnectHelper();
        MovementConnect createdMovementConnect = null;
        try {
            createdMovementConnect = movementDao.createMovementConnect(movementConnect);
            movementDao.flush();

            Movement DOESNOT_EXIST_IN_DB = createMovementHelper();
            DOESNOT_EXIST_IN_DB.setMovementConnect(createdMovementConnect);

            Long createdMovementId = DOESNOT_EXIST_IN_DB.getId();

            // the upser creates one if it is not there
            movementDao.upsertLatestMovement(DOESNOT_EXIST_IN_DB, createdMovementConnect);
            movementDao.flush();


            Boolean found = false;
            List<LatestMovement> all = movementDao.getLatestMovements(10000);

            for (LatestMovement latesMovement : all) {
                Movement movementFromLatestMovement = latesMovement.getMovement();
                Long movementFromLatestMovementId = movementFromLatestMovement.getId();
                if (movementFromLatestMovementId.equals(createdMovementId)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(!found);
        } catch (MovementDaoException e) {
            LOG.error(e.toString());
            Assert.fail();
        }


    }


    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/

    /** This one always creates on the same coordinates
     *
     * @return
     */
    private Movement createMovementHelper() {

        // delegaate to generic
        double longitude = 9.140625D;
        double latitude = 57.683804D;
        return createMovementHelper(longitude, latitude);
    }

    /** this one supports tampering with longitude latitude to simulate real movements
     *
     * @param longitude
     * @param latitude
     * @return
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

        Assert.assertTrue(movement.getId() == null);

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

        List<SearchValue> searchValues = new ArrayList<>();
        // SearchValue searchValue = new SearchValue();
        String sql = "select m from Movement m ";

        try {
            List<Movement> movements = movementDao.getMovementList(sql, searchValues);
           return movements;
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
        return new ArrayList<>();
    }


}
