package eu.europa.ec.mare.usm.administration.rest.security;

import eu.europa.ec.mare.usm.jwt.JwtTokenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;



/**
 * Filters incoming requests, converting Authorisation headers to a 
 * remote user identity (if the request does not already reference 
 * a remote user), extending the duration of the JWT token (if present).
 *
 */
@WebFilter(filterName = "AuthenticationFilter",
        urlPatterns = {"/rest/*"})
public class AuthenticationFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String AUTHORIZATION = "Authorization";
    private static final String CHALLENGEAUTH = "/challengeauth";
    private static final String AUTHENTICATE = "/authenticate";
    private static final String RESETPWD = "/users/resetUserPassword";
    private static final String PING = "/ping";

    @EJB(name="secureJwtHandler")
    JwtTokenHandler tokenHandler;



    /**
     * Creates a new instance
     */
    public AuthenticationFilter() {
    }

    /**
     * Filters an incoming request, converting a (custom) JWT token to
     * a (standard) remote user identity (if the request does not
     * already reference a remote user), extending the duration of
     * the JWT token (if present). If the request contains neither a remote
     * user identity nor a JWT token, request processing is skipped and an HTTP
     * status of 403 (Forbidden) is sent back to the requester,
     *
     * @param request The request we are processing
     * @param response The response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if another error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        LOGGER.info("doFilter(" + httpRequest.getMethod() + ", " +
                httpRequest.getPathInfo() + ") - (ENTER)");


        chain.doFilter(httpRequest, response);
    }

    @Override
    public void init(FilterConfig fc)
            throws ServletException
    {
        if (tokenHandler == null) {
            throw new ServletException("JwtTokenHandler is undefined");
        }
    }

    @Override
    public void destroy() {
        // NOP
    }

}