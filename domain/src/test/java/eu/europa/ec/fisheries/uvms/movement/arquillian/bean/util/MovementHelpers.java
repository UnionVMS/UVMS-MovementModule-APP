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

    public Movement createMovement(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId, String userName, Date positionTime, double bearing) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        MovementType movementType = testUtil.createMovementType(longitude, latitude, altitude, segmentCategoryType, connectId, bearing);
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

        long timeDelta = 300000;

        switch (order) {
            case 2:
                Collections.reverse(positions);
                timeDelta = -300000;
                break;
            case 3:
                Collections.shuffle(positions);
                break;
        }


        for(LatLong position : positions){
            loopCount++;
            Movement movement = createMovement(position.longitude, position.latitude, 2,segmentCategoryType, connectId, userName + "_" + String.valueOf(loopCount), new Date(timeStamp), position.bearing );
            if(firstLoop){
                firstLoop = false;
                segmentCategoryType = SegmentCategoryType.GAP;
            }
            timeStamp += timeDelta;
            createdRoute.add(movement);
        }

        return createdRoute;
    }


    List<LatLong> calculateReportedDataForRoute(List<LatLong>  route){

        LatLong previousPosition = null;
        LatLong currentPosition = null;
        int i = 0;
        int n = route.size();

        while(i < n){

            currentPosition = route.get(i);
            if(i == 0){
                previousPosition = route.get(i);
                i++;
                continue;
            }

            double bearing = bearingInDegrees(previousPosition, currentPosition);
            route.get(i - 1).bearing = bearing;
            if(i < n){
                previousPosition = currentPosition;
            }
            i++;
        }
        double bearing = bearingInDegrees(previousPosition, currentPosition);
        route.get(i - 1).bearing = bearing;



        return route;


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


        // now when we have a route we must calculate heading and speed
        rutt = calculateReportedDataForRoute(rutt);

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

        // now when we have a route we must calculate heading and speed
        rutt = calculateReportedDataForRoute(rutt);

        if (numberPositions == -1) {
            return rutt;
        } else {
            return rutt.subList(0, numberPositions);
        }
    }

    private Date getDate(Long millis) {
        return new Date(millis);
    }

    private  double bearingInRadians(LatLong src, LatLong dst) {
        double srcLat = Math.toRadians(src.latitude);
        double dstLat = Math.toRadians(dst.latitude);
        double dLng = Math.toRadians(dst.longitude - src.longitude);

        return Math.atan2(Math.sin(dLng) * Math.cos(dstLat),
                Math.cos(srcLat) * Math.sin(dstLat) -
                        Math.sin(srcLat) * Math.cos(dstLat) * Math.cos(dLng));
    }

    private  double bearingInDegrees(LatLong src, LatLong dst) {
        return Math.toDegrees((bearingInRadians(src, dst) + Math.PI) % Math.PI);
    }




}
