package org.opendevstack.apiservice.project.controller;

import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.springframework.http.HttpStatus;

public class ComponentsResponseFactory {

    private ComponentsResponseFactory() {
    }

    public static CreateComponentResponse error(String projectId) {
        CreateComponentResponse response = new CreateComponentResponse();
        response.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage("Failed to create component for project '" + projectId + "'");
        return response;
    }

    public static CreateComponentResponse entityCreated(String projectId, String componentName) {
        CreateComponentResponse response = new CreateComponentResponse();
        response.setErrorCode(HttpStatus.CREATED.value());
        response.setMessage(componentName + " component created successfully in project " + projectId);
        return response;
    }
}
