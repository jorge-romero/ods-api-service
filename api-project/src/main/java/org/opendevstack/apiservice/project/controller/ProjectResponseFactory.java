package org.opendevstack.apiservice.project.controller;

import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;

public final class ProjectResponseFactory {

    private ProjectResponseFactory() {
    }

    public static CreateProjectResponse conflict(String message, String location) {
        return error(
                ErrorKey.PROJECT_ALREADY_EXISTS.getMessage(), 
                ErrorKey.PROJECT_ALREADY_EXISTS.getKey(), 
                message, location);
    }

    public static CreateProjectResponse projectKeyGenerationFailed(String location) {
        return error(ErrorKey.INTERNAL_ERROR.getMessage(), 
                "PROJECT_KEY_GENERATION_FAILED",
                "Failed to generate a unique project key.",
                location);
    }

    public static CreateProjectResponse notFound(String projectKey, String location) {
        return error(
                ErrorKey.PROJECT_NOT_FOUND.getMessage(), 
                ErrorKey.PROJECT_NOT_FOUND.getKey(),
                String.format("Project with key '%s' not found", projectKey),
                location
        );
    }

    public static CreateProjectResponse internalError(String location) {
        return error(
                ErrorKey.INTERNAL_ERROR.getMessage(),
                ErrorKey.INTERNAL_ERROR.getKey(),
                "An error occurred while processing the request.",
                location);
    }

    public static CreateProjectResponse internalError(String location, String message) {
        return error(
                ErrorKey.INTERNAL_ERROR.getMessage(),
                ErrorKey.INTERNAL_ERROR.getKey(),
                message,
                location);
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