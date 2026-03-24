package org.opendevstack.apiservice.core.contracts.persistence;

import java.util.Optional;
import java.util.UUID;

/**
 * Data access contract for registered clients.
 * Implementations are provided by the persistence module.
 */
public interface ClientDao {

    Optional<ClientInfo> findByAzureClientId(String azureClientId);

    /**
     * Read-only projection of a client record.
     */
    record ClientInfo(
            UUID id,
            String azureClientId,
            String name,
            boolean enabled
    ) {}
}
