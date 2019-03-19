package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementDtoV2Extended;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedMovement;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

/* New version of sseResource, using a new datastructure when sending data to the new version of the realtime map. At some point in the future this version will replace the old version */
@ApplicationScoped
@Path("sseV2")
@RequiresFeature(UnionVMSFeature.viewMovements)
public class NewSSEResource {

    private final static Logger LOG = LoggerFactory.getLogger(SSEResource.class);


    Sse sse;
    OutboundSseEvent.Builder eventBuilder;
    SseBroadcaster sseBroadcaster;

    @Context
    public void setSse(Sse sse) {
        this.sse = sse;
        this.eventBuilder = sse.newEventBuilder();
        this.sseBroadcaster = sse.newBroadcaster();
    }

    public void createdMovement(@Observes @CreatedMovement Movement move){
        try {
            if (move != null) {
                MicroMovementDtoV2Extended micro = new MicroMovementDtoV2Extended(move.getLocation(), move.getHeading(), move.getId(), move.getMovementConnect(), move.getTimestamp(), move.getSpeed());
                OutboundSseEvent sseEvent = eventBuilder
                        .name("Movement")
                        .id("" + System.currentTimeMillis())
                        .mediaType(MediaType.APPLICATION_JSON_PATCH_JSON_TYPE)
                        .data(MicroMovementDtoV2Extended.class, micro)
                        //.reconnectDelay(3000) //this one is optional and governs how long the client should wait b4 attempting to reconnect to this server
                        .comment("New Movement")
                        .build();
                sseBroadcaster.broadcast(sseEvent);
            }
        }catch (Exception e){
            LOG.error("Error while broadcasting SSE: ", e);
            throw new RuntimeException(e);
        }
    }


    @GET
    @Path("subscribe")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void listen(@Context SseEventSink sseEventSink) {
        sseEventSink.send(sse.newEvent("Welcome to UVMS SSE notifications. Version 2"));
        sseBroadcaster.register(sseEventSink);
        sseEventSink.send(sse.newEvent("You are now registered for receiving new movements."));
    }

}
