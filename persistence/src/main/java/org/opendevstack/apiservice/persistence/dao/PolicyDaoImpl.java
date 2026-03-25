package org.opendevstack.apiservice.persistence.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.core.contracts.persistence.PolicyDao;
import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;
import org.opendevstack.apiservice.persistence.entity.AuthorizationPolicyEntity;
import org.opendevstack.apiservice.persistence.repository.AuthorizationPolicyJpaRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PolicyDaoImpl implements PolicyDao {

    private final AuthorizationPolicyJpaRepository repository;
    private final ObjectMapper objectMapper;

    PolicyDaoImpl(AuthorizationPolicyJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<PolicyRule> findByApiDefinitionId(String apiDefinitionId) {
        return repository.findByApiDefinitionId(apiDefinitionId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<PolicyRule> findByApiDefinitionIdAndClientId(String apiDefinitionId, String clientId) {
        return repository.findByApiDefinitionIdAndClientId(apiDefinitionId, clientId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<PolicyRule> findGlobalByApiDefinitionId(String apiDefinitionId) {
        return repository.findByApiDefinitionIdAndClientIdIsNull(apiDefinitionId).stream()
                .map(this::toDto)
                .toList();
    }

    private PolicyRule toDto(AuthorizationPolicyEntity entity) {
        Map<String, Object> config = parseConfig(entity.getPolicyConfig());
        return new PolicyRule(
                entity.getId(),
                entity.getApiDefinitionId(),
                entity.getClientId(),
                entity.getPolicyType(),
                config
        );
    }

    private Map<String, Object> parseConfig(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse policy config JSON: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
