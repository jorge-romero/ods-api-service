package org.opendevstack.apiservice.project.controller;

import org.opendevstack.apiservice.project.exception.ComponentErrorKey;
import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.springframework.http.HttpStatus;

public final class ComponentsResponseFactory {

    private ComponentsResponseFactory() {
    }

    public static CreateComponentResponse entityCreated(String projectId, String componentId) {
        String path = String.format("/api/pub/v0/projects/%s/components/%s", projectId, componentId);
        return ok(path, "Component created");
    }

    public static CreateComponentResponse badRequest(String path, String message, ComponentErrorKey errorKey) {
        return buildResponse(HttpStatus.BAD_REQUEST, errorKey, path, message);
    }

    public static CreateComponentResponse forbidden(String path, String message, ComponentErrorKey errorKey) {
        return buildResponse(HttpStatus.FORBIDDEN, errorKey, path, message);
    }

    public static CreateComponentResponse notFound(String path, String message, ComponentErrorKey errorKey) {
        return buildResponse(HttpStatus.NOT_FOUND, errorKey, path, message);
    }

    public static CreateComponentResponse internalError(String path, String message, ComponentErrorKey errorKey) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorKey, path, message);
    }

    private static CreateComponentResponse buildResponse(HttpStatus httpStatus, ComponentErrorKey errorKey, String path,
            String message) {
        CreateComponentResponse response = new CreateComponentResponse();
        response.setTimestamp(System.currentTimeMillis());
        response.setHttpStatus(httpStatus.name());
        response.setErrorKey(errorKey.getKey());
        response.setError(errorKey.getMessage());
        response.setMessage(message);
        response.setPath(path);
        return response;
    }

    public static CreateComponentResponse ok(String path, String message) {
        CreateComponentResponse response = new CreateComponentResponse();
        response.setTimestamp(System.currentTimeMillis());
        response.setHttpStatus(HttpStatus.OK.name());
        response.setErrorKey(ComponentErrorKey.OK.getKey());
        response.setMessage(message);
        response.setPath(path);
        return response;
    }
}
