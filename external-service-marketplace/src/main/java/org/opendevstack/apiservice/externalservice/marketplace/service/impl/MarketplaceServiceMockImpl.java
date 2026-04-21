package org.opendevstack.apiservice.externalservice.marketplace.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.marketplace.model.CreateComponentParameter;
import org.opendevstack.apiservice.externalservice.marketplace.model.ProjectComponent;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class MarketplaceServiceMockImpl implements MarketplaceService {

    @Override
    public boolean isHealthy() {
        return true;
    }

    private Map<ComposedId, ProjectComponent> mockComponentsCache = new HashMap<>();

    public ProjectComponent getProjectComponent(String projectId, String componentId) {
        log.info("Get component with id '" + componentId + "' for project '" + projectId + "'");
        ComposedId composedId = new ComposedId(projectId, componentId);
        return mockComponentsCache.get(composedId);
    }

    public ProjectComponent createProjectComponent(String projectId, List<CreateComponentParameter> createComponentParams) {
        log.info("Creating component for project '" + projectId + "'" + " with request: " + createComponentParams);
        ProjectComponent mockComponent = new ProjectComponent();
        mockComponent.setComponentId(UUID.randomUUID());
        mockComponent.setCanBeDeleted(true);
        mockComponent.setStatus("CREATING");
        mockComponent.setName(extractParam(createComponentParams, "component_id"));
        mockComponent.setProductId(extractParam(createComponentParams, "component_type"));
        mockComponent.setProductName("Mock Product");
        mockComponent.setProductDescription("Mock product description");
        mockComponent.setEnvironment("DEV");
        mockComponent.setComponentType("ODS");
        ComposedId composedId = new ComposedId(projectId, mockComponent.getComponentId().toString());
        mockComponentsCache.put(composedId, mockComponent);

        return mockComponent;
    }

    private String extractParam(List<CreateComponentParameter> params, String key) {
        return params.stream()
                .filter(p -> key.equals(p.getName()))
                .map(CreateComponentParameter::getValue)
                .findFirst()
                .orElse(null);
    }

    class ComposedId {
        private String projectId;
        private String componentId;

        public ComposedId(String projectId, String componentId) {
            this.projectId = projectId;
            this.componentId = componentId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ComposedId that = (ComposedId) o;

            if (!projectId.equals(that.projectId)) return false;
            return componentId.equals(that.componentId);
        }

        @Override
        public int hashCode() {
            int result = projectId.hashCode();
            result = 31 * result + componentId.hashCode();
            return result;
        }
    }
}
