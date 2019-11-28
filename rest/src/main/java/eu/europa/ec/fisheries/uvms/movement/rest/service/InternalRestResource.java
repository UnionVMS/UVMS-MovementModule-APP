package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MicroMovementsForConnectIdsBetweenDatesRequest;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementEntityToModelMapper;
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
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Path("/internal")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class InternalRestResource {
    private static final Logger LOG = LoggerFactory.getLogger(InternalRestResource.class);

    @Inject
    private MovementService movementService;

    @Inject
    private MovementDao movementDao;

    @POST
    @Path("/list")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getListByQuery(MovementQuery query) {
        try {
            GetMovementListByQueryResponse list = movementService.getList(query);
            return Response.ok(list).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    @POST
    @Path("/list/minimal")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getMinimalListByQuery(MovementQuery query) {
        try {
            GetMovementListByQueryResponse minimalList = movementService.getMinimalList(query);
            return Response.ok(minimalList).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    @POST
    @Path("/latest")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getLatestMovementsByConnectIds(List<UUID> connectIds) {
        if (connectIds == null || connectIds.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("No connectIds found").build();
        }
        try {
            List<Movement> latestMovements = movementService.getLatestMovementsByConnectIds(connectIds);
            List<MovementType> movementTypeList = MovementEntityToModelMapper.mapToMovementType(latestMovements);
            return Response.ok(movementTypeList).build();
        } catch (Exception ex) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(ex)).build();
        }
    }

    @POST
    @Path("/movementMapByQuery")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getMovementMapByQuery(MovementQuery query) {
        try {
            GetMovementMapByQueryResponse response = movementService.getMapByQuery(query);
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST).entity("No MovementQuery found").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    @GET
    @Path("/countMovementsInDateAndTheDayBeforeForAsset/{id}")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response countMovementsInDateAndTheDayBeforeForAsset(@PathParam("id") String id,
                                                                @QueryParam("after") String after) { // yyyy-MM-dd HH:mm:ss Z
        try {
            Instant afterInstant = DateUtil.convertDateTimeInUTC(after);
            Instant yesterday = afterInstant.minusSeconds(60L * 60L * 24L); // 1 day in seconds
            long count = movementDao.countNrOfMovementsForAssetBetween(UUID.fromString(id),yesterday, afterInstant);
            return Response.ok().entity(count).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("[ Error when counting movements. ]", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    @PUT
    @Path("/remapMovementConnectInMovement")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response remapMovementConnectInMovement(@QueryParam(value = "MovementConnectFrom") String movementConnectFrom,
                                                   @QueryParam(value = "MovementConnectTo") String movementConnectTo) {
        try {
            movementService.remapMovementConnectInMovement(movementConnectFrom, movementConnectTo);
            return Response.ok()
                    .header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("[ Error when remapping movements. ]", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    @DELETE
    @Path("/removeMovementConnect")
    public Response removeMovementConnect(@QueryParam(value = "MovementConnectId") String movementConnectId) {
        try {
            movementService.removeMovementConnect(movementConnectId);
            return Response.ok()
                    .header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("[ Error when removing movement connect. ]", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    @POST
    @Path("/microMovementsForConnectIdsBetweenDates")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getMicroMovementsForConnectIdsBetweenDates(MicroMovementsForConnectIdsBetweenDatesRequest request) {
        List<String> vesselIds = request.getAssetIds();
        Instant fromDate = request.getFromDate();
        Instant toDate = request.getToDate();
        List<MovementSourceType> sourceTypes = convertToMovementSourceTypes(request.getSources());

        if (vesselIds.isEmpty()) {
            return Response.ok(Collections.emptyList()).header("MDC", MDC.get("requestId")).build();
        }

        try {
            List<UUID> uuids = vesselIds
                    .stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());

            List<MicroMovementExtended> microMovements = movementDao.getMicroMovementsForConnectIdsBetweenDates(uuids, fromDate, toDate, sourceTypes);

            Response.ResponseBuilder ok = Response.ok(microMovements);
            return ok.header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting micro movements for vessel ids ]", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    private List<MovementSourceType> convertToMovementSourceTypes (List<String> sources) {
        List<MovementSourceType> sourceTypes = new ArrayList<>();
        if (sources == null || sources.isEmpty()) {
            sourceTypes = Arrays.asList(MovementSourceType.values());
        } else {
            for (String source : sources) {
                sourceTypes.add(MovementSourceType.fromValue(source));
            }
        }
        return sourceTypes;
    }
}
