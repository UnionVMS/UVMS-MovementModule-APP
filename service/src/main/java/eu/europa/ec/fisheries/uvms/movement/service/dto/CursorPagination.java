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
package eu.europa.ec.fisheries.uvms.movement.service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;

public class CursorPagination {

    private Instant timestampCursor;
    private UUID idCursor;
    
    private Instant from;
    private Instant to;
    
    private List<UUID> connectIds;
    private List<MovementSourceType> sources;
    
    private Integer limit;

    public Instant getTimestampCursor() {
        return timestampCursor;
    }

    public void setTimestampCursor(Instant timestampCursor) {
        this.timestampCursor = timestampCursor;
    }

    public UUID getIdCursor() {
        return idCursor;
    }

    public void setIdCursor(UUID idCursor) {
        this.idCursor = idCursor;
    }

    public Instant getFrom() {
        return from;
    }

    public void setFrom(Instant from) {
        this.from = from;
    }

    public Instant getTo() {
        return to;
    }

    public void setTo(Instant to) {
        this.to = to;
    }

    public List<UUID> getConnectIds() {
        return connectIds;
    }

    public void setConnectIds(List<UUID> connectIds) {
        this.connectIds = connectIds;
    }

    public List<MovementSourceType> getSources() {
        return sources;
    }

    public void setSources(List<MovementSourceType> sources) {
        this.sources = sources;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    
}
