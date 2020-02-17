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

import eu.europa.ec.fisheries.schema.exchange.module.v1.ExchangeModuleMethod;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.movement.service.dto.ManualMovementDto;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementMapper;
import eu.europa.ec.fisheries.uvms.movement.service.message.ExchangeBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSException;
import java.time.Instant;

@Stateless
public class ManualMovementService {

    private static final Logger LOG = LoggerFactory.getLogger(ManualMovementService.class);

    @Inject
    private ExchangeBean exchangeProducer;

    @Inject
    private AuditService auditService;
    
    public void sendManualMovement(ManualMovementDto incomingMovement, String username) {
        checkUsernameProvided(username);
        validatePosition(incomingMovement.getMovement().getLocation());
        try {
            SetReportMovementType report = MovementMapper.mapToSetReportMovementType(incomingMovement);
            String exchangeRequest = ExchangeModuleRequestMapper.createSetMovementReportRequest(report, username, null,
                    Instant.now(), PluginType.MANUAL, username, null);
            exchangeProducer.sendModuleMessage(exchangeRequest, ExchangeModuleMethod.SET_MOVEMENT_REPORT.value());
//            auditService.sendManualMovementCreatedAudit(exchangeRequest, username);
        } catch (JMSException ex) {
            throw new IllegalArgumentException("Error when marshaling exchange request.", ex);
        }
    }

    private void checkUsernameProvided(String username) {
        if(username == null || username.isEmpty()){
            throw new IllegalArgumentException("Could not get username from request context");
        }
    }

    private void validatePosition(MovementPoint movementPoint){
        Double lat = movementPoint.getLatitude();
        Double lon = movementPoint.getLongitude();
        if (lat == null || lon == null) {
            throw new IllegalArgumentException("Longitude and/or latitude is missing.");
        }
        if (Math.abs(lat) > 90) {
            throw new IllegalArgumentException("Latitude is outside range.");
        }
        if (Math.abs(lon) > 180) {
            throw new IllegalArgumentException("Longitude is outside range.");
        }
    }
}
