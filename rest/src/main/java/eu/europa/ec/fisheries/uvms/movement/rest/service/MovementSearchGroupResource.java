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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseCode;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseDto;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;

@Path("/search")
@Stateless
public class MovementSearchGroupResource {

    final static Logger LOG = LoggerFactory.getLogger(MovementSearchGroupResource.class);

    @EJB
    MovementSearchGroupService service;

    @Context
    private HttpServletRequest request;

    /**
     * @param searchGroup a search group to be created
     * @summary Creates a new movement search group.
     * @return the created search group
     */
    @POST
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Path("/group")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto createMovementSearchGroup(final MovementSearchGroup searchGroup) {
        try {
            final MovementSearchGroup createdSearchGroup = service.createMovementSearchGroup(searchGroup, request.getRemoteUser());
            return new ResponseDto(createdSearchGroup, ResponseCode.OK);
        } catch (final MovementServiceException e) {
            LOG.error("[ Error when creating movement search group. ] {}", e.getMessage(), e);
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        } catch (final MovementDuplicateException e) {
            LOG.error("[ Error when creating movement search group. ] {}", e.getMessage(), e);
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR_DUPLICTAE);
        }
    }

    /**
     * @param id the ID of a known movement search group
     * @summary Returns the movement search group with the provided ID.
     * @return a movement search group
     */
    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/group/{id}")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto getMovementSearchGroup(@PathParam("id") final Long id) {
        try {
            final MovementSearchGroup searchGroup = service.getMovementSearchGroup(id);
            return new ResponseDto(searchGroup, ResponseCode.OK);
        } catch (final MovementServiceException e) {
            LOG.error("[ Error when getting movement search group. ] {}", e.getMessage(), e);
            return new ResponseDto(e.getMessage() + ": " + e.getCause().getMessage(), ResponseCode.ERROR);
        } catch (final MovementDuplicateException e) {
            LOG.error("[ Error when getting movement search group. ] {}", e.getMessage(), e);
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR_DUPLICTAE);
        }
    }

    /**
     * @param searchGroup a search group object
     * @summary Updates an existing movement search group with new values.
     * @return the updated movement search group.
     */
    @PUT
    @Path("/group")
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto updateMovementSearchGroup(final MovementSearchGroup searchGroup) {
        try {
            final MovementSearchGroup updatedSearchGroup = service.updateMovementSearchGroup(searchGroup, request.getRemoteUser());
            return new ResponseDto(updatedSearchGroup, ResponseCode.OK);
        } catch (final MovementServiceException e) {
            LOG.error("[ Error when updating movement search group. ] {}", e.getMessage(), e);
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        } catch (final MovementDuplicateException e) {
            LOG.error("[ Error when updating movement search group. ] {}", e.getMessage(), e);
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR_DUPLICTAE);
        }
    }

    /**
     * @param user a user name
     * @summary Lists movement search groups by query.
     * @return a list of movement search groups for the provided user
     */
    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Path("/groups")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto getMovementSearchGroupsByUser(@QueryParam(value = "user") final String user) {
        try {
            final List<MovementSearchGroup> searchGroups = service.getMovementSearchGroupsByUser(user);
            return new ResponseDto(searchGroups, ResponseCode.OK);
        } catch (final MovementServiceException e) {
            LOG.error("[ Error when getting movement search groups by user. ] {}", e.getMessage(), e);
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        } catch (final MovementDuplicateException e) {
            LOG.error("[ Error when getting movement search groups by user. ] {}", e.getMessage(), e);
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR_DUPLICTAE);
        }
    }

    /**
     * @param id the ID of a movement search group to be deleted
     * @summary Deletes the movement search group with the provided ID.
     * @return the deleted movement search group
     */
    @DELETE
    @Path("/group/{id}")
    @Produces(value = {MediaType.APPLICATION_JSON})
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto deleteMovementSearchGroup(@PathParam(value = "id") final Long id) {
        try {
            final MovementSearchGroup deletedSearchGroup = service.deleteMovementSearchGroup(id);
            return new ResponseDto(deletedSearchGroup, ResponseCode.OK);
        } catch (final MovementServiceException e) {
            LOG.error("[ Error when deleting movement search group. ] {}", e.getMessage(), e);
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        } catch (final MovementDuplicateException e) {
            LOG.error("[ Error when deleting movement search group. ] {}", e.getMessage(), e);
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR_DUPLICTAE);
        }
    }
}