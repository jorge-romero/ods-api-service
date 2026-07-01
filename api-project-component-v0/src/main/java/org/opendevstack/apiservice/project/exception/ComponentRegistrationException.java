package org.opendevstack.apiservice.project.exception;

public class ComponentRegistrationException extends RuntimeException {

    public ComponentRegistrationException(String message) {
        super(message);
    }

    public ComponentRegistrationException(String message, Exception e) {
        super(message, e);
    }
}
