package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/movement")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class InternalResource {

    @Inject
    MovementService serviceLayer;

    @POST
    @Path("/list")
    public Response getListByQuery(MovementQuery query) {
        try {
            GetMovementListByQueryResponse list = serviceLayer.getList(query);
            return Response.ok(list).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Error when getting list.").build();
        }
    }

    @POST
    @Path("/list/minimal")
    public Response getMinimalListByQuery(MovementQuery query) {
        try {
            GetMovementListByQueryResponse minimalList = serviceLayer.getMinimalList(query);
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
            List<Movement> latestMovements = serviceLayer.getLatestMovementsByConnectIds(connectIds);
            return Response.ok(latestMovements).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Error when getting latest list").build();
        }
    }
}
