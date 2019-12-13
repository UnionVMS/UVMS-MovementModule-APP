package eu.europa.ec.fisheries.uvms.movement.service.bean;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
        UUID connectId = currentMovement.getMovementConnect().getId();
        Instant timeStamp = currentMovement.getTimestamp();

        Movement latestMovement = currentMovement.getMovementConnect().getLatestMovement();
        if (latestMovement == null) { // First position
            currentMovement.getMovementConnect().setLatestMovement(currentMovement);
            currentMovement.getMovementConnect().setLatestLocation(currentMovement.getLocation());
        } else {
            if (currentMovement.getTimestamp().isAfter(latestMovement.getTimestamp())) {
                segmentBean.newSegment(latestMovement, currentMovement); // Normal case (latest position)
                currentMovement.getMovementConnect().setLatestMovement(currentMovement);
                currentMovement.getMovementConnect().setLatestLocation(currentMovement.getLocation());
            } else {
                Movement previousMovement = dao.getPreviousMovement(connectId, timeStamp);
                if (previousMovement == null) { // Before first position
                    Movement firstMovement = dao.getFirstMovement(connectId, currentMovement.getTimestamp());
                    segmentBean.addMovementBeforeFirst(firstMovement, currentMovement);
                } else { // Between two positions
                    segmentBean.splitSegment(previousMovement, currentMovement);

                }
            }
        }
    }
    
    public boolean checkAndSetDuplicate(IncomingMovement movement) {
        if(movement.getPositionTime() == null || movement.getAssetGuid() == null){     //if these two are null the check cant complete and one of the other sanity rules will get it
            return false;
        }
        UUID connectId = UUID.fromString(movement.getAssetGuid());
        Instant timeStamp = movement.getPositionTime();

        List<Movement> duplicateMovements = dao.isDateAlreadyInserted(connectId, timeStamp);
        if (!duplicateMovements.isEmpty()) {
            // If they have different movement types or different source
            if (!Objects.equals(movement.getMovementType(), duplicateMovements.get(0).getMovementType().value())) {
                Instant newDate = timeStamp.plusSeconds(1);
                movement.setPositionTime(newDate);
            } else if (!Objects.equals(movement.getMovementSourceType(), MovementSourceType.AIS.value()) &&
                    !Objects.equals(movement.getMovementSourceType(), duplicateMovements.get(0).getMovementSource().value())) {
                // Don't modify NAF/Inmarsat timestamp, add second to AIS position instead 
                duplicateMovements.get(0).setTimestamp(timeStamp.plusSeconds(1));
            } else {
                LOG.info("Got a duplicate movement for Asset {}. Marking it as such.", movement.getAssetGuid());
                movement.setDuplicate(true);
                return true;
            }
        }
        return false;
    }
}