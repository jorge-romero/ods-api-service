package org.opendevstack.apiservice.project.exception;

public class ComponentAlreadyExistsException extends RuntimeException {

    public ComponentAlreadyExistsException(String message) {
        super(message);
    }

    public ComponentAlreadyExistsException(String message, Exception e) {
        super(message, e);
    }
}