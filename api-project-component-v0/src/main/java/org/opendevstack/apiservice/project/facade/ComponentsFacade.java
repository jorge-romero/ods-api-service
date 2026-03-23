package org.opendevstack.apiservice.project.facade;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.api.ExternalService;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ComponentsFacade {

    private final MarketplaceExternalServicePlaceholder marketplaceExternalService;

    public Component getProjectComponent(String projectId, String componentId) {
        return marketplaceExternalService.getProjectComponent(projectId, componentId);
    }

    public Component createProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
        return marketplaceExternalService.createProjectComponent(projectId, createComponentRequest);
    }

    @Service
    class MarketplaceExternalServicePlaceholder implements ExternalService {

        @Override
        public boolean isHealthy() {
            return false;
        }

        public Component getProjectComponent(String projectId, String componentId) {
            log.info("Get component with id '" + componentId + "' for project '" + projectId + "'");
            return null;
        }

        public Component createProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
            log.info("Creating component for project '" + projectId + "'" + " with request: " + createComponentRequest);
            return null;
        }
    }
}
