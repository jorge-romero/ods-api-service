package org.opendevstack.apiservice.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.repository.ClientAppRepository;
import org.opendevstack.apiservice.project.exception.ClientAppNotRegisteredException;

class ClientAppServiceTest {
    
    @Mock
    private ClientAppRepository clientAppRepository;
    private ClientAppService sut;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sut = new ClientAppService(clientAppRepository);
    }
    
    @Test
    void findByClientId_returns_entity_when_client_exists() {

        UUID clientId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        ClientAppEntity entity = ClientAppEntity.builder()
                .clientId(clientId)
                .clientName("Test App")
                .build();
        when(clientAppRepository.findDetailedByClientId(clientId)).thenReturn(Optional.of(entity));

        ClientAppEntity result = sut.findByClientId(clientId);

        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo(clientId);
        assertThat(result.getClientName()).isEqualTo("Test App");
        verify(clientAppRepository).findDetailedByClientId(clientId);
    }
    
    @Test
    void findByClientId_throws_exception_when_client_not_found() {

        UUID clientId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(clientAppRepository.findDetailedByClientId(clientId)).thenReturn(Optional.empty());

        assertThrows(
                ClientAppNotRegisteredException.class,
                () -> sut.findByClientId(clientId));
        verify(clientAppRepository).findDetailedByClientId(clientId);
    }
}