package eu.europa.ec.fisheries.uvms.movement.model.exception;

/**
 * Having a custom Exception class for each of the below enum constants,
 * was unnecessary. They had no extra functionality. This was we can create
 * online documentation for users to check error code they have in the hand.
 */
public enum ErrorCode {
    INPUT_ARGUMENT_ERROR,
    MODEL_MARSHALL_ERROR,
    MODEL_MAPPER_ERROR,
    MOVEMENT_DAO_ERROR,
    MOVEMENT_DB_ERROR,
    MOVEMENT_DUPLICATE_ERROR,
    MOVEMENT_FAULT_ERROR,
    MODEL_VALIDATION_ERROR,

    // Runtime Exceptions
    ILLEGAL_ARGUMENT_ERROR
}
