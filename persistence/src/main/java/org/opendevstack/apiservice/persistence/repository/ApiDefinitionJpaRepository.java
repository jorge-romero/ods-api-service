package org.opendevstack.apiservice.persistence.repository;

import org.opendevstack.apiservice.persistence.entity.ApiDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiDefinitionJpaRepository extends JpaRepository<ApiDefinitionEntity, UUID> {

    Optional<ApiDefinitionEntity> findByApiId(String apiId);

    Optional<ApiDefinitionEntity> findByBasePathAndVersion(String basePath, String version);

    List<ApiDefinitionEntity> findByEnabledTrue();
}
