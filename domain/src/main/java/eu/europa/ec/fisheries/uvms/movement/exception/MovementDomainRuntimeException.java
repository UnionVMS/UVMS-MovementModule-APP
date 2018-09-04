package eu.europa.ec.fisheries.uvms.movement.exception;

import eu.europa.ec.fisheries.schema.movement.common.v1.ExceptionType;

/**
 * The MovementModelException wraps all checked standard Java runtime exception and enriches them with a custom error code.
 * You can use this code to retrieve localized error messages from online documentation. (If implemented)
 *
 * @author Kasim Gul
 */
public class MovementDomainRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode code;
    private ExceptionType fault;

    public MovementDomainRuntimeException(ErrorCode code) {
        super();
        this.code = code;
    }

    public MovementDomainRuntimeException(String message, Throwable cause, ErrorCode code) {
        super(message, cause);
        this.code = code;
    }

    public MovementDomainRuntimeException(String message, ErrorCode code) {
        super(message);
        this.code = code;
    }

    public MovementDomainRuntimeException(Throwable cause, ErrorCode code) {
        super(cause);
        this.code = code;
    }

    public MovementDomainRuntimeException(String message, ExceptionType fault, ErrorCode code) {
        super(message);
        this.fault = fault;
        this.code = code;
    }

    public ErrorCode getCode() {
        return this.code;
    }

    public ExceptionType getFault() {
        return fault;
    }
}
