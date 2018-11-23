package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.schema.movement.v1.MovementTrack;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.SegmentDTO;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementEntityToModelMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.SegmentMapper;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

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
    public Response getTrack(@PathParam("id") String stringId) {
        try {

            long id = Long.parseLong(stringId);
            Track track = movementDao.getTrackById(id);
            MovementTrack returnTrack = MovementEntityToModelMapper.mapToMovementTrack(track);


            return Response.ok(returnTrack).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();

        } catch (Exception e) {
            LOG.error("[ Error when getting segment. ] {}", e.getMessage(), e);
            return Response.status(500).entity(e).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();
        }
    }
}
