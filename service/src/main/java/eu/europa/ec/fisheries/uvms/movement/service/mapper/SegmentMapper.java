package eu.europa.ec.fisheries.uvms.movement.service.mapper;

import eu.europa.ec.fisheries.uvms.movement.service.dto.SegmentDTO;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.service.util.WKTUtil;

public class SegmentMapper {

    public static SegmentDTO mapToSegmentDTO(Segment segment){
        SegmentDTO dto = new SegmentDTO();
        dto.setId(segment.getId());
        dto.setCourseOverGround(segment.getCourseOverGround().doubleValue());
        dto.setDistance(segment.getDistance());
        dto.setDuration(segment.getDuration());
        dto.setFromMovement(segment.getFromMovement().getId().toString());
        dto.setToMovement(segment.getToMovement().getId().toString());
        dto.setLocation(WKTUtil.getWktLineStringFromSegment(segment));
        dto.setSegmentCategory(segment.getSegmentCategory());
        dto.setSpeedOverGround(segment.getSpeedOverGround().doubleValue());
        dto.setTrack(segment.getTrack().getId());
        dto.setUpdated(segment.getUpdated());
        dto.setUpdatedBy(segment.getUpdatedBy());

        return dto;
    }
}
