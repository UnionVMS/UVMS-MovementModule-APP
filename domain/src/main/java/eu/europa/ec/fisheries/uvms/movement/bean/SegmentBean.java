package eu.europa.ec.fisheries.uvms.movement.bean;

import com.vividsolutions.jts.geom.LineString;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDaoBean;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.dto.SegmentCalculations;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.util.CalculationUtil;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.util.GeometryUtil;
import eu.europa.ec.fisheries.uvms.movement.util.SegmentCalculationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.ArrayList;

/**
 * Created by andreasw on 2017-03-08.
 */
@LocalBean
@Stateless
public class SegmentBean {

    final static Logger LOG = LoggerFactory.getLogger(SegmentBean.class);


    @EJB
    MovementDaoBean dao;

    /**
     *
     * @param fromMovement
     * @param toMovement
     * @throws MovementDaoException
     * @throws GeometryUtilException
     * @throws MovementDaoMappingException
     */
    public Segment createSegmentAndTrack(Movement fromMovement, Movement toMovement) throws MovementDaoException, GeometryUtilException, MovementDaoMappingException {
        Segment segment = createSegment(fromMovement, toMovement);
        Track track = upsertTrack(fromMovement.getTrack(), segment, toMovement);
        fromMovement.setTrack(track);
        toMovement.setTrack(track);
        dao.persist(fromMovement);
        return segment;
    }

    /**
     *
     * @param fromMovement
     * @param toMovement
     * @return
     * @throws GeometryUtilException
     * @throws MovementDaoMappingException
     */
    public Segment createSegment(Movement fromMovement, Movement toMovement) throws GeometryUtilException, MovementDaoMappingException {
        Segment segment = new Segment();

        if (toMovement == null && fromMovement == null) {
            LOG.error("[ ERROR when mapping to Segment entity: currentPosition AND previous Position cannot be null <createSegment> ]");
            throw new MovementDaoMappingException("ERROR when mapping to Segment entity: currentPosition AND previous Position cannot be null");
        }

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

        LineString segmentLineString = GeometryUtil.getLineStringFromMovments(fromMovement, toMovement);
        segment.setLocation(segmentLineString);

        return segment;
    }


    /**
     *
     * @param track
     * @param segment
     * @param newMovement
     * @return
     * @throws MovementDaoMappingException
     * @throws MovementDaoException
     * @throws GeometryUtilException
     */
    public Track upsertTrack(Track track, Segment segment, Movement newMovement) throws MovementDaoMappingException, MovementDaoException, GeometryUtilException {
        if (track == null) {
            dao.persist(newMovement);
            return createNewTrack(segment);
        } else {
            switch (segment.getSegmentCategory()) {
                case EXIT_PORT:
                    dao.persist(newMovement);
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
                    throw new MovementDaoMappingException("SEGMENT CATEGORY " + segment.getSegmentCategory().name() + " IS NOT MAPPED");
            }
        }
        return track;
    }


    /**
     *
     * @param track
     * @param newMovement
     * @param segment
     * @throws MovementDaoException
     * @throws GeometryUtilException
     */
    public void updateTrack(Track track, Movement newMovement, Segment segment) throws MovementDaoException, GeometryUtilException {
        LOG.debug("UPDATING TRACK ");

        if (track.getMovementList() == null) {
            track.setMovementList(new ArrayList<Movement>());
        }

        track.getMovementList().add(newMovement);
        segment.setTrack(track);
        newMovement.setTrack(track);
        track.getSegmentList().add(segment);

        double calculatedDistance = track.getDistance() + segment.getDistance();
        track.setDistance(calculatedDistance);
        double calculatedDurationInSeconds = track.getDuration() + segment.getDuration();
        track.setDuration(calculatedDurationInSeconds);

        LineString updatedTrackLineString = GeometryUtil.getLineStringFromMovments(track.getMovementList());

        if (!segment.getSegmentCategory().equals(SegmentCategoryType.ENTER_PORT) || !segment.getSegmentCategory().equals(SegmentCategoryType.IN_PORT)) {
            double distance = track.getTotalTimeAtSea();
            track.setTotalTimeAtSea(distance + calculatedDistance);
        }

        track.setLocation(updatedTrackLineString);

        dao.persist(track);
        dao.persist(segment);
        dao.persist(newMovement);
    }

    public Track createNewTrack(Segment segment) throws MovementDaoMappingException, MovementDaoException {
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
        dao.create(track);
        dao.persist(segment);
        return track;
    }

    /**
     * @param previousMovement
     * @param currentMovement
     * @throws GeometryUtilException
     * @throws MovementDaoException
     * @throws MovementDaoMappingException
     * @throws MovementModelException
     */
    public void splitSegment(Movement previousMovement, Movement currentMovement) throws GeometryUtilException, MovementDaoException, MovementDaoMappingException, MovementModelException {

        Segment theSegmentToBeBroken = dao.findByFromMovement(previousMovement);

        if (theSegmentToBeBroken == null) {

            theSegmentToBeBroken = dao.findByToMovement(previousMovement);
            if (theSegmentToBeBroken == null) {
                throw new MovementModelException("PREVIOS MOVEMENT MUST HAVE A SEGMENT BUT IT DONT, SOME LOGICAL ERROR HAS OCCURED");
            } else {
                Segment segment = createSegment(theSegmentToBeBroken.getToMovement(), currentMovement);
                upsertTrack(theSegmentToBeBroken.getTrack(), segment, currentMovement);
                return;
            }
        }
        LOG.debug("Splitting segment {}", theSegmentToBeBroken.getId());

        Movement oldToMovement = theSegmentToBeBroken.getToMovement();

        if (currentMovement == null && previousMovement == null) {
            LOG.error("[ ERROR when updating Segment entity: currentPosition AND previous Position cannot be null <updateSegment> ]");
            throw new MovementDaoMappingException("ERROR when updating Segment entity: currentPosition AND previous Position cannot be null");
        }

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

        LineString segmentLineString = GeometryUtil.getLineStringFromMovments(previousMovement, currentMovement);
        theSegmentToBeBroken.setLocation(segmentLineString);

        theSegmentToBeBroken = dao.persist(theSegmentToBeBroken);
        dao.flush();

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
     * @throws MovementDaoMappingException
     * @throws MovementModelException
     * @throws MovementDaoException
     * @throws GeometryUtilException
     */
    public void addMovementBeforeFirst(Movement firstMovement, Movement currentMovement) throws MovementDaoMappingException, MovementModelException, MovementDaoException, GeometryUtilException {
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
