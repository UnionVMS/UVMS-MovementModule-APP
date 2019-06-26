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

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementSearchGroupService;
import eu.europa.ec.fisheries.uvms.movement.service.entity.group.MovementFilterGroup;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementGroupMapper;
import eu.europa.ec.fisheries.uvms.movement.service.util.CalculationUtil;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/search")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class MovementSearchGroupResource {

    private static final Logger LOG = LoggerFactory.getLogger(MovementSearchGroupResource.class);

    @EJB
    private MovementSearchGroupService service;

    @Context
    private HttpServletRequest request;

    @POST
    @Path("/group")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response createMovementSearchGroup(MovementSearchGroup searchGroup) {
        try {
            MovementFilterGroup createdFilterGroup = service.createMovementFilterGroup(searchGroup, request.getRemoteUser());
            MovementSearchGroup movementSearchGroup = MovementGroupMapper.toMovementSearchGroup(createdFilterGroup);
            return Response.ok(movementSearchGroup).build();
        } catch (Exception e) {
            LOG.error("[ Error when creating movement search group. ] {}", e.getMessage(), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/group/{id}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getMovementSearchGroup(@PathParam("id") BigInteger id) {
        try {
            UUID uuid = CalculationUtil.convertFromBigInteger(id);
            MovementFilterGroup filterGroup = service.getMovementFilterGroup(uuid);
            MovementSearchGroup searchGroup = MovementGroupMapper.toMovementSearchGroup(filterGroup);
            return Response.ok(searchGroup).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting movement search group. ] {}", e.getMessage(), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/group")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response updateMovementSearchGroup(MovementSearchGroup searchGroup) {
        try {
            MovementFilterGroup updatedFilterGroup = service.updateMovementFilterGroup(searchGroup, request.getRemoteUser());
            MovementSearchGroup movementSearchGroup = MovementGroupMapper.toMovementSearchGroup(updatedFilterGroup);
            return Response.ok(movementSearchGroup).build();
        } catch (Exception e) {
            LOG.error("[ Error when updating movement search group. ] {}", e.getMessage(), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/groups")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getMovementSearchGroupsByUser(@QueryParam(value = "user") String user) {
        try {
            List<MovementFilterGroup> filterGroups = service.getMovementFilterGroupsByUser(user);
            List<MovementSearchGroup> searchGroups = new ArrayList<>();
            for (MovementFilterGroup filterGroup : filterGroups) {
                searchGroups.add(MovementGroupMapper.toMovementSearchGroup(filterGroup));
            }
            return Response.ok(searchGroups).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting movement search groups by user. ] {}", e.getMessage(), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/group/{id}")
    @Produces(value = {MediaType.APPLICATION_JSON})
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response deleteMovementSearchGroup(@PathParam(value = "id") BigInteger id) {
        try {
            UUID uuid = CalculationUtil.convertFromBigInteger(id);
            MovementFilterGroup deletedSearchGroup = service.deleteMovementFilterGroup(uuid);
            MovementSearchGroup movementSearchGroup = MovementGroupMapper.toMovementSearchGroup(deletedSearchGroup);
            return Response.ok(movementSearchGroup).build();
        } catch (Exception e) {
            LOG.error("[ Error when deleting movement search group. ] {}", e.getMessage(), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
