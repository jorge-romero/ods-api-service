package org.opendevstack.apiservice.project.exception;

public class ComponentDeletionException extends RuntimeException {

    public ComponentDeletionException(String message) {
        super(message);
    }

    public ComponentDeletionException(String message, Exception e) {
        super(message, e);
    }
}
