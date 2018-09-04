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
package eu.europa.ec.fisheries.uvms.movement.exception;

import eu.europa.ec.fisheries.schema.movement.common.v1.ExceptionType;

/**
 * The MovementDomainException wraps all checked standard Java exception and enriches them with a custom error code.
 * You can use this code to retrieve localized error messages from online documentation. (If implemented)
 *
 * @author Kasim Gul
 */
public class MovementDomainException extends Exception {

    private static final long serialVersionUID = 6413438667115349522L;

    private final ErrorCode code;
    private ExceptionType fault;

    public MovementDomainException(ErrorCode code) {
        super();
        this.code = code;
    }

    public MovementDomainException(String message, Throwable cause, ErrorCode code) {
        super(message, cause);
        this.code = code;
    }

    public MovementDomainException(String message, ErrorCode code) {
        super(message);
        this.code = code;
    }

    public MovementDomainException(Throwable cause, ErrorCode code) {
        super(cause);
        this.code = code;
    }

    public MovementDomainException(String message, Throwable cause, ErrorCode code, ExceptionType fault) {
        super(message, cause);
        this.code = code;
        this.fault = fault;
    }

    public ErrorCode getCode() {
        return this.code;
    }

    public ExceptionType getFault() {
        return fault;
    }
}