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
package eu.europa.ec.fisheries.uvms.movement.model.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import eu.europa.ec.fisheries.schema.movement.v1.MovementType;

public class ListResponseDto implements Serializable {

    private static final long serialVersionUID = -6741750801554281012L;

    private List<MovementType> movementList;
    private BigInteger totalNumberOfPages;
    private BigInteger currentPage;

    public List<MovementType> getMovementList() {
        return movementList;
    }

    public void setMovementList(List<MovementType> movementList) {
        this.movementList = movementList;
    }

    public BigInteger getTotalNumberOfPages() {
        return totalNumberOfPages;
    }

    public void setTotalNumberOfPages(BigInteger totalNumberOfPages) {
        this.totalNumberOfPages = totalNumberOfPages;
    }

    public BigInteger getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(BigInteger currentPage) {
        this.currentPage = currentPage;
    }
}