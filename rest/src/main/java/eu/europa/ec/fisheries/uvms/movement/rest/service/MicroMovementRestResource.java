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

import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.RealTimeMapInitialData;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovement;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Stateless
@Path("micro")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MicroMovementRestResource {

    private static final Logger LOG = LoggerFactory.getLogger(MicroMovementRestResource.class);

    @Inject
    private MovementService movementService;

    @Inject
    private MovementDao movementDao;

    @GET
    @Path("/track/asset/{id}/{timestamp}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getMicroMovementTrackForAsset(@PathParam("id") UUID connectId, @PathParam("timestamp") String date) {
        try {
            List<MicroMovement> microList = movementDao.getMicroMovementsForAssetAfterDate(connectId, DateUtil.getDateFromString(date));
            return Response.ok(microList).header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("Error when getting Micro Movement for connectId: {}", connectId, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    @GET
    @Path("/track/movement/{id}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getMicroMovementTrackByMovement(@PathParam("id") UUID id, @DefaultValue("2000") @QueryParam("maxNbr") Integer maxNbr) {
        try {
            Movement movement = movementDao.getMovementByGUID(id);
            List<MicroMovement> returnList = movementDao.getMicroMovementsDtoByTrack(movement.getTrack(), maxNbr);
            return Response.ok(returnList).header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("Error when getting track by movement id: {}", id, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    @GET
    @Path("/latest")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getLastMicroMovementForAllAssets() {
        try {
            List<MicroMovementExtended> microMovements = movementService.getLatestMovementsLast8Hours();

            List<String> assetIdList = new ArrayList<>(microMovements.size());
            for (MicroMovementExtended micro: microMovements) {
                assetIdList.add(micro.getAsset());
            }

            String assetInfo = movementService.getMicroAssets(assetIdList);
            RealTimeMapInitialData retVal = new RealTimeMapInitialData(microMovements, assetInfo);

            return Response.ok(retVal).header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("Error when getting latest Micro Movements", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }
}
