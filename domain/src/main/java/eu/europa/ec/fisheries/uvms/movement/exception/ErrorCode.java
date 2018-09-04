package eu.europa.ec.fisheries.uvms.movement.exception;

/**
 * Having a custom Exception class for each of the below enum constants,
 * was unnecessary. They had no extra functionality. This was we can create
 * online documentation for users to check error code they have in the hand.
 */
public enum ErrorCode {
    ILLEGAL_ARGUMENT_ERROR,
    AREA_DAO_ERROR,
    DAO_MAPPING_ERROR,
    SEARCH_GROUP_DAO_ERROR,
    DAO_SEARCH_MAPPER_ERROR,
    DAO_NO_ENTITY_FOUND_ERROR,
    CALCULATION_UTIL_ERROR,
    ENTITY_DUPLICATION_ERROR,
    GEOMETRY_UTIL_ERROR,
    MODEL_SEARCH_MAPPER_ERROR,
    DATA_RETRIEVING_ERROR,
    RETRIEVING_LATEST_MOVEMENT_ERROR,
    RETRIEVING_FIST_MOVEMENT_ERROR,
    DAO_PERSIST_ERROR,
    UNSUCCESSFUL_DB_OPERATION,
    CLASS_CAST_ERROR,
    PARSING_ERROR
}