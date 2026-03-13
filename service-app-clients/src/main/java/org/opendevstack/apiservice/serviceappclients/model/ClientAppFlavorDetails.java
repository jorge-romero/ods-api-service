package org.opendevstack.apiservice.serviceappclients.model;

import java.util.List;

/**
 * Service-layer view of a project flavor available to a client application.
 */
public record ClientAppFlavorDetails(String name, String projectKeyPattern, Integer templateId,
        String projectOwner, String serviceAccount, String configItem,
        List<String> allowedConfigItems, String location) {

}