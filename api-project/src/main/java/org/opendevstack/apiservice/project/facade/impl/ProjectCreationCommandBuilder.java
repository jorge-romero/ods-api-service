package org.opendevstack.apiservice.project.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.entity.ClientAppProjectFlavorEntity;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectAlreadyExistsException;
import org.opendevstack.apiservice.project.exception.ProjectCreationException;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.serviceproject.exception.ProjectExistenceServiceException;
import org.opendevstack.apiservice.serviceproject.exception.ProjectKeyGenerationException;
import org.opendevstack.apiservice.serviceproject.service.GenerateProjectKeyService;
import org.opendevstack.apiservice.serviceproject.service.ProjectExistenceService;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectCreationCommandBuilder {
    
    private final GenerateProjectKeyService generateProjectKeyService;
    
    private final ProjectExistenceService projectExistenceService;

    public ProjectCreationCommand buildForCreation(CreateProjectRequest request, ClientAppEntity clientApp) {
        ClientAppProjectFlavorEntity flavor = resolveFlavor(request, clientApp);
        String projectKey = resolveProjectKey(request.getProjectKey(), flavor);

        return build(request, flavor, projectKey, clientApp);
    }

    public ProjectCreationCommand buildForRegistration(CreateProjectRequest request, ClientAppEntity clientApp) {
        ClientAppProjectFlavorEntity flavor = resolveFlavor(request, clientApp);
        String projectKey = resolveProjectKeyForRegistration(request.getProjectKey(), flavor);

        return build(request, flavor, projectKey, clientApp);
    }

    private ProjectCreationCommand build(CreateProjectRequest request, ClientAppProjectFlavorEntity flavor, String projectKey, ClientAppEntity clientApp) {
        String projectFlavor = firstNonBlank(request.getProjectFlavor(), flavor.getName());
        String configurationItem = firstNonBlank(request.getConfigurationItem(), flavor.getConfigItem());
        String owner = firstNonBlank(request.getOwner(), flavor.getProjectOwner());
        String x2account = firstNonBlank(request.getX2OdsAccount(), flavor.getServiceAccount());
        String location = firstNonBlank(request.getLocation(), flavor.getLocation());
        String projectName = resolveProjectName(request.getProjectName(), projectKey);
        String projectDescription = firstNonBlank(request.getProjectDescription(), "project " + projectFlavor);

        return new ProjectCreationCommand(
                projectKey,
                projectName,
                projectDescription,
                projectFlavor,
                configurationItem,
                location,
                x2account,
                owner,
                clientApp.getId());
    }

    private ClientAppProjectFlavorEntity resolveFlavor(CreateProjectRequest request, ClientAppEntity clientApp) {
        List<ClientAppProjectFlavorEntity> flavors = clientApp.getProjectFlavors();
        
        if (flavors == null || flavors.isEmpty()) {
            log.warn("ClientApp '{}' has no project flavors configured", clientApp.getClientId());
            throw new ProjectValidationException(ErrorKey.INVALID_PROJECT_FLAVOR);
        }

        if (Strings.isNotEmpty(request.getProjectFlavor())) {
            return resolveByFlavorName(request.getProjectFlavor(), flavors, clientApp.getClientId());
        }

        if (Strings.isNotEmpty(request.getConfigurationItem())) {
            return resolveByConfigurationItem(request.getConfigurationItem(), flavors, clientApp.getClientId());
        }

        throw new ProjectValidationException(ErrorKey.BAD_REQUEST_FLAVOR_CONFIG_ITEM);
    }

    private ClientAppProjectFlavorEntity resolveByFlavorName(
            String flavorName, List<ClientAppProjectFlavorEntity> flavors, UUID clientId) {
        return flavors.stream()
                .filter(f -> flavorName.equals(f.getName()))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Flavor '{}' is not configured for clientApp '{}'", flavorName, clientId);
                    return new ProjectValidationException(ErrorKey.INVALID_PROJECT_FLAVOR);
                });
    }

    private ClientAppProjectFlavorEntity resolveByConfigurationItem(
            String configurationItem, List<ClientAppProjectFlavorEntity> flavors, UUID clientId) {
        List<ClientAppProjectFlavorEntity> matchingFlavors = flavors.stream()
                .filter(f -> configurationItem.equals(f.getConfigItem()))
                .toList();

        if (matchingFlavors.size() != 1) {
            log.warn("ConfigItem '{}' does not match exactly one flavor for clientApp '{}'",
                    configurationItem, clientId);
            String message = MessageFormat.
                    format("Not exists a project flavor configured for the Config Item {0}. " +
                            "To create a project under {0} the projectFlavor parameter is mandatory.", 
                            configurationItem);
            throw new ProjectValidationException(ErrorKey.INVALID_CONFIG_ITEM, message);
        }

        ClientAppProjectFlavorEntity matchedFlavor = matchingFlavors.getFirst();
        
        if (!isAllowedConfigItem(configurationItem, matchedFlavor)) {
            log.warn("ConfigItem '{}' is not in the allowed list for flavor '{}' of clientApp '{}'",
                    configurationItem, matchedFlavor.getName(), clientId);
            throw new ProjectValidationException(ErrorKey.INVALID_CONFIG_ITEM);
        }

        return matchedFlavor;
    }

    private String resolveProjectKey(String existingProjectKey, ClientAppProjectFlavorEntity flavor) {
        try {
            if (Strings.isNotEmpty(existingProjectKey)) {
                if (!projectExistenceService.isProjectFound(existingProjectKey)) {
                    return existingProjectKey;
                }

                throw new ProjectAlreadyExistsException(ErrorKey.PROJECT_ALREADY_EXISTS);
            }

            String pattern = flavor.getProjectKeyPattern();
            
            return generateProjectKeyService.generateProjectKey(pattern);
        } catch (ProjectKeyGenerationException e) {
            throw new ProjectCreationException("Error generating the project key", e);
        } catch (ProjectExistenceServiceException e) {
            throw new ProjectCreationException("Error checking if the generated key exists: " + e.getMessage(), e);
        }
    }

    private String resolveProjectKeyForRegistration(String existingProjectKey, ClientAppProjectFlavorEntity flavor) {
        try {
            if (Strings.isNotEmpty(existingProjectKey)) {
                if (!projectExistenceService.isProjectFoundInCollection(existingProjectKey)) {
                    return existingProjectKey;
                }

                throw new ProjectAlreadyExistsException(ErrorKey.PROJECT_ALREADY_EXISTS);
            }

            String pattern = flavor.getProjectKeyPattern();

            return generateProjectKeyService.generateProjectKey(pattern);
        } catch (ProjectKeyGenerationException e) {
            throw new ProjectCreationException("Error generating the project key", e);
        } catch (ProjectExistenceServiceException e) {
            throw new ProjectCreationException("Error checking if the generated key exists: " + e.getMessage(), e);
        }
    }

    private boolean isAllowedConfigItem(String configurationItem, ClientAppProjectFlavorEntity flavor) {
        String[] allowedConfigItems = flavor.getAllowedConfigItems();
        
        if (allowedConfigItems == null || allowedConfigItems.length == 0) {
            return true;
        }
        
        return Arrays.asList(allowedConfigItems).contains(configurationItem);
    }

    private String firstNonBlank(String preferred, String fallback) {
        return Strings.isNotEmpty(preferred) ? preferred : fallback;
    }
    
    private String resolveProjectName(String preferred, String fallback) {
        if (!Strings.isEmpty(preferred)) {
            try {
                if (projectExistenceService.isProjectFoundByName(preferred)) {
                    throw new ProjectAlreadyExistsException(ErrorKey.PROJECT_SAME_PROJECT_NAME_ALREADY_EXISTS);
                }
            } catch (ProjectExistenceServiceException e) {
                throw new ProjectCreationException("Error checking if project name already exists: " + e.getMessage(), e);
            }
        }
        
        return firstNonBlank(preferred, fallback);
    }
}

