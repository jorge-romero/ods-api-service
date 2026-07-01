package org.opendevstack.apiservice.project.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.repository.ClientAppRepository;
import org.opendevstack.apiservice.project.exception.ClientAppNotRegisteredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientAppService {

    private final ClientAppRepository clientAppRepository;
    
    @Transactional(readOnly = true)
    public ClientAppEntity findByClientId(UUID clientId) {
        log.debug("Looking up ClientApp for clientId={}", clientId);
        return clientAppRepository.findDetailedByClientId(clientId)
                .orElseThrow(() -> {
                    log.warn("ClientApp not found for clientId={}", clientId);
                    return new ClientAppNotRegisteredException(clientId.toString());
                });
    }
}