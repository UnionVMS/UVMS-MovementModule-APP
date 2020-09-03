package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.movement.model.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MicroMovementsForConnectIdsBetweenDatesRequest;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.CursorPagination;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovement;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementEntityToModelMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.movement.service.util.JsonBConfiguratorMovement;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
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

    private Jsonb jsonb;    //since for some reason jackson is used to serialize stuff if we use the framework

    @PostConstruct
    public void init(){
        jsonb = new JsonBConfiguratorMovement().getContext(null);
    }

    @GET
    @Path("/ping")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response ping() {
        return Response.ok("pong").build();
    }

    @POST
    @Path("/list")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getListByQuery(MovementQuery query) {
        try {
            GetMovementListByQueryResponse list = movementService.getList(query);
            String jsonString = jsonb.toJson(list);
            return Response.ok(jsonString).build();
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
            LOG.error("Error when retrieving movement map", e);
            return Response.status(Status.BAD_REQUEST).entity("No MovementQuery found").build();
        } catch (Exception e) {
            LOG.error("Error when retrieving movement map", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    @GET
    @Path("/getMicroMovement/{movementId}")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getMicroMovementById(@PathParam("movementId") UUID movementId) {
        try {
            MicroMovement byId = movementService.getMicroMovementById(movementId);
            return Response.ok(byId).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting microMovement. ]", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    @GET
    @Path("/getMovement/{movementId}")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getMovementById(@PathParam("movementId") UUID movementId) {
        try {
            MovementDto byId = MovementMapper.mapToMovementDto(movementService.getById(movementId));
            return Response.ok(byId).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting movement. ]", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    @GET
    @Path("/countMovementsInDateAndTheDayBeforeForAsset/{id}")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response countMovementsInDateAndTheDayBeforeForAsset(@PathParam("id") String id,
                                                                @QueryParam("after") String after) { // yyyy-MM-dd HH:mm:ss Z
        try {
            Instant afterInstant = DateUtils.stringToDate(after);
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
    
    @POST
    @Path("/getMicroMovementList")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getMicroMovementByIdList(List<UUID> moveIds) {
        try {
            List<MicroMovement> microMovement = movementService.getMicroMovementsByMoveIds(moveIds);
            return Response.ok(microMovement).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting microMovements. ]", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }

    @POST
    @Path("/getMovementList")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getMovementDtoByIdList(List<UUID> moveIds) {
        try {
            List<MovementDto> MovementDtos = movementService.getMovementsByMoveIds(moveIds);
            return Response.ok(MovementDtos).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting movementDtos. ]", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e)).build();
        }
    }
    
    @POST
    @Path("/list/cursor")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getCursorBasedList(CursorPagination cursorPagination) {
        try {
            List<MovementType> list = movementService.getCursorBasedList(cursorPagination);
            return Response.ok(list).header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getRootCause(e))
                    .header("MDC", MDC.get("requestId")).build();
        }
    }
}
