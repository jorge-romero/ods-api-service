package org.opendevstack.apiservice.project.exception;

public class ComponentRetrievalException extends RuntimeException {

    public ComponentRetrievalException(String message) {
        super(message);
    }

    public ComponentRetrievalException(String message, Exception e) {
        super(message, e);
    }
}