/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.movement.bean;

import eu.europa.ec.fisheries.schema.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDaoBean;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.exception.EntityDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.mapper.MovementEntityToModelMapper;
import eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;

import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 **/
@LocalBean
@Stateless
public class MovementBatchModelBean {

    final static Logger LOG = LoggerFactory.getLogger(MovementBatchModelBean.class);

    @EJB
    MovementDaoBean dao;

    /**
     *
     * @param connectId
     * @return
     * @throws MovementModelException
     */
    public MovementConnect getMovementConnect(String connectId) {
        MovementConnect movementConnectByConnectId = null;

        if (connectId == null) {
            return null;
        }

        try {
            movementConnectByConnectId = dao.getMovementConnectByConnectId(connectId);
        } catch (MovementDaoException ex) {
            LOG.error("ERROR WHEN GETTING MOVEMENTCONNECT", ex);
        }

        if (movementConnectByConnectId == null) {
            try {
                LOG.info("CREATING NEW MOVEMENTCONNECT");
                MovementConnect mapToMovementConnenct = MovementModelToEntityMapper.mapToMovementConnenct(connectId, AssetIdType.GUID);
                return dao.create(mapToMovementConnenct);
            } catch (MovementDaoException ex) {
                LOG.error("COULD NOT INSERT MOVEMENTCONNECT", ex);
            } catch (MovementDaoMappingException ex) {
                LOG.error("ERROR WHEN MAPPING TO MOVEMENTCONNECT", ex);
            } catch (Exception ex) {
                LOG.error("OTHER ERROR WHEN CREATING MOVEMENTCONNECT", ex);
            }
        }
        return movementConnectByConnectId;
    }

    /**
     *
     * @param movement
     * @return
     * @throws MovementModelException
     * @throws
     * EntityDuplicateException
     */
    public MovementType createMovement(MovementType movement, String username) {
        long start = System.currentTimeMillis();
        try {
            long before = System.currentTimeMillis();
            final Movement currentMovement = MovementModelToEntityMapper.mapNewMovementEntity(movement, username);
            MovementConnect moveConnect = getMovementConnect(movement.getConnectId());
            currentMovement.setMovementConnect(moveConnect);
            LOG.debug("Adding movement connect time: {}", (System.currentTimeMillis() - before));

            before = System.currentTimeMillis();
            List<Movementarea> areas = getAreas(currentMovement, movement);
            currentMovement.setMovementareaList(areas);
            LOG.debug("Adding areas time: {}", (System.currentTimeMillis() - before));

            LOG.debug("CREATING MOVEMENT FOR CONNECTID: " + movement.getConnectId() + " MOVEMENT ID: " + currentMovement.getId());
            dao.create(currentMovement);
            // TODO: Make sure that relation is correct
            if(moveConnect.getMovementList() == null) {
                moveConnect.setMovementList(new ArrayList<Movement>());
            }
            moveConnect.getMovementList().add(currentMovement);
            dao.persist(moveConnect);

            MovementType movementType = mapToMovementType(currentMovement);
            long diff = System.currentTimeMillis() - start;
            LOG.debug("Create movement done: " + " ---- TIME ---- " + diff + "ms" );
            return movementType;
        } catch (MovementDaoMappingException | MovementDaoException e) {
            LOG.error("[ Error when creating movement. ] {}", e);
            throw new EJBException("Could not create movement.", e);
        }
    }

    /**
     *
     * @param currentMovement
     * @return
     */
    private MovementType mapToMovementType(Movement currentMovement) {
        long start = System.currentTimeMillis();
        MovementType mappedMovement = MovementEntityToModelMapper.mapToMovementType(currentMovement);
        enrichMetaData(mappedMovement, currentMovement.getTempFromSegment());
        enrichAreas(mappedMovement, currentMovement.getAreatransitionList());
        long diff = System.currentTimeMillis() - start;
        LOG.debug("mapToMovementType: " + " ---- TIME ---- " + diff + "ms" );
        return mappedMovement;
    }

    /**
     *
     * @param currentMovement
     * @param movementType
     * @return
     * @throws MovementDaoException
     */
    private List<Movementarea> getAreas(Movement currentMovement, MovementType movementType) throws MovementDaoException {
        LOG.debug("CREATING AND GETTING AREAS AND AREATYPES");
        List<Movementarea> areas = new ArrayList<>();
        long start = System.currentTimeMillis();
        if (movementType.getMetaData() != null) {

            for (MovementMetaDataAreaType area : movementType.getMetaData().getAreas()) {
                Movementarea movementArea = new Movementarea();
                Area areaEntity = dao.getAreaByRemoteIdAndCode(area.getCode(), area.getRemoteId());

                if (areaEntity != null) {
                    String wrkRemoteId = areaEntity.getRemoteId();
                    if(wrkRemoteId != null) {
                        if (!wrkRemoteId.equals(area.getRemoteId())) {
                            areaEntity.setRemoteId(area.getRemoteId());
                        }
                    }
                    movementArea.setMovareaAreaId(areaEntity);
                } else {
                    AreaType areaType = getAreaType(area);
                    Area newArea = MovementModelToEntityMapper.maptoArea(area, areaType);
                    try {
                        dao.create(newArea);
                        movementArea.setMovareaAreaId(newArea);
                    } catch (ConstraintViolationException e) {
                        // Area was created while we tried to create it.
                        LOG.info("Area \"{}\"was created while we tried to create it. Trying to fetch it.", area.getCode());
                        areaEntity = dao.getAreaByRemoteIdAndCode(area.getCode(), area.getRemoteId());
                        if (areaEntity != null) {
                            if (!areaEntity.getRemoteId().equals(area.getRemoteId())) {
                                areaEntity.setRemoteId(area.getRemoteId());
                            }
                            movementArea.setMovareaAreaId(areaEntity);
                        }
                    }
                }

                movementArea.setMovareaMoveId(currentMovement);
                movementArea.setMovareaUpdattim(DateUtil.nowUTC());
                movementArea.setMovareaUpuser("UVMS");
                areas.add(movementArea);
            }
            long diff = System.currentTimeMillis() - start;
            LOG.debug("getAreas: " + " ---- TIME ---- " + diff + "ms" );
        }

        return areas;

    }

    /**
     *
     * @param mappedMovement
     * @param fromSegment
     */
    private void enrichMetaData(MovementType mappedMovement, Segment fromSegment) {

        if (fromSegment != null) {
            mappedMovement.setCalculatedSpeed(fromSegment.getSpeedOverGround());
            mappedMovement.setCalculatedCourse(fromSegment.getCourseOverGround());
        }

        if (mappedMovement.getMetaData() != null) {
            if (fromSegment != null) {
                mappedMovement.getMetaData().setFromSegmentType(fromSegment.getSegmentCategory());
            }

        } else {
            MovementMetaData meta = new MovementMetaData();

            if (fromSegment != null) {
                meta.setFromSegmentType(fromSegment.getSegmentCategory());
            }

            mappedMovement.setMetaData(meta);
        }

    }

    /**
     * Enriches the MovemementTypes Areas in the metadata object. If there are
     * transitions present that are not already mapped in the movement they are
     * added to the area list in metadata.
     *
     * @param mappedMovement the movement where the metadata is extracted
     * @param areatransitionList the list of transitions that will enrich the
     * mapped movmement
     */
    public void enrichAreas(MovementType mappedMovement, List<Areatransition> areatransitionList) {

        HashMap<String, MovementMetaDataAreaType> areas = new HashMap<>();
        for (MovementMetaDataAreaType area : mappedMovement.getMetaData().getAreas()) {
            areas.put(area.getCode(), area);
        }

        if (areatransitionList != null) {
            for (Areatransition areaTransition : areatransitionList) {
                if (areas.containsKey(areaTransition.getAreatranAreaId().getAreaCode())) {
                    areas.get(areaTransition.getAreatranAreaId().getAreaCode()).setTransitionType(areaTransition.getMovementType());
                } else {
                    MovementMetaDataAreaType newArea = MapToMovementMetaDataAreaType(areaTransition);
                    areas.put(newArea.getCode(), newArea);
                }
            }
        }

        mappedMovement.getMetaData().getAreas().clear();
        mappedMovement.getMetaData().getAreas().addAll(areas.values());
    }

    public MovementMetaDataAreaType MapToMovementMetaDataAreaType(Areatransition areaTransition) {
        MovementMetaDataAreaType newArea = new MovementMetaDataAreaType();
        newArea.setTransitionType(areaTransition.getMovementType());
        newArea.setCode(areaTransition.getAreatranAreaId().getAreaCode());
        newArea.setName(areaTransition.getAreatranAreaId().getAreaName());
        newArea.setRemoteId(areaTransition.getAreatranAreaId().getRemoteId());
        newArea.setAreaType(areaTransition.getAreatranAreaId().getAreaType().getName());
        return newArea;
    }

    /**
     *
     * @param type
     * @return
     * @throws MovementDaoException
     */
    public AreaType getAreaType(MovementMetaDataAreaType type) throws MovementDaoException {
        AreaType areaType = dao.getAreaTypeByCode(type.getAreaType());
        if (areaType == null) {
            AreaType newAreaType = MovementModelToEntityMapper.mapToAreaType(type);
            return dao.create(newAreaType);
        } else {
            return areaType;
        }
    }

    public void flush() throws MovementDaoException {
        try {
            dao.flush();
        } catch (Exception e) {
            LOG.error("[ Error when creating ] {}", e.getMessage());
            throw new MovementDaoException(12, "[ Error when creating ] ", e);
        }
    }

}