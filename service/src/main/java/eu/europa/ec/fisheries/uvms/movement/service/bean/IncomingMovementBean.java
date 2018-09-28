package eu.europa.ec.fisheries.uvms.movement.service.bean;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;

import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

@Stateless
public class IncomingMovementBean {

    private static final Logger LOG = LoggerFactory.getLogger(IncomingMovementBean.class);

    @Inject
    private SegmentBean segmentBean;

    @Inject
    private MovementDao dao;

    public void processMovement(Movement currentMovement) throws MovementServiceException {
        if (currentMovement == null) {
            throw new IllegalArgumentException("Movement to process is null!");
        }
        if (currentMovement.isProcessed()) {
            return;
        }
        String connectId = currentMovement.getMovementConnect().getValue();
        Instant timeStamp = currentMovement.getTimestamp();

        List<Movement> duplicateMovements = dao.isDateAlreadyInserted(connectId, timeStamp);
        if (!duplicateMovements.isEmpty()) {
            // If they have different movement types
            if (!currentMovement.getMovementType().equals(duplicateMovements.get(0).getMovementType())) {
 				Instant newDate = DateUtil.addSecondsToDate(timeStamp, 1);                    
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

        List<Movement> latestMovements = dao.getLatestMovementsByConnectId(connectId, 1);
        if (latestMovements.isEmpty()) { // First position
            currentMovement.setAreaTransitionList(populateTransitions(currentMovement, null));
        } else {
            Movement latestMovement = latestMovements.get(0);
            if (currentMovement.getTimestamp().isAfter(latestMovement.getTimestamp())) {
                if (latestMovement.getFromSegment() == null) {
                    segmentBean.createSegmentAndTrack(latestMovement, currentMovement); // Second position
                } else {
                    segmentBean.newSegment(latestMovement, currentMovement); // Normal case (latest position)
                }
                currentMovement.setAreaTransitionList(populateTransitions(currentMovement, latestMovement));
            } else {
                Movement previousMovement = dao.getPreviousMovement(connectId, timeStamp);
                if (previousMovement == null) { // Before first position
                    Movement firstMovement = dao.getFirstMovement(connectId);
                    segmentBean.addMovementBeforeFirst(firstMovement, currentMovement);
                    currentMovement.setAreaTransitionList(populateTransitions(currentMovement, null));
                    firstMovement.setAreaTransitionList(populateTransitions(firstMovement, currentMovement));
                } else { // Between two positions
                    Movement nextMovement = previousMovement.getToSegment().getToMovement();
                    segmentBean.splitSegment(previousMovement, currentMovement);
                    currentMovement.setAreaTransitionList(populateTransitions(currentMovement, previousMovement));
                    nextMovement.setAreaTransitionList(populateTransitions(nextMovement, currentMovement));
                }
            }
        }
        dao.upsertLatestMovement(currentMovement, currentMovement.getMovementConnect());
        currentMovement.setProcessed(true);
    }
    
    /**
     *
     * @param currentMovement
     * @param prevMovement
     * @return
     */
    public List<AreaTransition> populateTransitions(Movement currentMovement, Movement prevMovement) {

        List<AreaTransition> currentTransitions = new ArrayList<>();
        if (prevMovement == null) {
            for (Movementarea firstMovementTransitions : currentMovement.getMovementareaList()) {
                AreaTransition transition = new AreaTransition();
                transition.setAreaId(firstMovementTransitions.getMovareaAreaId());
                transition.setMovementId(currentMovement);
                transition.setMovementType(MovementTypeType.ENT);
                transition.setUpdateTime(DateUtil.nowUTC());
                transition.setUpdateUser("UVMS");
                currentTransitions.add(transition);
            }
            return currentTransitions;
        }

        List<Movementarea> currentAreas = currentMovement.getMovementareaList();
        List<AreaTransition> previousAreas = prevMovement.getAreaTransitionList();

        HashMap<Long, AreaTransition> previosAreasMap = new HashMap<>();
        for (AreaTransition previousAreaTransition : previousAreas) {
            previosAreasMap.put(previousAreaTransition.getAreaId().getAreaId(), previousAreaTransition);
        }

        for (Movementarea currentAreaTransit : currentAreas) {

            AreaTransition transition = new AreaTransition();
            transition.setAreaId(currentAreaTransit.getMovareaAreaId());
            transition.setMovementId(currentMovement);
            transition.setUpdateTime(DateUtil.nowUTC());
            transition.setUpdateUser("UVMS");

            if (previosAreasMap.containsKey(currentAreaTransit.getMovareaAreaId().getAreaId())) {

                AreaTransition prevMoveAreaTransition = previosAreasMap.get(currentAreaTransit.getMovareaAreaId().getAreaId());

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

        HashMap<Long, AreaTransition> currentAreasMap = new HashMap<>();
        for (AreaTransition previousArea : currentTransitions) {
            currentAreasMap.put(previousArea.getAreaId().getAreaId(), previousArea);
        }

        for (AreaTransition previousArea : previousAreas) {
            if (!currentAreasMap.containsKey(previousArea.getAreaId().getAreaId())) {
                if (!previousArea.getMovementType().equals(MovementTypeType.EXI)) {
                    AreaTransition transition = mapToAreaTransition(previousArea, currentMovement);
                    currentTransitions.add(transition);
                }
            }
        }
        return currentTransitions;
    }

    private AreaTransition mapToAreaTransition(AreaTransition previousArea, Movement currentMovement) {
        AreaTransition transition = new AreaTransition();
        transition.setAreaId(previousArea.getAreaId());
        transition.setMovementId(currentMovement);
        transition.setMovementType(MovementTypeType.EXI);
        transition.setUpdateTime(DateUtil.nowUTC());
        transition.setUpdateUser("UVMS");
        return transition;
    }
}
