package eu.europa.ec.fisheries.uvms.movement.service.bean;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;

@Stateless
public class IncomingMovementBean {

    private static final Logger LOG = LoggerFactory.getLogger(IncomingMovementBean.class);

    @Inject
    private SegmentBean segmentBean;

    @Inject
    private MovementDao dao;

    public void processMovement(Movement currentMovement) {
        if (currentMovement == null) {
            throw new IllegalArgumentException("Movement to process is null!");
        }
        if (currentMovement.isProcessed()) {
            return;
        }
        UUID connectId = currentMovement.getMovementConnect().getId();
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
            //left empty
        } else {
            Movement latestMovement = latestMovements.get(0);
            if (currentMovement.getTimestamp().isAfter(latestMovement.getTimestamp())) {
                segmentBean.newSegment(latestMovement, currentMovement); // Normal case (latest position)
            } else {
                Movement previousMovement = dao.getPreviousMovement(connectId, timeStamp);
                if (previousMovement == null) { // Before first position
                    Movement firstMovement = dao.getFirstMovement(connectId);
                    segmentBean.addMovementBeforeFirst(firstMovement, currentMovement);
                } else { // Between two positions
                    Movement nextMovement = previousMovement.getToSegment().getToMovement();
                    segmentBean.splitSegment(previousMovement, currentMovement);

                }
            }
        }
        dao.upsertLatestMovement(currentMovement, currentMovement.getMovementConnect());
        currentMovement.setProcessed(true);
    }
}
