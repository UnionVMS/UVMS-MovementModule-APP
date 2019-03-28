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
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseDto;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.RestResponseCode;
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
    public ResponseDto<?> create(final TempMovementType data) {
        LOG.debug("Create temp movement invoked in rest layer");
        try {
            DraftMovement draftMovement = DraftMovementMapper.toTempMovementEntity(data, request.getRemoteUser());
            draftMovement = service.createDraftMovement(draftMovement, request.getRemoteUser());
            TempMovementType tempMovementType = DraftMovementMapper.toTempMovement(draftMovement);
            return new ResponseDto<>(tempMovementType, RestResponseCode.OK);
        } catch (Throwable throwable) {
            LOG.error("[ Error when creating. ] {} ", throwable);
            return new ResponseDto<>(throwable.getMessage(), RestResponseCode.ERROR);
        }
    }

    @GET
    @Path("/{guid}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto<?> get(@PathParam("guid") String guid) {
        try {
            DraftMovement draftMovement = service.getDraftMovement(UUID.fromString(guid));
            TempMovementType tempMovementType = DraftMovementMapper.toTempMovement(draftMovement);
            return new ResponseDto<>(tempMovementType, RestResponseCode.OK);
        } catch (Exception ex) {
            LOG.error("[ Error when getting temp movement with GUID. ] {} ", ex);
            return new ResponseDto<>(ex.getMessage(), RestResponseCode.ERROR);
        }
    }

    @PUT
    @Path("/remove/{guid}")
    @RequiresFeature(UnionVMSFeature.manageManualMovements)
    public ResponseDto<?> remove(@PathParam("guid") String guid) {
        LOG.debug("Archive(remove) temp movement invoked in rest layer");
        try {
            DraftMovement draftMovement = service.archiveDraftMovement(UUID.fromString(guid), request.getRemoteUser());
            TempMovementType tempMovementType = DraftMovementMapper.toTempMovement(draftMovement);
            return new ResponseDto<>(tempMovementType, RestResponseCode.OK);
        } catch (Exception ex) {
            LOG.error("[ Error when archiving temp movement ] {} ", ex);
            return new ResponseDto<>(ex.getMessage(), RestResponseCode.ERROR);
        }
    }

    @PUT
    @RequiresFeature(UnionVMSFeature.manageManualMovements)
    public ResponseDto<?> update(final TempMovementType data) {
        LOG.debug("Update temp movement invoked in rest layer");
        try {
            DraftMovement draftMovement = DraftMovementMapper.toTempMovementEntity(data, request.getRemoteUser());
            draftMovement = service.updateDraftMovement(draftMovement, request.getRemoteUser());
            TempMovementType tempMovementType = DraftMovementMapper.toTempMovement(draftMovement);
            return new ResponseDto<>(tempMovementType, RestResponseCode.OK);
        } catch (Exception ex) {
            LOG.error("[ Error while updating temp movement. ] {} ", ex);
            return new ResponseDto<>(ex.getMessage(), RestResponseCode.ERROR);
        }
    }

    @POST
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/list")
    @RequiresFeature(UnionVMSFeature.viewManualMovements)
    public ResponseDto<?> getDraftMovements(MovementQuery query) {
        LOG.debug("List all active temp movement invoked in rest layer");
        try {
            return new ResponseDto<>(service.getDraftMovements(query), RestResponseCode.OK);
        } catch (Exception ex) {
            LOG.error("[ Error while getting list. ] {} ", ex);
            return new ResponseDto<>(ex.getMessage(), RestResponseCode.ERROR);
        }
    }

    @PUT
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/send/{guid}")
    @RequiresFeature(UnionVMSFeature.manageManualMovements)
    public ResponseDto<?> send(@PathParam("guid") String guid) {
        LOG.debug("Send temp movement invoked in rest layer");
        try {
            DraftMovement draftMovement = service.sendDraftMovement(UUID.fromString(guid), request.getRemoteUser());
            TempMovementType tempMovementType = DraftMovementMapper.toTempMovement(draftMovement);
            return new ResponseDto<>(tempMovementType, RestResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error while sending temp movement] {} ", e);
            return new ResponseDto<>(e.getMessage(), RestResponseCode.ERROR);
        }
    }

    @GET
    @Path("/archive/{guid}")
    @RequiresFeature(UnionVMSFeature.manageManualMovements)
    public ResponseDto<?> archiveDraftMovement(@PathParam("guid") String guid) {
        LOG.debug("Archive movement");
        try {
            DraftMovement draftMovement = service.archiveDraftMovement(UUID.fromString(guid), request.getRemoteUser());
            TempMovementType tempMovementType = DraftMovementMapper.toTempMovement(draftMovement);
            return new ResponseDto<>(tempMovementType, RestResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error while creating temp movement ] {} ", e);
            return new ResponseDto<>(e.getMessage(), RestResponseCode.ERROR);
        }
    }
}
