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
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import eu.europa.ec.fisheries.uvms.movement.rest.dto.RestResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.AlarmStatusType;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;

@Path("/config")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
@RequiresFeature(UnionVMSFeature.viewMovements)
public class ConfigResource {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigResource.class);

    @GET
    @Path(value = "/movementTypes")
    public ResponseDto getMovementTypes() {
        try {
            return new ResponseDto<>(MovementTypeType.values(), RestResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            return new ResponseDto<>(e.getMessage(), RestResponseCode.ERROR);
        }
    }

    @GET
    @Path(value = "/segmentCategoryTypes")
    public ResponseDto getSegmentTypes() {
        try {
            return new ResponseDto<>(SegmentCategoryType.values(), RestResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            return new ResponseDto<>(e.getMessage(), RestResponseCode.ERROR);
        }
    }

    @GET
    @Path(value = "/searchKeys")
    public ResponseDto getMovementSearchKeys() {
        try {
            return new ResponseDto<>(SearchKeyType.values(), RestResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            return new ResponseDto<>(e.getMessage(), RestResponseCode.ERROR);
        }
    }

    @GET
    @Path(value = "/movementSourceTypes")
    public ResponseDto getMovementSourceTypes() {
        try {
            return new ResponseDto<>(MovementSourceType.values(), RestResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            return new ResponseDto<>(e.getMessage(), RestResponseCode.ERROR);
        }
    }

    @GET
    @Path(value = "/activityTypes")
    public ResponseDto getActivityTypes() {
        try {
            return new ResponseDto(MovementActivityTypeType.values(), RestResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            return new ResponseDto(e.getMessage(), RestResponseCode.ERROR);
        }
    }

    @GET
    public ResponseDto getConfiguration() {
        try {
            return new ResponseDto(MovementMockConfig.getValues(), RestResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            return new ResponseDto(e.getMessage(), RestResponseCode.ERROR);
        }
    }
    

    @GET
    @Path(value = "/alarmstatus")
    public ResponseDto getAlarmStatuses() {
        try {
            return new ResponseDto(AlarmStatusType.values(), RestResponseCode.OK);
        } catch (Exception ex) {
            LOG.error("[ Error when getting alarm statuses. ] {} ", ex.getMessage());
            return new ResponseDto(ex.getMessage(), RestResponseCode.ERROR);
        }
    }
}