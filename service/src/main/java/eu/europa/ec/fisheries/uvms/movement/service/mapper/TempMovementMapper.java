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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.asset.v1.VesselType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.model.constants.TempMovementStateEnum;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.entity.temp.TempMovement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceRuntimeException;

public class TempMovementMapper {

    private TempMovementMapper() {}
    
    private static final Logger LOG = LoggerFactory.getLogger(TempMovementMapper.class);

    public static TempMovement toTempMovementEntity(TempMovementType tempMovementType, String username) {
        if (tempMovementType == null || tempMovementType.getPosition() == null) {
            LOG.warn("TempMovementType is null, aborting mapping");
            throw new MovementServiceRuntimeException("TempMovementType is null, aborting mapping", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }

        TempMovement tempMovement = new TempMovement();
        
        if (tempMovementType.getGuid() != null) {
            tempMovement.setId(tempMovementType.getGuid());
        }
        
        // Carrier values
        if (tempMovementType.getAsset() != null) {

            if (tempMovementType.getAsset().getCfr() != null) {
                tempMovement.setCfr(tempMovementType.getAsset().getCfr());
            }
            if (tempMovementType.getAsset().getExtMarking() != null) {
                tempMovement.setExternalMarkings(tempMovementType.getAsset().getExtMarking());
            }
            if (tempMovementType.getAsset().getFlagState() != null) {
                tempMovement.setFlag(tempMovementType.getAsset().getFlagState());
            }
            if (tempMovementType.getAsset().getIrcs() != null) {
                tempMovement.setIrcs(tempMovementType.getAsset().getIrcs());
            }
            if (tempMovementType.getAsset().getName() != null) {
                tempMovement.setName(tempMovementType.getAsset().getName());
            }
        }

        // Movement point
        if (tempMovementType.getPosition() != null) {
            tempMovement.setLatitude(tempMovementType.getPosition().getLatitude());
            tempMovement.setLongitude(tempMovementType.getPosition().getLongitude());
        }
        if (tempMovementType.getCourse() != null) {
            tempMovement.setCourse(tempMovementType.getCourse());
        }
        if (tempMovementType.getSpeed() != null) {
            tempMovement.setSpeed(tempMovementType.getSpeed());
        }
        if (tempMovementType.getStatus() != null) {
            tempMovement.setStatus(tempMovementType.getStatus());
        }
        if (tempMovementType.getTime() != null) {
            tempMovement.setTimestamp(DateUtil.convertDateTimeInUTC(tempMovementType.getTime()));
        }

        if (tempMovementType.getState() != null) {
            tempMovement.setState(TempMovementStateEnum.valueOf(tempMovementType.getState().name()));
        } else {
            tempMovement.setState(TempMovementStateEnum.DRAFT);
        }
        tempMovement.setUpdated(DateUtil.nowUTC());
        tempMovement.setUpdatedBy(username);
        return tempMovement;
    }

    // TODO: This method is redundant. Let callers to use merge instead of relaying on managed entity.
    public static TempMovement toExistingTempMovementEntity(TempMovement currentTempMovement, TempMovement newTempMovement, String username) {
        if (currentTempMovement == null) {
            LOG.warn("TempMovement is null, aborting mapping");
            throw new MovementServiceRuntimeException("TempMovement is null, aborting mapping", ErrorCode.ILLEGAL_ARGUMENT_ERROR);
        }
        if (newTempMovement.getCourse() != null) {
            currentTempMovement.setCourse(newTempMovement.getCourse());
        }
        if (newTempMovement.getSpeed() != null) {
            currentTempMovement.setSpeed(newTempMovement.getSpeed());
        }
        if (newTempMovement.getCfr() != null && !newTempMovement.getCfr().isEmpty()) {
            currentTempMovement.setCfr(newTempMovement.getCfr());
        }
        if (newTempMovement.getExternalMarkings() != null && !newTempMovement.getExternalMarkings().isEmpty()) {
            currentTempMovement.setExternalMarkings(newTempMovement.getExternalMarkings());
        }
        if (newTempMovement.getFlag() != null && !newTempMovement.getFlag().isEmpty()) {
            currentTempMovement.setFlag(newTempMovement.getFlag());
        }
        if (newTempMovement.getIrcs() != null && !newTempMovement.getIrcs().isEmpty()) {
            currentTempMovement.setIrcs(newTempMovement.getIrcs());
        }
        if (newTempMovement.getName() != null && !newTempMovement.getName().isEmpty()) {
            currentTempMovement.setName(newTempMovement.getName());
        }
        if (newTempMovement.getLatitude() != null) {
            currentTempMovement.setLatitude(newTempMovement.getLatitude());
        }
        if (newTempMovement.getLongitude() != null) {
            currentTempMovement.setLongitude(newTempMovement.getLongitude());
        }
        if (newTempMovement.getStatus() != null && !newTempMovement.getStatus().isEmpty()) {
            currentTempMovement.setStatus(newTempMovement.getStatus());
        }
        if (newTempMovement.getTimestamp() != null) {
            currentTempMovement.setTimestamp(newTempMovement.getTimestamp());
        }
        if (newTempMovement.getState() != null) {
            currentTempMovement.setState(TempMovementStateEnum.valueOf(newTempMovement.getState().name()));
        }
        currentTempMovement.setUpdated(DateUtil.nowUTC());
        currentTempMovement.setUpdatedBy(username);

        return currentTempMovement;
    }

    public static TempMovementType toTempMovement(TempMovement tempMovement)  {
        TempMovementType tempMovementType = new TempMovementType();

        VesselType vessleType = new VesselType();
        vessleType.setCfr(tempMovement.getCfr());
        vessleType.setExtMarking(tempMovement.getExternalMarkings());
        vessleType.setFlagState(tempMovement.getFlag());
        vessleType.setIrcs(tempMovement.getIrcs());
        vessleType.setName(tempMovement.getName());

        tempMovementType.setAsset(vessleType);

        if (tempMovement.getCourse() != null) {
            tempMovementType.setCourse(tempMovement.getCourse());
        }
        tempMovementType.setGuid(tempMovement.getId());

        MovementPoint tempMovementPoint = new MovementPoint();
        tempMovementPoint.setLatitude(tempMovement.getLatitude());
        tempMovementPoint.setLongitude(tempMovement.getLongitude());
        tempMovementType.setPosition(tempMovementPoint);

        if (tempMovement.getSpeed() != null) {
            tempMovementType.setSpeed(tempMovement.getSpeed());
        }
        tempMovementType.setStatus(tempMovement.getStatus());

        tempMovementType.setTime(DateUtil.parseUTCDateToString(tempMovement.getTimestamp()));
        tempMovementType.setUpdatedTime(DateUtil.parseUTCDateToString(tempMovement.getUpdated()));
        tempMovementType.setState(eu.europa.ec.fisheries.schema.movement.v1.TempMovementStateEnum.fromValue(tempMovement.getState().name()));

        return tempMovementType;
    }
}
