package eu.europa.ec.fisheries.uvms.movement.rest.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByAreaAndTimeIntervalResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Path("/internal")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class InternalRestResource {
    private static final Logger LOG = LoggerFactory.getLogger(InternalRestResource.class);

    @Inject
    MovementService movementService;

    @Inject
    MovementDao movementDao;

    @POST
    @Path("/list")
    public Response getListByQuery(MovementQuery query) {
        try {
            GetMovementListByQueryResponse list = movementService.getList(query);
            return Response.ok(list).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Error when getting list.").build();
        }
    }

    @POST
    @Path("/list/minimal")
    public Response getMinimalListByQuery(MovementQuery query) {
        try {
            GetMovementListByQueryResponse minimalList = movementService.getMinimalList(query);
            return Response.ok(minimalList).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Error when getting minimal list.").build();
        }
    }

    @POST
    @Path("/latest")
    public Response getLatestMovementsByConnectIds(List<String> connectIds) {
        if (connectIds == null || connectIds.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No connectIds found").build();
        }
        try {
            List<Movement> latestMovements = movementService.getLatestMovementsByConnectIds(connectIds);
            return Response.ok(latestMovements).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Error when getting latest list").build();
        }
    }

    @POST
    @Path("/movementListByAreaAndTimeInterval")
    public Response getMovementListByAreaAndTimeIntervalByCriteria(MovementAreaAndTimeIntervalCriteria criteria) {
        try {
        GetMovementListByAreaAndTimeIntervalResponse response = movementService.getMovementListByAreaAndTimeInterval(criteria);
        return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Error when getting movement list by area and time interval").build();
        }
    }

    @POST
    @Path("/movementListByAreaAndTimeInterval")
    public Response getMovementListByQuery(MovementQuery query) {
        try {
            GetMovementListByQueryResponse response = movementService.getList(query);
            return Response.ok(response).build();
        } catch (MovementServiceRuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No MovementQuery found").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Error when getting movement list by query").build();
        }
    }

    @POST
    @Path("/movementMapByQuery")
    public Response getMovementMapByQuery(MovementQuery query) {
        try {
            GetMovementMapByQueryResponse response = movementService.getMapByQuery(query);
            return Response.ok(response).build();
        } catch (MovementServiceRuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No MovementQuery found").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Error when getting movement map by query").build();
        }
    }

    @GET
    @Path("/countMovementsInDateAndTheDayBeforeForAsset/{id}")
    public Response countMovementsInDateAndTheDayBeforeForAsset(@PathParam("id") String id, @QueryParam("after") String after) {   //yyyy-MM-dd HH:mm:ss Z
        try {
            Instant afterInstant = DateUtil.convertDateTimeInUTC(after);
            Instant yesterday = afterInstant.minusSeconds(60L * 60L * 24L);   //one day in seconds
            long count = movementDao.countNrOfMovementsForAssetBetween(id,yesterday, afterInstant);
            return Response.ok().entity(count).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();
        } catch (Exception e) {
            LOG.error("[ Error when counting movements. ]", e);
            return Response.status(500).entity(e).build();
        }
    }
}
