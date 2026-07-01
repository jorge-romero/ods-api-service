package org.opendevstack.apiservice.core.security.authorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.contracts.persistence.PolicyDao;
import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PolicyServiceTest {

    private PolicyDao policyDao;
    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        policyDao = mock(PolicyDao.class);
        policyService = new PolicyService(policyDao);
    }

    @Test
    void findPolicies_withClientId_combinesGlobalAndClientSpecificRules() {
        PolicyRule globalRule = new PolicyRule(UUID.randomUUID(), "api-1", null, "TYPE_A", Map.of());
        PolicyRule clientRule = new PolicyRule(UUID.randomUUID(), "api-1", "client-a", "TYPE_B", Map.of());

        when(policyDao.findGlobalByApiDefinitionId("api-1")).thenReturn(List.of(globalRule));
        when(policyDao.findByApiDefinitionIdAndClientId("api-1", "client-a")).thenReturn(List.of(clientRule));

        List<PolicyRule> result = policyService.findPolicies("api-1", "client-a");

        assertEquals(2, result.size());
        assertTrue(result.contains(globalRule));
        assertTrue(result.contains(clientRule));
    }

    @Test
    void findPolicies_withNullClientId_usesApiDefinitionOnlyLookup() {
        PolicyRule rule = new PolicyRule(UUID.randomUUID(), "api-1", null, "TYPE_A", Map.of());
        when(policyDao.findByApiDefinitionId("api-1")).thenReturn(List.of(rule));

        List<PolicyRule> result = policyService.findPolicies("api-1", null);

        assertEquals(1, result.size());
        verify(policyDao, never()).findGlobalByApiDefinitionId(any());
        verify(policyDao, never()).findByApiDefinitionIdAndClientId(any(), any());
    }

    @Test
    void findPolicies_withClientId_noRulesFound_returnsEmpty() {
        when(policyDao.findGlobalByApiDefinitionId("api-1")).thenReturn(List.of());
        when(policyDao.findByApiDefinitionIdAndClientId("api-1", "client-x")).thenReturn(List.of());

        List<PolicyRule> result = policyService.findPolicies("api-1", "client-x");

        assertTrue(result.isEmpty());
    }
}
