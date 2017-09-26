package eu.europa.ec.fisheries.uvms.movement.bean;

import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDaoBean;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.transaction.SystemException;
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

    final static Logger LOG = LoggerFactory.getLogger(IncomingMovementBean.class);

    @EJB
    private SegmentBean segmentBean;

    @EJB
    private MovementDaoBean dao;

    public void processMovement(Movement currentMovement) throws MovementDaoException, GeometryUtilException, MovementDaoMappingException, MovementModelException, SystemException {
        LOG.debug("Processing movement {}", currentMovement.getId());
        if (currentMovement != null && !currentMovement.getProcessed()) {
            String connectId = currentMovement.getMovementConnect().getValue();
            Date timeStamp = currentMovement.getTimestamp();

            //ToDo: Timestamp will be null in the database if not set actively to a boolean value. This means duplicate timestamp Movements will not be detected by the processMovement method
            //ToDo: since the isDateAlreadyInserted method does not handle the null case (by e.g. setting a default value to false instead of null). Look at class MovementDaoBean and check if
            //ToDo: a null check is needed there or not.
            List<Movement> duplicateMovements = dao.isDateAlreadyInserted(connectId, timeStamp);
            if (!duplicateMovements.isEmpty()) {
                if (!currentMovement.getMovementType().equals(duplicateMovements.get(0).getMovementType())) {
                    Date newDate = DateUtil.addSecondsToDate(timeStamp, 1);
                    currentMovement.setTimestamp(newDate);
                } else {
                    LOG.info("Got a duplicate movement. Marking it as such.{}", currentMovement.getId());
                    currentMovement.setProcessed(true);
                    currentMovement.setDuplicate(true);
                    currentMovement.setDuplicateId(duplicateMovements.get(0).getGuid());
                    return;
                }
            }
            currentMovement.setDuplicate(false);

            Movement previousMovement = dao.getLatestMovement(connectId, timeStamp);
            Movement firstMovement = null;

            if (previousMovement == null) {
                firstMovement = dao.getFirstMovement(connectId, timeStamp);
            } else if (previousMovement.getId().equals(currentMovement.getId())) {
                return;
            } else {
                // Should only be true when a new position reports which is not the latest position. Should not occur often but may occur when the mobile terminal has buffered its positions or inserted a manual position.
                if (previousMovement.getTimestamp().after(timeStamp)) {
                    firstMovement = dao.getFirstMovement(connectId, timeStamp);
                    previousMovement = dao.getLatestMovement(connectId, timeStamp);
                }
            }
            currentMovement.setAreatransitionList(populateTransitions(currentMovement, previousMovement));

            LOG.debug("ADDING CURRENT MOVEMENT TO LATESTMOVEMENT FOR {}", connectId);
            dao.upsertLatestMovement(currentMovement, currentMovement.getMovementConnect());

            if (firstMovement == null && previousMovement == null) {
                LOG.debug("CREATING FIRST MOVEMENT FOR CONNECTID: " + connectId + " MOVEMENT ID: " + currentMovement.getId());
            } else if (previousMovement != null && firstMovement == null) {
                if (!dao.hasMovementToOrFromSegment(previousMovement)) {
                    segmentBean.createSegmentAndTrack(previousMovement, currentMovement);
                } else {
                    LOG.debug("PREVIOUS MOVEMENT FOUND, ID: " + previousMovement.getId() + " [ SPLITTING or ADDING SEGMENT ]");
                    segmentBean.splitSegment(previousMovement, currentMovement);
                }

            } else if (firstMovement != null && previousMovement == null) {
                Track track = firstMovement.getTrack();
                if(track == null) {
                    segmentBean.createSegmentAndTrack(currentMovement, firstMovement);
                } else {
                    Segment segment = segmentBean.createSegment(currentMovement, firstMovement);
                    track.getSegmentList().add(segment);
                    segment.setTrack(track);
                    currentMovement.setTrack(track);
                    firstMovement.setTrack(track);
                }
            } else {
                segmentBean.splitSegment(previousMovement, currentMovement);
            }

            currentMovement.setProcessed(true);
        }
    }


    public void processMovement(Long id) throws MovementDaoException, GeometryUtilException, MovementDaoMappingException, MovementModelException, SystemException {
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
