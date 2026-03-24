package org.opendevstack.apiservice.core.contracts.persistence;

import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;

import java.util.List;

/**
 * Data access contract for authorization policies.
 * Implementations are provided by the persistence module.
 */
public interface PolicyDao {

    List<PolicyRule> findByApiDefinitionId(String apiDefinitionId);

    List<PolicyRule> findByApiDefinitionIdAndClientId(String apiDefinitionId, String clientId);

    List<PolicyRule> findGlobalByApiDefinitionId(String apiDefinitionId);
}
