package eu.europa.ec.fisheries.uvms.movement.rest.filter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MovementRestExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = LoggerFactory.getLogger(MovementRestExceptionMapper.class);

    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;
    @Context
    private ResourceInfo resourceInfo;
    @Context
    private UriInfo uriInfo;

    public MovementRestExceptionMapper() {
        super();
    }

    @Override
    public Response toResponse(Exception exception) {

        AppError error = new AppError(500, ExceptionUtils.getRootCauseMessage(exception));
        return Response.ok(error).header("MDC", MDC.get("requestId")).build();

    }
}
