package eu.europa.ec.fisheries.uvms.movement.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovement;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MicroMovementsForConnectIdsBetweenDatesRequest;
import eu.europa.ec.fisheries.uvms.rest.security.InternalRestTokenHandler;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Stateless
public class MovementRestClient {

    private WebTarget webTarget;

    @Resource(name = "java:global/movement_endpoint")
    private String movementEndpoint;

    @Inject
    private InternalRestTokenHandler internalRestTokenHandler;

    @PostConstruct
    public void initClient() {
        String url = movementEndpoint + "/";

        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        clientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        clientBuilder.readTimeout(30, TimeUnit.SECONDS);
        Client client = clientBuilder.build();

        // This has to be an anonymous class since the method doesn't support lambdas, it will throw exception in runtime
        client.register(new ContextResolver<ObjectMapper>() {
            @Override
            public ObjectMapper getContext(Class<?> type) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                client.register(new JacksonJaxbJsonProvider(mapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS));
                return mapper;
            }
        });
        webTarget = client.target(url);
    }

    public List<MicroMovementExtended> getMicroMovementsForConnectIdsBetweenDates(List<String> connectIds, Instant fromDate, Instant toDate) {
        MicroMovementsForConnectIdsBetweenDatesRequest request = new MicroMovementsForConnectIdsBetweenDatesRequest(connectIds, fromDate, toDate);

        Response response = webTarget
                .path("internal/microMovementsForConnectIdsBetweenDates")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        return response.readEntity(new GenericType<List<MicroMovementExtended>>() {});
    }

    public String getMicroMovementsForConnectIdsBetweenDates(List<String> connectIds, Instant fromDate, Instant toDate, List<String> sources) {
        MicroMovementsForConnectIdsBetweenDatesRequest request = new MicroMovementsForConnectIdsBetweenDatesRequest(connectIds, fromDate, toDate);
        request.setSources(sources);

        Response response = webTarget
                .path("internal/microMovementsForConnectIdsBetweenDates")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        return response.readEntity(String.class);
    }

    public MicroMovement getMicroMovementById(UUID id) {
        return webTarget
                .path("internal/getMicroMovement")
                .path(id.toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .get(MicroMovement.class);
    }
}
