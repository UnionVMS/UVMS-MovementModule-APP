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

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseCode;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseDto;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;

@Path("/tempmovement")
@Stateless
public class TempMovementResource {

    final static Logger LOG = LoggerFactory.getLogger(TempMovementResource.class);

    @EJB
    TempMovementService service;

    @Context
    private HttpServletRequest request;


    /**
     *
     * @responseMessage 200 Temp Movement successfully created
     * @responseMessage 500 Error when creating Temp Movement
     *
     * @summary Creates a temp movement
     *
     */
    @POST
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @RequiresFeature(UnionVMSFeature.manageManualMovements)
    public ResponseDto create(final TempMovementType data) {
        LOG.debug("Create temp movement invoked in rest layer");
        try {
            return new ResponseDto(service.createTempMovement(data, request.getRemoteUser()), ResponseCode.OK);
        } catch (final Throwable throwable) {
            LOG.error("[ Error when creating. ] {} ", throwable);
            return new ResponseDto(throwable.getMessage(), ResponseCode.ERROR);
        }
    }

    @GET
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/{guid}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto get(@PathParam("guid") final String guid) {
        try {
            return new ResponseDto(service.getTempMovement(guid), ResponseCode.OK);
        } catch (MovementServiceException | NullPointerException e) {
            LOG.error("[ Error when getting temp movement with GUID. ] {} ", e.getStackTrace());
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        } catch (final MovementDuplicateException ex) {
            LOG.error("[ Error when creating. ] {} ", ex.getStackTrace());
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR_DUPLICTAE);
        }
    }

    /**
     *
     * @responseMessage 200 Temp Movement successfully archived
     * @responseMessage 500 Error when archiving Temp Movement
     *
     * @summary Archives a temp movement
     *
     */
    @PUT
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/remove/{guid}")
    @RequiresFeature(UnionVMSFeature.manageManualMovements)
    public ResponseDto remove(@PathParam("guid") final String guid) {
        LOG.debug("Archive(remove) temp movement invoked in rest layer");
        try {
            return new ResponseDto(service.archiveTempMovement(guid, request.getRemoteUser()), ResponseCode.OK);
        } catch (MovementServiceException | NullPointerException ex) {
            LOG.error("[ Error when archiving temp movement. ] {} ", ex.getStackTrace());
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR);
        } catch (final MovementDuplicateException ex) {
            LOG.error("[ Error when creating. ] {} ", ex.getStackTrace());
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR_DUPLICTAE);
        }
    }

    /**
     *
     * @responseMessage 200 Temp Movement successfully updated
     * @responseMessage 500 Error when updating Temp Movement
     *
     * @summary Updates a temp movement
     *
     */
    @PUT
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @RequiresFeature(UnionVMSFeature.manageManualMovements)
    public ResponseDto update(final TempMovementType data) {
        LOG.debug("Update temp movement invoked in rest layer");
        try {
            return new ResponseDto(service.updateTempMovement(data, request.getRemoteUser()), ResponseCode.OK);
        } catch (MovementServiceException | NullPointerException ex) {
            LOG.error("[ Error when updating temp movement. ] {} ", ex.getStackTrace());
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR);
        } catch (final MovementDuplicateException ex) {
            LOG.error("[ Error when creating. ] {} ", ex.getStackTrace());
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR_DUPLICTAE);
        }
    }

    /**
     *
     * @responseMessage 200 All active temp movements fetched
     * @responseMessage 500 Error when fetching active temp movements
     *
     * @summary List all active temp movements
     *
     */
    @POST
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/list")
    @RequiresFeature(UnionVMSFeature.viewManualMovements)
    public ResponseDto<TempMovementListResponseDto> getTempMovements(final MovementQuery query) {
        LOG.debug("List all active temp movement invoked in rest layer");
        try {
            return new ResponseDto(service.getTempMovements(query), ResponseCode.OK);
        } catch (MovementServiceException | NullPointerException ex) {
            LOG.error("[ Error when listing active temp movements. ] {} ", ex.getStackTrace());
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR);
        } catch (final MovementDuplicateException ex) {
            LOG.error("[ Error when creating. ] {} ", ex.getStackTrace());
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR_DUPLICTAE);
        }
    }

    /**
     *
     * @responseMessage 200 Temp Movement successfully sent
     * @responseMessage 500 Error when sending Temp Movement
     *
     * @summary Sends a temp movement
     *
     */
    @PUT
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/send/{guid}")
    @RequiresFeature(UnionVMSFeature.manageManualMovements)
    public ResponseDto send(@PathParam("guid") final String guid) {
        LOG.debug("Send temp movement invoked in rest layer");
        try {
            return new ResponseDto(service.sendTempMovement(guid, request.getRemoteUser()), ResponseCode.OK);
        } catch (final MovementDuplicateException e) {
            LOG.error("[ Error: Cannot create duplicate movement] {} ", e.getStackTrace());
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR_DUPLICTAE);
        } catch (MovementServiceException | NullPointerException ex) {
            LOG.error("[ Error when sending temp movement. ] {} ", ex.getStackTrace());
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR);
        }
    }

}