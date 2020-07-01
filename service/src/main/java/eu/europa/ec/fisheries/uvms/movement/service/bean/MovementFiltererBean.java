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

import eu.europa.ec.fisheries.schema.movement.area.v1.AreaType;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.model.exception.InvalidArgumentException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelRuntimeException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class MovementFiltererBean {

    @Inject
    private MovementService movementService;

    public List<String> filterGuidListForPeriodAndAreaTypesByArea(List<String> guidList, Date startDate, Date endDate, List<AreaType> areaTypes) throws MovementModelRuntimeException {
        checkArguments(guidList, startDate, endDate, areaTypes);

        List<Long> movementAreaIds = movementService.findMovementAreaIdsByAreaRemoteIdAndNameList(areaTypes);
        if (movementAreaIds == null || movementAreaIds.isEmpty()) {
            throw new MovementModelRuntimeException("No movement area ids found", ErrorCode.MOVEMENT_DAO_ERROR);
        }

        return guidList.stream()
                .filter(connectId -> movementService.checkMovementExistence(connectId,startDate,endDate,movementAreaIds))
                .collect(Collectors.toList());
    }

    private void checkArguments(List<String> guidList, Date startDate, Date endDate, List<AreaType> areaTypes) {
        if(guidList == null || guidList.isEmpty()) throw new InvalidArgumentException("No Guid list to filter");
        if(startDate == null) throw new InvalidArgumentException("No start date provided/or invalid syntax, try UTC");
        if(endDate == null) throw new InvalidArgumentException("No end date provided/or invalid syntax, try UTC");
        if(startDate.toInstant().isAfter(endDate.toInstant())) throw new InvalidArgumentException("Start date cannot be after end date");
        if(areaTypes == null || areaTypes.isEmpty()) throw new InvalidArgumentException("Area type list was empty");
    }
}
