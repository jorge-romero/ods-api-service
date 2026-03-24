package org.opendevstack.apiservice.persistence.repository;

import org.opendevstack.apiservice.persistence.entity.AuthorizationPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuthorizationPolicyJpaRepository extends JpaRepository<AuthorizationPolicyEntity, UUID> {

    List<AuthorizationPolicyEntity> findByApiDefinitionId(String apiDefinitionId);

    List<AuthorizationPolicyEntity> findByApiDefinitionIdAndClientId(String apiDefinitionId, String clientId);

    List<AuthorizationPolicyEntity> findByApiDefinitionIdAndClientIdIsNull(String apiDefinitionId);
}
