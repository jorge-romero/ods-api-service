package org.opendevstack.apiservice.core.contracts.persistence;

import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;

import java.util.List;
import java.util.Optional;

/**
 * Data access contract for API definitions.
 * Implementations are provided by the persistence module.
 */
public interface ApiDefinitionDao {

    Optional<ApiDefinition> findByApiId(String apiId);

    Optional<ApiDefinition> findByBasePathAndVersion(String basePath, String version);

    List<ApiDefinition> findAllEnabled();

    List<ApiDefinition> findAll();
}
