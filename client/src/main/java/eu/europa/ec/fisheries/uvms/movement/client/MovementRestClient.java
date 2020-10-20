package eu.europa.ec.fisheries.uvms.movement.client;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import eu.europa.ec.fisheries.uvms.movement.client.model.CursorPagination;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovement;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.model.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MicroMovementsForConnectIdsBetweenDatesRequest;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.rest.security.InternalRestTokenHandler;
import org.slf4j.MDC;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequestScoped
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

    public String ping() {
        Response response = webTarget
            .path("internal/ping")
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
            .get(Response.class);
        
        checkForErrorResponse(response);
        return response.readEntity(String.class);
    }

    public List<MicroMovementExtended> getMicroMovementsForConnectIdsBetweenDates(List<String> connectIds, Instant fromDate, Instant toDate) {
        MicroMovementsForConnectIdsBetweenDatesRequest request = new MicroMovementsForConnectIdsBetweenDatesRequest(connectIds, fromDate, toDate);

        Response response = webTarget
                .path("internal/microMovementsForConnectIdsBetweenDates")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        checkForErrorResponse(response);
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

        checkForErrorResponse(response);
        return response.readEntity(String.class);
    }

    public MicroMovement getMicroMovementById(UUID id) {
        Response response = webTarget
                .path("internal/getMicroMovement")
                .path(id.toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .get();

        checkForErrorResponse(response);
        return response.readEntity(MicroMovement.class);
    }

    public MovementDto getMovementById(UUID id) {
        Response response = webTarget
                .path("internal/getMovement")
                .path(id.toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .get();

        checkForErrorResponse(response);
        return response.readEntity(MovementDto.class);
    }
    
    public GetMovementListByQueryResponse getMovementList(MovementQuery movementQuery){ 
    	Response response = webTarget
                .path("internal/list")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .post(Entity.entity(movementQuery, MediaType.APPLICATION_JSON_TYPE), Response.class);
        checkForErrorResponse(response);
    	// json parsning from String to GetMovementListByQueryResponse to avoid time parsing issues
    	GetMovementListByQueryResponse getMovementListByQueryResponse = jsonb.fromJson(response.readEntity(String.class), GetMovementListByQueryResponse.class);
        return getMovementListByQueryResponse;
    }
    
    public List<MicroMovement> getMicroMovementByIdList(List<UUID> ids) {
        Response response = webTarget
                .path("internal/getMicroMovementList")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .post(Entity.entity(ids, MediaType.APPLICATION_JSON_TYPE));

        checkForErrorResponse(response);
        return response.readEntity(new GenericType<List<MicroMovement>>() {});
    }

    public List<MovementDto> getMovementDtoByIdList(List<UUID> ids) {
        Response response = webTarget
                .path("internal/getMovementList")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .post(Entity.entity(ids, MediaType.APPLICATION_JSON_TYPE));

        checkForErrorResponse(response);
        return response.readEntity(new GenericType<List<MovementDto>>() {});
    }
    
    public List<MovementType> getCursorBasedList(CursorPagination cursorPagination){ 
        Response response = webTarget
                .path("internal/list/cursor")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, internalRestTokenHandler.createAndFetchToken("user"))
                .header("requestId", MDC.get("requestId"))
                .post(Entity.json(cursorPagination), Response.class);
        checkForErrorResponse(response);
        return jsonb.fromJson(response.readEntity(String.class), new ArrayList<MovementType>(){}.getClass().getGenericSuperclass());
    }

    private void checkForErrorResponse(Response response){
        if(response.getStatus() != 200){
            throw new RuntimeException("Statuscode from movement was: " + response.getStatus() + " with payload " + response.readEntity(String.class));
        }
    }
}
