package org.opendevstack.apiservice.serviceappclients.service;

import java.util.Optional;
import org.opendevstack.apiservice.serviceappclients.model.ClientAppDetails;

/**
 * Resolves registered API clients and their access configuration.
 */
public interface AppClientAccessService {

    /**
     * Loads a client application regardless of its enabled flag.
     * @param clientId Azure AD application/client UUID
     * @return client configuration if found
     */
    Optional<ClientAppDetails> findClientApp(String clientId);

    /**
     * Loads an enabled client application.
     * @param clientId Azure AD application/client UUID
     * @return enabled client configuration if found
     */
    Optional<ClientAppDetails> findEnabledClientApp(String clientId);

    /**
     * Checks whether the given client is registered and enabled.
     * @param clientId Azure AD application/client UUID
     * @return {@code true} when the client is allowed to access secured APIs
     */
    boolean hasAccess(String clientId);

    /**
     * Checks whether the given enabled client is configured for a specific flavor.
     * @param clientId Azure AD application/client UUID
     * @param flavorName flavor name to check
     * @return {@code true} when the client is enabled and has the requested flavor
     */
    boolean hasAccessToFlavor(String clientId, String flavorName);

}