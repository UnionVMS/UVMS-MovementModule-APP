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
package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.uvms.movement.service.dto.AlarmListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.AlarmQuery;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmReport;
import eu.europa.ec.fisheries.uvms.movement.service.validation.MovementSanityValidatorBean;
import eu.europa.ec.fisheries.uvms.movement.service.validation.SanityRule;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/alarms")
@Stateless
@Consumes(value = { MediaType.APPLICATION_JSON })
@Produces(value = { MediaType.APPLICATION_JSON })
public class AlarmRestResource {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmRestResource.class);

    @EJB
    private MovementSanityValidatorBean validationService;

    @Context
    private HttpServletRequest request;

    @POST
    @Path("/list")
    @RequiresFeature(UnionVMSFeature.viewAlarmsHoldingTable)
    public Response getAlarmList(AlarmQuery query) {
        LOG.info("Get alarm list invoked in rest layer");
        AlarmListResponseDto alarmList = validationService.getAlarmList(query);
        return Response.ok(alarmList).build();
    }

    @PUT
    @RequiresFeature(UnionVMSFeature.manageAlarmsHoldingTable)
    public Response updateAlarmStatus(final String alarmReport) {
        /*LOG.info("Update alarm status invoked in rest layer");
        AlarmReport updated = validationService.updateAlarmStatus(alarmReport);*/
        return Response.ok(/*updated*/).build();
    }

    @PUT
    @Path("/incomingMovement")
    @RequiresFeature(UnionVMSFeature.manageAlarmsHoldingTable)
    public Response updateIncomingMovement(IncomingMovement movement) {
        LOG.info("Update incomingMovement in holding table");
        IncomingMovement updated = validationService.updateIncomingMovement(movement);
        return Response.ok(updated).build();
    }

    @GET
    @Path("/{guid}")
    @RequiresFeature(UnionVMSFeature.viewAlarmsHoldingTable)
    public Response getAlarmReportByGuid(@PathParam("guid") UUID guid) {
        AlarmReport byGuid = validationService.getAlarmReportByGuid(guid);
        return Response.ok(byGuid).build();
    }

    @POST
    @Path("/reprocess")
    @RequiresFeature(UnionVMSFeature.manageAlarmsHoldingTable)
    public Response reprocessAlarm(final List<String> alarmGuidList) {
        LOG.info("Reprocess alarm invoked in rest layer");
        validationService.reprocessAlarm(alarmGuidList, request.getRemoteUser());
        return Response.ok().build();
    }

    @GET
    @Path("/countopen")
    @RequiresFeature(UnionVMSFeature.viewAlarmsHoldingTable)
    public Response getNumberOfOpenAlarmReports() {
        long count = validationService.getNumberOfOpenAlarmReports();
        return Response.ok(count).build();
    }

    @GET
    @Path("/sanityrules")
    @RequiresFeature(UnionVMSFeature.viewAlarmsHoldingTable)
    public Response getSanityRuleNames() {
        List<String> sanityRuleNames = Arrays.stream(SanityRule.values())
                .map(SanityRule::getRuleName)
                .collect(Collectors.toList());
        return Response.ok(sanityRuleNames).build();
    }
}
