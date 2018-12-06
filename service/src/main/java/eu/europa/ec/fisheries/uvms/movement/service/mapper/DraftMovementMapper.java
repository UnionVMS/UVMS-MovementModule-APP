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
package eu.europa.ec.fisheries.uvms.movement.service.mapper;

import java.util.UUID;

import eu.europa.ec.fisheries.uvms.movement.service.entity.temp.DraftMovement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.asset.v1.VesselType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.model.constants.TempMovementStateEnum;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;

public class DraftMovementMapper {

    private static final Logger LOG = LoggerFactory.getLogger(DraftMovementMapper.class);

    public static DraftMovement toTempMovementEntity(TempMovementType tempMovementType, String username) {
        if (tempMovementType == null || tempMovementType.getPosition() == null) {
            LOG.warn("TempMovementType is null, aborting mapping");
            throw new IllegalArgumentException("TempMovementType is null, aborting mapping");
        }

        DraftMovement draftMovement = new DraftMovement();
        
        if (tempMovementType.getGuid() != null) {
            draftMovement.setId(UUID.fromString(tempMovementType.getGuid()));
        }
        
        // Carrier values
        if (tempMovementType.getAsset() != null) {

            if (tempMovementType.getAsset().getCfr() != null) {
                draftMovement.setCfr(tempMovementType.getAsset().getCfr());
            }
            if (tempMovementType.getAsset().getExtMarking() != null) {
                draftMovement.setExternalMarkings(tempMovementType.getAsset().getExtMarking());
            }
            if (tempMovementType.getAsset().getFlagState() != null) {
                draftMovement.setFlag(tempMovementType.getAsset().getFlagState());
            }
            if (tempMovementType.getAsset().getIrcs() != null) {
                draftMovement.setIrcs(tempMovementType.getAsset().getIrcs());
            }
            if (tempMovementType.getAsset().getName() != null) {
                draftMovement.setName(tempMovementType.getAsset().getName());
            }
        }

        // Movement point
        if (tempMovementType.getPosition() != null) {
            draftMovement.setLatitude(tempMovementType.getPosition().getLatitude());
            draftMovement.setLongitude(tempMovementType.getPosition().getLongitude());
        }
        if (tempMovementType.getCourse() != null) {
            draftMovement.setCourse(tempMovementType.getCourse());
        }
        if (tempMovementType.getSpeed() != null) {
            draftMovement.setSpeed(tempMovementType.getSpeed());
        }
        if (tempMovementType.getStatus() != null) {
            draftMovement.setStatus(tempMovementType.getStatus());
        }
        if (tempMovementType.getTime() != null) {
            draftMovement.setTimestamp(DateUtil.convertDateTimeInUTC(tempMovementType.getTime()));
        }

        if (tempMovementType.getState() != null) {
            draftMovement.setState(TempMovementStateEnum.valueOf(tempMovementType.getState().name()));
        } else {
            draftMovement.setState(TempMovementStateEnum.DRAFT);
        }
        draftMovement.setUpdated(DateUtil.nowUTC());
        draftMovement.setUpdatedBy(username);
        return draftMovement;
    }

    // TODO: This method is redundant. Let callers to use merge instead of relaying on managed entity.
    public static DraftMovement toExistingTempMovementEntity(DraftMovement currentDraftMovement, DraftMovement newDraftMovement, String username) {
        if (currentDraftMovement == null) {
            LOG.warn("DraftMovement is null, aborting mapping");
            throw new IllegalArgumentException("DraftMovement is null, aborting mapping");
        }
        if (newDraftMovement.getCourse() != null) {
            currentDraftMovement.setCourse(newDraftMovement.getCourse());
        }
        if (newDraftMovement.getSpeed() != null) {
            currentDraftMovement.setSpeed(newDraftMovement.getSpeed());
        }
        if (newDraftMovement.getCfr() != null && !newDraftMovement.getCfr().isEmpty()) {
            currentDraftMovement.setCfr(newDraftMovement.getCfr());
        }
        if (newDraftMovement.getExternalMarkings() != null && !newDraftMovement.getExternalMarkings().isEmpty()) {
            currentDraftMovement.setExternalMarkings(newDraftMovement.getExternalMarkings());
        }
        if (newDraftMovement.getFlag() != null && !newDraftMovement.getFlag().isEmpty()) {
            currentDraftMovement.setFlag(newDraftMovement.getFlag());
        }
        if (newDraftMovement.getIrcs() != null && !newDraftMovement.getIrcs().isEmpty()) {
            currentDraftMovement.setIrcs(newDraftMovement.getIrcs());
        }
        if (newDraftMovement.getName() != null && !newDraftMovement.getName().isEmpty()) {
            currentDraftMovement.setName(newDraftMovement.getName());
        }
        if (newDraftMovement.getLatitude() != null) {
            currentDraftMovement.setLatitude(newDraftMovement.getLatitude());
        }
        if (newDraftMovement.getLongitude() != null) {
            currentDraftMovement.setLongitude(newDraftMovement.getLongitude());
        }
        if (newDraftMovement.getStatus() != null && !newDraftMovement.getStatus().isEmpty()) {
            currentDraftMovement.setStatus(newDraftMovement.getStatus());
        }
        if (newDraftMovement.getTimestamp() != null) {
            currentDraftMovement.setTimestamp(newDraftMovement.getTimestamp());
        }
        if (newDraftMovement.getState() != null) {
            currentDraftMovement.setState(TempMovementStateEnum.valueOf(newDraftMovement.getState().name()));
        }
        currentDraftMovement.setUpdated(DateUtil.nowUTC());
        currentDraftMovement.setUpdatedBy(username);

        return currentDraftMovement;
    }

    public static TempMovementType toTempMovement(DraftMovement draftMovement)  {
        TempMovementType tempMovementType = new TempMovementType();

        VesselType vessleType = new VesselType();
        vessleType.setCfr(draftMovement.getCfr());
        vessleType.setExtMarking(draftMovement.getExternalMarkings());
        vessleType.setFlagState(draftMovement.getFlag());
        vessleType.setIrcs(draftMovement.getIrcs());
        vessleType.setName(draftMovement.getName());

        tempMovementType.setAsset(vessleType);

        if (draftMovement.getCourse() != null) {
            tempMovementType.setCourse(draftMovement.getCourse());
        }
        tempMovementType.setGuid(draftMovement.getId().toString());

        MovementPoint tempMovementPoint = new MovementPoint();
        tempMovementPoint.setLatitude(draftMovement.getLatitude());
        tempMovementPoint.setLongitude(draftMovement.getLongitude());
        tempMovementType.setPosition(tempMovementPoint);

        if (draftMovement.getSpeed() != null) {
            tempMovementType.setSpeed(draftMovement.getSpeed());
        }
        tempMovementType.setStatus(draftMovement.getStatus());

        tempMovementType.setTime(DateUtil.parseUTCDateToString(draftMovement.getTimestamp()));
        tempMovementType.setUpdatedTime(DateUtil.parseUTCDateToString(draftMovement.getUpdated()));
        tempMovementType.setState(eu.europa.ec.fisheries.schema.movement.v1.TempMovementStateEnum.fromValue(draftMovement.getState().name()));

        return tempMovementType;
    }
}
