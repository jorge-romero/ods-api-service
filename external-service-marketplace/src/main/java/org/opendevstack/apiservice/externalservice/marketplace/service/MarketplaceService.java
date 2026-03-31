package org.opendevstack.apiservice.externalservice.marketplace.service;

import org.opendevstack.apiservice.externalservice.api.ExternalService;
import org.opendevstack.apiservice.externalservice.marketplace.model.CreateComponentParameter;
import org.opendevstack.apiservice.externalservice.marketplace.model.ProjectComponent;

import java.util.List;

public interface MarketplaceService extends ExternalService {


    ProjectComponent getProjectComponent(String projectId, String componentId);

    ProjectComponent createProjectComponent(String projectId, List<CreateComponentParameter> params);
}
