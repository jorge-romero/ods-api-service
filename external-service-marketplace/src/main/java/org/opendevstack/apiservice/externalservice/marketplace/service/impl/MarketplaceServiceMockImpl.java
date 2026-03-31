package org.opendevstack.apiservice.externalservice.marketplace.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.marketplace.model.CreateComponentParameter;
import org.opendevstack.apiservice.externalservice.marketplace.model.ProjectComponent;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        mockComponent.setComponentId(generateNextId());
        mockComponent.setCanBeDeleted(true);
        mockComponent.setStatus("CREATING");
        ComposedId composedId = new ComposedId(projectId, mockComponent.getComponentId());
        mockComponentsCache.put(composedId, mockComponent);

        return mockComponent;
    }

    private String generateNextId() {
        return "mock-component-id-" + (mockComponentsCache.size() + 1);
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
