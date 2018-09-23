package eu.europa.ec.fisheries.uvms.movement.service.bean;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Areatransition;
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
            currentMovement.setAreatransitionList(populateTransitions(currentMovement, null));
        } else {
            Movement latestMovement = latestMovements.get(0);
            if (currentMovement.getTimestamp().isAfter(latestMovement.getTimestamp())) {
                if (latestMovement.getFromSegment() == null) {
                    segmentBean.createSegmentAndTrack(latestMovement, currentMovement); // Second position
                } else {
                    segmentBean.splitSegment(latestMovement, currentMovement); // Normal case (latest position), create segment
                }
                currentMovement.setAreatransitionList(populateTransitions(currentMovement, latestMovement));
            } else {
                Movement previousMovement = dao.getPreviousMovement(connectId, timeStamp);
                if (previousMovement == null) { // Before first position
                    Movement firstMovement = dao.getFirstMovement(connectId);
                    segmentBean.addMovementBeforeFirst(firstMovement, currentMovement);
                    currentMovement.setAreatransitionList(populateTransitions(currentMovement, null));
                    firstMovement.setAreatransitionList(populateTransitions(firstMovement, currentMovement));
                } else { // Between two positions
                    Movement nextMovement = previousMovement.getToSegment().getToMovement();
                    segmentBean.splitSegment(previousMovement, currentMovement);
                    currentMovement.setAreatransitionList(populateTransitions(currentMovement, previousMovement));
                    nextMovement.setAreatransitionList(populateTransitions(nextMovement, currentMovement));
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
    public List<Areatransition> populateTransitions(Movement currentMovement, Movement prevMovement) {

        List<Areatransition> currentTransitions = new ArrayList<>();
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
