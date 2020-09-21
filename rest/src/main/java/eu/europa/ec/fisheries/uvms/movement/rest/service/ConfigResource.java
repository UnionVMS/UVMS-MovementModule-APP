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

import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKeyType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementActivityTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.MovementMockConfig;
import eu.europa.ec.fisheries.uvms.movement.service.dto.AlarmStatusType;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/config")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
@RequiresFeature(UnionVMSFeature.viewMovements)
public class ConfigResource {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigResource.class);

    @GET
    @Path(value = "/movementTypes")
    public Response getMovementTypes() {
        try {
            return Response.ok(MovementTypeType.values()).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            throw e;
        }
    }

    @GET
    @Path(value = "/segmentCategoryTypes")
    public Response getSegmentTypes() {
        try {
            return Response.ok(SegmentCategoryType.values()).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            throw e;
        }
    }

    @GET
    @Path(value = "/searchKeys")
    public Response getMovementSearchKeys() {
        try {
            return Response.ok(SearchKeyType.values()).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            throw e;
        }
    }

    @GET
    @Path(value = "/movementSourceTypes")
    public Response getMovementSourceTypes() {
        try {
            return Response.ok(MovementSourceType.values()).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            throw e;
        }
    }

    @GET
    @Path(value = "/activityTypes")
    public Response getActivityTypes() {
        try {
            return Response.ok(MovementActivityTypeType.values()).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            throw e;
        }
    }

    @GET
    public Response getConfiguration() {
        try {
            return Response.ok(MovementMockConfig.getValues()).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            throw e;
        }
    }

    @GET
    @Path(value = "/alarmstatus")
    public Response getAlarmStatuses() {
        try {
            return Response.ok(AlarmStatusType.values()).build();
        } catch (Exception e) {
            LOG.error("[ Error when getting alarm statuses. ] {} ", e.getMessage());
            throw e;
        }
    }
}
