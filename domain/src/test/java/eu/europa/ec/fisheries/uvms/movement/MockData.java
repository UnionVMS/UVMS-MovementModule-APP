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
package eu.europa.ec.fisheries.uvms.movement;

import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Areatransition;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Movementarea;
import java.util.Arrays;
import java.util.List;

public class MockData {

    public static Areatransition getAreaTransition(String code, MovementTypeType transitionType) {
        Areatransition transition = new Areatransition();
        transition.setAreatranAreaId(getArea(code));
        return transition;
    }

    public static Area getArea(String areaCode) {
        Area area = new Area();
        area.setAreaCode(areaCode);
        area.setAreaName(areaCode);
        area.setAreaType(getAraType(areaCode));
        return area;
    }

    public static AreaType getAraType(String name) {
        AreaType areaType = new AreaType();
        areaType.setName(name);
        return areaType;
    }

    /**
     * Get a movement type with an added metadata and areas in the metadata
     * depending on how many areas you want ( numberOfAreas )
     *
     * @param numberOfAreas
     * @return
     */
    public static MovementType getMappedMovement(int numberOfAreas) {
        MovementType type = new MovementType();
        MovementMetaData metaData = new MovementMetaData();

        for (int i = 0; i < numberOfAreas; i++) {
            metaData.getAreas().add(getMovementMetadataType("AREA" + i));
        }

        type.setMetaData(metaData);
        return type;
    }

    public static MovementMetaDataAreaType getMovementMetadataType(String areaCode) {
        MovementMetaDataAreaType area = new MovementMetaDataAreaType();
        area.setCode(areaCode);
        area.setName(areaCode);
        area.setAreaType(areaCode);
        return area;
    }

    public static Movement getCurrentMovement(int areaId) {
        Movement currentMovement = new Movement();
        Movementarea currentMoveArea = new Movementarea();
        Area currentArea = new Area();
        currentArea.setAreaId(Long.valueOf(areaId));
        currentMoveArea.setMovareaAreaId(currentArea);
        List<Movementarea> currentMoveAreaList = Arrays.asList(currentMoveArea);
        currentMovement.setMovementareaList(currentMoveAreaList);
        return currentMovement;
    }

    public static Movement getPreviousMovement(int areaId, MovementTypeType movementType) {
        Movement previousMovement = new Movement();
        Areatransition privoisTransition = new Areatransition();
        Area previousArea = new Area();
        previousArea.setAreaId(Long.valueOf(areaId));
        privoisTransition.setAreatranAreaId(previousArea);
        privoisTransition.setMovementType(movementType);
        List<Areatransition> previousMoveAreaList = Arrays.asList(privoisTransition);
        previousMovement.setAreatransitionList(previousMoveAreaList);
        return previousMovement;
    }

}