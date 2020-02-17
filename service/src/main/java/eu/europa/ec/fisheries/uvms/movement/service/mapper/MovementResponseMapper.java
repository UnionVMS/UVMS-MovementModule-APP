/*
Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
Â© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.movement.service.mapper;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementMapResponseType;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetTempMovementListResponse;
import eu.europa.ec.fisheries.uvms.movement.model.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.model.dto.ListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.model.dto.TempMovementsListResponseDto;

import java.util.List;


public class MovementResponseMapper {

    private MovementResponseMapper() {}
    
    /**
     * Creates and marshalls a type to a response marshalled as a XML String
     * representation that can be sent as a message on the JSM queue with the
     * help of JMS TextMessage
     *
     * @param responseDto
     * @return
     */
    public static GetTempMovementListResponse tempMovementListResponse(TempMovementsListResponseDto responseDto) {
        GetTempMovementListResponse response = new GetTempMovementListResponse();
        response.getMovement().addAll(responseDto.getTempMovementList());
        response.setCurrentPage(responseDto.getCurrentPage());
        response.setTotalNumberOfPages(responseDto.getTotalNumberOfPages());
        return response;
    }

    /**
     * Creates and marshalls a type to a response marshalled as a XML String
     * representation that can be sent as a message on the JSM queue with the
     * help of JMS TextMessage
     *
     * @param mapResponse
     * @return
     */
    public static GetMovementMapByQueryResponse createMovementMapResponse(List<MovementMapResponseType> mapResponse) {
        GetMovementMapByQueryResponse response = new GetMovementMapByQueryResponse();
        response.getMovementMap().addAll(mapResponse);
        return response;
    }

    /**
     * Creates and marshalls a type to a response marshalled as a XML String
     * representation that can be sent as a message on the JSM queue with the
     * help of JMS TextMessage
     *
     * @param responseDto
     * @return
     */
    public static GetMovementListByQueryResponse createMovementListResponse(ListResponseDto responseDto) {
        GetMovementListByQueryResponse response = new GetMovementListByQueryResponse();
        response.getMovement().addAll(responseDto.getMovementList());
        response.setCurrentPage(responseDto.getCurrentPage());
        response.setTotalNumberOfPages(responseDto.getTotalNumberOfPages());
        return response;
    }

}
