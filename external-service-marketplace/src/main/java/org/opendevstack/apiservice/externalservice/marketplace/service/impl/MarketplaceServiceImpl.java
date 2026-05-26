package org.opendevstack.apiservice.externalservice.marketplace.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.core.security.jwt.JwtUtils;
import org.opendevstack.apiservice.core.security.obo.OboTokenService;
import org.opendevstack.apiservice.externalservice.marketplace.client.MarketplaceApiClient;
import org.opendevstack.apiservice.externalservice.marketplace.client.MarketplaceApiClientFactory;
import org.opendevstack.apiservice.externalservice.marketplace.config.MarketplaceInstanceConfig;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.client.ApiClient;
import org.opendevstack.apiservice.externalservice.marketplace.client.api.CatalogHealthApi;
import org.opendevstack.apiservice.externalservice.marketplace.client.api.CatalogItemsApi;
import org.opendevstack.apiservice.externalservice.marketplace.client.api.ProjectComponentsApi;
import org.opendevstack.apiservice.externalservice.marketplace.client.api.ProvisionResultsApi;
import org.opendevstack.apiservice.externalservice.marketplace.client.api.ProvisionerActionsApi;
import org.opendevstack.apiservice.externalservice.marketplace.client.api.ProvisionerHealthApi;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.GetCatalogHealth200Response;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.GetProvisionerHealth200Response;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentExtendedInfo;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisionAction;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisionActionParameter;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisionActionResponse;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisioningDeleteRequest;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisioningStatusUpdateRequest;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisioningStatusUpdateRequestAllOfParameters;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class MarketplaceServiceImpl implements MarketplaceService {
    private static final String HEALTH_STATUS_UP = "UP";

    private final MarketplaceApiClientFactory clientFactory;
    private final OboTokenService oboTokenService;

    public MarketplaceServiceImpl(MarketplaceApiClientFactory clientFactory,
                                  OboTokenService oboTokenService) {
        this.clientFactory = clientFactory;
        this.oboTokenService = oboTokenService;
        log.info("MarketplaceServiceImpl initialized");
    }


    @Override
    public CatalogItem getCatalogItem(String catalogItemId) throws MarketplaceException {
        return getCatalogItem(getDefaultInstance(), catalogItemId);
    }

    @Override
    public CatalogItem getCatalogItem(String instanceName, String catalogItemId) throws MarketplaceException {
        log.debug("Marketplace service GET catalog item with id {} in instance {} ", catalogItemId, instanceName);
        try {
            MarketplaceApiClient marketplaceClient = getOboAuthenticatedClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();
            CatalogItemsApi catalogItemsApi = new CatalogItemsApi(apiClient);
            apiClient.setBasePath(marketplaceClient.getConfig().getProjectComponentsBaseUrl());
            return catalogItemsApi.getCatalogItemById(catalogItemId);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Catalog item with id '{}' not found in Marketplace instance '{}'",
                    catalogItemId, instanceName);
            return null;
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new MarketplaceException(
                    String.format("Access denied when getting catalog item '%s' in instance '%s'",
                            catalogItemId, instanceName), e);
        } catch (RestClientException e) {
            throw new MarketplaceException(
                    String.format("Failed to retrieve catalog item '%s' in instance '%s'",
                            catalogItemId, instanceName), e);
        }
    }

    @Override
    public CatalogItem getCatalogItemBySlug(String slug) throws MarketplaceException {
        return getCatalogItemBySlug(getDefaultInstance(), slug);
    }

    @Override
    public CatalogItem getCatalogItemBySlug(String instanceName, String slug) throws MarketplaceException {
        log.debug("Marketplace service GET catalog item with slug {} in instance {} ", slug, instanceName);
        try {
            MarketplaceApiClient marketplaceClient = getOboAuthenticatedClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();
            CatalogItemsApi catalogItemsApi = new CatalogItemsApi(apiClient);
            apiClient.setBasePath(marketplaceClient.getConfig().getProjectComponentsBaseUrl());
            return catalogItemsApi.getCatalogItemBySlug(slug);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Catalog item with slug '{}' not found in Marketplace instance '{}'",
                    slug, instanceName);
            return null;
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new MarketplaceException(
                    String.format("Access denied when getting catalog item with slug '%s' in instance '%s'",
                            slug, instanceName), e);
        } catch (RestClientException e) {
            throw new MarketplaceException(
                    String.format("Failed to retrieve catalog item with slug '%s' in instance '%s'",
                            slug, instanceName), e);
        }
    }

    @Override
    public ProjectComponentExtendedInfo getProjectComponent(String projectId, String componentId) throws MarketplaceException {
        return getProjectComponent(getDefaultInstance(), projectId, componentId);
    }

    @Override
    public ProjectComponentExtendedInfo getProjectComponent(String instanceName, String projectId, String componentId) throws MarketplaceException {
        log.debug("Marketplace service GET component with id {} for project {} in instance {} ", componentId, projectId, instanceName);
        try {
            MarketplaceApiClient marketplaceClient = getOboAuthenticatedClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();
            ProjectComponentsApi projectComponentsApi = new ProjectComponentsApi(apiClient);
            apiClient.setBasePath(marketplaceClient.getConfig().getProjectComponentsBaseUrl());
            return projectComponentsApi.getProjectComponentById(projectId, componentId);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Component with id '{}' not found in Marketplace instance '{}' for project '{}'",
                    componentId, instanceName, projectId);
            return null;
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new MarketplaceException(
                    String.format("Access denied when getting project component '%s' in project '%s' and instance '%s'",
                            componentId, projectId, instanceName), e);
        } catch (RestClientException e) {
            throw new MarketplaceException(
                    String.format("Failed to retrieve project component '%s' in project '%s' and instance '%s'",
                            componentId, projectId, instanceName), e);
        }
    }

    @Override
    public boolean provisionProjectComponent(String projectId, List<ProvisionActionParameter> params) throws MarketplaceException {
        return provisionProjectComponent(getDefaultInstance(), projectId, params);
    }

    @Override
    public boolean provisionProjectComponent(String instanceName,
                                             String projectId,
                                             List<ProvisionActionParameter> params)
            throws MarketplaceException {
        log.debug("Marketplace service PROVISION component for project {}: ", projectId);
        try {
            MarketplaceApiClient marketplaceClient = getOboAuthenticatedClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();
            MarketplaceInstanceConfig config = marketplaceClient.getConfig();

            String provisionerActionsBaseUrl = config.getProvisionerActionsBaseUrl();

            ProvisionAction provisionAction = new ProvisionAction();
            provisionAction.setId("PROVISION");
            provisionAction.addParametersItem(new ProvisionActionParameter().name("project_key").type("string").value(projectId));

            params.forEach(provisionAction::addParametersItem);

            ProvisionerActionsApi provisionerActionsApi = new ProvisionerActionsApi(apiClient);
            apiClient.setBasePath(provisionerActionsBaseUrl);

            ProvisionActionResponse response = provisionerActionsApi.triggerProvisionAction(provisionAction);
            return !Boolean.TRUE.equals(response.getFailed());
        } catch (HttpClientErrorException.Conflict e) {
            throw new MarketplaceException("This component name already exists, please choose another name.", e);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new MarketplaceException(
                    String.format("Access denied when provisioning project component in project '%s' and instance '%s'",
                            projectId, instanceName), e);
        } catch (RestClientException e) {
            throw new MarketplaceException(
                    String.format("Failed to provision project component in project '%s' and instance '%s'",
                            projectId, instanceName), e);
        }
    }

    @Override
    public void deleteProjectComponent(String projectId, String componentId) throws MarketplaceException {
        deleteProjectComponent(getDefaultInstance(), projectId, componentId);
    }

    @Override
    public void deleteProjectComponent(String instanceName, String projectId, String componentId) throws MarketplaceException {
        log.debug("Marketplace service DELETE component {} for project {}: ", componentId, projectId);
        try {
            MarketplaceApiClient marketplaceClient = clientFactory.getClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();

            ProvisionResultsApi provisionResultsApi = new ProvisionResultsApi(apiClient);
            apiClient.setBasePath(marketplaceClient.getConfig().getProvisionerActionsBaseUrl());
            log.debug("Api client base path: {}", apiClient.getBasePath());

            ProvisioningDeleteRequest provisioningDeleteRequest = new ProvisioningDeleteRequest();
            provisioningDeleteRequest.setComponentId(componentId);
            provisionResultsApi.deleteProjectComponent(projectId, provisioningDeleteRequest);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new MarketplaceException(
                    String.format("Access denied when deleting project component '%s' in project '%s' and instance '%s'",
                            componentId, projectId, instanceName), e);
        } catch (RestClientException e) {
            throw new MarketplaceException(
                    String.format("Failed to delete project component '%s' in project '%s' and instance '%s'",
                            componentId, projectId, instanceName), e);
        }
    }

    @Override
    public void registerProjectComponent(String projectId,
                                         String componentId,
                                         String catalogItemSlug,
                                         List<ProvisioningStatusUpdateRequestAllOfParameters> params)
            throws MarketplaceException {
        registerProjectComponent(getDefaultInstance(), projectId, componentId, catalogItemSlug, params);
    }

    @Override
    public void registerProjectComponent(String instanceName,
                                         String projectId,
                                         String componentId,
                                         String catalogItemSlug,
                                         List<ProvisioningStatusUpdateRequestAllOfParameters> params)
            throws MarketplaceException {
        log.debug("Marketplace service REGISTER component {} for project {}: ", componentId, projectId);
        try {
            MarketplaceApiClient marketplaceClient = clientFactory.getClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();

            ProvisionResultsApi provisionResultsApi = new ProvisionResultsApi(apiClient);
            apiClient.setBasePath(marketplaceClient.getConfig().getProvisionerActionsBaseUrl());
            log.debug("Api client base path: {}", apiClient.getBasePath());

            ProvisioningStatusUpdateRequest registerRequest = new ProvisioningStatusUpdateRequest()
                .componentId(componentId)
                .catalogItemSlug(catalogItemSlug)
                .componentUrl(String.format("%s/projects/%s/repos/%s-%s/browse",
                    marketplaceClient.getConfig().getBitbucketBaseUrl(),
                    projectId.toUpperCase(), projectId.toLowerCase(), componentId))
                .parameters(params);
            provisionResultsApi.notifyProvisioningStatusUpdate(projectId, "CREATED", registerRequest);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new MarketplaceException(
                    String.format("Access denied when registering project component '%s' in project '%s' and instance '%s'",
                            componentId, projectId, instanceName), e);
        } catch (RestClientException e) {
            throw new MarketplaceException(
                    String.format("Failed to register project component '%s' in project '%s' and instance '%s'",
                            componentId, projectId, instanceName), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultInstance() throws MarketplaceException {
        return clientFactory.getDefaultInstanceName();
    }

    /**
     * {@inheritDoc}
     *
     * Returns {@code false} (without throwing) if no instances are configured or
     * if either public Marketplace health endpoint is not UP.
     */
    @Override
    public boolean isHealthy() {
        Set<String> instances = getAvailableInstances();
        if (instances.isEmpty()) {
            log.warn("No Marketplace instances configured - reporting unhealthy");
            return false;
        }

        final MarketplaceApiClient marketplaceClient;
        try {
            marketplaceClient = clientFactory.getClient(getDefaultInstance());
        } catch (MarketplaceException ex) {
            log.warn("Could not resolve Marketplace client for health check", ex);
            return false;
        }

        boolean provisionerHealthy = isProvisionerEndpointUp(marketplaceClient);
        if (!provisionerHealthy) {
            return false;
        }

        return isCatalogEndpointUp(marketplaceClient);
    }

    protected boolean isProvisionerEndpointUp(MarketplaceApiClient marketplaceClient) {
        try {
            String provisionerBaseUrl = marketplaceClient.getConfig().getProvisionerActionsBaseUrl();
            ApiClient healthApiClient = marketplaceClient.getApiClient();
            healthApiClient.setBasePath(provisionerBaseUrl);

            ProvisionerHealthApi healthApi = new ProvisionerHealthApi(healthApiClient);
            GetProvisionerHealth200Response response = healthApi.getProvisionerHealth();
            return isStatusUp(response != null ? response.getStatus() : null,
                    "provisionerActionsBaseUrl", provisionerBaseUrl);
        } catch (RestClientException ex) {
            log.warn("Health check via provisionerActionsBaseUrl failed", ex);
            return false;
        }
    }

    protected boolean isCatalogEndpointUp(MarketplaceApiClient marketplaceClient) {
        try {
            String catalogBaseUrl = marketplaceClient.getConfig().getProjectComponentsBaseUrl();
            ApiClient healthApiClient = marketplaceClient.getApiClient();
            healthApiClient.setBasePath(catalogBaseUrl);

            CatalogHealthApi healthApi = new CatalogHealthApi(healthApiClient);
            GetCatalogHealth200Response response = healthApi.getCatalogHealth();
            return isStatusUp(response != null ? response.getStatus() : null,
                    "projectComponentsBaseUrl", catalogBaseUrl);
        } catch (RestClientException ex) {
            log.warn("Health check via projectComponentsBaseUrl failed", ex);
            return false;
        }
    }

    private boolean isStatusUp(String status, String endpointName, String baseUrl) {
        boolean healthy = HEALTH_STATUS_UP.equals(status);
        if (!healthy) {
            log.warn("Health endpoint from {}='{}' returned status '{}'", endpointName, baseUrl, status);
        }
        return healthy;
    }

    @Override
    public Set<String> getAvailableInstances() {
        return clientFactory.getAvailableInstances();
    }

    @Override
    public boolean hasInstance(String instanceName) {
        return clientFactory.hasInstance(instanceName);
    }

    /**
     * Creates a {@link MarketplaceApiClient} authenticated with an OBO token
     * obtained from the current request's JWT.
     */
    private MarketplaceApiClient getOboAuthenticatedClient(String instanceName) throws MarketplaceException {
        MarketplaceApiClient client = clientFactory.getClient(instanceName);
        String oboScope = client.getConfig().getOboScope();
        if (oboScope == null || oboScope.isBlank()) {
            throw new MarketplaceException(
                    String.format("OBO scope not configured for Marketplace instance '%s'", instanceName));
        }
        String assertion = JwtUtils.getTokenValue();
        final String oboToken;
        try {
            oboToken = oboTokenService.exchangeToken(assertion, oboScope);
        } catch (RuntimeException ex) {
            throw new MarketplaceException(
                    String.format(
                            "Failed to exchange OBO token for Marketplace instance '%s' with scope '%s'",
                            instanceName, oboScope),
                    ex);
        }
        client.setBearerToken(oboToken);
        return client;
    }
}
