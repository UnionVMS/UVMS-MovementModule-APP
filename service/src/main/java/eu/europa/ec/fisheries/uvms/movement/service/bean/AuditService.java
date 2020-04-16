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

import javax.ejb.Stateless;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.audit.model.exception.AuditModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.message.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.mapper.AuditModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.temp.TempMovement;

@Stateless
public class AuditService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditService.class);

    @Inject
    private MessageProducer producer;

    public void sendMovementCreatedAudit(Movement movement, String username) {
        try {
            String auditData;
            if (MovementTypeType.MAN.equals(movement.getMovementType())) {
                auditData = AuditModuleRequestMapper.mapAuditLogManualMovementCreated(movement.getGuid(), username);
            } else {
                auditData = AuditModuleRequestMapper.mapAuditLogMovementCreated(movement.getGuid(), username);
            }
            producer.sendModuleMessage(auditData, ModuleQueue.AUDIT);
        } catch (AuditModelMarshallException | MovementMessageException e) {
            LOG.error("Failed to send audit log message! Movement with guid {} was created ", movement.getGuid(), e);
        }
    }
    
    // TODO what should be audited during batch?
    public void sendMovementBatchCreatedAudit(String guid, String username) {
        try {
            String auditData = AuditModuleRequestMapper.mapAuditLogMovementBatchCreated(guid, username);
            producer.sendModuleMessage(auditData, ModuleQueue.AUDIT);
        } catch (AuditModelMarshallException | MovementMessageException e) {
            LOG.error("Failed to send audit log message! Movement batch with guid {} was created ", guid, e);
        }
    }

    public void sendTempMovementCreatedAudit(TempMovement tempMovement, String username) {
        try {
            String auditRequest = AuditModuleRequestMapper.mapAuditLogTempMovementCreated(tempMovement.getId(),
                    username);
            producer.sendModuleMessage(auditRequest, ModuleQueue.AUDIT);
        } catch (MovementMessageException | AuditModelMarshallException e) {
            LOG.error("Failed to send audit log message! TempMovement with guid {} was created ", tempMovement.getId(), e);
        }
    }
}
