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
package eu.europa.ec.fisheries.uvms.movement.service.bean;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import eu.europa.ec.fisheries.uvms.config.constants.ConfigHelper;
import eu.europa.ec.fisheries.uvms.movement.service.constant.ParameterKey;

@Stateless
public class MovementConfigHelper implements ConfigHelper {

    public static final String MODULE_VERSION_PROPERTIES_KEY = "uvms.module.version";

    @Inject
    private PropertiesBean propertiesBean;

    @PersistenceContext
    protected EntityManager em;

    private final static String MOVEMENT_PU = "movement";

    @Override
    public List<String> getAllParameterKeys() {
        return Arrays.stream(ParameterKey.values())
                .map(ParameterKey::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public String getModuleName() {
        return MOVEMENT_PU;
    }

    @Override
    public String getModuleVersion() {
        return propertiesBean.getProperty(MODULE_VERSION_PROPERTIES_KEY);
    }

    @Override
	public EntityManager getEntityManager() {
		return em;
	}
}
