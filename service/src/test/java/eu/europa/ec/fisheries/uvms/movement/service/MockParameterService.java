package eu.europa.ec.fisheries.uvms.movement.service;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.util.List;

import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.uvms.config.exception.ConfigServiceException;
import eu.europa.ec.fisheries.uvms.config.service.ParameterService;

@Priority(100)
@Alternative
@ApplicationScoped
public class MockParameterService implements ParameterService {
	@Override
	public String getParamValueById(String key) throws ConfigServiceException {
		if ("flux_local_nation_code".equals(key)) {
			return "SRC";
		}
		return null;
	}

	@Override
	public boolean removeParameter(String key) throws ConfigServiceException {
		return false;
	}

	@Override
	public List<SettingType> getSettings(List<String> keys) throws ConfigServiceException {
		return null;
	}

	@Override
	public List<SettingType> getAllSettings() throws ConfigServiceException {
		return null;
	}

	@Override
	public boolean setStringValue(String key, String value, String description) throws ConfigServiceException {
		return false;
	}

	@Override
	public Boolean getBooleanValue(String key) throws ConfigServiceException {
		return null;
	}

	@Override
	public void reset(String key) throws ConfigServiceException {

	}

	@Override
	public void clearAll() throws ConfigServiceException {

	}

	@Override
	public Long countParameters() {
		return null;
	}
}
