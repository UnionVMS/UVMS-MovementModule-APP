package eu.europa.ec.fisheries.uvms.movement.rest.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementDtoV2;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import com.vividsolutions.jts.geom.Geometry;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTrack;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementEntityToModelMapper;
import eu.europa.ec.fisheries.uvms.movement.service.util.WKTUtil;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;

@Path("/track")
@Stateless
public class TrackRestResource {
    private static final Logger LOG = LoggerFactory.getLogger(TrackRestResource.class);

    @Inject
    private MovementDao movementDao;

    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path(value = "/{id}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getTrack(@PathParam("id") String stringId, @DefaultValue("2000") @QueryParam("maxNbr") Integer maxNbr) {
        try {

            UUID id = UUID.fromString(stringId);
            Track track = movementDao.getTrackById(id);
            List<Geometry> points = ((track == null) ? new ArrayList<>() : movementDao.getPointsFromTrack(track, maxNbr));
            MovementTrack returnTrack = MovementEntityToModelMapper.mapToMovementTrack(track, points);

            return Response.ok(returnTrack).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();

        } catch (Exception e) {
            LOG.error("[ Error when getting track. ] {}", e.getMessage(), e);
            return Response.status(500).entity(ExceptionUtils.getRootCause(e)).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();
        }
    }

    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path(value = "/byMovementGUID/{id}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getWKTTrackByMovement(@PathParam("id") String stringId, @DefaultValue("2000") @QueryParam("maxNbr") Integer maxNbr) {
        try {

            UUID id = UUID.fromString(stringId);
            Movement movement = movementDao.getMovementByGUID(id);
            List<Geometry> points = ((movement.getTrack() == null) ? new ArrayList<>() : movementDao.getPointsFromTrack(movement.getTrack(), maxNbr));
            MovementTrack returnTrack = MovementEntityToModelMapper.mapToMovementTrack(movement.getTrack(), points);

            return Response.ok(returnTrack).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();

        } catch (Exception e) {
            LOG.error("[ Error when getting track. ] {}", e.getMessage(), e);
            return Response.status(500).entity(ExceptionUtils.getRootCause(e)).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();
        }
    }

    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path(value = "/microMovement/byMovementGUID/{id}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response getMicroMovementTrackByMovement(@PathParam("id") String stringId, @DefaultValue("2000") @QueryParam("maxNbr") Integer maxNbr) {
        try {

            UUID id = UUID.fromString(stringId);
            Movement movement = movementDao.getMovementByGUID(id);
            List<Movement> movementList = movementDao.getMovementsByTrack(movement.getTrack());
            List<MicroMovementDtoV2> returnList = new ArrayList<>();
            for (Movement move : movementList) {
                returnList.add(new MicroMovementDtoV2(move));
            }

            return Response.ok(returnList).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();

        } catch (Exception e) {
            LOG.error("[ Error when getting track. ] {}", e.getMessage(), e);
            return Response.status(500).entity(ExceptionUtils.getRootCause(e)).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();
        }
    }

}
