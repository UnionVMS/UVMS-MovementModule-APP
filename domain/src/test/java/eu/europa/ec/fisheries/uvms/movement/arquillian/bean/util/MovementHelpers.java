package eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util;

import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import org.junit.Assert;

import javax.persistence.EntityManager;
import java.util.*;

public class MovementHelpers {


    private TestUtil testUtil = new TestUtil();

    private final MovementBatchModelBean movementBatchModelBean;

    private final MovementDao movementDao;

    private final EntityManager em;

    public MovementHelpers(EntityManager em, MovementBatchModelBean movementBatchModelBean, MovementDao movementDao) {
        this.em = em;
        this.movementBatchModelBean = movementBatchModelBean;
        this.movementDao = movementDao;
    }


    /*****************************************************************************************************************************************************
     *  helpers
     *****************************************************************************************************************************************************/

    // old version
    public  Movement createMovement(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        return createMovement(longitude, latitude, altitude, segmentCategoryType, connectId, "TEST");
    }

    // added possibility to specify user for easier debug
    public Movement createMovement(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId, String userName) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        MovementType movementType = testUtil.createMovementType(longitude, latitude, altitude, segmentCategoryType, connectId);
        movementType = movementBatchModelBean.createMovement(movementType, userName);
        em.flush();
        Assert.assertNotNull(movementType.getConnectId());
        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getConnectId());
        List<Movement> movementList = movementConnect.getMovementList();
        Assert.assertNotNull(movementList);
        return movementList.get(movementList.size() - 1);
    }

    /* positiontime is imortant */
    public Movement createMovement(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId, String userName, Date positionTime) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        MovementType movementType = testUtil.createMovementType(longitude, latitude, altitude, segmentCategoryType, connectId);
        movementType.setPositionTime(positionTime);
        movementType = movementBatchModelBean.createMovement(movementType, userName);
        em.flush();
        Assert.assertNotNull(movementType.getConnectId());
        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getConnectId());
        List<Movement> movementList = movementConnect.getMovementList();
        Assert.assertNotNull(movementList);
        return movementList.get(movementList.size() - 1);
    }


    /**
     * create l coordinates for well known routes
     Collections.shuffle(route);

     */

    /**
     *
     * @param order  1 = as created  first EXIT_PORT then GAP  all in time_order
     *               2 = reversed
     *               3 = randomly ordered
     * @param numberPositions
     * @param connectId
     * @return
     * @throws MovementDuplicateException
     * @throws MovementDaoException
     * @throws MovementModelException
     */
    public List<Movement> createVarbergGrenaMovements(int order, int numberPositions, String connectId) throws MovementDuplicateException, MovementDaoException, MovementModelException {

        List<LatLong> positions = createRuttVarbergGrena(numberPositions);
        List<Movement> createdRoute = new ArrayList<>();
        String userName = "TEST";

        boolean firstLoop = true;
        SegmentCategoryType segmentCategoryType = SegmentCategoryType.EXIT_PORT;
        long timeStamp = System.currentTimeMillis();
        int loopCount = 0;

        for(LatLong position : positions){
            loopCount++;
            Movement movement = createMovement(position.longitude, position.latitude, 2,segmentCategoryType, connectId, userName + "_" + String.valueOf(loopCount), new Date(timeStamp) );
            if(firstLoop){
                firstLoop = false;
                segmentCategoryType = SegmentCategoryType.GAP;
            }
            timeStamp += 300000;
            createdRoute.add(movement);
        }

        switch (order) {
            case 2:
                List<Movement> shallowCopy = createdRoute.subList(0, createdRoute.size());
                Collections.reverse(shallowCopy);
                return shallowCopy;
            case 3:
                Collections.shuffle(createdRoute);
                break;
        }
        return createdRoute;
    }





    private List<LatLong> createRuttVarbergGrena(int numberPositions) {

        int movementTimeDeltaInMillis = 30000;
        List<LatLong> rutt = new ArrayList<>();
        long ts = System.currentTimeMillis();


        double latitude = 57.110 ;
        double longitude = 12.244  ;

        double END_LATITUDE = 56.408;
        double END_LONGITUDE = 10.926;

		while (true) {

            if (latitude >= END_LATITUDE)
                latitude = latitude - 0.03;
            if (longitude >= END_LONGITUDE)
                longitude = longitude - 0.03;
            if (latitude < END_LATITUDE && longitude < END_LONGITUDE)
                break;
            rutt.add(new LatLong(latitude, longitude, getDate(ts += movementTimeDeltaInMillis)));
        }

		if (numberPositions == -1) {
            return rutt;
        } else {
            return rutt.subList(0, numberPositions);
        }
    }

    private List<LatLong> createRuttCobhNewYork(int numberPositions) {

        int movementTimeDeltaInMillis = 30000;
        List<LatLong> rutt = new ArrayList<>();
        long ts = System.currentTimeMillis();

        double latitude = 51.844;
        double longitude = -8.311;


        double END_LATITUDE = 40.313;
        double END_LONGITUDE = -73.740;

        while (true) {

            if (latitude >= END_LATITUDE)
                latitude = latitude - 0.5;
            if (longitude >= END_LONGITUDE)
                longitude = longitude - 0.5;
            if (latitude < END_LATITUDE && longitude < END_LONGITUDE)
                break;
            rutt.add(new LatLong(latitude, longitude, getDate(ts += movementTimeDeltaInMillis)));
        }

        if (numberPositions == -1) {
            return rutt;
        } else {
            return rutt.subList(0, numberPositions);
        }
    }

    private Date getDate(Long millis) {
        return new Date(millis);
    }





}
