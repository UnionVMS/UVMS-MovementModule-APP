package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementRequest;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.bean.UserServiceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("/movementinternal")
public class MovementInternalRestResource {


    final static Logger LOG = LoggerFactory.getLogger(MovementInternalRestResource.class);

    @EJB
    private MovementService movementService;


    @Context
    private HttpServletRequest request;



    @POST
    @Path("/create")
    public MovementType createMovement(CreateMovementRequest createMovementRequest) {

        try {
            MovementType createdMovement = movementService.createMovement(createMovementRequest.getMovement(), createMovementRequest.getUsername());
            return createdMovement;
        } catch (EJBException e) {
            LOG.error("[ Error when creating movement ] ", e);
            throw new EJBException(e);
        }
    }






}

/*

          switch (request.getMethod()) {
                case MOVEMENT_LIST:
                    getMovementListByQueryBean.getMovementListByQuery(textMessage);
                    break;
                case CREATE:
                    createMovementBean.createMovement(textMessage);
                    break;
                case CREATE_BATCH:
                    createMovementBatchBean.createMovementBatch(textMessage);
                    break;
                case MOVEMENT_MAP:
                    getMovementMapByQueryBean.getMovementMapByQuery(textMessage);
                    break;
                case PING:
                   pingBean.ping(textMessage);
                    break;
                case MOVEMENT_LIST_BY_AREA_TIME_INTERVAL:
                    getMovementListByAreaAndTimeIntervalBean.getMovementListByAreaAndTimeInterval(textMessage);
                    break;
                case GET_SEGMENT_BY_ID:
                case GET_TRIP_BY_ID:
                default:
                    LOG.error("[ Request method {} is not implemented ]", request.getMethod().name());
                    errorEvent.fire(new Event




 */
