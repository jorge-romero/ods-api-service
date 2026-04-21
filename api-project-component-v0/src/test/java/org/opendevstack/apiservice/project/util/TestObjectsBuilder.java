package org.opendevstack.apiservice.project.util;

import org.opendevstack.apiservice.externalservice.marketplace.model.ProjectComponent;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.ComponentsStatusDTO;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.EnvironmentsDTO;

import java.util.HashMap;
import java.util.UUID;

public class TestObjectsBuilder {

    private TestObjectsBuilder() {
    }

    public static Component buildTestComponent() {
        Component component = new Component();
        component.setId("testId");
        component.setName("testComponentName");
        component.setEnvironment(EnvironmentsDTO.DEV);
        component.setStatus(ComponentsStatusDTO.RUNNING);
        component.setComponentType("testComponentType");
        component.setParams(new HashMap<>());
        return component;
    }

    public static ProjectComponent buildTestMarketplaceComponent() {
        ProjectComponent component = new ProjectComponent();
        component.setComponentId(UUID.randomUUID());
        component.setStatus("RUNNING");
        component.setCanBeDeleted(false);
        component.setComponentUrl("http://test.component.url");
        return component;
    }

    public static CreateComponentRequest buildTestCreateComponentRequest() {
        CreateComponentRequest request = new CreateComponentRequest();
        request.setName("testcomponent");
        request.setProductId("testProductId");
        request.setParams(new HashMap<>());
        return request;
    }
}
