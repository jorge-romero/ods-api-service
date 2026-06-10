package org.opendevstack.apiservice.project.facade;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentProvisionStatus;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisionActionParameter;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisioningStatusUpdateRequestAllOfParameters;
import org.opendevstack.apiservice.externalservice.marketplace.service.CatalogItemOperations;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.opendevstack.apiservice.project.config.ProjectComponentsCreateProperties;
import org.opendevstack.apiservice.project.exception.CatalogItemNotFoundException;
import org.opendevstack.apiservice.project.exception.ComponentAlreadyExistsException;
import org.opendevstack.apiservice.project.exception.ComponentBadRequestException;
import org.opendevstack.apiservice.project.exception.ComponentCreationException;
import org.opendevstack.apiservice.project.exception.ComponentDeletionException;
import org.opendevstack.apiservice.project.exception.ComponentNotFoundException;
import org.opendevstack.apiservice.project.exception.ComponentRegistrationException;
import org.opendevstack.apiservice.project.exception.ComponentReservedParamException;
import org.opendevstack.apiservice.project.exception.ComponentRetrievalException;
import org.opendevstack.apiservice.project.mapper.MarketplaceMapper;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ComponentsFacade {

    private final MarketplaceService marketplaceExternalService;

    private final MarketplaceMapper marketplaceMapper;

    private final ProjectComponentsCreateProperties createProperties;

    public Component getProjectComponent(String projectId, String componentId) {
        try {
            ProjectComponentProvisionStatus marketplaceComponent = marketplaceExternalService.getProjectComponent(projectId, componentId);
            if (marketplaceComponent == null) {
                log.info("Marketplace component with id {} not found", componentId);
                throw new ComponentNotFoundException(
                        String.format("Component '%s' not found for project '%s'", componentId, projectId)
                );
            }
            String catalogItemId = CatalogItemOperations.buildCatalogItemId(marketplaceComponent);
            CatalogItem catalogItem = marketplaceExternalService.getCatalogItem(catalogItemId);
            if (catalogItem == null) {
                log.info("Catalog item with id {} not found", catalogItemId);
                throw new CatalogItemNotFoundException(
                        String.format("Catalog item with id '%s' not found", catalogItemId)
                );
            }
            Component component = marketplaceMapper.mapMarketplaceComponentToV0Component(marketplaceComponent, catalogItem);
            log.info("Marketplace v0 component retrieved: {}", component);
            return component;
        } catch (MarketplaceException e) {
            log.error("Failed to retrieve component with id {} for project with id {}: {}", componentId, projectId, e.getMessage(), e);
            throw new ComponentRetrievalException(
                    String.format("Failed to retrieve component '%s' for project '%s': %s", componentId, projectId, e.getMessage()), e
            );
        }
    }

    public void provisionProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
        validateReservedParams(createComponentRequest);
        try {
            CatalogItem catalogItem = resolveCatalogItem(createComponentRequest);
            List<ProvisionActionParameter> createComponentParameterList = marketplaceMapper
                    .mapCreateComponentRequestToCreateComponentParameterList(createComponentRequest, catalogItem);
            boolean success = marketplaceExternalService.provisionProjectComponent(projectId, createComponentParameterList);
            if (!success) {
                log.error("Failed to create component in marketplace for project with id {}", projectId);
                throw new ComponentCreationException(
                        String.format("Failed to create component for project '%s'", projectId)
                );
            }
        } catch (MarketplaceException e) {
            if (isConflictCause(e)) {
                throw new ComponentAlreadyExistsException(e.getMessage(), e);
            }
            if (isBadRequestCause(e)) {
                String downstreamMessage = extractHttpErrorMessage(e);
                throw new ComponentBadRequestException(downstreamMessage, e);
            }
            throw new ComponentCreationException(
                    String.format("Failed to create component for project '%s': %s", projectId, e.getMessage()), e
            );
        }
    }

    private void validateReservedParams(CreateComponentRequest createComponentRequest) {
        if (createComponentRequest == null || createComponentRequest.getParams() == null
                || createComponentRequest.getParams().isEmpty()) {
            return;
        }

        if (createProperties.getReservedParams() == null || createProperties.getReservedParams().isEmpty()) {
            return;
        }

        Set<String> reservedParams = createProperties.getReservedParams().stream()
                .map(param -> param.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        createComponentRequest.getParams().keySet().stream()
                .filter(param -> param != null && reservedParams.contains(param.toLowerCase(Locale.ROOT)))
                .findFirst()
                .ifPresent(param -> {
                    throw new ComponentReservedParamException(
                            String.format("Parameter '%s' cannot be provided because it is managed by the system.", param)
                    );
                });
    }

    /**
     * Resolves the catalog item that matches the requested {@code productId} (interpreted as the
     * Marketplace catalog item slug). Returns {@code null} when the request or product id is missing,
     * when the catalog item cannot be found, or when the lookup fails – callers must tolerate a
     * missing catalog item and fall back to default parameter handling.
     */
    private CatalogItem resolveCatalogItem(CreateComponentRequest createComponentRequest) {
        if (createComponentRequest == null || createComponentRequest.getProductId() == null
                || createComponentRequest.getProductId().isBlank()) {
            return null;
        }
        String slug = createComponentRequest.getProductId();
        try {
            CatalogItem catalogItem = marketplaceExternalService.getCatalogItemBySlug(slug);
            if (catalogItem == null) {
                log.warn("No catalog item found for slug '{}'; provisioning will fall back to default parameter types", slug);
            }
            return catalogItem;
        } catch (MarketplaceException e) {
            log.warn("Failed to retrieve catalog item for slug '{}': {}. Provisioning will fall back to default parameter types",
                    slug, e.getMessage());
            return null;
        }
    }

    private boolean isConflictCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof HttpClientErrorException.Conflict) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isBadRequestCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof HttpClientErrorException.UnprocessableEntity
                    || current instanceof HttpClientErrorException.BadRequest) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String extractHttpErrorMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof HttpClientErrorException httpError) {
                return httpError.getResponseBodyAsString();
            }
            current = current.getCause();
        }
        return throwable.getMessage();
    }

    public void deleteProjectComponent(String projectId, String componentId) {
        try {
            marketplaceExternalService.deleteProjectComponent(projectId, componentId);
            log.info("Successfully deleted component '{}' for project '{}'", componentId, projectId);
        } catch (MarketplaceException e) {
            log.error("Failed to delete component '{}' for project '{}': {}", componentId, projectId, e.getMessage(), e);
            // Check if it's an access denied error
            if (isAccessDeniedCause(e)) {
                throw new ComponentDeletionException(
                        String.format("Access denied when deleting component '%s' from project '%s'", componentId, projectId), e);
            }
            // Generic deletion failure
            throw new ComponentDeletionException(
                    String.format("Failed to delete component '%s' for project '%s': %s", componentId, projectId, e.getMessage()), e
            );
        }
    }

    private boolean isAccessDeniedCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof HttpClientErrorException.Unauthorized
                    || current instanceof HttpClientErrorException.Forbidden) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    public void registerProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
        String componentId = createComponentRequest.getName();
        String productId = createComponentRequest.getProductId();
        try {
            List<ProvisioningStatusUpdateRequestAllOfParameters> parameters = marketplaceMapper
                    .mapCreateComponentRequestToRegisterComponentParameterList(createComponentRequest);
            marketplaceExternalService.registerProjectComponent(projectId, componentId, productId, parameters);
            log.info("Successfully registered component '{}' for project '{}'", componentId, projectId);
        } catch (MarketplaceException e) {
            log.error("Failed to register component '{}' for project '{}': {}", componentId, projectId, e.getMessage(), e);
            throw new ComponentRegistrationException(
                    String.format("Failed to register component '%s' for project '%s': %s", componentId, projectId, e.getMessage()), e
            );
        }
    }
}
