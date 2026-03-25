package org.opendevstack.apiservice.core.contracts.persistence;

import java.util.Optional;

/**
 * Data access contract for registered clients.
 * Implementations are provided by the persistence module.
 */
public interface ClientDao {

    Optional<ClientInfo> findByClientId(String clientId);
}
