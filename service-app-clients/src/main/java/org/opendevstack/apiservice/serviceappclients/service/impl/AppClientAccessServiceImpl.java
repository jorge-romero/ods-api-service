package org.opendevstack.apiservice.serviceappclients.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.entity.ClientAppProjectFlavorEntity;
import org.opendevstack.apiservice.persistence.repository.ClientAppRepository;
import org.opendevstack.apiservice.serviceappclients.model.ClientAppDetails;
import org.opendevstack.apiservice.serviceappclients.model.ClientAppFlavorDetails;
import org.opendevstack.apiservice.serviceappclients.service.AppClientAccessService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppClientAccessServiceImpl implements AppClientAccessService {

    private final ClientAppRepository clientAppRepository;

    @Override
    public Optional<ClientAppDetails> findClientApp(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return Optional.empty();
        }

        return clientAppRepository.findDetailedByClientId(clientId).map(this::mapToDetails);
    }

    @Override
    public Optional<ClientAppDetails> findEnabledClientApp(String clientId) {
        return findClientApp(clientId).filter(ClientAppDetails::enabled);
    }

    @Override
    public boolean hasAccess(String clientId) {
        return findEnabledClientApp(clientId).isPresent();
    }

    @Override
    public boolean hasAccessToFlavor(String clientId, String flavorName) {
        if (flavorName == null || flavorName.isBlank()) {
            return false;
        }

        return findEnabledClientApp(clientId)
            .map(clientApp -> clientApp.projectFlavors().stream()
                .anyMatch(projectFlavor -> projectFlavor.name().equalsIgnoreCase(flavorName)))
            .orElse(false);
    }

    private ClientAppDetails mapToDetails(ClientAppEntity clientApp) {
        return new ClientAppDetails(clientApp.getClientId(), clientApp.getClientName(),
                clientApp.isEnabled(),
                clientApp.getProjectFlavors().stream()
                    .map(this::mapFlavorToDetails)
                    .sorted((left, right) -> left.name().compareToIgnoreCase(right.name()))
                    .toList());
    }

    private ClientAppFlavorDetails mapFlavorToDetails(ClientAppProjectFlavorEntity projectFlavor) {
        return new ClientAppFlavorDetails(projectFlavor.getName(), projectFlavor.getProjectKeyPattern(),
                projectFlavor.getTemplateId(), projectFlavor.getProjectOwner(),
                projectFlavor.getServiceAccount(), projectFlavor.getConfigItem(),
                toList(projectFlavor.getAllowedConfigItems()), projectFlavor.getLocation());
    }

    private List<String> toList(String[] values) {
        if (values == null || values.length == 0) {
            return List.of();
        }
        return Arrays.stream(values).toList();
    }

}