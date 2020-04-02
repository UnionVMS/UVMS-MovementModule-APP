package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedMovement;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.json.bind.Jsonb;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

@ApplicationScoped
@Path("sse")
@RequiresFeature(UnionVMSFeature.viewMovements)
public class SSEResource {

    private final static Logger LOG = LoggerFactory.getLogger(SSEResource.class);

    private Sse sse;
    private OutboundSseEvent.Builder eventBuilder;
    private SseBroadcaster sseBroadcaster;
    private Jsonb jsonb;

    public void initAtStartup(@Observes @Initialized(ApplicationScoped.class) Object init) {
        LOG.debug("Starting SSEResource by initAtStartup");
    }

    @PostConstruct
    public void init() {
        LOG.debug("Starting SSEResource by init");
        jsonb = new JsonBConfigurator().getContext(null);
    }

    @Context
    public void setSse(Sse sse) {
        this.sse = sse;
        this.eventBuilder = sse.newEventBuilder();
        this.sseBroadcaster = sse.newBroadcaster();
    }

    public void createdMovement(@Observes(during = TransactionPhase.AFTER_SUCCESS) @CreatedMovement Movement move){
        LOG.debug("Movement {} came to SseResource" , move);
        try {
            if (move != null) {
                MicroMovementExtended micro = new MicroMovementExtended(move.getLocation(),
                        move.getHeading(), move.getId(), move.getMovementConnect().getId(), move.getTimestamp(), move.getSpeed(), move.getMovementSource());
                String outboundJson = jsonb.toJson(micro);
                OutboundSseEvent sseEvent = eventBuilder
                        .name("Movement")
                        .id("" + System.currentTimeMillis())
                        .mediaType(MediaType.APPLICATION_JSON_PATCH_JSON_TYPE)
                        //.data(MicroMovementExtended.class, micro)         //for some unknown reason this does not get serialized, so we serialize manually and send the string instead.
                        .data(String.class, outboundJson)
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
