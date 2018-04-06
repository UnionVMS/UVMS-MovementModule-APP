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
package eu.europa.ec.fisheries.uvms.movement.message.mapper;

import eu.europa.ec.fisheries.uvms.audit.model.exception.AuditModelMarshallException;
import eu.europa.ec.fisheries.uvms.audit.model.mapper.AuditLogMapper;
import eu.europa.ec.fisheries.uvms.movement.model.constants.AuditObjectTypeEnum;
import eu.europa.ec.fisheries.uvms.movement.model.constants.AuditOperationEnum;

public class AuditModuleRequestMapper {

    public static String mapAuditLogMovementCreated(String guid, String username) throws AuditModelMarshallException {
        return mapToAuditLog(AuditObjectTypeEnum.AUTOMATIC_POSITION_REPORT.getValue(), AuditOperationEnum.CREATE.getValue(), guid, username);
    }

    public static String mapAuditLogManualMovementCreated(String guid, String username) throws AuditModelMarshallException {
        return mapToAuditLog(AuditObjectTypeEnum.MANUAL_POSITION_REPORT.getValue(), AuditOperationEnum.CREATE.getValue(), guid, username);
    }

    public static String mapAuditLogTempMovementCreated(String guid, String username) throws AuditModelMarshallException {
        return mapToAuditLog(AuditObjectTypeEnum.TEMP_POSITION_REPORT.getValue(), AuditOperationEnum.CREATE.getValue(), guid, username);
    }

    public static String mapAuditLogMovementBatchCreated(String guid, String username) throws AuditModelMarshallException {
        return mapToAuditLog(AuditObjectTypeEnum.POSITION_REPORT_BATCH.getValue(), AuditOperationEnum.CREATE.getValue(), guid, username);
    }

    private static String mapToAuditLog(String objectType, String operation, String affectedObject, String username) throws AuditModelMarshallException {
        return AuditLogMapper.mapToAuditLog(objectType, operation, affectedObject, username);
    }

}