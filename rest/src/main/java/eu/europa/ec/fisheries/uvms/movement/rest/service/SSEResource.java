package eu.europa.ec.fisheries.uvms.movement.rest.service;


import eu.europa.ec.fisheries.uvms.longpolling.notifications.NotificationMessage;
import eu.europa.ec.fisheries.uvms.movement.longpolling.constants.LongPollingConstants;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedMovement;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.www.http.KeepAliveStream;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

import java.util.UUID;

@ApplicationScoped
@Path("sse")
@RequiresFeature(UnionVMSFeature.viewMovements)
public class SSEResource {

    private final static Logger LOG = LoggerFactory.getLogger(SSEResource.class);

    @Inject
    MovementDao movementDao;

    Sse sse;
    OutboundSseEvent.Builder eventBuilder;
    SseBroadcaster sseBroadcaster;

    @Context
    public void setSse(Sse sse) {
        this.sse = sse;
        this.eventBuilder = sse.newEventBuilder();
        this.sseBroadcaster = sse.newBroadcaster();
    }

    public void createdMovement(@Observes @CreatedMovement NotificationMessage message){
        try {
            UUID guid = (UUID) message.getProperties().get(LongPollingConstants.MOVEMENT_GUID_KEY);
            Movement move = movementDao.getMovementByGUID(guid);
            if (move != null) {
                MicroMovementDto micro = new MicroMovementDto(move.getLocation(), move.getHeading(), move.getGuid(), move.getMovementConnect(), move.getTimestamp(), move.getSpeed());
                OutboundSseEvent sseEvent = eventBuilder
                        .name("Movement")
                        .id("" + System.currentTimeMillis())
                        .mediaType(MediaType.APPLICATION_JSON_PATCH_JSON_TYPE)
                        .data(MicroMovementDto.class, micro)
                        //.reconnectDelay(3000) //this one is optional and governs how long the client should wait b4 attempting to reconnect to this server
                        .comment("New Movement")
                        .build();
                sseBroadcaster.broadcast(sseEvent);
            }
        }catch (Exception e){
            LOG.error("Bad stuffz: ", e);
            throw new RuntimeException(e);
        }
    }


    @GET
    @Path("subscribe")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void listen(@Context SseEventSink sseEventSink) {
        sseEventSink.send(sse.newEvent("Welcome to UVMS SSE notifications."));
        sseBroadcaster.register(sseEventSink);
        sseEventSink.send(sse.newEvent("You are now registered for receiving new movements."));
    }

}
