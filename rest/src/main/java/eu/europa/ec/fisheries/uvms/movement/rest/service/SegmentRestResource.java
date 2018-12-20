package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.SegmentDTO;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.SegmentMapper;
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
import java.util.UUID;

@Path("/segment")
@Stateless
public class SegmentRestResource {
    private static final Logger LOG = LoggerFactory.getLogger(SegmentRestResource.class);

    @Inject
    private MovementDao movementDao;

    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/segmentByDestinationMovement/{destination}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public Response createMovementSearchGroup(@PathParam("destination") String destination) {
        try {
            Movement destinationMovement = movementDao.getMovementByGUID(UUID.fromString(destination));

            SegmentDTO returnSeg = SegmentMapper.mapToSegmentDTO(destinationMovement.getFromSegment());

            return Response.ok(returnSeg).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();

        } catch (Exception e) {
            LOG.error("[ Error when getting segment. ] {}", e.getMessage(), e);
            return Response.status(500).entity(ExceptionUtils.getRootCause(e)).type(MediaType.APPLICATION_JSON)
                    .header("MDC", MDC.get("requestId")).build();
        }
    }
}
