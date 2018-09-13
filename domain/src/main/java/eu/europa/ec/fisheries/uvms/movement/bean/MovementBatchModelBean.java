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

import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDaoBean;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MissingMovementConnectException;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Movementarea;
import eu.europa.ec.fisheries.uvms.movement.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;
import eu.europa.ec.fisheries.uvms.movement.mapper.MovementEntityToModelMapper;
import eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@LocalBean
@Stateless
public class MovementBatchModelBean {

    private static final Logger LOG = LoggerFactory.getLogger(MovementBatchModelBean.class);

    @EJB
    private MovementDaoBean dao;

    @Inject
    IncomingMovementBean incomingMovementBean;   //for some reason this functionality is not in MovementProcessorBean

    @Resource
    private EJBContext context;


    public MovementConnect getMovementConnectByConnectId(String connectId) {
        MovementConnect movementConnect;

        if (connectId == null) {
            return null;
        }
        movementConnect = dao.getMovementConnectByConnectId(connectId);

        if (movementConnect == null) {
            LOG.info("Creating new MovementConnect");
            MovementConnect connect = new MovementConnect();
            connect.setUpdated(DateUtil.nowUTC());
            connect.setUpdatedBy("UVMS");
            connect.setValue(connectId);
            return dao.create(connect);
        }
        return movementConnect;
    }

    public MovementType createMovement(MovementType receivedMovementType, String username) throws MissingMovementConnectException {
        long start = System.currentTimeMillis();
        try {
            MovementType createdMovementType;
            long before = System.currentTimeMillis();
            final Movement currentMovement = MovementModelToEntityMapper.mapNewMovementEntity(receivedMovementType, username);
            MovementConnect moveConnect = getMovementConnectByConnectId(receivedMovementType.getConnectId());
            if(moveConnect != null){
                currentMovement.setMovementConnect(moveConnect);
                LOG.debug("Adding movement connect time: {}", (System.currentTimeMillis() - before));
                before = System.currentTimeMillis();
                List<Movementarea> areas = getAreas(currentMovement, receivedMovementType);
                currentMovement.setMovementareaList(areas);
                LOG.debug("Adding areas time: {}", (System.currentTimeMillis() - before));
                LOG.debug("CREATING MOVEMENT FOR CONNECTID: " + receivedMovementType.getConnectId() + " MOVEMENT ID: " + currentMovement.getId());
                dao.create(currentMovement);
                // TODO: Make sure that relation is correct
                if(moveConnect.getMovementList() == null) {
                    moveConnect.setMovementList(new ArrayList<>());
                }
                //moveConnect.getMovementList().add(currentMovement);
                //dao.persist(moveConnect);

                long diff = System.currentTimeMillis() - start;
                LOG.debug("Create movement done: " + " ---- TIME ---- " + diff + "ms" );
            } else {
                throw new MissingMovementConnectException("Couldn't find movementConnect!");
            }
            moveConnect.getMovementList().add(currentMovement);
            dao.persist(moveConnect);

            //Initiate the processing of movements, This is copied almost straight from MovementProcessorBean
            //TODO: Move this to MovementServiceBean when we start to refactor the mappings
            try {

                incomingMovementBean.processMovement(currentMovement);
            } catch (Exception e) {
                LOG.error("Error while processing movement", e);
                throw new RuntimeException("Error while processing movement: " + e);

            }

            createdMovementType = mapToMovementType(currentMovement);
            long diff = System.currentTimeMillis() - start;
            LOG.debug("Create movement done: " + " ---- TIME ---- " + diff + "ms" );
            return createdMovementType;
        } catch (MovementDomainException e) {
            LOG.error("[ Error when creating movement. ] {}", e);
            throw new EJBException("Could not create movement.", e);
        }
    }

    private MovementType mapToMovementType(Movement currentMovement) {
        long start = System.currentTimeMillis();
        MovementType mappedMovement = MovementEntityToModelMapper.mapToMovementType(currentMovement);
        // We cannot enrich a movementType at this point, there is no segment
        //enrichMetaData(mappedMovement);
        enrichAreas(mappedMovement, currentMovement.getAreatransitionList());
        long diff = System.currentTimeMillis() - start;
        LOG.debug("mapToMovementType: " + " ---- TIME ---- " + diff + "ms" );
        return mappedMovement;
    }

    private List<Movementarea> getAreas(Movement currentMovement, MovementType movementType) {
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
     * Enriches the MovementTypes Areas in the metadata object. If there are
     * transitions present that are not already mapped in the movementType they are
     * added to the area list in metadata.
     *
     * @param mappedMovement the movementType where the metadata is extracted
     * @param areaTransitionList the list of transitions that will enrich the
     * mapped movementType
     */
    public void enrichAreas(MovementType mappedMovement, List<Areatransition> areaTransitionList) {

        if(mappedMovement.getMetaData() == null) {
            mappedMovement.setMetaData(new MovementMetaData());
        }

        HashMap<String, MovementMetaDataAreaType> areas = new HashMap<>();
        for (MovementMetaDataAreaType area : mappedMovement.getMetaData().getAreas()) {
            areas.put(area.getCode(), area);
        }

        if (areaTransitionList != null) {
            for (Areatransition areaTransition : areaTransitionList) {
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

    public AreaType getAreaType(MovementMetaDataAreaType type) {
        AreaType areaType = dao.getAreaTypeByCode(type.getAreaType());
        if (areaType == null) {
            AreaType newAreaType = MovementModelToEntityMapper.mapToAreaType(type);
            return dao.create(newAreaType);
        } else {
            return areaType;
        }
    }

    public void flush() throws MovementDomainException {
        try {
            dao.flush();
        } catch (Exception e) {
            LOG.error("[ Error when creating ] {}", e.getMessage());
            throw new MovementDomainException("Error when creating", e, ErrorCode.DAO_PERSIST_ERROR);
        }
    }
}
