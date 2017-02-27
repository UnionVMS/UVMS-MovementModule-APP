/*
 Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
 © European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
 redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
 the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
 copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

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
import eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by osdjup on 2016-12-19.
 */
@Singleton
@Startup
@TransactionManagement( TransactionManagementType.BEAN )
public class MovementProcessorBean {

    final static Logger LOG = LoggerFactory.getLogger(MovementProcessorBean.class);

    private ScheduledExecutorService executor;

    @EJB
    MovementDaoBean dao;

    @Resource
    private EJBContext context;

    @PostConstruct
    public void init() {
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    processMovements();
                } catch (SystemException e) {
                    LOG.error("", e);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public void processMovements() throws SystemException {
        UserTransaction utx = context.getUserTransaction();
        LOG.debug("------------------- Processing started ------------------------");
        long start = System.currentTimeMillis();
        try {
            utx.begin();
            List<Long> movements = dao.getUnprocessedMovementIds();
            LOG.debug("Movement processing time for {} movements: {} ms", movements.size(), (System.currentTimeMillis() - start));
            utx.commit();

            for (Long id : movements) {
                processMovement(id);
            }

        } catch (Exception e) {
            LOG.error("Error while processing movement", e);
            utx.rollback();
        }
    }

    private void processMovement(Long id) throws MovementDaoException, GeometryUtilException, MovementDaoMappingException, MovementModelException, SystemException {
        UserTransaction utx = context.getUserTransaction();
        try {
            utx.begin();
            Movement movement = dao.getMovementById(id);
            LOG.debug("Processing movement {}", id);
            if (movement != null && !movement.getProcessed()) {
                String connectId = movement.getMovementConnect().getValue();
                Date timeStamp = movement.getTimestamp();

                long before = System.currentTimeMillis();
                List<Movement> duplicateMovements = dao.isDateAlreadyInserted(connectId, timeStamp);
                if (!duplicateMovements.isEmpty() && duplicateMovements.size() == 1) {
                    if (!movement.getMovementType().equals(duplicateMovements.get(0).getMovementType())) {
                        Date newDate = DateUtil.addSecondsToDate(timeStamp, 1);
                        movement.setTimestamp(newDate);
                    } else {
                        LOG.info("Got a duplicate movement. Marking it as such.");
                        movement.setProcessed(true);
                        movement.setDuplicate(true);
                        movement.setDuplicateId(duplicateMovements.get(0).getGuid());
                        utx.commit();
                        return;
                    }
                }
                movement.setDuplicate(false);
                LOG.debug("Check for duplicate movement: {}", (System.currentTimeMillis() - before));

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
                        createSegmentOnFirstMovement(previousMovement, movement);
                    } else {
                        try {
                            LOG.debug("PREVIOUS MOVEMENT FOUND, ID: " + previousMovement.getId() + " [ SPLITTING or ADDING SEGMENT ]");
                            splitSegment(previousMovement, movement);
                        } catch (ConstraintViolationException e) {
                            LOG.error("[ Error when splitting segment. Concurrency issue. {}", e.getMessage());
                        }
                    }
                } else if (firstMovement != null && previousMovement == null) {
                    if (dao.hasMovementToOrFromSegment(firstMovement)) {
                        LOG.debug("PREVIOUS MOVEMENT IS THE FIRST CREATED AND HAS NO SEGMENT YET CREATING ONE..");
                        createSegmentOnFirstMovement(movement, firstMovement);
                    } else {
                        LOG.debug("PREVIOUS MOVEMENT NOT FOUND BUT FIRST MOVEMENT FOUND ID: " + firstMovement.getId() + " [ ADDING NEW MOVEMENT BEFORE FIRST ]");
                        addMovementBeforeFirst(firstMovement, movement);
                    }
                } else {
                    splitSegment(previousMovement, movement);
                }

                movement.setProcessed(true);
            }
            utx.commit();
        } catch (Exception e) {
            LOG.error("", e);
            if (utx.getStatus() != Status.STATUS_NO_TRANSACTION) {
                utx.rollback();
            }
        }
    }

    /**
     *
     * @param currentMovement
     * @param prevMovement
     * @return
     */
    List<Areatransition> populateTransitions(Movement currentMovement, Movement prevMovement) {

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

    /**
     *
     * @param fromMovement
     * @param toMovement
     * @throws MovementDaoException
     * @throws GeometryUtilException
     * @throws MovementDaoMappingException
     */
    private void createSegmentOnFirstMovement(Movement fromMovement, Movement toMovement) throws MovementDaoException, GeometryUtilException, MovementDaoMappingException {
        Segment segment = MovementModelToEntityMapper.createSegment(fromMovement, toMovement);
        Track track = upsertTrack(fromMovement.getTrack(), segment, toMovement);
        fromMovement.setTrack(track);
        dao.persist(fromMovement);
    }

    /**
     * @param previousMovement
     * @param currentMovement
     * @throws GeometryUtilException
     * @throws MovementDaoException
     * @throws MovementDaoMappingException
     * @throws MovementModelException
     */
    void splitSegment(Movement previousMovement, Movement currentMovement) throws GeometryUtilException, MovementDaoException, MovementDaoMappingException, MovementModelException {

        Segment theSegmentToBeBroken = dao.findByFromMovement(previousMovement);

        if (theSegmentToBeBroken == null) {

            theSegmentToBeBroken = dao.findByToMovement(previousMovement);
            if (theSegmentToBeBroken == null) {
                throw new MovementModelException("PREVIOS MOVEMENT MUST HAVE A SEGMENT BUT IT DONT, SOME LOGICAL ERROR HAS OCCURED");
            } else {
                Segment segment = MovementModelToEntityMapper.createSegment(theSegmentToBeBroken.getToMovement(), currentMovement);
                upsertTrack(theSegmentToBeBroken.getTrack(), segment, currentMovement);
                return;
            }
        }
        LOG.debug("Splitting segment {}", theSegmentToBeBroken.getId());

        Movement oldToMovement = theSegmentToBeBroken.getToMovement();

        MovementModelToEntityMapper.updateSegment(theSegmentToBeBroken, previousMovement, currentMovement);
        theSegmentToBeBroken = dao.persist(theSegmentToBeBroken);
        dao.flush();

        Segment segment = MovementModelToEntityMapper.createSegment(currentMovement, oldToMovement);
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
    void addMovementBeforeFirst(Movement firstMovement, Movement currentMovement) throws MovementDaoMappingException, MovementModelException, MovementDaoException, GeometryUtilException {
        Segment segment = firstMovement.getFromSegment();
        if (segment == null) {
            segment = MovementModelToEntityMapper.createSegment(currentMovement, firstMovement);
        } else {
            segment.setFromMovement(currentMovement);
        }
        Track track = upsertTrack(firstMovement.getTrack(), segment, currentMovement);
        if (firstMovement.getTrack() == null) {
            firstMovement.setTrack(track);
            dao.persist(firstMovement);
        }
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
    Track upsertTrack(Track track, Segment segment, Movement newMovement) throws MovementDaoMappingException, MovementDaoException, GeometryUtilException {
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
    void updateTrack(Track track, Movement newMovement, Segment segment) throws MovementDaoException, GeometryUtilException {
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
    Track createNewTrack(Segment segment, Movement newMovement) throws MovementDaoMappingException, MovementDaoException {
        LOG.debug("CREATING NEW TRACK ");
        Track newTrack = MovementModelToEntityMapper.createTrack(segment);
        segment.setTrack(newTrack);
        newMovement.setTrack(newTrack);
        dao.create(newTrack);
        dao.persist(segment);
        dao.persist(newMovement);
        return newTrack;
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
