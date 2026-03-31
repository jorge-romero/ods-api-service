package org.opendevstack.apiservice.project.facade;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.marketplace.model.CreateComponentParameter;
import org.opendevstack.apiservice.externalservice.marketplace.model.ProjectComponent;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.opendevstack.apiservice.project.mapper.MarketplaceMapper;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ComponentsFacade {

    private final MarketplaceService marketplaceExternalService;

    private final MarketplaceMapper marketplaceMapper;

    public Component getProjectComponent(String projectId, String componentId) {
        ProjectComponent marketplaceComponent = marketplaceExternalService.getProjectComponent(projectId, componentId);
        if (marketplaceComponent == null) {
            log.info("Marketplace component with id {} not found", componentId);
            return null;
        }
        return marketplaceMapper.mapMarketplaceComponentToV0Component(marketplaceComponent);
    }

    public Component createProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
        List<CreateComponentParameter> createComponentParameterList = marketplaceMapper.mapCreateComponentRequestToCreateComponentParameterList(createComponentRequest);
        ProjectComponent marketplaceComponent = marketplaceExternalService.createProjectComponent(projectId, createComponentParameterList);
        if (marketplaceComponent == null) {
            log.error("Failed to create component in marketplace for project with id {}", projectId);
            return null;
        }
        return marketplaceMapper.mapMarketplaceComponentToV0Component(marketplaceComponent);
    }
}
