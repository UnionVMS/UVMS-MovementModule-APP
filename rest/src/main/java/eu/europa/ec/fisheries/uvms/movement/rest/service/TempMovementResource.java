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

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetTempMovementListResponse;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.service.bean.DraftMovementService;
import eu.europa.ec.fisheries.uvms.movement.service.entity.temp.DraftMovement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.DraftMovementMapper;
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
import java.util.UUID;

@Path("/tempmovement")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class TempMovementResource {

    private final static Logger LOG = LoggerFactory.getLogger(TempMovementResource.class);

    @EJB
    private DraftMovementService service;

    @Context
    private HttpServletRequest request;

    @POST
    @RequiresFeature(UnionVMSFeature.manageManualMovements)
    public Response create(final TempMovementType data) {
        LOG.debug("Create temp movement invoked in rest layer");
        try {
            DraftMovement draftMovement = DraftMovementMapper.toTempMovementEntity(data, request.getRemoteUser());
            draftMovement = service.createDraftMovement(draftMovement, request.getRemoteUser());
            TempMovementType tempMovementType = DraftMovementMapper.toTempMovement(draftMovement);
            return Response.ok(tempMovementType).build();
        } catch (Throwable throwable) {
            LOG.error("[ Error when creating. ] {} ", throwable);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(throwable.getMessage()).build();
        }
    }

    @GET
    @Path("/{guid}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response get(@PathParam("guid") String guid) {
        try {
            DraftMovement draftMovement = service.getDraftMovement(UUID.fromString(guid));
            TempMovementType tempMovementType = DraftMovementMapper.toTempMovement(draftMovement);
            return Response.ok(tempMovementType).build();
        } catch (Exception ex) {
            LOG.error("[ Error when getting temp movement with GUID. ] {} ", ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @PUT
    @Path("/remove/{guid}")
    @RequiresFeature(UnionVMSFeature.manageManualMovements)
    public Response remove(@PathParam("guid") String guid) {
        LOG.debug("Archive(remove) temp movement invoked in rest layer");
        try {
            DraftMovement draftMovement = service.archiveDraftMovement(UUID.fromString(guid), request.getRemoteUser());
            TempMovementType tempMovementType = DraftMovementMapper.toTempMovement(draftMovement);
            return Response.ok(tempMovementType).build();
        } catch (Exception ex) {
            LOG.error("[ Error when archiving temp movement ] {} ", ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @PUT
    @RequiresFeature(UnionVMSFeature.manageManualMovements)
    public Response update(final TempMovementType data) {
        LOG.debug("Update temp movement invoked in rest layer");
        try {
            DraftMovement draftMovement = DraftMovementMapper.toTempMovementEntity(data, request.getRemoteUser());
            draftMovement = service.updateDraftMovement(draftMovement, request.getRemoteUser());
            TempMovementType tempMovementType = DraftMovementMapper.toTempMovement(draftMovement);
            return Response.ok(tempMovementType).build();
        } catch (Exception ex) {
            LOG.error("[ Error while updating temp movement. ] {} ", ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("/list")
    @RequiresFeature(UnionVMSFeature.viewManualMovements)
    public Response getDraftMovements(MovementQuery query) {
        LOG.debug("List all active temp movement invoked in rest layer");
        try {
            GetTempMovementListResponse draftMovements = service.getDraftMovements(query);
            return Response.ok(draftMovements).build();
        } catch (Exception ex) {
            LOG.error("[ Error while getting list. ] {} ", ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @PUT
    @Path("/send/{guid}")
    @RequiresFeature(UnionVMSFeature.manageManualMovements)
    public Response send(@PathParam("guid") String guid) {
        LOG.debug("Send temp movement invoked in rest layer");
        try {
            DraftMovement draftMovement = service.sendDraftMovement(UUID.fromString(guid), request.getRemoteUser());
            TempMovementType tempMovementType = DraftMovementMapper.toTempMovement(draftMovement);
            return Response.ok(tempMovementType).build();
        } catch (Exception ex) {
            LOG.error("[ Error while sending temp movement] {} ", ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("/archive/{guid}")
    @RequiresFeature(UnionVMSFeature.manageManualMovements)
    public Response archiveDraftMovement(@PathParam("guid") String guid) {
        LOG.debug("Archive movement");
        try {
            DraftMovement draftMovement = service.archiveDraftMovement(UUID.fromString(guid), request.getRemoteUser());
            TempMovementType tempMovementType = DraftMovementMapper.toTempMovement(draftMovement);
            return Response.ok(tempMovementType).build();
        } catch (Exception ex) {
            LOG.error("[ Error while creating temp movement ] {} ", ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }
}
