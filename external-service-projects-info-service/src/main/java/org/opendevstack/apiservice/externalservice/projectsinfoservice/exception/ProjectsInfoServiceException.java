package org.opendevstack.apiservice.externalservice.projectsinfoservice.exception;

/**
 * Exception thrown when there are issues with projects info service operations.
 */
public class ProjectsInfoServiceException extends Exception {

    public ProjectsInfoServiceException(String message) {
        super(message);
    }

    public ProjectsInfoServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
