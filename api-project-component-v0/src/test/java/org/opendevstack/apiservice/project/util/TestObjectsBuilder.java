package org.opendevstack.apiservice.project.util;

import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.springframework.http.HttpStatus;

public class TestObjectsBuilder {

    private TestObjectsBuilder() {
    }

    public static Component buildTestComponent() {
        Component component = new Component();
        component.setId("testId");
        component.setName("testComponentName");
        component.environment("testEnv");
        component.setComponentType("testComponentType");
        return component;
    }

    public static CreateComponentRequest buildTestCreateComponentRequest() {
        CreateComponentRequest request = new CreateComponentRequest();
        request.setName("testComponentName");
        request.setProductId("testProductId");
        return request;
    }

    public static CreateComponentResponse buildTestCreateComponentResponseSuccess(String componentName, String projectId) {
        CreateComponentResponse response = new CreateComponentResponse();
        response.setErrorCode(HttpStatus.CREATED.value());
        response.setMessage(componentName + " component created successfully in project " + projectId);
        return response;
    }

    public static CreateComponentResponse buildTestCreateComponentResponseFailure(String projectId) {
        CreateComponentResponse response = new CreateComponentResponse();
        response.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage("Failed to create component for project '" + projectId + "'");
        return response;
    }
}
