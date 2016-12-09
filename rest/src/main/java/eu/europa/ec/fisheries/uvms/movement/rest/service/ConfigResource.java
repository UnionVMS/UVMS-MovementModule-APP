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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseCode;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseDto;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;

/**
 **/
@Path("/config")
@Stateless
@RequiresFeature(UnionVMSFeature.viewMovements)
public class ConfigResource {

    final static Logger LOG = LoggerFactory.getLogger(ConfigResource.class);

    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path(value = "/movementTypes")
    public ResponseDto getMovementTypes() {
        try {
            return new ResponseDto(MovementTypeType.values(), ResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        }
    }

    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path(value = "/segmentCategoryTypes")
    public ResponseDto getSegmetTypes() {
        try {
            return new ResponseDto(SegmentCategoryType.values(), ResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        }
    }

    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path(value = "/searchKeys")
    public ResponseDto getMovementSearchKeys() {
        try {
            return new ResponseDto(SearchKeyType.values(), ResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        }
    }

    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path(value = "/movementSourceTypes")
    public ResponseDto getMovementSourceTypes() {
        try {
            return new ResponseDto(MovementSourceType.values(), ResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        }
    }

    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path(value = "/activityTypes")
    public ResponseDto getActivityTypes() {
        try {
            return new ResponseDto(MovementActivityTypeType.values(), ResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        }
    }

    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    public ResponseDto getConfiguration() {
        try {
            return new ResponseDto(MovementMockConfig.getValues(), ResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting config search fields. ] {} {}", e.getLocalizedMessage(), e.getStackTrace());
            return new ResponseDto(e.getMessage(), ResponseCode.ERROR);
        }
    }
}