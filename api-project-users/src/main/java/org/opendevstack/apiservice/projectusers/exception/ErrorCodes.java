package org.opendevstack.apiservice.projectusers.exception;

/**
 * Defines error codes specific to the Project Users API module.
 * <p>
 * This class centralizes all error code definitions used for exception handling
 * within the project users domain. By maintaining error codes in a single location,
 * it ensures consistency and facilitates easier maintenance and troubleshooting.
 * </p>
 * <p>
 * Error codes defined here are typically referenced in custom exceptions and error
 * responses, enabling clients and developers to identify and resolve issues efficiently.
 * </p>
 */
public final class ErrorCodes {
    private ErrorCodes() {}

    public static final String PROJECT_USER_ERROR = "PROJECT_USER_ERROR";
    public static final String PROJECT_NOT_FOUND = "PROJECT_NOT_FOUND";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String INVALID_ROLE = "INVALID_ROLE";
    public static final String AUTOMATION_PLATFORM_ERROR = "AUTOMATION_PLATFORM_ERROR";
    public static final String TOKEN_CREATION_ERROR = "TOKEN_CREATION_ERROR";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String TOKEN_DECODING_ERROR = "TOKEN_DECODING_ERROR";
}
