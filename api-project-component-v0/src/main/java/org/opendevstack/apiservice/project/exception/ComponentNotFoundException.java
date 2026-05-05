package org.opendevstack.apiservice.project.exception;

public class ComponentNotFoundException extends RuntimeException {

    public ComponentNotFoundException(String message) {
        super(message);
    }

    public ComponentNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
