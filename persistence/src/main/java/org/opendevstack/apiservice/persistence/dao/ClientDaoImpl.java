package org.opendevstack.apiservice.persistence.dao;

import org.opendevstack.apiservice.core.contracts.persistence.ClientDao;
import org.opendevstack.apiservice.persistence.repository.ClientJpaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientDaoImpl implements ClientDao {

    private final ClientJpaRepository repository;

    public ClientDaoImpl(ClientJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ClientInfo> findByAzureClientId(String azureClientId) {
        return repository.findByAzureClientId(azureClientId)
                .map(entity -> new ClientInfo(
                        entity.getId(),
                        entity.getAzureClientId(),
                        entity.getName(),
                        entity.isEnabled()
                ));
    }
}
