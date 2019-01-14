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
package eu.europa.ec.fisheries.uvms.movement.service.validation;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;

public enum SanityRule {

    TIME_MISSING("Time missing") {
        @Override
        public boolean evaluate(IncomingMovement movement) {
            return movement.getPositionTime() == null;
        }
    },
    LAT_MISSING("Lat missing") {
        @Override
        public boolean evaluate(IncomingMovement movement) {
            return movement.getLatitude() == null;
        }
    },
    LONG_MISSING("Long missing") {
        @Override
        public boolean evaluate(IncomingMovement movement) {
            return movement.getLongitude() == null;
        }
    },
    TIME_IN_FUTURE("Time in future") {
        @Override
        public boolean evaluate(IncomingMovement movement) {
            return movement.getPositionTime() != null && 
                    (!movement.getMovementSourceType().equals("AIS") && movement.getPositionTime().isAfter(Instant.now()) ||
                    (movement.getMovementSourceType().equals("AIS") && movement.getPositionTime().isAfter(Instant.now().plus(2, ChronoUnit.MINUTES))));
        }
    },
    PLUGIN_TYPE_MISSING("Plugin Type missing") {
        @Override
        public boolean evaluate(IncomingMovement movement) {
            return movement.getPluginType() == null || movement.getPluginType().isEmpty();
        }
    },
    TRANSPONDER_NOT_FOUND("Transponder not found") {
        @Override
        public boolean evaluate(IncomingMovement movement) {
            return (movement.getPluginType() == null || movement.getPluginType().equals("SATELLITE_RECEIVER")) 
                    && (movement.getMobileTerminalConnectId() == null || movement.getMobileTerminalConnectId().isEmpty());
        }
    },
    MEM_NO_MISSING("Mem No. missing") {
        @Override
        public boolean evaluate(IncomingMovement movement) {
            return (movement.getPluginType() == null || movement.getPluginType().equals("SATELLITE_RECEIVER")) 
                    && movement.getMovementSourceType().equals("INMARSAT_C") 
                    && (movement.getMobileTerminalMemberNumber() == null || movement.getMobileTerminalMemberNumber().isEmpty());
        }
    },
    DNID_MISSING("DNID missing") {
        @Override
        public boolean evaluate(IncomingMovement movement) {
            return (movement.getPluginType() == null || movement.getPluginType().equals("SATELLITE_RECEIVER")) 
                    && movement.getMovementSourceType().equals("INMARSAT_C") 
                    && (movement.getMobileTerminalDNID() == null || movement.getMobileTerminalDNID().isEmpty());
        }
    },
    SERIAL_NO_MISSING("Serial No. missing") {
        @Override
        public boolean evaluate(IncomingMovement movement) {
            return (movement.getPluginType() == null || movement.getPluginType().equals("SATELLITE_RECEIVER")) 
                    && movement.getMovementSourceType().equals("IRIDIUM") 
                    && (movement.getMobileTerminalSerialNumber() == null || movement.getMobileTerminalSerialNumber().isEmpty());
        }
    },
    COMCHANNEL_TYPE_MISSING("ComChannel Type missing") {
        @Override
        public boolean evaluate(IncomingMovement movement) {
            return (movement.getPluginType() == null || movement.getPluginType().equals("SATELLITE_RECEIVER")) 
                    && (movement.getComChannelType() == null || movement.getComChannelType().isEmpty());
        }
    },
    CFR_AND_IRCS_MISSING("CFR and IRCS missing") {
        @Override
        public boolean evaluate(IncomingMovement movement) {
            return ((movement.getAssetCFR() == null || movement.getAssetCFR().isEmpty()) 
                    && (movement.getAssetIRCS() == null || movement.getAssetIRCS().isEmpty())) 
                    && ("FLUX".equals(movement.getPluginType()) || "MANUAL".equals(movement.getComChannelType()));
        }
    },
    ASSET_NOT_FOUND("Asset not found") {
        @Override
        public boolean evaluate(IncomingMovement movement) {
            return movement.getAssetGuid() == null || movement.getAssetGuid().isEmpty();
        }
    };
    
    private String ruleName;
    
    private SanityRule(String ruleName) {
        this.ruleName = ruleName;
    }
    
    public String getRuleName() {
        return ruleName;
    }

    public abstract boolean evaluate(IncomingMovement movement);
    
}
