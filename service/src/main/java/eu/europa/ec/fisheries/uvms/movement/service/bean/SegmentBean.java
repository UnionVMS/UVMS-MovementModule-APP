package eu.europa.ec.fisheries.uvms.movement.service.bean;

import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.clients.SpatialRestClient;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.SegmentCalculations;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.service.util.CalculationUtil;
import eu.europa.ec.fisheries.uvms.movement.service.util.GeometryUtil;

@Stateless
public class SegmentBean {

    @Inject
    private MovementDao dao;

    @Inject
    SpatialRestClient spatialClient;

    public void newSegment(Movement previousMovement, Movement currentMovement) {
        Segment segment = createSegment(previousMovement, currentMovement);
        Track track = upsertTrack(previousMovement.getTrack(), segment, currentMovement);
        if (previousMovement.getTrack() == null) {
            previousMovement.setTrack(track);
            currentMovement.setTrack(track);
        }
        dao.createSegment(segment);
    }

    public void addMovementBeforeFirst(Movement firstMovement, Movement currentMovement) {
        Segment segment = createSegment(currentMovement, firstMovement);
        Track track = upsertTrack(firstMovement.getTrack(), segment, currentMovement);
        if (firstMovement.getTrack() == null) {
            firstMovement.setTrack(track);
        }
        currentMovement.setTrack(track);
        dao.createSegment(segment);
    }

    public void splitSegment(Movement previousMovement, Movement currentMovement) {
        if (currentMovement == null || previousMovement == null) {
            throw new IllegalArgumentException("Error when splitting segment, currentPosition and previousPosition cannot be null");
        }
        
        Segment theSegmentToBeBroken = previousMovement.getToSegment();
        
        if (theSegmentToBeBroken == null) {
            throw new IllegalArgumentException("PREVIOUS MOVEMENT MUST HAVE A SEGMENT BUT IT DOESN'T, SOME LOGICAL ERROR HAS OCCURRED");
        }
        
        Movement oldToMovement = theSegmentToBeBroken.getToMovement();
        
        //calculating and setting new segment values
        populateSegment(theSegmentToBeBroken, previousMovement, currentMovement);
        
        theSegmentToBeBroken = dao.updateSegment(theSegmentToBeBroken);
        
        Segment segment = createSegment(currentMovement, oldToMovement);
        
        upsertTrack(theSegmentToBeBroken.getTrack(), segment, currentMovement);
        dao.createSegment(segment);
    }
    
    protected Segment createSegment(Movement fromMovement, Movement toMovement) {
        if (toMovement == null || fromMovement == null) {
            throw new IllegalArgumentException("ERROR when mapping to Segment entity: currentPosition AND previous Position cannot be null");
        }
        Segment segment = new Segment();
        populateSegment(segment, fromMovement, toMovement);
        return segment;
    }
    
    private void populateSegment(Segment segment, Movement fromMovement, Movement toMovement) {
        //calculations for segment
        SegmentCalculations positionCalculations = CalculationUtil.getPositionCalculations(fromMovement, toMovement);

        SegmentCategoryType segCat = spatialClient.getSegmentCategoryType(fromMovement, toMovement);

        segment.setSegmentCategory(segCat);

        segment.setDistance(positionCalculations.getDistanceBetweenPoints());
        segment.setSpeedOverGround(positionCalculations.getAvgSpeed());
        segment.setCourseOverGround(positionCalculations.getCourse());
        segment.setDuration(positionCalculations.getDurationBetweenPoints());

        segment.setFromMovement(fromMovement);
        segment.setToMovement(toMovement);

        segment.setUpdated(DateUtil.nowUTC());
        segment.setUpdatedBy("UVMS");

        LineString segmentLineString = GeometryUtil.getLineStringFromMovements(fromMovement, toMovement);
        segment.setLocation(segmentLineString);
        
        fromMovement.setToSegment(segment);
        toMovement.setFromSegment(segment);
    }

    protected Track upsertTrack(Track track, Segment segment, Movement newMovement){
        if (track == null) {        //if there is no tracks
        	return createNewTrack(segment);
        } else {
            switch (segment.getSegmentCategory()) {     //if a segment is is an area transition out of a port, then create a new track, else add to the old one
                case EXIT_PORT:
                	return createNewTrack(segment);
                case GAP:
                case JUMP:
                case IN_PORT:
                case ENTER_PORT:
                case NULL_DUR:
                case ANCHORED:
                case LOW_SPEED:
                case OTHER:
                    updateTrack(track, newMovement, segment);
                    break;
                default:
                    throw new IllegalArgumentException("SEGMENT CATEGORY " + segment.getSegmentCategory().name() + " IS NOT MAPPED");
            }
        }
        return track;
    }

    protected void updateTrack(Track track, Movement newMovement, Segment segment)  {
        segment.setTrack(track);
        newMovement.setTrack(track);
        //add segments values to those of the track
        double calculatedDistance = track.getDistance() + segment.getDistance();
        track.setDistance(calculatedDistance);
        double calculatedDurationInSeconds = track.getDuration() + segment.getDuration();
        track.setDuration(calculatedDurationInSeconds);

        if (!segment.getSegmentCategory().equals(SegmentCategoryType.ENTER_PORT) || !segment.getSegmentCategory().equals(SegmentCategoryType.IN_PORT)) {     //if we have not entered a port or are in a port, add to the total amount of time
            double totalTimeAtSea = track.getTotalTimeAtSea();                                                                                               //this makes it so that the last segment of a track is not counted towards the total time of a track
            track.setTotalTimeAtSea(totalTimeAtSea + segment.getDuration());
        }
    }

    protected Track createNewTrack(Segment segment) {
        Track track = new Track();
        track.setDistance(segment.getDistance());
        track.setDuration(segment.getDuration());
        track.setUpdated(DateUtil.nowUTC());
        track.setUpdatedBy("UVMS");
        segment.setTrack(track);
        return track;
    }
}
