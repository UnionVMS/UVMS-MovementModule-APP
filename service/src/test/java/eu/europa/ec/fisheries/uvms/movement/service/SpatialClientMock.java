package eu.europa.ec.fisheries.uvms.movement.service;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.service.clients.SpatialClient;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.Area;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaExtendedIdentifierType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreasByLocationType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.BatchSpatialEnrichmentRS;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.ClosestAreasType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.ClosestLocationsType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.Location;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.LocationType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRS;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRSListElement;

@Stateless
public class SpatialClientMock implements SpatialClient {

    public static final String MESSAGE_PRODUCER_METHODS_FAIL = "MESSAGE_PRODUCER_METHODS_FAIL";

    private void shouldIFail() {
        String fail = System.getProperty(MESSAGE_PRODUCER_METHODS_FAIL, "false");
        if(!"false".equals(fail.toLowerCase())) {
            throw new IllegalStateException();
        }
    }

    @Override
    public SpatialEnrichmentRS getEnrichment(Point location) {
        shouldIFail();
        
        SpatialEnrichmentRS spatialEnrichmentRS = new SpatialEnrichmentRS();
        
        populateClosestAreas(spatialEnrichmentRS);
        populateClosestLocations(spatialEnrichmentRS);
        populateAreas(spatialEnrichmentRS);
        
        return spatialEnrichmentRS;
    }

    @Override
    public BatchSpatialEnrichmentRS getBatchEnrichment(List<Point> locations) {
        shouldIFail();
        
        BatchSpatialEnrichmentRS batchSpatialEnrichmentRS = new BatchSpatialEnrichmentRS();
        
        for (Point point : locations) {
            SpatialEnrichmentRSListElement spatialEnrichmentRS = new SpatialEnrichmentRSListElement();
            
            populateClosestAreas(spatialEnrichmentRS);
            populateClosestLocations(spatialEnrichmentRS);
            populateAreas(spatialEnrichmentRS);
            
            batchSpatialEnrichmentRS.getEnrichmentRespLists().add(spatialEnrichmentRS);
        }
        
        return batchSpatialEnrichmentRS;
    }

    public SegmentCategoryType getSegmentCategoryType(Movement movement1, Movement movement2){
        shouldIFail();

        return SegmentCategoryType.IN_PORT;
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
    
    private void populateClosestAreas(SpatialEnrichmentRSListElement spatialEnrichmentRS) {
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
    
    private void populateClosestLocations(SpatialEnrichmentRSListElement spatialEnrichmentRS) {
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
    
    private void populateAreas(SpatialEnrichmentRSListElement spatialEnrichmentRS) {
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