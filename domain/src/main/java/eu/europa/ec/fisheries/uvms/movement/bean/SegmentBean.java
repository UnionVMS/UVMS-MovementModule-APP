package eu.europa.ec.fisheries.uvms.movement.bean;

import com.vividsolutions.jts.geom.LineString;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.dto.SegmentCalculations;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.util.CalculationUtil;
import eu.europa.ec.fisheries.uvms.movement.util.GeometryUtil;
import eu.europa.ec.fisheries.uvms.movement.util.SegmentCalculationUtil;
import java.util.ArrayList;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by andreasw on 2017-03-08.
 */
@LocalBean
@Stateless
public class SegmentBean {

    private static final Logger LOG = LoggerFactory.getLogger(SegmentBean.class);

    @EJB
    private MovementDao dao;

    public Segment createSegmentAndTrack(Movement fromMovement, Movement toMovement) throws MovementDomainException {
        Segment segment = createSegment(fromMovement, toMovement);
        Track track = upsertTrack(fromMovement.getTrack(), segment, toMovement);
        fromMovement.setTrack(track);
        toMovement.setTrack(track);
        return segment;
    }

    /**
     *
     * @param fromMovement
     * @param toMovement
     * @return
     * @throws MovementDomainException
     */
    public Segment createSegment(Movement fromMovement, Movement toMovement) throws MovementDomainException {
        Segment segment = new Segment();

        if (toMovement == null && fromMovement == null) {
            LOG.error("[ ERROR when mapping to Segment entity: currentPosition AND previous Position cannot be null <createSegment> ]");
            throw new MovementDomainException("ERROR when mapping to Segment entity: currentPosition AND previous Position cannot be null", ErrorCode.DAO_MAPPING_ERROR);
        }
        //calculations for segment
        SegmentCalculations positionCalculations = CalculationUtil.getPositionCalculations(fromMovement, toMovement);

        SegmentCategoryType segCat = SegmentCalculationUtil.getSegmentCategoryType(positionCalculations, fromMovement, toMovement);
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

        return segment;
    }

    /**
     *
     * @param track
     * @param segment
     * @param newMovement
     * @return
     *
     * @throws MovementDomainException
     */
    public Track upsertTrack(Track track, Segment segment, Movement newMovement) throws MovementDomainException {
        if (track == null) {        //if there is no tracks
        	Track newTrack = createNewTrack(segment);
            dao.persist(newMovement);
            return newTrack;
        } else {
            switch (segment.getSegmentCategory()) {     //if a segment is is an area transition out of a port, then create a new track, else add to the old one
                case EXIT_PORT:
                	Track newTrack = createNewTrack(segment);
                    dao.persist(newMovement);
                    return newTrack;
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
                    throw new MovementDomainException("SEGMENT CATEGORY " + segment.getSegmentCategory().name() + " IS NOT MAPPED", ErrorCode.DAO_MAPPING_ERROR);
            }
        }
        return track;
    }

    /**
     *
     * @param track
     * @param newMovement
     * @param segment
     */
    public void updateTrack(Track track, Movement newMovement, Segment segment)  {
        //TODO: This needs som serious overlooking
        LOG.debug("UPDATING TRACK ");

        if (track.getMovementList() == null) {
            track.setMovementList(new ArrayList<>());
        }

        //add things to each other
        track.getMovementList().add(newMovement);
        segment.setTrack(track);
        newMovement.setTrack(track);
        track.getSegmentList().add(segment);

        //add segments values to those of the track
        double calculatedDistance = track.getDistance() + segment.getDistance();
        track.setDistance(calculatedDistance);
        double calculatedDurationInSeconds = track.getDuration() + segment.getDuration();
        track.setDuration(calculatedDurationInSeconds);

        if(track.getMovementList().size() > 1) {    //if there is more then one movement, create a string of movements, aka a string of positions
            LineString updatedTrackLineString = GeometryUtil.getLineStringFromMovements(track.getMovementList());
            track.setLocation(updatedTrackLineString);
        }

        if (!segment.getSegmentCategory().equals(SegmentCategoryType.ENTER_PORT) || !segment.getSegmentCategory().equals(SegmentCategoryType.IN_PORT)) {     //if we have not entered a port or are in a port, add to the total amount of time
            double totalTimeAtSea = track.getTotalTimeAtSea();                                                                                               //this makes it so that the last segment of a track is not counted towards the total time of a track
            track.setTotalTimeAtSea(totalTimeAtSea + segment.getDuration());
        }

        //do you really need three persists here?
        dao.persist(track);
        dao.persist(segment);
        dao.persist(newMovement);
    }

    public Track createNewTrack(Segment segment) {
        LOG.debug("CREATING NEW TRACK ");

        Track track = new Track();
        track.setDistance(segment.getDistance());
        track.setDuration(segment.getDuration());
        track.setUpdated(DateUtil.nowUTC());
        track.setLocation(segment.getLocation());
        track.setUpdatedBy("UVMS");
        track.setMovementList(new ArrayList<Movement>());
        track.getMovementList().add(segment.getFromMovement());
        track.getMovementList().add(segment.getToMovement());
        track.setSegmentList(new ArrayList<Segment>());
        track.getSegmentList().add(segment);
        segment.setTrack(track);
        return track;
    }

    /**
     * @param previousMovement
     * @param currentMovement
     *
     * @throws MovementDomainException
     */
    public void splitSegment(Movement previousMovement, Movement currentMovement) throws MovementDomainException {

        Segment theSegmentToBeBroken = dao.findByFromMovement(previousMovement);

        if (theSegmentToBeBroken == null) {         //if there is no segment with the previous movement as From

            theSegmentToBeBroken = dao.findByToMovement(previousMovement);
            if (theSegmentToBeBroken == null) {     //if there also is no segment with the previous movement as To
                throw new MovementDomainException("PREVIOUS MOVEMENT MUST HAVE A SEGMENT BUT IT DOESN'T, SOME LOGICAL ERROR HAS OCCURRED",
                        ErrorCode.ILLEGAL_ARGUMENT_ERROR);
            } else {
                Segment segment = createSegment(theSegmentToBeBroken.getToMovement(), currentMovement); //create a new segment and fill it
                upsertTrack(theSegmentToBeBroken.getTrack(), segment, currentMovement);                 //connect to track
                return;
            }
        }

        // If we are here then it means that current movement is not the youngest movement there is for this connectID,
        // but that it is somewhere in the middle and that we need to rearange two segments to incorporate this new movement
        LOG.debug("Splitting segment {}", theSegmentToBeBroken.getId());

        Movement oldToMovement = theSegmentToBeBroken.getToMovement();

        if (currentMovement == null && previousMovement == null) {
            LOG.error("[ ERROR when updating Segment entity: currentPosition AND previous Position cannot be null <updateSegment> ]");
            throw new MovementDomainException("ERROR when updating Segment entity: currentPosition AND previous Position cannot be null", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        //calculating and setting new segment values
        SegmentCalculations positionCalculations = CalculationUtil.getPositionCalculations(previousMovement, currentMovement);

        SegmentCategoryType segCat = SegmentCalculationUtil.getSegmentCategoryType(positionCalculations, previousMovement, currentMovement);
        theSegmentToBeBroken.setSegmentCategory(segCat);

        theSegmentToBeBroken.setDistance(positionCalculations.getDistanceBetweenPoints());
        theSegmentToBeBroken.setSpeedOverGround(positionCalculations.getAvgSpeed());
        theSegmentToBeBroken.setCourseOverGround(positionCalculations.getCourse());
        theSegmentToBeBroken.setDuration(positionCalculations.getDurationBetweenPoints());

        theSegmentToBeBroken.setUpdated(DateUtil.nowUTC());
        theSegmentToBeBroken.setUpdatedBy("UVMS");

        theSegmentToBeBroken.setFromMovement(previousMovement);
        theSegmentToBeBroken.setToMovement(currentMovement);
        
        previousMovement.setToSegment(theSegmentToBeBroken);
        currentMovement.setFromSegment(theSegmentToBeBroken);

        LineString segmentLineString = GeometryUtil.getLineStringFromMovements(previousMovement, currentMovement);
        theSegmentToBeBroken.setLocation(segmentLineString);

        theSegmentToBeBroken = dao.persist(theSegmentToBeBroken);
        dao.flush();

        //and creating a new one
        Segment segment = createSegment(currentMovement, oldToMovement);
        segment.setTrack(theSegmentToBeBroken.getTrack());
        dao.persist(segment);

        LOG.debug("OLD SEGMENT FROM {} TO {}", theSegmentToBeBroken.getFromMovement().getId(), theSegmentToBeBroken.getToMovement().getId());
        LOG.debug("NEW SEGMENT FROM {} TO {}", segment.getFromMovement().getId(), segment.getToMovement().getId());

        upsertTrack(theSegmentToBeBroken.getTrack(), segment, currentMovement);
    }

    /**
     *
     * @param firstMovement
     * @param currentMovement
     *
     * @throws MovementDomainException
     */
    public void addMovementBeforeFirst(Movement firstMovement, Movement currentMovement) throws MovementDomainException {
        Segment segment = firstMovement.getFromSegment();
        if (segment == null) {
            segment = createSegment(currentMovement, firstMovement);
        } else {
            segment.setFromMovement(currentMovement);
        }
        Track track = upsertTrack(firstMovement.getTrack(), segment, currentMovement);
        if (firstMovement.getTrack() == null) {
            firstMovement.setTrack(track);
            dao.persist(firstMovement);
        }
    }
}
