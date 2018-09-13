package eu.europa.ec.fisheries.uvms.movement.bean;

import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDaoBean;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by andreasw on 2017-03-08.
 */
@Stateless
@LocalBean
public class IncomingMovementBean {

    private static final Logger LOG = LoggerFactory.getLogger(IncomingMovementBean.class);

    @EJB
    private SegmentBean segmentBean;

    @EJB
    private MovementDaoBean dao;

    @PersistenceContext
    EntityManager em;

    public void processMovement(Movement currentMovement) throws MovementDomainException {
        LOG.debug("processMovement() method get called.");
        if (currentMovement != null && !currentMovement.isProcessed()) {
            LOG.debug("Processing movement {}", currentMovement.getId());
            String connectId = currentMovement.getMovementConnect().getValue();
            Date timeStamp = currentMovement.getTimestamp();

            /* TODO:
                Is this supposed to be a reference to processed instead of Timestamp?
                Easy fix is just to default processed to false, if that is the case.

                Timestamp will be null in the database if not set actively to a boolean value.
                This means duplicate timestamp Movements will not be detected by the processMovement
                method since the isDateAlreadyInserted method does not handle the null case
                (by e.g. setting a default value to false instead of null). Look at class MovementDaoBean
                and check if a null check is needed there or not.

             */
            List<Movement> duplicateMovements = dao.isDateAlreadyInserted(connectId, timeStamp);
            if (!duplicateMovements.isEmpty()) {    //if a duplicate date exists
                // If they have different movement types
                if (!currentMovement.getMovementType().equals(duplicateMovements.get(0).getMovementType())) {
                    // Add a second so that it is marginally different from the previous one and proceed
                    Date newDate = DateUtil.addSecondsToDate(timeStamp, 1);
                    currentMovement.setTimestamp(newDate);
                } else {
                    // else it is a duplicate of another move and should be ignored
                    LOG.info("Got a duplicate movement. Marking it as such.{}", currentMovement.getId());
                    currentMovement.setProcessed(true);
                    currentMovement.setDuplicate(true);
                    currentMovement.setDuplicateId(duplicateMovements.get(0).getGuid());
                    return;
                }
            }
            currentMovement.setDuplicate(false);

            // Get the movement that is closest in time b4 this one. Is one of these things supposed to
            // reference LatestMovement instead of movement?, or just get the "youngest" movement made?
            Movement previousMovement = dao.getLatestMovement(connectId, timeStamp);
            Movement firstMovement = null;

            if (previousMovement == null) {
                // Get the absolute first movement that this ConnectID(asset?)
                // reported, in case that one is younger then the current one
                firstMovement = dao.getFirstMovement(connectId);
                // If previous movement is somehow the same as this movement,
                // how this is supposed to be possible I have no idea.
            } else if (previousMovement.getId().equals(currentMovement.getId())) {
                return;
            } else {
                // Should only be true when a new position reports which is not the latest position.
                // Should not occur often but may occur when the mobile terminal has buffered its
                // positions or inserted a manual position.

                // How is this possible since getLatestMovement returns a position that is
                // as close as possible b4 timestamp? so how can it be after?
                if (previousMovement.getTimestamp().after(timeStamp)) {
                    firstMovement = dao.getFirstMovement(connectId);
                    previousMovement = dao.getLatestMovement(connectId, timeStamp);
                }
            }
            currentMovement.setAreatransitionList(populateTransitions(currentMovement, previousMovement));

            LOG.debug("Adding currentMovement to latestMovement for {}", connectId);
            // Updating the table latestMovement, why IDK
            dao.upsertLatestMovement(currentMovement, currentMovement.getMovementConnect());

            // First move for this connectID (asset?)
            if (firstMovement == null && previousMovement == null) {
                // You only log this? And should it not be processing rather then creating?
                LOG.debug("Creating firstMovement for connectId: " + connectId + " Movement Id: " + currentMovement.getId());
                // Normal case ie current movement is the latest movement
            } else if (previousMovement != null && firstMovement == null) {
                // There is no segment with previous movement as the To or From element
                if (!dao.hasMovementToOrFromSegment(previousMovement)) {
                    segmentBean.createSegmentAndTrack(previousMovement, currentMovement);
                } else { // or there is one
                    LOG.debug("PreviousMovement found, Id: " + previousMovement.getId() + " [ Splitting or Adding Segment ]");
                    // Create a new one or split an old one and create a new one
                    segmentBean.splitSegment(previousMovement, currentMovement);
                }

                // If the current movement is before the first movement
            } else if (previousMovement == null) {
                Track track = firstMovement.getTrack();
                if(track == null) { // no track = create one
                    segmentBean.createSegmentAndTrack(currentMovement, firstMovement);
                } else {
                    Segment segment = segmentBean.createSegment(currentMovement, firstMovement);
                    track.getSegmentList().add(segment);
                    track.getMovementList().add(currentMovement);
                    segment.setTrack(track);
                    currentMovement.setTrack(track);
                    firstMovement.setTrack(track);
                }
            } else {
                // Both first and previous exist, so current movement is in the middle of a track,
                // according to the logic at line 80
                segmentBean.splitSegment(previousMovement, currentMovement);
            }
            currentMovement.setProcessed(true);
            em.flush();
        }
    }

    public void processMovement(Long id) throws MovementDomainException {
        Movement currentMovement = dao.getMovementById(id);
        processMovement(currentMovement);
    }

    /**
     *
     * @param currentMovement
     * @param prevMovement
     * @return
     */
    public List<Areatransition> populateTransitions(Movement currentMovement, Movement prevMovement) {

        List<Areatransition> currentTransitions = new ArrayList<>();
        long start = System.currentTimeMillis();
        if (prevMovement == null) {
            for (Movementarea firstMovementTransitions : currentMovement.getMovementareaList()) {
                Areatransition transition = new Areatransition();
                transition.setAreatranAreaId(firstMovementTransitions.getMovareaAreaId());
                transition.setAreatranMoveId(currentMovement);
                transition.setMovementType(MovementTypeType.ENT);
                transition.setAreatranUpdattim(DateUtil.nowUTC());
                transition.setAreatranUpuser("UVMS");
                currentTransitions.add(transition);
            }
            return currentTransitions;
        }

        List<Movementarea> currentAreas = currentMovement.getMovementareaList();
        List<Areatransition> previousAreas = prevMovement.getAreatransitionList();

        HashMap<Long, Areatransition> previosAreasMap = new HashMap<>();
        for (Areatransition previousAreaTransition : previousAreas) {
            previosAreasMap.put(previousAreaTransition.getAreatranAreaId().getAreaId(), previousAreaTransition);
        }

        for (Movementarea currentAreaTransit : currentAreas) {

            Areatransition transition = new Areatransition();
            transition.setAreatranAreaId(currentAreaTransit.getMovareaAreaId());
            transition.setAreatranMoveId(currentMovement);
            transition.setAreatranUpdattim(DateUtil.nowUTC());
            transition.setAreatranUpuser("UVMS");

            if (previosAreasMap.containsKey(currentAreaTransit.getMovareaAreaId().getAreaId())) {

                Areatransition prevMoveAreaTransition = previosAreasMap.get(currentAreaTransit.getMovareaAreaId().getAreaId());

                switch (prevMoveAreaTransition.getMovementType()) {
                    case ENT:
                        transition.setMovementType(MovementTypeType.POS);
                        break;
                    case EXI:
                        transition.setMovementType(MovementTypeType.ENT);
                        break;
                    case POS:
                        transition.setMovementType(MovementTypeType.POS);
                        break;
                    case MAN:
                        transition.setMovementType(MovementTypeType.MAN);
                        break;
                    default:
                        transition.setMovementType(MovementTypeType.POS);
                }
            } else {
                transition.setMovementType(MovementTypeType.ENT);
            }

            currentTransitions.add(transition);
        }

        HashMap<Long, Areatransition> currentAreasMap = new HashMap<>();
        for (Areatransition previousArea : currentTransitions) {
            currentAreasMap.put(previousArea.getAreatranAreaId().getAreaId(), previousArea);
        }

        for (Areatransition previousArea : previousAreas) {
            if (!currentAreasMap.containsKey(previousArea.getAreatranAreaId().getAreaId())) {
                if (!previousArea.getMovementType().equals(MovementTypeType.EXI)) {
                    Areatransition transition = mapToAreaTransition(previousArea, currentMovement);
                    currentTransitions.add(transition);
                }
            }
        }

        long diff = System.currentTimeMillis() - start;
        LOG.debug("populateTransitions: " + " ---- TIME ---- " + diff + "ms" );
        return currentTransitions;
    }

    private Areatransition mapToAreaTransition(Areatransition previousArea, Movement currentMovement) {
        Areatransition transition = new Areatransition();
        transition.setAreatranAreaId(previousArea.getAreatranAreaId());
        transition.setAreatranMoveId(currentMovement);
        transition.setMovementType(MovementTypeType.EXI);
        transition.setAreatranUpdattim(DateUtil.nowUTC());
        transition.setAreatranUpuser("UVMS");
        return transition;
    }
}
