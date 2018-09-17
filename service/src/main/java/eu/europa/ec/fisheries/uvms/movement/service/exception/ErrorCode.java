package eu.europa.ec.fisheries.uvms.movement.service.exception;

/**
 * Having a custom Exception class for each of the below enum constants,
 * was unnecessary. They had no extra functionality. This was we can create
 * online documentation for users to check error code they have in the hand.
 */
public enum ErrorCode {
    ILLEGAL_ARGUMENT_ERROR,
    SERVICE_DUPLICATION_ERROR,
    UNSUCCESSFUL_DB_OPERATION,
    SERVICE_MARSHALL_ERROR,
    JMS_SENDING_ERROR,
    EXCHANGE_MARSHALLING_ERROR,
    PARSING_ERROR,
    DATA_RETRIEVING_ERROR,
    MISSING_MOVEMENT_CONNECT_ERROR
}
