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
package eu.europa.ec.fisheries.uvms.movement.rest.service;

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.AreaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.area.v1.AreaType;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseCode;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;

@Stateless
@Path("/areas")
@RequiresFeature(UnionVMSFeature.viewMovements)
public class AreaRestResource {

    private final static Logger LOG = LoggerFactory.getLogger(MovementRestResource.class);

    @EJB
    private MovementService movementService;

    @GET
    @Produces(value = {MediaType.APPLICATION_JSON})
    public ResponseDto getAreas() {
        try {
            List<Area> areas = movementService.getAreas();
            List<AreaType> response = AreaMapper.mapToAreaTypes(areas);
            return new ResponseDto<>(response, ResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting areas. ] ", e);
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        }
    }

    @GET
    @Path("/{code}")
    @Produces(value = {MediaType.APPLICATION_JSON})
    public ResponseDto<AreaType> getAreaByCode(@PathParam(value = "code") final String areaCode) {
        try {
            Area area = movementService.getAreaByCode(areaCode);
            AreaType response = AreaMapper.mapToAreaType(area);
            return new ResponseDto<>(response, ResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting AreaType. ] ", e);
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        }
    }
}
