package org.opendevstack.apiservice.projectusers.exception;

/**
 * Exception thrown when a project is not found.
 */
public class ProjectNotFoundException extends ProjectUserException {
    public ProjectNotFoundException(String projectKey) {
        super(String.format(ErrorMessages.PROJECT_NOT_FOUND, projectKey), ErrorCodes.PROJECT_NOT_FOUND);
    }
}
