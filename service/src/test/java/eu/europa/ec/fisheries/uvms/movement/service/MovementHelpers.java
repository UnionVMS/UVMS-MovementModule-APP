package eu.europa.ec.fisheries.uvms.movement.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import com.peertopark.java.geocalc.Coordinate;
import com.peertopark.java.geocalc.DegreeCoordinate;
import com.peertopark.java.geocalc.EarthCalc;
import com.peertopark.java.geocalc.Point;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceRuntimeException;

public class MovementHelpers {

    private final MovementBatchModelBean movementBatchModelBean;

    private Random rnd = new Random();

    public MovementHelpers(MovementBatchModelBean movementBatchModelBean) {
        this.movementBatchModelBean = movementBatchModelBean;
    }

    /******************************************************************************************************************
     *  helpers
     *****************************************************************************************************************/

    public Movement createMovement(double longitude, double latitude, String connectId, String userName,
                                   Instant positionTime) throws MovementServiceException {

        try {
            Movement movement = MockData.createMovement(longitude, latitude, connectId, 0, userName);
            movement.setTimestamp(positionTime);
            movement = movementBatchModelBean.createMovement(movement);
            return movement;
        } catch (MovementServiceRuntimeException e) {
            throw new MovementServiceException("Movement Connect missing", e, ErrorCode.MISSING_MOVEMENT_CONNECT_ERROR);
        }

    }

    private Movement createMovement(LatLong latlong,  String connectId, String userName, Instant positionTime) throws MovementServiceException {

        try {
            Movement movement = MockData.createMovement(latlong,  connectId, userName);
            movement.setTimestamp(positionTime);
            return movementBatchModelBean.createMovement(movement);
        } catch (MovementServiceRuntimeException e) {
            throw new MovementServiceException("Movement Connect missing", e, ErrorCode.MISSING_MOVEMENT_CONNECT_ERROR);
        }
    }

    // create l coordinates for well known routes. Collections.shuffle(route);

    /**
     *
     * @param order  1 = as created  first EXIT_PORT then GAP  all in time_order
     *               2 = reversed
     *               3 = randomly ordered
     * @param numberPositions
     * @param connectId
     * @return
     *
     * @throws MovementServiceException
     */
    public List<Movement> createVarbergGrenaMovements(int order, int numberPositions, String connectId) throws MovementServiceException {
        List<LatLong> positions = createRuttVarbergGrena(numberPositions);
        return getMovements(order, connectId, positions);
    }

    public List<Movement> createFishingTourVarberg(int order, String connectId) throws MovementServiceException {
        List<LatLong> positions = createRuttSmallFishingTourFromVarberg();
        return getMovements(order, connectId, positions);
    }

    private List<Movement> getMovements(int order, String connectId, List<LatLong> positions) throws MovementServiceException {
        List<Movement> createdRoute = new ArrayList<>();
        String userName = "TEST";

        Instant timeStamp = Instant.now();
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
            Movement movement = createMovement(position, connectId,userName + "_" + String.valueOf(loopCount), timeStamp);
            timeStamp = timeStamp.plusMillis(timeDelta);
            createdRoute.add(movement);
        }
        return createdRoute;
    }

    private List<LatLong> calculateReportedDataForRoute(List<LatLong> route){

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
            route.get(i - 1).distance = distance;
            route.get(i - 1).speed= calcSpeed(previousPosition, currentPosition);

            if(i < n){
                previousPosition = currentPosition;
            }
            i++;
        }
        double bearing = bearing(previousPosition, currentPosition);
        route.get(i - 1).distance = distance(previousPosition, currentPosition);
        route.get(i - 1).bearing = bearing;
        //double speed = calcSpeed(previousPosition, currentPosition);
        route.get(i - 1).speed = 0d;
        return route;
    }

    private List<LatLong> createRuttVarbergGrena(int numberPositions) {

        int movementTimeDeltaInMillis = 30000;
        List<LatLong> rutt = new ArrayList<>();
        Instant ts = Instant.now();

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
            rutt.add(new LatLong(latitude, longitude, ts.plusMillis(movementTimeDeltaInMillis)));
        }

        // now when we have a route we must calculate heading and speed
        rutt = calculateReportedDataForRoute(rutt);

		if (numberPositions == -1) {
            return rutt;
        } else {
            return rutt.subList(0, numberPositions);
        }
    }

    private List<LatLong> createRuttSmallFishingTourFromVarberg() {

        int movementTimeDeltaInMillis = 30000;
        List<LatLong> rutt = new ArrayList<>();
        Instant ts = Instant.now();

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
            rutt.add(new LatLong(latitude, longitude, ts.plusMillis(movementTimeDeltaInMillis)));
        }
        // do some fishing
        for (int i = 0; i < 15; i++) {
            latitude = latitude - 0.001;
            longitude = longitude - 0.002;
            rutt.add(new LatLong(latitude, longitude, ts.plusMillis(movementTimeDeltaInMillis)));
        }
        // go home
        int n = rutt.size();
        List<LatLong> ruttHome = new ArrayList<>();
        for(int i = n - 1 ; i > 0 ; i--){
            LatLong wrk = rutt.get(i);
            ruttHome.add(new LatLong(wrk.latitude + 0.001, wrk.longitude, ts.plusMillis(movementTimeDeltaInMillis)));
        }

        rutt.addAll(ruttHome);
        rutt = calculateReportedDataForRoute(rutt);
        return rutt;
    }

    private Double bearing(LatLong src, LatLong dst) {

        Coordinate latFrom = new DegreeCoordinate(src.latitude);
        Coordinate lngFrom = new DegreeCoordinate(src.longitude);
        Point from = new Point(latFrom, lngFrom);

        Coordinate latTo = new DegreeCoordinate(dst.latitude);
        Coordinate lngTo = new DegreeCoordinate(dst.longitude);
        Point to = new Point(latTo, lngTo);

        return EarthCalc.getBearing(from, to);
    }

    private Double distance(LatLong src, LatLong dst) {

        Coordinate latFrom = new DegreeCoordinate(src.latitude);
        Coordinate lngFrom = new DegreeCoordinate(src.longitude);
        Point from = new Point(latFrom, lngFrom);

        Coordinate latTo = new DegreeCoordinate(dst.latitude);
        Coordinate lngTo = new DegreeCoordinate(dst.longitude);
        Point to = new Point(latTo, lngTo);

        return EarthCalc.getDistance(from, to);
    }

    private double calcSpeed(LatLong src, LatLong dst) {

        try {
            if (src.positionTime == null)
                return 0;
            if (dst.positionTime == null)
                return 0;

            // distance to next
            double distanceM = src.distance;

            double durationms = (double) Math.abs(dst.positionTime.toEpochMilli() - src.positionTime.toEpochMilli());
            double durationSecs = durationms / 1000;
            double speedMeterPerSecond = (distanceM / durationSecs);
            double speedMPerHour = speedMeterPerSecond * 3600;
            return speedMPerHour / 1000;
        } catch (RuntimeException e) {
            return 0.0;
        }
    }
    
    public static String getRandomIntegers(int length) {
        return new Random()
                .ints(0,9)
                .mapToObj(i -> String.valueOf(i))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
