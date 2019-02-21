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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseDto;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.RestResponseCode;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.LatestMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MicroMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementEntityToModelMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;

@Path("/movement")
@Stateless
public class MovementRestResource {

    private static final Logger LOG = LoggerFactory.getLogger(MovementRestResource.class);

    @EJB
    private MovementService serviceLayer;

    @Inject
    private MovementDao movementDao;

    @Context 
    private HttpServletRequest request;

    /**
     *
     * @responseMessage 200 Movement list successfully retreived
     * @responseMessage 500 Error when retrieveing the list values for
     * transponders
     *
     * @summary Gets a list of movements filtered by a query
     *
     */
    @POST
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/list")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto<MovementListResponseDto> getListByQuery(MovementQuery query) {
        try {
            return new ResponseDto(serviceLayer.getList(query), RestResponseCode.OK);
        } catch (Exception ex) {
            LOG.error("[ Error when getting list. ]", ex);
            return new ResponseDto(ex, RestResponseCode.ERROR);
        }
    }

    /**
     *
     * @responseMessage 200 Movement list successfully retreived
     * @responseMessage 500 Error when retrieveing the list values for
     * transponders
     *
     * @summary Gets a list of movements filtered by a query with minimal information
     *
     */
    @POST
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/list/minimal")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto<MovementListResponseDto> getMinimalListByQuery(MovementQuery query) {
        LOG.debug("Get list invoked in rest layer");
        try {
            long start = System.currentTimeMillis();
            ResponseDto response = new ResponseDto(serviceLayer.getMinimalList(query), RestResponseCode.OK);
            long end = System.currentTimeMillis();
            LOG.debug("GET MINIMAL MOVEMENT: {} ms", (end - start));
            return response;
        } catch (Exception ex) {
            LOG.error("[ Error when getting list. ]", ex);
            return new ResponseDto(ex, RestResponseCode.ERROR);
        }
    }

    /**
     *
     * @responseMessage 200 Movement list successfully retreived
     * @responseMessage 500 Error when retrieveing the list values for
     * transponders
     *
     * @summary Gets the latest movements for the selected connectIds
     *
     */
    @POST
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/latest")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto<List<MovementDto>> getLatestMovementsByConnectIds(List<String> connectIds) {
        LOG.debug("GetLatestMovementsByConnectIds invoked in rest layer");
        if (connectIds == null || connectIds.isEmpty()) {
            return new ResponseDto("ConnectIds cannot be empty" , RestResponseCode.ERROR);
        }
        try {
            List<UUID> uuids = connectIds.stream().map(UUID::fromString).collect(Collectors.toList());
            List<Movement> latestMovements = serviceLayer.getLatestMovementsByConnectIds(uuids);
            List<MovementType> movementTypeList = MovementEntityToModelMapper.mapToMovementType(latestMovements);
            List<MovementDto> movementDtoList = MovementMapper.mapToMovementDtoList(movementTypeList);
            return new ResponseDto<>(movementDtoList, RestResponseCode.OK);
        } catch (Exception ex) {
            LOG.error("[ Error when getting list. ]", ex);
            return new ResponseDto(ex, RestResponseCode.ERROR);
        }
    }

    /**
     *
     * @responseMessage 200 Movement list successfully retreived
     * @responseMessage 500 Error when retrieveing the list values for
     * transponders
     *
     * @summary Gets the latest movements for the selected connectIds
     *
     */
    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/latest/{numberOfMovements}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto<List<MovementDto>> getLatestMovements(@PathParam(value = "numberOfMovements") Integer numberOfMovements) {
        LOG.debug("getLatestMovements invoked in rest layer");
        long start = System.currentTimeMillis();
        // TODO why not default to 1 ?
        if (numberOfMovements == null || numberOfMovements < 1) {
            return new ResponseDto("numberOfMovements cannot be null and must be greater than 0" , RestResponseCode.ERROR);
        }
        try {
            List<LatestMovement> movements = serviceLayer.getLatestMovements(numberOfMovements);
            List<MovementType> latestMovements = MovementEntityToModelMapper.mapToMovementTypeFromLatestMovement(movements);
            List<MovementDto> response = MovementMapper.mapToMovementDtoList(latestMovements);
            LOG.debug("GET LATEST MOVEMENTS TIME: {}", (System.currentTimeMillis() - start));
            return new ResponseDto<>(response, RestResponseCode.OK);
        } catch (Exception ex) {
            LOG.error("[ Error when getting list. ]", ex);
            return new ResponseDto(ex, RestResponseCode.ERROR);
        }
    }

    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/{id}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto getById(@PathParam(value = "id") final String id) {
        LOG.debug("Get by id invoked in rest layer");
        try {
            Movement movement = serviceLayer.getById(UUID.fromString(id));
            if (movement == null) {
                return new ResponseDto("No movement with ID " + id, RestResponseCode.ERROR);
            }
            MovementType response = MovementEntityToModelMapper.mapToMovementType(movement);

            return new ResponseDto<>(response, RestResponseCode.OK);
        } catch (NonUniqueResultException ex) {
            LOG.error("[ Error when getting by id. ]", ex);
            return new ResponseDto(ex, RestResponseCode.ERROR_DUPLICTAE);
        } catch (Exception ex) {
            LOG.error("[ Error when getting by id. ] ", ex);
            return new ResponseDto(ex, RestResponseCode.ERROR);
        }

    }

    @POST
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/movementMap")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto<GetMovementMapByQueryResponse> getMapByQuery(MovementQuery query) {
        try {
            return new ResponseDto(serviceLayer.getMapByQuery(query), RestResponseCode.OK);
        } catch (Exception ex) {
            LOG.error("[ Error when getting movement map. ]", ex);
            return new ResponseDto(ex, RestResponseCode.ERROR);
        }
    }

    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/microMovementListAfter/{timestamp}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getMicroMovementListAfter(@PathParam("timestamp") String date) {
        try {
            List<MicroMovementDto> microList = movementDao.getMicroMovementsAfterDate(DateUtil.getDateFromString(date));
            Map<String, List<MicroMovementDto>> returnMap = new HashMap<>(microList.size());
            for (MicroMovementDto micro: microList) {
                if(!returnMap.containsKey(micro.getAsset())){
                    returnMap.put(micro.getAsset(), new ArrayList<>());
                }
                returnMap.get(micro.getAsset()).add(micro);
            }
            return Response.ok().entity(returnMap).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting Micro Movement. ]", e);
            return Response.status(500).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/lastMicroMovementForAllAssets")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getLastMicroMovementForAllAssets() {
        try {
            List<MicroMovement> microList = movementDao.getLastMicroMovementForAllAssets();
            List<MicroMovementDto> returnList = new ArrayList<>();
            for (MicroMovement mm : microList) {
                returnList.add(MovementMapper.mapToMicroMovement(mm));
            }
            return Response.ok().entity(returnList).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting Micro Movement. ]", e);
            return Response.status(500).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }
}
