package eu.europa.ec.fisheries.uvms.movement.rest;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * This is a copy of the {@code eu.europa.ec.fisheries.uvms.spatial.rest.util.ObjectMapperContextResolver}
 * of the Spatial module, so that the configuration for serialization is the same in tests and in runtime.
 */
@Provider
public class TestObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
	private final ObjectMapper mapper;

	public TestObjectMapperContextResolver() {
		mapper = new ObjectMapper();

		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return mapper;
	}
}
