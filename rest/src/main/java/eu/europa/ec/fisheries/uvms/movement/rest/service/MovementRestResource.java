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
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.model.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.rest.RestUtilMapper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovement;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementEntityToModelMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/movement")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class MovementRestResource {

    private static final Logger LOG = LoggerFactory.getLogger(MovementRestResource.class);

    @EJB
    private MovementService serviceLayer;

    @Inject
    private MovementDao movementDao;

    @Context 
    private HttpServletRequest request;

    @POST
    @Path("/list")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getListByQuery(MovementQuery query) {
        try {
            GetMovementListByQueryResponse list = serviceLayer.getList(query);
            return Response.ok(list).build();
        } catch (Exception ex) {
            LOG.error("[ Error when getting list. ]", ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex).build();
        }
    }

    @POST
    @Path("/list/minimal")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getMinimalListByQuery(MovementQuery query) {
        LOG.debug("Get list invoked in rest layer");
        try {
            long start = System.currentTimeMillis();
            GetMovementListByQueryResponse minimalList = serviceLayer.getMinimalList(query);
            long end = System.currentTimeMillis();
            LOG.debug("GET MINIMAL MOVEMENT: {} ms", (end - start));
            return Response.ok(minimalList).build();
        } catch (Exception ex) {
            LOG.error("[ Error when getting list. ]", ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex).build();
        }
    }

    @POST
    @Path("/latest")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getLatestMovementsByConnectIds(List<String> connectIds) {
        LOG.debug("GetLatestMovementsByConnectIds invoked in rest layer");
        if (connectIds == null || connectIds.isEmpty()) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("ConnectIds cannot be empty").build();
        }
        try {
            List<UUID> uuids = connectIds.stream().map(UUID::fromString).collect(Collectors.toList());
            List<Movement> latestMovements = serviceLayer.getLatestMovementsByConnectIds(uuids);
            List<MovementType> movementTypeList = MovementEntityToModelMapper.mapToMovementType(latestMovements);
            List<MovementDto> movementDtoList = MovementMapper.mapToMovementDtoList(movementTypeList);
            return Response.ok(movementDtoList).build();
        } catch (Exception ex) {
            LOG.error("[ Error when getting list. ]", ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex).build();
        }
    }

    @GET
    @Path("/latest/{numberOfMovements}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getLatestMovements(@PathParam(value = "numberOfMovements") Integer numberOfMovements) {
        LOG.debug("getLatestMovements invoked in rest layer");
        long start = System.currentTimeMillis();
        // TODO why not default to 1 ?
        if (numberOfMovements == null || numberOfMovements < 1) {
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("numberOfMovements cannot be null and must be greater than 0").build();
        }
        try {
            List<Movement> movements = serviceLayer.getLatestMovements(numberOfMovements);
            List<MovementType> latestMovements = MovementEntityToModelMapper.mapToMovementType(movements);
            List<MovementDto> response = MovementMapper.mapToMovementDtoList(latestMovements);
            LOG.debug("GET LATEST MOVEMENTS TIME: {}", (System.currentTimeMillis() - start));
            return Response.ok(response).build();
        } catch (Exception ex) {
            LOG.error("[ Error when getting list. ]", ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex).build();
        }
    }

    @GET
    @Path("/{id}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getById(@PathParam(value = "id") final String id) {
        LOG.debug("Get by id invoked in rest layer");
        try {
            Movement movement = serviceLayer.getById(UUID.fromString(id));
            if (movement == null) {
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity("No movement with ID " + id).build();
            }
            MovementType response = MovementEntityToModelMapper.mapToMovementType(movement);

            return Response.ok(response).build();
        } catch (NonUniqueResultException ex) {
            LOG.error("[ Error when getting by id. ]", ex);
            return Response.status(Status.CONFLICT).entity(ex).build();
        } catch (Exception ex) {
            LOG.error("[ Error when getting by id. ] ", ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex).build();
        }

    }

    @POST
    @Path("/movementMap")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getMapByQuery(MovementQuery query) {
        try {
            GetMovementMapByQueryResponse mapByQuery = serviceLayer.getMapByQuery(query);
            return Response.ok(mapByQuery).build();
        } catch (Exception ex) {
            LOG.error("[ Error when getting movement map. ]", ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex).build();
        }
    }

    @POST
    @Path("/track/latest/asset/{id}/")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getMicroMovementTrackForAssetByNumber(@PathParam("id") UUID connectId, @DefaultValue("2000") @QueryParam("maxNbr") Integer maxNumber, List<String> sources) {
        try {

            List<MovementSourceType> sourceTypes = RestUtilMapper.convertToMovementSourceTypes(sources);
            List<Movement> movements = movementDao.getLatestNumberOfMovementsForAsset(connectId, maxNumber, sourceTypes);
            return Response.ok(movements).header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("Error when getting Movement for connectId: {}", connectId, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }


}
