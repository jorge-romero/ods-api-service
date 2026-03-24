package org.opendevstack.apiservice.persistence.repository;

import org.opendevstack.apiservice.persistence.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientJpaRepository extends JpaRepository<ClientEntity, UUID> {

    Optional<ClientEntity> findByAzureClientId(String azureClientId);
}
