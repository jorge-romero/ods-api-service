package org.opendevstack.apiservice.project.util;

import org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentExtendedInfo;
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

    public static ProjectComponentExtendedInfo buildTestMarketplaceComponent() {
        ProjectComponentExtendedInfo component = new ProjectComponentExtendedInfo();
        component.setComponentId(UUID.randomUUID().toString());
        component.setStatus("CREATING");
        component.setComponentUrl("http://test.component.url");
        component.setCatalogItemId("cHJvamVjdHMvVEVTVC9yZXBvcy9DYXRhbG9nSXRlbS55YW1s");
        component.setCatalogItemRef("P2F0PXJlZnMvaGVhZHMvbWFzdGVy");
        return component;
    }

    public static CatalogItem buildTestCatalogItem() {
        CatalogItem catalogItem = new CatalogItem();
        catalogItem.setId(UUID.randomUUID().toString());
        catalogItem.setTitle("Test Catalog Item");
        catalogItem.setShortDescription("This is a test catalog item");
        return catalogItem;
    }

    public static CreateComponentRequest buildTestCreateComponentRequest() {
        CreateComponentRequest request = new CreateComponentRequest();
        request.setName("testcomponent");
        request.setProductId("testProductId");
        request.setParams(new HashMap<>());
        return request;
    }
}
