package org.opendevstack.apiservice.core.engine.authorization;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PolicyCacheService {

    private final PolicyService policyService;

    private final Cache<String, List<PolicyRule>> cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    public PolicyCacheService(PolicyService policyService) {
        this.policyService = policyService;
    }

    public List<PolicyRule> getPolicies(String apiDefinitionId, String clientId) {
        String key = apiDefinitionId + "::" + (clientId != null ? clientId : "*");
        return cache.get(key, k -> policyService.findPolicies(apiDefinitionId, clientId));
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public void invalidate(String apiDefinitionId) {
        cache.asMap().keySet().removeIf(k -> k.startsWith(apiDefinitionId + "::"));
    }
}
