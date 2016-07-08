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

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementAreaAndTimeIntervalCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByAreaAndTimeIntervalResponse;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseCode;
import eu.europa.ec.fisheries.uvms.movement.rest.dto.ResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.bean.UserServiceBean;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.dto.MovementListResponseDto;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;

/**
 **/
@Path("/movement")
@Stateless
public class MovementRestResource {

    final static Logger LOG = LoggerFactory.getLogger(MovementRestResource.class);

    @EJB
    MovementService serviceLayer;
    
    @EJB
    UserServiceBean userService;

    @Context 
    private HttpServletRequest request;

    /**
     *
     * @responseMessage 200 Movement list successfully retreived
     * @responseMessage 500 Error when retrieveing the list values for
     * transponders
     *
     * @summary Gets a list of movements filtered by a query
     *
     */
    @POST
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/list")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto<MovementListResponseDto> getListByQuery(MovementQuery query) {
        LOG.info("Get list invoked in rest layer");
        try {
            return new ResponseDto(serviceLayer.getList(query), ResponseCode.OK);
        } catch (MovementServiceException | NullPointerException ex) {
            LOG.error("[ Error when getting list. ]", ex);
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR);
        } catch (MovementDuplicateException ex) {
            LOG.error("[ Error when getting list. ]", ex);
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR_DUPLICTAE);
        }
    }

    /**
     *
     * @responseMessage 200 Movement list successfully retreived
     * @responseMessage 500 Error when retrieveing the list values for
     * transponders
     *
     * @summary Gets the latest movements for the selected connectIds
     *
     */
    @POST
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/latest")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto<List<MovementDto>> getLatestMovementsByConnectIds(List<String> connectIds) {
        LOG.info("GetLatestMovementsByConnectIds invoked in rest layer");
        if (connectIds == null || connectIds.isEmpty()) {
            return new ResponseDto("ConnectIds cannot be empty" , ResponseCode.ERROR);
        }
        try {
            return new ResponseDto(serviceLayer.getLatestMovementsByConnectIds(connectIds), ResponseCode.OK);
        } catch (MovementServiceException | NullPointerException ex) {
            LOG.error("[ Error when getting list. ]", ex);
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR);
        } catch (MovementDuplicateException ex) {
            LOG.error("[ Error when getting list. ]", ex);
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR_DUPLICTAE);
        }
    }

    @GET
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/{id}")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto getById(@PathParam(value = "id") final String id) {
        LOG.info("Get by id invoked in rest layer");
        try {
            return new ResponseDto(serviceLayer.getById(id), ResponseCode.OK);
        } catch (MovementServiceException | NullPointerException ex) {
            LOG.error("[ Error when getting by id. ] ", ex);
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR);
        } catch (MovementDuplicateException ex) {
            LOG.error("[ Error when getting by id. ]", ex);
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR_DUPLICTAE);
        }
    }

    @POST
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/listByAreaAndTimeInterval")
    @RequiresFeature(UnionVMSFeature.viewMovements)
    public ResponseDto<MovementListResponseDto> getListMovementByAreaAndTimeInterval (MovementAreaAndTimeIntervalCriteria criteria) {
        LOG.info("Get list invoked in rest layer");
        try {
            if (criteria.getAreaCode() == null) {
                criteria.setAreaCode(userService.getUserNationality(request.getRemoteUser()));
            }

            GetMovementListByAreaAndTimeIntervalResponse movementListByAreaAndTimeInterval = serviceLayer.getMovementListByAreaAndTimeInterval(criteria);
            return new ResponseDto(movementListByAreaAndTimeInterval, ResponseCode.OK);
        } catch (MovementServiceException | NullPointerException ex) {
            LOG.error("[ Error when getting list. ]", ex);
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR);
        } catch (MovementDuplicateException ex) {
            LOG.error("[ Error when getting list. ]", ex);
            return new ResponseDto(ex.getMessage(), ResponseCode.ERROR_DUPLICTAE);
        }
    }

}