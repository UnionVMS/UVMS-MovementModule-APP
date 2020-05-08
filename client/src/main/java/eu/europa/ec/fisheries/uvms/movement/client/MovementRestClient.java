package eu.europa.ec.fisheries.uvms.movement.client;

import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovement;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.model.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MicroMovementsForConnectIdsBetweenDatesRequest;
import eu.europa.ec.fisheries.uvms.rest.security.InternalRestTokenHandler;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Stateless
public class MovementRestClient {

    private WebTarget webTarget;
    
    private Jsonb jsonb;  
    
    @Resource(name = "java:global/movement_endpoint")
    private String movementEndpoint;

    @Inject
    private InternalRestTokenHandler internalRestTokenHandler;

    @PostConstruct
    public void initClient() {
        String url = movementEndpoint + "/";

        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        clientBuilder.connectTimeout(10, TimeUnit.MINUTES);
        clientBuilder.readTimeout(10, TimeUnit.MINUTES);
        Client client = clientBuilder.build();

        client.register(JsonBConfigurator.class);
        webTarget = client.target(url);
        
        jsonb = new JsonBConfigurator().getContext(null);
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
            Response response = webTarget
                    .path("internal/getMicroMovement")
                    .path(id.toString())
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                    .get();

            return response.readEntity(MicroMovement.class);
    }
    
    public GetMovementListByQueryResponse getMovementList(MovementQuery movementQuery){ 
    	String response = webTarget
                .path("internal/list")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .post(Entity.entity(movementQuery, MediaType.APPLICATION_JSON_TYPE), String.class);
    	// json parsning from String to GetMovementListByQueryResponse to avoid time parsing issues
    	GetMovementListByQueryResponse getMovementListByQueryResponse = jsonb.fromJson(response, GetMovementListByQueryResponse.class);
        return getMovementListByQueryResponse;
    }
}
