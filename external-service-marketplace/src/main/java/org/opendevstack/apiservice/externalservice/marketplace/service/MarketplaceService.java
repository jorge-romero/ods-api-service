package org.opendevstack.apiservice.externalservice.marketplace.service;

import org.opendevstack.apiservice.externalservice.api.ExternalService;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentProvisionStatus;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisionActionParameter;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisioningStatusUpdateRequestAllOfParameters;

import java.util.List;
import java.util.Set;

public interface MarketplaceService extends ExternalService {

    Set<String> getAvailableInstances();

    boolean hasInstance(String instanceName);

    String getDefaultInstance() throws MarketplaceException;

    ProjectComponentProvisionStatus getProjectComponent(String projectId, String componentId) throws MarketplaceException;

    ProjectComponentProvisionStatus getProjectComponent(String instanceName, String projectId, String componentId) throws MarketplaceException;

    CatalogItem getCatalogItem(String catalogItemId) throws MarketplaceException;

    CatalogItem getCatalogItem(String instanceName, String catalogItemId) throws MarketplaceException;

    CatalogItem getCatalogItemBySlug(String slug) throws MarketplaceException;

    CatalogItem getCatalogItemBySlug(String instanceName, String slug) throws MarketplaceException;

    boolean provisionProjectComponent(String projectId, List<ProvisionActionParameter> params) throws MarketplaceException;

    boolean provisionProjectComponent(String instanceName, String projectId, List<ProvisionActionParameter> params) throws MarketplaceException;

    void deleteProjectComponent(String projectId, String componentId) throws MarketplaceException;

    void deleteProjectComponent(String instanceName, String projectId, String componentId) throws MarketplaceException;

    void registerProjectComponent(String projectId, String componentId, String catalogItemSlug, List<ProvisioningStatusUpdateRequestAllOfParameters> params) throws MarketplaceException;

    void registerProjectComponent(String instanceName, String projectId, String componentId, String catalogItemSlug, List<ProvisioningStatusUpdateRequestAllOfParameters> params) throws MarketplaceException;

}
