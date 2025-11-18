package org.opendevstack.apiservice.projectplatform.exception;

/**
 * Exception thrown when there are issues with projects info service operations.
 */
public class ProjectPlatformsException extends Exception {

    public ProjectPlatformsException(String message) {
        super(message);
    }

    public ProjectPlatformsException(String message, Throwable cause) {
        super(message, cause);
    }
}
