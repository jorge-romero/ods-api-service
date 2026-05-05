package org.opendevstack.apiservice.persistence.repository;

import org.opendevstack.apiservice.persistence.entity.AuthorizationPolicyEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuthorizationPolicyJpaRepository extends JpaRepository<AuthorizationPolicyEntity, UUID> {

    List<AuthorizationPolicyEntity> findByApiDefinitionId(String apiDefinitionId);

    List<AuthorizationPolicyEntity> findByApiDefinitionIdAndClientId(String apiDefinitionId, String clientId);

    @Query("""
                        SELECT p
                        FROM AuthorizationPolicyEntity p
                        WHERE p.apiDefinitionId = :apiDefinitionId
                            AND (p.clientId IS NULL OR TRIM(p.clientId) = '')
                        """)
    List<AuthorizationPolicyEntity> findGlobalByApiDefinitionId(@Param("apiDefinitionId") String apiDefinitionId);
}
