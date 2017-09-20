package eu.europa.ec.fisheries.uvms.movement.bean;

import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDaoBean;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

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
    public void createSegmentOnFirstMovement(final Movement fromMovement, final Movement toMovement) throws MovementDaoException, GeometryUtilException, MovementDaoMappingException {
        // TODO this method should return a segment
        // TODO this method should be renamed to createSegmentAndTrackOnFirstMovement
        final Segment segment = MovementModelToEntityMapper.createSegment(fromMovement, toMovement);
        final Track track = upsertTrack(fromMovement.getTrack(), segment, toMovement);
        fromMovement.setTrack(track);
        dao.persist(fromMovement);
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
    public Track upsertTrack(final Track track, final Segment segment, final Movement newMovement) throws MovementDaoMappingException, MovementDaoException, GeometryUtilException {
        if (track == null) {
            return createNewTrack(segment, newMovement);
        } else {
            switch (segment.getSegmentCategory()) {
                case EXIT_PORT:
                    return createNewTrack(segment, newMovement);
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
    public void updateTrack(final Track track, final Movement newMovement, final Segment segment) throws MovementDaoException, GeometryUtilException {
        LOG.debug("UPDATING TRACK ");
        MovementModelToEntityMapper.updateTrack(track, newMovement, segment);
        dao.persist(track);
        dao.persist(segment);
        dao.persist(newMovement);
    }

    /**
     *
     * @param segment
     * @param newMovement
     * @return
     * @throws MovementDaoMappingException
     * @throws MovementDaoException
     */
    public Track createNewTrack(final Segment segment, final Movement newMovement) throws MovementDaoMappingException, MovementDaoException {
        LOG.debug("CREATING NEW TRACK ");
        final Track newTrack = MovementModelToEntityMapper.createTrack(segment);
        segment.setTrack(newTrack);
        newMovement.setTrack(newTrack);
        dao.create(newTrack);
        dao.persist(segment);
        dao.persist(newMovement);
        return newTrack;
    }

    public Track createNewTrack(final Segment segment) throws MovementDaoMappingException, MovementDaoException {
        LOG.debug("CREATING NEW TRACK ");
        final Track newTrack = MovementModelToEntityMapper.createTrack(segment);
        segment.setTrack(newTrack);
        dao.create(newTrack);
        dao.persist(segment);
        return newTrack;
    }

    /**
     * @param previousMovement
     * @param currentMovement
     * @throws GeometryUtilException
     * @throws MovementDaoException
     * @throws MovementDaoMappingException
     * @throws MovementModelException
     */
    public void splitSegment(final Movement previousMovement, final Movement currentMovement) throws GeometryUtilException, MovementDaoException, MovementDaoMappingException, MovementModelException {

        Segment theSegmentToBeBroken = dao.findByFromMovement(previousMovement);

        if (theSegmentToBeBroken == null) {

            theSegmentToBeBroken = dao.findByToMovement(previousMovement);
            if (theSegmentToBeBroken == null) {
                throw new MovementModelException("PREVIOS MOVEMENT MUST HAVE A SEGMENT BUT IT DONT, SOME LOGICAL ERROR HAS OCCURED");
            } else {
                final Segment segment = MovementModelToEntityMapper.createSegment(theSegmentToBeBroken.getToMovement(), currentMovement);
                upsertTrack(theSegmentToBeBroken.getTrack(), segment, currentMovement);
                return;
            }
        }
        LOG.debug("Splitting segment {}", theSegmentToBeBroken.getId());

        final Movement oldToMovement = theSegmentToBeBroken.getToMovement();

        MovementModelToEntityMapper.updateSegment(theSegmentToBeBroken, previousMovement, currentMovement);
        theSegmentToBeBroken = dao.persist(theSegmentToBeBroken);
        dao.flush();

        final Segment segment = MovementModelToEntityMapper.createSegment(currentMovement, oldToMovement);
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
    public void addMovementBeforeFirst(final Movement firstMovement, final Movement currentMovement) throws MovementDaoMappingException, MovementModelException, MovementDaoException, GeometryUtilException {
        Segment segment = firstMovement.getFromSegment();
        if (segment == null) {
            segment = MovementModelToEntityMapper.createSegment(currentMovement, firstMovement);
        } else {
            segment.setFromMovement(currentMovement);
        }
        final Track track = upsertTrack(firstMovement.getTrack(), segment, currentMovement);
        if (firstMovement.getTrack() == null) {
            firstMovement.setTrack(track);
            dao.persist(firstMovement);
        }
    }

}
