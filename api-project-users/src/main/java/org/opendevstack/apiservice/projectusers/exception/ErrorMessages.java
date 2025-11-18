package org.opendevstack.apiservice.projectusers.exception;

/**
 * Contains constant error messages used throughout the project users module.
 * <p>
 * This class centralizes all error message strings to ensure consistency and 
 * maintainability when handling exceptions and reporting errors within the 
 * Project Users API. By using this class, developers can avoid hardcoding 
 * error messages in multiple places, making it easier to update and manage 
 * error responses across the application.
 * </p>
 */
public final class ErrorMessages {
    private ErrorMessages() {}

    public static final String REQUEST_VALIDATION_FAILED = "Request validation failed with %d error(s). Please check the field errors for details.";
    public static final String PARAMETER_VALIDATION_FAILED = "Parameter validation failed: %s";
    public static final String INVALID_REQUEST_BODY = "Invalid request body";
    public static final String INVALID_VALUE_FOR_FIELD = "Invalid value '%s' for field '%s'. Expected type: %s";
    public static final String EXPECTED_TYPE_BUT_RECEIVED_INVALID_INPUT = "Expected %s but received invalid input";
    public static final String MISSING_OR_INVALID_VALUE_FOR_FIELD = "Missing or invalid value for required field '%s'. Expected type: %s";
    public static final String INVALID_JSON_STRUCTURE_AT_FIELD = "Invalid JSON structure at field '%s': %s";
    public static final String INVALID_JSON_STRUCTURE = "Invalid JSON structure: %s";
    public static final String REQUIRED_PATH_PARAMETER_MISSING = "Required path parameter '%s' is missing from the URL";
    public static final String REQUIRED_REQUEST_PARAMETER_MISSING = "Required request parameter '%s' of type '%s' is missing";
    public static final String PARAMETER_TYPE_CONVERSION_FAILED = "Parameter '%s' with value '%s' could not be converted to type '%s'";
    public static final String EXTERNAL_SERVICE_ERROR = "External service error: %s";
    public static final String OPERATION_FAILED = "Operation failed: %s";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred. Please try again later.";
    public static final String PARAMETER_FORMAT = "Parameter '%s': %s";
    public static final String PARAMETER_FORMAT_WITH_VALUE = "Parameter '%s': %s (provided: '%s')";

    public static final String PROJECT_NOT_FOUND = "Project with key '%s' not found";
    public static final String USER_NOT_FOUND = "User '%s' not found";
    public static final String INVALID_ROLE = "Invalid role '%s'";
}
