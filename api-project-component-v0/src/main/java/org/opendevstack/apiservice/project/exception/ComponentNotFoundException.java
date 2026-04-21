package org.opendevstack.apiservice.project.exception;

public class ComponentNotFoundException extends RuntimeException {

    public ComponentNotFoundException(String message) {
        super(message);
    }
}
