package org.opendevstack.apiservice.persistence.dao;

import org.opendevstack.apiservice.core.contracts.persistence.ClientDao;
import org.opendevstack.apiservice.core.contracts.persistence.ClientInfo;
import org.opendevstack.apiservice.persistence.repository.ClientAppRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ClientDaoImpl implements ClientDao {

    private final ClientAppRepository repository;

    public ClientDaoImpl(ClientAppRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ClientInfo> findByClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return Optional.empty();
        }

        final UUID clientUuid;
        try {
            clientUuid = UUID.fromString(clientId);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        return repository.findByClientId(clientUuid)
                .map(entity -> new ClientInfo(
                        entity.getId(),
                        entity.getClientId().toString(),
                        entity.getClientName(),
                        entity.isEnabled()
                ));
    }
}
