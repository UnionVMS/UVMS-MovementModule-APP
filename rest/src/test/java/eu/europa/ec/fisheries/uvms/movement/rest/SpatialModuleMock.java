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
package eu.europa.ec.fisheries.uvms.movement.rest;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.Area;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaExtendedIdentifierType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreasByLocationType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.ClosestAreasType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.ClosestLocationsType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.Location;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.LocationType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRQ;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRS;

@Path("spatial/spatialnonsecure/json")
@Stateless
public class SpatialModuleMock {

    @POST
    @Path("getEnrichment")
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    public Response getEnrichment(SpatialEnrichmentRQ spatialEnrichmentRQ) {
        SpatialEnrichmentRS spatialEnrichmentRS = new SpatialEnrichmentRS();

        populateClosestAreas(spatialEnrichmentRS);
        populateClosestLocations(spatialEnrichmentRS);
        populateAreas(spatialEnrichmentRS);

        return Response.ok(spatialEnrichmentRS).build();
    }


    @POST
    @Path("getSegmentCategoryType")
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(value = {MediaType.APPLICATION_JSON})
    public Response getSegmentCategoryType(List<MovementType> movements) {
        return Response.ok(SegmentCategoryType.IN_PORT).build();
    }
    
    private void populateClosestAreas(SpatialEnrichmentRS spatialEnrichmentRS) {
        List<Area> closestAreas = new ArrayList<>();
        Area area = new Area();
        area.setAreaType(AreaType.COUNTRY);
        area.setCode("SWE");
        area.setId("SWE");
        area.setName("Sweden");
        area.setDistance(0d);
        closestAreas.add(area);
        spatialEnrichmentRS.setClosestAreas(new ClosestAreasType(closestAreas));
    }
    
    private void populateClosestLocations(SpatialEnrichmentRS spatialEnrichmentRS) {
        ClosestLocationsType closestLocationsType = new ClosestLocationsType();
        ArrayList<Location> closestLocations = new ArrayList<>();
        Location location = new Location();
        location.setLocationType(LocationType.PORT);
        location.setCode("GOT");
        location.setName("Gothenburg");
        location.setId("PortId");
        location.setDistance(0d);
        closestLocations.add(location);
        closestLocationsType.setClosestLocations(closestLocations);
        spatialEnrichmentRS.setClosestLocations(closestLocationsType);
    }
    
    private void populateAreas(SpatialEnrichmentRS spatialEnrichmentRS) {
        AreasByLocationType areasByLocationType = new AreasByLocationType();
        List<AreaExtendedIdentifierType> areas = new ArrayList<>();
        AreaExtendedIdentifierType area1 = new AreaExtendedIdentifierType();
        area1.setId("SWE");
        area1.setAreaType(AreaType.COUNTRY);
        area1.setCode("SWE");
        area1.setName("Sweden");
        areas.add(area1);
        AreaExtendedIdentifierType area2 = new AreaExtendedIdentifierType();
        area2.setId("EU");
        area2.setAreaType(AreaType.EEZ);
        area2.setCode("EU");
        area2.setName("Europe");
        areas.add(area2);
        areasByLocationType.setAreas(areas);
        spatialEnrichmentRS.setAreasByLocation(areasByLocationType);
    }
    
}
