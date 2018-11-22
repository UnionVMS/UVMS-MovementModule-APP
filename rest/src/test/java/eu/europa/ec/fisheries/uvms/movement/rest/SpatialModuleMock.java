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
package eu.europa.ec.fisheries.uvms.movement.rest;

import java.util.List;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;

@Path("spatial/spatialnonsecure/json")
@Stateless
public class SpatialModuleMock {



    @POST
    @Path("getSegmentCategoryType")
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    public Response getSegmentCategoryType(List<MovementType> movements) {
        if(shouldIFail()){
            return Response.status(668).build();
        }
        if(movements.get(1).getPosition().getLongitude() == 10 && movements.get(1).getPosition().getLatitude() == 20){
            return Response.ok(SegmentCategoryType.EXIT_PORT).build();
        }
        return Response.ok(SegmentCategoryType.IN_PORT).build();
    }


    private boolean shouldIFail() {
        String fail = System.getProperty("MESSAGE_PRODUCER_METHODS_FAIL", "false");
        if(!"false".equals(fail.toLowerCase())) {
            return true;
        }
        return false;
    }
    
}
