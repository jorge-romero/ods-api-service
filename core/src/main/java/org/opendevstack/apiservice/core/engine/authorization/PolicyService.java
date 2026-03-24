package org.opendevstack.apiservice.core.engine.authorization;

import org.opendevstack.apiservice.core.config.CacheConfig;
import org.opendevstack.apiservice.core.contracts.persistence.PolicyDao;
import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PolicyService {

    private final PolicyDao policyDao;

    public PolicyService(PolicyDao policyDao) {
        this.policyDao = policyDao;
    }

    @Cacheable(cacheNames = CacheConfig.POLICIES_CACHE,
               key = "#apiDefinitionId + '::' + (#clientId != null ? #clientId : '*')")
    public List<PolicyRule> findPolicies(String apiDefinitionId, String clientId) {
        if (clientId != null) {
            List<PolicyRule> rules = new ArrayList<>();
            rules.addAll(policyDao.findGlobalByApiDefinitionId(apiDefinitionId));
            rules.addAll(policyDao.findByApiDefinitionIdAndClientId(apiDefinitionId, clientId));
            return rules;
        }
        return policyDao.findByApiDefinitionId(apiDefinitionId);
    }
}
