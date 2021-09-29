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
package eu.europa.ec.fisheries.uvms.movement.service.bean;

import eu.europa.ec.fisheries.uvms.movement.service.dto.ManualMovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.UUID;

@Stateless
public class ManualMovementService {

    @Inject
    private MovementCreateBean movementCreateBean;

    public UUID sendManualMovement(ManualMovementDto incomingManual, String username) {
        checkUsernameProvided(username);

        IncomingMovement incomingMovement = MovementMapper.manualMovementToIncomingMovement(incomingManual, username);

        return movementCreateBean.processIncomingMovement(incomingMovement);

    }

    private void checkUsernameProvided(String username) {
        if(username == null || username.isEmpty()){
            throw new IllegalArgumentException("Could not get username from request context");
        }
    }
}
