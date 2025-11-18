package org.opendevstack.apiservice.projectusers.exception;

import lombok.Getter;

/**
 * Base exception class for project user operations.
 * This is a runtime exception to allow for cleaner exception handling in Spring controllers
 * and to avoid issues with CGLIB proxies and interface implementation.
 */
@Getter
public class ProjectUserException extends RuntimeException {
    private final String errorCode;

    public ProjectUserException(String message) {
        super(message);
        this.errorCode = ErrorCodes.PROJECT_USER_ERROR;
    }

    public ProjectUserException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ProjectUserException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCodes.PROJECT_USER_ERROR;
    }

    public ProjectUserException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
