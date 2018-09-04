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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LocalBean
@Stateless
public class MovementBatchModelBean {

    private static final Logger LOG = LoggerFactory.getLogger(MovementBatchModelBean.class);

    @EJB
    private MovementDaoBean daoBean;

    public MovementConnect getMovementConnectByConnectId(String connectId) {
        MovementConnect movementConnect;

        if (connectId == null) {
            return null;
        }
        movementConnect = daoBean.getMovementConnectByConnectId(connectId);

        if (movementConnect == null) {
            LOG.info("Creating new MovementConnect");
            MovementConnect connect = new MovementConnect();
            connect.setUpdated(DateUtil.nowUTC());
            connect.setUpdatedBy("UVMS");
            connect.setValue(connectId);
            return daoBean.create(connect);
        }
        return movementConnect;
    }

    public MovementType createMovement(MovementType movement, String username) throws MovementDomainException {
        long start = System.currentTimeMillis();
        try {
            long before = System.currentTimeMillis();
            final Movement currentMovement = MovementModelToEntityMapper.mapNewMovementEntity(movement, username);
            MovementConnect moveConnect = getMovementConnectByConnectId(movement.getConnectId());
            currentMovement.setMovementConnect(moveConnect);
            LOG.debug("Adding movement connect time: {}", (System.currentTimeMillis() - before));

            before = System.currentTimeMillis();
            List<Movementarea> areas = getAreas(currentMovement, movement);
            currentMovement.setMovementareaList(areas);
            LOG.debug("Adding areas time: {}", (System.currentTimeMillis() - before));

            LOG.debug("Creating Movement for ConnectId: " + movement.getConnectId() + " Movement Id: " + currentMovement.getId());
            daoBean.create(currentMovement);
            // TODO: Make sure that relation is correct
            if(moveConnect.getMovementList() == null) {
                moveConnect.setMovementList(new ArrayList<>());
            }
            moveConnect.getMovementList().add(currentMovement);
            daoBean.persist(moveConnect);

            MovementType movementType = mapToMovementType(currentMovement);
            long diff = System.currentTimeMillis() - start;
            LOG.debug("Create movement done: " + " ---- TIME ---- " + diff + "ms" );
            return movementType;
        } catch (Exception e) {
            LOG.error("[ Error when creating movement. ] {}", e);
            throw new MovementDomainException("Could not create movement.", e, ErrorCode.DAO_MAPPING_ERROR);
        }
    }

    private MovementType mapToMovementType(Movement currentMovement) {
        long start = System.currentTimeMillis();
        MovementType mappedMovement = MovementEntityToModelMapper.mapToMovementType(currentMovement);
        // We cannot enrich a movement at this point, there is no segment
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
                Area areaEntity = daoBean.getAreaByRemoteIdAndCode(area.getCode(), area.getRemoteId());

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
                        daoBean.create(newArea);
                        movementArea.setMovareaAreaId(newArea);
                    } catch (ConstraintViolationException e) {
                        // Area was created while we tried to create it.
                        LOG.info("Area \"{}\"was created while we tried to create it. Trying to fetch it.", area.getCode());
                        areaEntity = daoBean.getAreaByRemoteIdAndCode(area.getCode(), area.getRemoteId());
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
     * transitions present that are not already mapped in the movement they are
     * added to the area list in metadata.
     *
     * @param mappedMovement the movement where the metadata is extracted
     * @param areaTransitionList the list of transitions that will enrich the
     * mapped movement
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

    public AreaType getAreaType(MovementMetaDataAreaType type)  {
        AreaType areaType = daoBean.getAreaTypeByCode(type.getAreaType());
        if (areaType == null) {
            AreaType newAreaType = MovementModelToEntityMapper.mapToAreaType(type);
            return daoBean.create(newAreaType);
        } else {
            return areaType;
        }
    }

    public void flush() throws MovementDomainException {
        try {
            daoBean.flush();
        } catch (Exception e) {
            LOG.error("[ Error when creating ] {}", e.getMessage());
            throw new MovementDomainException("Error when creating ] ", e, ErrorCode.DAO_PERSIST_ERROR);
        }
    }
}
