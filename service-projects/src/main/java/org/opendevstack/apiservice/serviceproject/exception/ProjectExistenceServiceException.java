package org.opendevstack.apiservice.serviceproject.exception;

public class ProjectExistenceServiceException extends Exception {

    public ProjectExistenceServiceException(String message) {
        super(message);
    }

    public ProjectExistenceServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
