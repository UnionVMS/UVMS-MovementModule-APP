package eu.europa.ec.fisheries.uvms.movement.bean;

import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDaoBean;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.transaction.SystemException;
import javax.validation.ConstraintViolationException;
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

    public void processMovement(final Long id) throws MovementDaoException, GeometryUtilException, MovementDaoMappingException, MovementModelException, SystemException {
        final Movement movement = dao.getMovementById(id);
        LOG.debug("Processing movement {}", id);
        if (movement != null && !movement.getProcessed()) {
            final String connectId = movement.getMovementConnect().getValue();
            final Date timeStamp = movement.getTimestamp();

            //ToDo: Timestamp will be null in the database if not set actively to a boolean value. This means duplicate timestamp Movements will not be detected by the processMovement method
            //ToDo: since the isDateAlreadyInserted method does not handle the null case (by e.g. setting a default value to false instead of null). Look at class MovementDaoBean and check if
            //ToDo: a null check is needed there or not.
            final List<Movement> duplicateMovements = dao.isDateAlreadyInserted(connectId, timeStamp);
            if (!duplicateMovements.isEmpty() && duplicateMovements.size() == 1) {
                if (!movement.getMovementType().equals(duplicateMovements.get(0).getMovementType())) {
                    final Date newDate = DateUtil.addSecondsToDate(timeStamp, 1);
                    movement.setTimestamp(newDate);
                } else {
                    LOG.info("Got a duplicate movement. Marking it as such.{}",id);
                    movement.setProcessed(true);
                    movement.setDuplicate(true);
                    movement.setDuplicateId(duplicateMovements.get(0).getGuid());
                    return;
                }
            }
            movement.setDuplicate(false);

            Movement previousMovement = dao.getLatestMovement(connectId, timeStamp, false);
            Movement firstMovement = null;

            if (previousMovement == null) {
                firstMovement = dao.getFirstMovement(connectId, timeStamp);
            } else if (previousMovement.getId().equals(movement.getId())) {
                return;
            } else {
                // Should only be true when a new position reports which is not the latest position. Should not occur often but may occur when the mobile terminal has buffered its positions or inserted a manual position.
                if (previousMovement.getTimestamp().after(timeStamp)) {
                    firstMovement = dao.getFirstMovement(connectId, timeStamp);
                    previousMovement = dao.getLatestMovementByTimeStamp(connectId, timeStamp);
                }
            }
            movement.setAreatransitionList(populateTransitions(movement, previousMovement));

            LOG.debug("ADDING CURRENT MOVEMENT TO LATESTMOVEMENT FOR {}", connectId);
            dao.upsertLatestMovement(movement, movement.getMovementConnect());

            if (firstMovement == null && previousMovement == null) {
                LOG.debug("CREATING FIRST MOVEMENT FOR CONNECTID: " + connectId + " MOVEMENT ID: " + movement.getId());
            } else if (previousMovement != null && firstMovement == null) {
                if (dao.hasMovementToOrFromSegment(previousMovement)) {
                    LOG.debug("PREVIOUS MOVEMENT IS THE FIRST CREATED AND HAS NO SEGMENT YET, CREATING ONE..");
                    segmentBean.createSegmentOnFirstMovement(previousMovement, movement);
                } else {
                    try {
                        LOG.debug("PREVIOUS MOVEMENT FOUND, ID: " + previousMovement.getId() + " [ SPLITTING or ADDING SEGMENT ]");
                        segmentBean.splitSegment(previousMovement, movement);
                    } catch (final ConstraintViolationException e) {
                        LOG.error("[ Error when splitting segment. Concurrency issue. {}", e.getMessage());
                    }
                }
            } else if (firstMovement != null && previousMovement == null) {
                if (dao.hasMovementToOrFromSegment(firstMovement)) {
                    LOG.debug("PREVIOUS MOVEMENT IS THE FIRST CREATED AND HAS NO SEGMENT YET CREATING ONE..");
                    segmentBean.createSegmentOnFirstMovement(movement, firstMovement);
                } else {
                    LOG.debug("PREVIOUS MOVEMENT NOT FOUND BUT FIRST MOVEMENT FOUND ID: " + firstMovement.getId() + " [ ADDING NEW MOVEMENT BEFORE FIRST ]");
                    segmentBean.addMovementBeforeFirst(firstMovement, movement);
                }
            } else {
                segmentBean.splitSegment(previousMovement, movement);
            }

            movement.setProcessed(true);
        }
    }

    /**
     *
     * @param currentMovement
     * @param prevMovement
     * @return
     */
    public List<Areatransition> populateTransitions(final Movement currentMovement, final Movement prevMovement) {

        final List<Areatransition> currentTransitions = new ArrayList<>();
        final long start = System.currentTimeMillis();
        if (prevMovement == null) {
            for (final Movementarea firstMovementTransitions : currentMovement.getMovementareaList()) {
                final Areatransition transition = new Areatransition();
                transition.setAreatranAreaId(firstMovementTransitions.getMovareaAreaId());
                transition.setAreatranMoveId(currentMovement);
                transition.setMovementType(MovementTypeType.ENT);
                transition.setAreatranUpdattim(DateUtil.nowUTC());
                transition.setAreatranUpuser("UVMS");
                currentTransitions.add(transition);
            }
            return currentTransitions;
        }

        final List<Movementarea> currentAreas = currentMovement.getMovementareaList();
        final List<Areatransition> previousAreas = prevMovement.getAreatransitionList();

        final HashMap<Long, Areatransition> previosAreasMap = new HashMap<>();
        for (final Areatransition previousAreaTransition : previousAreas) {
            previosAreasMap.put(previousAreaTransition.getAreatranAreaId().getAreaId(), previousAreaTransition);
        }

        for (final Movementarea currentAreaTransit : currentAreas) {

            final Areatransition transition = new Areatransition();
            transition.setAreatranAreaId(currentAreaTransit.getMovareaAreaId());
            transition.setAreatranMoveId(currentMovement);
            transition.setAreatranUpdattim(DateUtil.nowUTC());
            transition.setAreatranUpuser("UVMS");

            if (previosAreasMap.containsKey(currentAreaTransit.getMovareaAreaId().getAreaId())) {

                final Areatransition prevMoveAreaTransition = previosAreasMap.get(currentAreaTransit.getMovareaAreaId().getAreaId());

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

        final HashMap<Long, Areatransition> currentAreasMap = new HashMap<>();
        for (final Areatransition previousArea : currentTransitions) {
            currentAreasMap.put(previousArea.getAreatranAreaId().getAreaId(), previousArea);
        }

        for (final Areatransition previousArea : previousAreas) {
            if (!currentAreasMap.containsKey(previousArea.getAreatranAreaId().getAreaId())) {
                if (!previousArea.getMovementType().equals(MovementTypeType.EXI)) {
                    final Areatransition transition = mapToAreaTransition(previousArea, currentMovement);
                    currentTransitions.add(transition);
                }
            }
        }

        final long diff = System.currentTimeMillis() - start;
        LOG.debug("populateTransitions: " + " ---- TIME ---- " + diff + "ms" );
        return currentTransitions;
    }

    private Areatransition mapToAreaTransition(final Areatransition previousArea, final Movement currentMovement) {
        final Areatransition transition = new Areatransition();
        transition.setAreatranAreaId(previousArea.getAreatranAreaId());
        transition.setAreatranMoveId(currentMovement);
        transition.setMovementType(MovementTypeType.EXI);
        transition.setAreatranUpdattim(DateUtil.nowUTC());
        transition.setAreatranUpuser("UVMS");
        return transition;
    }
}
