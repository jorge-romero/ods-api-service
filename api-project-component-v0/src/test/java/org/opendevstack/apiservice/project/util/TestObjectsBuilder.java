package org.opendevstack.apiservice.project.util;

import org.opendevstack.apiservice.externalservice.marketplace.model.CreateComponentParameter;
import org.opendevstack.apiservice.externalservice.marketplace.model.ProjectComponent;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

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

    public static ProjectComponent buildTestMarketplaceComponent() {
        ProjectComponent component = new ProjectComponent();
        component.setComponentId("testComponentId");
        component.setCanBeDeleted(false);
        component.setComponentUrl("http://test.component.url");
        return component;
    }

    public static CreateComponentRequest buildTestCreateComponentRequest() {
        CreateComponentRequest request = new CreateComponentRequest();
        request.setName("testComponentName");
        request.setProductId("testProductId");
        return request;
    }

    public static List<CreateComponentParameter> buildTestMarketplaceCreateComponentParameters() {
        List<CreateComponentParameter> parameters = new ArrayList<>();
        parameters.add(new CreateComponentParameter("name", "string", "testComponentName"));
        parameters.add(new CreateComponentParameter("productId", "string", "testProductId"));
        return parameters;
    }

    public static CreateComponentResponse buildTestCreateComponentResponseSuccess(String componentId, String projectId) {
        CreateComponentResponse response = new CreateComponentResponse();
        response.setErrorCode(HttpStatus.CREATED.value());
        response.setMessage(componentId + " component created successfully in project " + projectId);
        return response;
    }

    public static CreateComponentResponse buildTestCreateComponentResponseFailure(String projectId) {
        CreateComponentResponse response = new CreateComponentResponse();
        response.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage("Failed to create component for project '" + projectId + "'");
        return response;
    }
}
