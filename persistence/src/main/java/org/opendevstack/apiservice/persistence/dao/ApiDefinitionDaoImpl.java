package org.opendevstack.apiservice.persistence.dao;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.opendevstack.apiservice.core.contracts.persistence.ApiDefinitionDao;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.opendevstack.apiservice.persistence.entity.ApiDefinitionEntity;
import org.opendevstack.apiservice.persistence.repository.ApiDefinitionJpaRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApiDefinitionDaoImpl implements ApiDefinitionDao {

    private final ApiDefinitionJpaRepository repository;

    public ApiDefinitionDaoImpl(ApiDefinitionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ApiDefinition> findByApiId(String apiId) {
        return repository.findByApiId(apiId)
                .map(this::toDto);
    }

    @Override
    public Optional<ApiDefinition> findByBasePathAndVersion(String basePath, String version) {
        return repository.findByBasePathAndVersion(basePath, version)
                .map(this::toDto);
    }

    @Override
    public List<ApiDefinition> findAllEnabled() {
        return repository.findByEnabledTrue().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<ApiDefinition> findAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    private ApiDefinition toDto(ApiDefinitionEntity entity) {
        Set<AuthType> authTypes = Arrays.stream(entity.getAuthTypes())
                .map(s -> {
                    try {
                        return AuthType.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown AuthType '{}' in api_definitions row {}", s, entity.getApiId());
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        return new ApiDefinition(
                entity.getApiId(),
                entity.getName(),
                entity.getBasePath(),
                entity.getVersion(),
                authTypes,
                entity.isPublic(),
                entity.getProxyUrl(),
                entity.isEnabled()
        );
    }
}
