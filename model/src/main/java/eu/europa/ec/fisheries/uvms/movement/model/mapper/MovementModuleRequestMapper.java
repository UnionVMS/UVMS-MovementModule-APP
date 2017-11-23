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
package eu.europa.ec.fisheries.uvms.movement.model.mapper;

import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByQueryRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementMapByQueryRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.MovementModuleMethod;
import eu.europa.ec.fisheries.schema.movement.module.v1.PingRequest;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import java.util.List;

/**
 **/
public class MovementModuleRequestMapper {

    public static String mapToCreateMovementRequest(MovementBaseType baseType, String username) throws ModelMarshallException {
        CreateMovementRequest request = new CreateMovementRequest();
        request.setMethod(MovementModuleMethod.CREATE);
        request.setUsername(username);
        request.setMovement(baseType);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String mapToCreateMovementBatchRequest(List<MovementBaseType> baseTypeList) throws ModelMarshallException {
        CreateMovementBatchRequest request = new CreateMovementBatchRequest();
        request.setMethod(MovementModuleMethod.CREATE_BATCH);
        if (baseTypeList != null) {
            request.getMovement().addAll(baseTypeList);
        }
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String mapToGetMovementMapByQueryRequest(MovementQuery query) throws ModelMarshallException {
        GetMovementMapByQueryRequest request = new GetMovementMapByQueryRequest();
        request.setMethod(MovementModuleMethod.MOVEMENT_MAP);
        request.setQuery(query);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String mapToGetMovementListByQueryRequest(MovementQuery query) throws ModelMarshallException {
        GetMovementListByQueryRequest request = new GetMovementListByQueryRequest();
        request.setMethod(MovementModuleMethod.MOVEMENT_LIST);
        request.setQuery(query);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

    public static String mapToPingRequest(MovementQuery query) throws ModelMarshallException {
        PingRequest request = new PingRequest();
        request.setMethod(MovementModuleMethod.PING);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

}