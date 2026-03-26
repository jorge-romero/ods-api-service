package org.opendevstack.apiservice.persistence.dao;

import org.opendevstack.apiservice.core.contracts.persistence.ClientDao;
import org.opendevstack.apiservice.core.contracts.persistence.ClientInfo;
import org.opendevstack.apiservice.persistence.repository.ClientAppRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientDaoImpl implements ClientDao {

    private final ClientAppRepository repository;

    public ClientDaoImpl(ClientAppRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ClientInfo> findByClientId(String clientId) {
        return repository.findByClientId(clientId)
                .map(entity -> new ClientInfo(
                        entity.getId(),
                        entity.getClientId(),
                        entity.getClientName(),
                        entity.isEnabled()
                ));
    }
}
