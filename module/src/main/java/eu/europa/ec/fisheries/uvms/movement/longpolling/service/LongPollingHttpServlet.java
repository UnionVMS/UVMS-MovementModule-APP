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

import eu.europa.ec.fisheries.uvms.longpolling.notifications.NotificationMessage;
import eu.europa.ec.fisheries.uvms.movement.longpolling.constants.LongPollingConstants;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.event.AlarmReportCountEvent;
import eu.europa.ec.fisheries.uvms.movement.service.event.AlarmReportEvent;
import eu.europa.ec.fisheries.uvms.movement.service.event.CreatedMovement;

import javax.ejb.EJB;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@WebServlet(asyncSupported = true, urlPatterns = {LongPollingConstants.MOVEMENT_PATH, LongPollingConstants.MANUAL_MOVEMENT_PATH, LongPollingConstants.ALARM_REPORT_PATH, LongPollingConstants.ALARM_REPORT_COUNT_PATH})
public class LongPollingHttpServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private LongPollingContextHelper asyncContexts;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        AsyncContext ctx = req.startAsync(req, resp);
        ctx.setTimeout(LongPollingConstants.ASYNC_TIMEOUT);
        ctx.addListener(new LongPollingAsyncListener() {

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                AsyncContext ctx = event.getAsyncContext();
                asyncContexts.remove(ctx);
                completePoll(ctx, createJsonMessage(null));
            }

        });

        asyncContexts.add(ctx, req.getServletPath());
    }

    public void createdMovement(@Observes(during = TransactionPhase.AFTER_SUCCESS) @CreatedMovement Movement movement) throws IOException {
        completePoll(LongPollingConstants.MOVEMENT_PATH, createJsonMessage(movement.getId().toString()));
    }

    public void observeAlarmCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @AlarmReportEvent NotificationMessage message) throws IOException {
        UUID guid = (UUID) message.getProperties().get(LongPollingConstants.PROPERTY_GUID);
        completePoll(LongPollingConstants.ALARM_REPORT_PATH, createJsonMessage(guid.toString()));
    }
    
    public void observeTicketCount(@Observes(during = TransactionPhase.AFTER_SUCCESS) @AlarmReportCountEvent NotificationMessage message) throws IOException {
        completePoll(LongPollingConstants.ALARM_REPORT_COUNT_PATH, createJsonMessageCount(true));
    }

    private String createJsonMessage(String guid) {
        JsonArrayBuilder array = Json.createArrayBuilder();
        if (guid != null) {
            array.add(guid);
        }

        return Json.createObjectBuilder().add("ids", array).build().toString();
    }
    
    protected String createJsonMessageCount(boolean value) {
        return Json.createObjectBuilder().add(LongPollingConstants.ACTION_UPDATED, value).build().toString();
    }

    private void completePoll(String resourcePath, String message) throws IOException {
        AsyncContext ctx = null;
        while ((ctx = asyncContexts.popContext(resourcePath)) != null) {
            completePoll(ctx, message);
        }
    }

    private void completePoll(AsyncContext ctx, String jsonMessage) throws IOException {
        ctx.getResponse().setContentType("application/json");
        ctx.getResponse().getWriter().write(jsonMessage);
        ctx.complete();
    }

    private abstract static class LongPollingAsyncListener implements AsyncListener {

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            // Do nothing
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
            // Do nothing
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            // Do nothing
        }

    }

}