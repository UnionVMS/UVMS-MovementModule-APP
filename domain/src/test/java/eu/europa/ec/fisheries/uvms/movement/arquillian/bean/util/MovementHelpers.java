package eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util;

import com.peertopark.java.geocalc.Coordinate;
import com.peertopark.java.geocalc.DegreeCoordinate;
import com.peertopark.java.geocalc.EarthCalc;
import com.peertopark.java.geocalc.Point;
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
import java.util.concurrent.TimeUnit;

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

    /* positiontime is imortant */
    public Movement createMovement(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId, String userName, Date positionTime) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        MovementType movementType = testUtil.createMovementType(longitude, latitude, altitude, segmentCategoryType, connectId, 0);
        movementType.setPositionTime(positionTime);
        movementType = movementBatchModelBean.createMovement(movementType, userName);
        em.flush();
        Assert.assertNotNull(movementType.getConnectId());
        MovementConnect movementConnect = movementDao.getMovementConnectByConnectId(movementType.getConnectId());
        List<Movement> movementList = movementConnect.getMovementList();
        Assert.assertNotNull(movementList);
        return movementList.get(movementList.size() - 1);
    }

    public Movement createMovement(LatLong latlong,  double altitude, SegmentCategoryType segmentCategoryType, String connectId, String userName, Date positionTime) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        MovementType movementType = testUtil.createMovementType(latlong,  segmentCategoryType, connectId, altitude);
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
            Movement movement = createMovement(position, 2,segmentCategoryType, connectId, userName + "_" + String.valueOf(loopCount), new Date(timeStamp));
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

            double bearing = bearing(previousPosition, currentPosition);
            double distance = distance(previousPosition, currentPosition);
            route.get(i - 1).bearing = bearing;
            route.get(i - 1).distance= distance;
            double speed = calcSpeed(previousPosition, currentPosition);
            route.get(i - 1).speed= speed;

            if(i < n){
                previousPosition = currentPosition;
            }
            i++;
        }
        double bearing = bearing(previousPosition, currentPosition);
        double distance = distance(previousPosition, currentPosition);
        route.get(i - 1).distance= distance;
        route.get(i - 1).bearing = bearing;
        //double speed = calcSpeed(previousPosition, currentPosition);
        route.get(i - 1).speed= 0d;
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
                latitude = latitude - 0.003;
            if (longitude >= END_LONGITUDE)
                longitude = longitude - 0.003;
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

    private List<LatLong> createRuttCobhNewYork(int numberPositions, float distanceBetweenPositions) {

        int movementTimeDeltaInMillis = 30000;
        List<LatLong> rutt = new ArrayList<>();
        long ts = System.currentTimeMillis();

        double latitude = 51.844;
        double longitude = -8.311;


        double END_LATITUDE = 40.313;
        double END_LONGITUDE = -73.740;

        while (true) {

            if (latitude >= END_LATITUDE)
                latitude = latitude - distanceBetweenPositions;
            if (longitude >= END_LONGITUDE)
                longitude = longitude - distanceBetweenPositions;
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

    private Random rnd = new Random();
    public List<LatLong> createRuttSmallFishingTourFromVarberg() {

        int movementTimeDeltaInMillis = 30000;
        List<LatLong> rutt = new ArrayList<>();
        long ts = System.currentTimeMillis();

        double randomFactorLat = rnd.nextDouble() ;
        double randomFactorLong = rnd.nextDouble() ;

        double latitude = 57.110 + randomFactorLat;
        double longitude = 12.244 + randomFactorLong;

        // these will never be reached but still good to have to steer on
        double END_LATITUDE = 56.408;
        double END_LONGITUDE = 10.926;

        // leave the harbour
        for (int i = 0; i < 25; i++) {

            if (latitude >= END_LATITUDE)
                latitude = latitude - 0.004;
            if (longitude >= END_LONGITUDE)
                longitude = longitude - 0.004;
            if (latitude < END_LATITUDE && longitude < END_LONGITUDE)
                break;
            rutt.add(new LatLong(latitude, longitude, getDate(ts += movementTimeDeltaInMillis)));
        }
        // do some fishing
        for (int i = 0; i < 15; i++) {
            latitude = latitude - 0.001;
            longitude = longitude - 0.002;
            rutt.add(new LatLong(latitude, longitude, getDate(ts += movementTimeDeltaInMillis)));
        }
        // go home
        int n = rutt.size();
        List<LatLong> ruttHome = new ArrayList<>();
        for(int i = n - 1 ; i > 0 ; i--){
            LatLong wrk = rutt.get(i);
            ruttHome.add(new LatLong(wrk.latitude + 0.001, wrk.longitude, getDate(ts += movementTimeDeltaInMillis)));
        }

        rutt.addAll(ruttHome);
        rutt = calculateReportedDataForRoute(rutt);
        return rutt;
    }


    private Date getDate(Long millis) {
        return new Date(millis);
    }

    private Double bearing(LatLong src, LatLong dst) {

        Coordinate latFrom = new DegreeCoordinate(src.latitude);
        Coordinate lngFrom = new DegreeCoordinate(src.longitude);
        Point from = new Point(latFrom, lngFrom);

        Coordinate latTo = new DegreeCoordinate(dst.latitude);
        Coordinate lngTo = new DegreeCoordinate(dst.longitude);
        Point to = new Point(latTo, lngTo);

        double bearing = EarthCalc.getBearing(from, to); // in decimal degrees
        return bearing;

    }

    private Double distance(LatLong src, LatLong dst) {

        Coordinate latFrom = new DegreeCoordinate(src.latitude);
        Coordinate lngFrom = new DegreeCoordinate(src.longitude);
        Point from = new Point(latFrom, lngFrom);

        Coordinate latTo = new DegreeCoordinate(dst.latitude);
        Coordinate lngTo = new DegreeCoordinate(dst.longitude);
        Point to = new Point(latTo, lngTo);

        double distanceInMeters = EarthCalc.getDistance(from, to);
        return distanceInMeters;

    }

    private double calcSpeed(LatLong src, LatLong dst) {

        try {

            if (src.positionTime == null)
                return 0;
            if (dst.positionTime == null)
                return 0;

            // distance to next
            double distanceM = src.distance;

            double durationms = (double) Math.abs(dst.positionTime.getTime() - src.positionTime.getTime());
            double durationSecs = durationms / 1000;
            double speedMeterPerSecond = (distanceM / durationSecs);
            double speedMPerHour = speedMeterPerSecond * 3600;
            return speedMPerHour / 1000;
        } catch (RuntimeException e) {
            return 0.0;
        }
    }


    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }



    public List<Movement> createFishingTourVarberg(int order,  String connectId) throws MovementDuplicateException, MovementDaoException, MovementModelException {

        List<LatLong> positions = createRuttSmallFishingTourFromVarberg();
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
            Movement movement = createMovement(position, 2,segmentCategoryType, connectId, userName + "_" + String.valueOf(loopCount), new Date(timeStamp));
            if(firstLoop){
                firstLoop = false;
                segmentCategoryType = SegmentCategoryType.GAP;
            }
            timeStamp += timeDelta;
            createdRoute.add(movement);
        }

        return createdRoute;
    }




}