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

import eu.europa.ec.fisheries.uvms.movement.service.bean.ManualMovementService;
import eu.europa.ec.fisheries.uvms.movement.service.dto.ManualMovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmItem;
import eu.europa.ec.fisheries.uvms.movement.service.entity.alarm.AlarmReport;
import eu.europa.ec.fisheries.uvms.movement.service.validation.MovementSanityValidatorBean;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/manualMovement")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class ManualMovementRestResource {

    private final static Logger LOG = LoggerFactory.getLogger(ManualMovementRestResource.class);

    @Inject
    private ManualMovementService service;

    @Inject
    private MovementSanityValidatorBean validationService;

    @Context
    private HttpServletRequest request;

    @POST
    @RequiresFeature(UnionVMSFeature.manageManualMovements)
    public Response create(ManualMovementDto data) {
        LOG.debug("Create manual movement invoked in rest layer");
        try {
            UUID reportId = service.sendManualMovement(data, request.getRemoteUser());
            if(reportId == null){
                return Response.ok().header("MDC", MDC.get("requestId")).build();
            } else {
                AlarmReport report = validationService.getAlarmReportByGuid(reportId);
                List<String> sanityRulesTriggered = report.getAlarmItemList().stream().map(AlarmItem::getRuleName).collect(Collectors.toList());
                return Response.status(400).entity(sanityRulesTriggered).header("MDC", MDC.get("requestId")).build();
            }
        } catch (Exception e) {
            LOG.error("[ Error when creating a manual movement. ] {} ", e);
            throw e;
        }
    }

}
