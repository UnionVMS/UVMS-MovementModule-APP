/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.movement.rest.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.europa.ec.fisheries.uvms.movement.rest.constants.RestConstants;
import org.slf4j.MDC;

/**
 **/
@WebFilter(asyncSupported = true, urlPatterns = {"/*"})
public class RequestFilter implements Filter {

    final static Logger LOG = LoggerFactory.getLogger(RequestFilter.class);

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        LOG.info("Requstfilter starting up!");
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {

        // TODO MDC is prototyping TEST
        // TODO if we want to put the entire request in a db-table with for instance requestId as key (to be able to fast and easy reproduce a reported error)
        // TODO we must do some things first since it will "consume" the stream and make it unusable at a later stage (the rest of the application)
        // TODO read this article http://stackoverflow.com/questions/10210645/http-servlet-request-lose-params-from-post-body-after-read-it-once
        try {
            final String val = MDC.get("requestId");
            if(val == null || val.trim().length()< 1){
                MDC.put("requestId", UUID.randomUUID().toString());
            }
        }catch(final IllegalArgumentException e){
            LOG.error("MDC init error. App works but tracing will be unpredictible", e);
        }

        final HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader(RestConstants.ACCESS_CONTROL_ALLOW_ORIGIN, RestConstants.ACCESS_CONTROL_ALLOW_METHODS_ALL);
        response.setHeader(RestConstants.ACCESS_CONTROL_ALLOW_METHODS, RestConstants.ACCESS_CONTROL_ALLOWED_METHODS);
       // response.setHeader(RestConstants.ACCESS_CONTROL_ALLOW_HEADERS, RestConstants.ACCESS_CONTROL_ALLOW_HEADERS_ALL);
        response.setHeader(RestConstants.ACCESS_CONTROL_ALLOW_HEADERS, ((HttpServletRequest)request).getHeader("Access-Control-Request-Headers"));
        chain.doFilter(request, res);
    }

    @Override
    public void destroy() {
        LOG.info("Requstfilter shuting down!");
    }

}