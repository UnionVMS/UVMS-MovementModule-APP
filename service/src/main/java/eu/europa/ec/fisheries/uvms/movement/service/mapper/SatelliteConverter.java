package eu.europa.ec.fisheries.uvms.movement.service.mapper;

import eu.europa.ec.fisheries.uvms.movement.model.constants.SatId;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class SatelliteConverter implements AttributeConverter<SatId, Integer> {


        @Override
        public Integer convertToDatabaseColumn(SatId attribute) {
            return attribute.getValue();
        }

        @Override
        public SatId convertToEntityAttribute(Integer dbData) {
            return SatId.fromInt(dbData);

        }
}
