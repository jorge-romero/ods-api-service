package org.opendevstack.apiservice.project.controller;

import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;

public final class ProjectResponseFactory {

    private ProjectResponseFactory() {
    }

    public static CreateProjectResponse notFound(String projectKey, String location) {
        return error(
                ErrorKey.PROJECT_NOT_FOUND.getMessage(), 
                ErrorKey.PROJECT_NOT_FOUND.getKey(),
                String.format("Project with key '%s' not found", projectKey),
                location
        );
    }

    private static CreateProjectResponse error(String error, String errorKey, String message, String location) {
        CreateProjectResponse response = new CreateProjectResponse();
        response.setError(error);
        response.setErrorKey(errorKey);
        response.setMessage(message);
        response.setLocation(location);
        return response;
    }
}