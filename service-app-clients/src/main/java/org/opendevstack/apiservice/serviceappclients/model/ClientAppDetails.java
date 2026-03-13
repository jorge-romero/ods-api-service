package org.opendevstack.apiservice.serviceappclients.model;

import java.util.List;

/**
 * Service-layer view of a registered API client application.
 */
public record ClientAppDetails(String clientId, String clientName, List<String> permissions,
        String roleScope, boolean enabled, List<ClientAppFlavorDetails> projectFlavors) {

}