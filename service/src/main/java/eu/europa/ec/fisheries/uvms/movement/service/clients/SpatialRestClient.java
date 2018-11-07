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
package eu.europa.ec.fisheries.uvms.movement.service.clients;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.spatial.model.mapper.SpatialModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.*;
import org.slf4j.Logger;

@Stateless
public class SpatialRestClient implements SpatialClient {

    private WebTarget webTarget;

    @Resource(name = "java:global/spatial_endpoint")
    private String spatialEndpoint;
    
    @PostConstruct
    public void initClient() {
        String url = spatialEndpoint + "/spatialnonsecure/json/";

        Client client = ClientBuilder.newClient();
        client.register(new ContextResolver<ObjectMapper>() {
            @Override
            public ObjectMapper getContext(Class<?> type) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JaxbAnnotationModule());
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return mapper;
            }
        });
        webTarget = client.target(url);
    }
    
    public SpatialEnrichmentRS getEnrichment(Point location) {
        PointType point = new PointType();
        point.setCrs(4326); //this magical int is the World Geodetic System 1984, aka EPSG:4326. See: https://en.wikipedia.org/wiki/World_Geodetic_System or http://spatialreference.org/ref/epsg/wgs-84/
        point.setLatitude(location.getY());
        point.setLongitude(location.getX());
        List<LocationType> locationTypes = Collections.singletonList(LocationType.PORT);
        List<AreaType> areaTypes = Collections.singletonList(AreaType.COUNTRY);
        SpatialEnrichmentRQ request = mapToCreateSpatialEnrichmentRequest(point, UnitType.NAUTICAL_MILES, locationTypes, areaTypes);
        
        Response response =  webTarget
                .path("getEnrichment")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request), Response.class);

        SpatialEnrichmentRS spatialEnrichments = response.readEntity(new GenericType<SpatialEnrichmentRS>() {});
        response.close();
        return spatialEnrichments;
    }

    public SegmentCategoryType getSegmentCategoryType(Movement movement1, Movement movement2){

        eu.europa.ec.fisheries.schema.movement.v1.MovementType movementType1 = MovementMapper.mapMovementToMovementTypeForSpatial(movement1);
        MovementType movementType2 = MovementMapper.mapMovementToMovementTypeForSpatial(movement2);

        List<MovementType> request = new ArrayList<>();
        request.add(movementType1);
        request.add(movementType2);

        //this is here to make a correct json string for the rest call since I cant make Entity.json do so.......
        ObjectMapper om = new ObjectMapper();
        String s = "";
        try {
            s = om.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Response response =  webTarget
                .path("getSegmentCategoryType")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(s), Response.class);

        SegmentCategoryType returnValue = response.readEntity(new GenericType<SegmentCategoryType>() {});
        response.close();

        return returnValue;
    }
    
    public BatchSpatialEnrichmentRS getBatchEnrichment(List<Point> locations) {
        List<SpatialEnrichmentRQListElement> batchReqElements = new ArrayList<>();
        for (Point location : locations) {
            PointType point = new PointType();
            point.setCrs(4326);
            point.setLatitude(location.getY());
            point.setLongitude(location.getX());
            List<LocationType> locationTypes = Collections.singletonList(LocationType.PORT);
            List<AreaType> areaTypes = Collections.singletonList(AreaType.COUNTRY);
            SpatialEnrichmentRQListElement spatialEnrichmentRQListElement = SpatialModuleRequestMapper.mapToCreateSpatialEnrichmentRQElement(point, UnitType.NAUTICAL_MILES, locationTypes, areaTypes);
            batchReqElements.add(spatialEnrichmentRQListElement);
        }
        BatchSpatialEnrichmentRQ request = mapToCreateBatchSpatialEnrichmentRequest(batchReqElements);

        Response response = webTarget
                .path("getEnrichmentBatch")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request), Response.class);

        BatchSpatialEnrichmentRS spatialEnrichments = response.readEntity(new GenericType<BatchSpatialEnrichmentRS>() {});
        response.close();
        return spatialEnrichments;
    }
    
    private SpatialEnrichmentRQ mapToCreateSpatialEnrichmentRequest(PointType point, UnitType unit, List<LocationType> locationTypes, List<AreaType> areaTypes) {
        SpatialEnrichmentRQ request = new SpatialEnrichmentRQ();
        request.setMethod(SpatialModuleMethod.GET_ENRICHMENT);
        request.setPoint(point);
        request.setUnit(unit);
        SpatialEnrichmentRQ.LocationTypes loc = new SpatialEnrichmentRQ.LocationTypes();
        if (locationTypes != null) {
            loc.getLocationTypes().addAll(locationTypes);
        }
        request.setLocationTypes(loc);

        SpatialEnrichmentRQ.AreaTypes area = new SpatialEnrichmentRQ.AreaTypes();
        if (areaTypes != null) {
            area.getAreaTypes().addAll(areaTypes);
        }
        request.setAreaTypes(area);

        return request;
    }
    
    private BatchSpatialEnrichmentRQ mapToCreateBatchSpatialEnrichmentRequest(List<SpatialEnrichmentRQListElement> spatialEnrichmentRQListElements) {
        BatchSpatialEnrichmentRQ batchRequest = new BatchSpatialEnrichmentRQ();
        batchRequest.setEnrichmentLists(spatialEnrichmentRQListElements);
        batchRequest.setMethod(SpatialModuleMethod.GET_ENRICHMENT_BATCH);
        return batchRequest;
    }
}
