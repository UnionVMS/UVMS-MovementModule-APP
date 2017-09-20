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
package eu.europa.ec.fisheries.uvms.movement.longpolling.service;

import java.io.IOException;

import javax.enterprise.event.Observes;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.europa.ec.fisheries.uvms.movement.longpolling.constants.LongPollingConstants;
import eu.europa.ec.fisheries.uvms.longpolling.notifications.NotificationMessage;
import javax.ejb.EJB;

@WebServlet(asyncSupported = true, urlPatterns = { LongPollingConstants.MOVEMENT_PATH, LongPollingConstants.MANUAL_MOVEMENT_PATH })
public class LongPollingHttpServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    LongPollingContextHelper asyncContexts;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final AsyncContext ctx = req.startAsync(req, resp);
        ctx.setTimeout(LongPollingConstants.ASYNC_TIMEOUT);
        ctx.addListener(new LongPollingAsyncListener() {

            @Override
            public void onTimeout(final AsyncEvent event) throws IOException {
                final AsyncContext ctx = event.getAsyncContext();
                asyncContexts.remove(ctx);
                completePoll(ctx, createJsonMessage(null));
            }

        });

        asyncContexts.add(ctx, req.getServletPath());
    }

    public void createdMovement(@Observes @CreatedMovement final NotificationMessage message) throws IOException {
        final String guid = (String) message.getProperties().get(LongPollingConstants.MOVEMENT_GUID_KEY);
        completePoll(LongPollingConstants.MOVEMENT_PATH, createJsonMessage(guid));
    }

    public void createdManualMovement(@Observes @CreatedManualMovement final NotificationMessage message) throws IOException {
        final String guid = (String) message.getProperties().get(LongPollingConstants.MOVEMENT_GUID_KEY);
        completePoll(LongPollingConstants.MANUAL_MOVEMENT_PATH, createJsonMessage(guid));
    }

    private String createJsonMessage(final String guid) {
        final JsonArrayBuilder array = Json.createArrayBuilder();
        if (guid != null) {
            array.add(guid);
        }

        return Json.createObjectBuilder().add("ids", array).build().toString();
    }

    private void completePoll(final String resourcePath, final String message) throws IOException {
        AsyncContext ctx = null;
        while ((ctx = asyncContexts.popContext(resourcePath)) != null) {
            completePoll(ctx, message);
        }
    }

    private void completePoll(final AsyncContext ctx, final String jsonMessage) throws IOException {
        ctx.getResponse().setContentType("application/json");
        ctx.getResponse().getWriter().write(jsonMessage);
        ctx.complete();
    }

    private abstract static class LongPollingAsyncListener implements AsyncListener {

        @Override
        public void onComplete(final AsyncEvent event) throws IOException {
            // Do nothing
        }

        @Override
        public void onError(final AsyncEvent event) throws IOException {
            // Do nothing
        }

        @Override
        public void onStartAsync(final AsyncEvent event) throws IOException {
            // Do nothing
        }

    }

}