package eu.europa.ec.fisheries.uvms.movement.service.bean;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;

import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.LatestMovement;
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

        currentMovement.setDuplicate(false);

        LatestMovement latest = dao.getLatestMovement(connectId);
        if (latest == null) { // First position
            //left empty
        } else {
            Movement latestMovement = latest.getMovement();
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
        dao.upsertLatestMovement(currentMovement, currentMovement.getMovementConnect(), latest);
        currentMovement.setProcessed(true);
    }
    public boolean checkAndSetDuplicate(IncomingMovement movement) {
        if(movement.getPositionTime() == null || movement.getAssetHistoryId() == null){     //if these two are null the check cant complete and one of the other sanity rules will get it
            return false;
        }
        UUID connectId = UUID.fromString(movement.getAssetHistoryId());
        Instant timeStamp = movement.getPositionTime();

        List<Movement> duplicateMovements = dao.isDateAlreadyInserted(connectId, timeStamp);
        if (!duplicateMovements.isEmpty()) {
            // If they have different movement types
            if (!Objects.equals(movement.getMovementType(), duplicateMovements.get(0).getMovementType().value())) {
                Instant newDate = DateUtil.addSecondsToDate(timeStamp, 1);
                movement.setPositionTime(newDate);
            } else {
                LOG.info("Got a duplicate movement for Asset {}. Marking it as such.", movement.getAssetGuid());
                movement.setDuplicate(true);
                return true;
            }
        }
        return false;
    }
}
