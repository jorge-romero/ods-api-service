package org.opendevstack.apiservice.serviceappclients.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.entity.ClientAppProjectFlavorEntity;
import org.opendevstack.apiservice.persistence.repository.ClientAppRepository;
import org.opendevstack.apiservice.serviceappclients.model.ClientAppDetails;

@ExtendWith(MockitoExtension.class)
class AppClientAccessServiceImplTest {

    private static final String CLIENT_ID = "11111111-1111-1111-1111-111111111111";

    @Mock
    private ClientAppRepository clientAppRepository;

    private AppClientAccessServiceImpl tested;

    @BeforeEach
    void setUp() {
        tested = new AppClientAccessServiceImpl(clientAppRepository);
    }

    @Test
    void findClientAppWhenFoundMapsClientWithFlavors() {
        when(clientAppRepository.findDetailedByClientId(CLIENT_ID))
            .thenReturn(Optional.of(buildClientApp(true)));

        Optional<ClientAppDetails> result = tested.findClientApp(CLIENT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().clientName()).isEqualTo("Portal App");
        assertThat(result.get().projectFlavors()).hasSize(2);
        assertThat(result.get().projectFlavors())
            .extracting(flavor -> flavor.name())
            .containsExactly("AMP", "DLSS");
    }

    @Test
    void findEnabledClientAppWhenClientDisabledReturnsEmpty() {
        when(clientAppRepository.findDetailedByClientId(CLIENT_ID))
            .thenReturn(Optional.of(buildClientApp(false)));

        Optional<ClientAppDetails> result = tested.findEnabledClientApp(CLIENT_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void hasAccessWhenClientEnabledReturnsTrue() {
        when(clientAppRepository.findDetailedByClientId(CLIENT_ID))
            .thenReturn(Optional.of(buildClientApp(true)));

        boolean result = tested.hasAccess(CLIENT_ID);

        assertThat(result).isTrue();
    }

    @Test
    void hasAccessWhenClientMissingReturnsFalse() {
        when(clientAppRepository.findDetailedByClientId(CLIENT_ID))
            .thenReturn(Optional.empty());

        boolean result = tested.hasAccess(CLIENT_ID);

        assertThat(result).isFalse();
    }

    @Test
    void hasAccessToFlavorWhenEnabledClientHasFlavorReturnsTrue() {
        when(clientAppRepository.findDetailedByClientId(CLIENT_ID))
            .thenReturn(Optional.of(buildClientApp(true)));

        boolean result = tested.hasAccessToFlavor(CLIENT_ID, "amp");

        assertThat(result).isTrue();
    }

    @Test
    void hasAccessToFlavorWhenEnabledClientDoesNotHaveFlavorReturnsFalse() {
        when(clientAppRepository.findDetailedByClientId(CLIENT_ID))
            .thenReturn(Optional.of(buildClientApp(true)));

        boolean result = tested.hasAccessToFlavor(CLIENT_ID, "MISSING");

        assertThat(result).isFalse();
    }

    @Test
    void hasAccessToFlavorWhenClientDisabledReturnsFalse() {
        when(clientAppRepository.findDetailedByClientId(CLIENT_ID))
            .thenReturn(Optional.of(buildClientApp(false)));

        boolean result = tested.hasAccessToFlavor(CLIENT_ID, "AMP");

        assertThat(result).isFalse();
    }

    @Test
    void hasAccessToFlavorWhenFlavorBlankReturnsFalseWithoutCallingRepository() {
        boolean result = tested.hasAccessToFlavor(CLIENT_ID, " ");

        assertThat(result).isFalse();
        verifyNoInteractions(clientAppRepository);
    }

    @Test
    void findClientAppWhenClientIdBlankReturnsEmptyWithoutCallingRepository() {
        Optional<ClientAppDetails> result = tested.findClientApp(" ");

        assertThat(result).isEmpty();
        verifyNoInteractions(clientAppRepository);
    }

    private ClientAppEntity buildClientApp(boolean enabled) {
        ClientAppEntity clientApp = ClientAppEntity.builder()
            .clientId(CLIENT_ID)
            .clientName("Portal App")
            .enabled(enabled)
            .projectFlavors(new LinkedHashSet<>())
            .build();

        clientApp.addProjectFlavor(ClientAppProjectFlavorEntity.builder()
            .name("DLSS")
            .projectKeyPattern("DLSS%06d")
            .templateId(101)
            .projectOwner("owner.one")
            .serviceAccount("svc-one")
            .configItem("CI-001")
            .allowedConfigItems(new String[] { "CI-001" })
            .location("eu")
            .build());

        clientApp.addProjectFlavor(ClientAppProjectFlavorEntity.builder()
            .name("AMP")
            .projectKeyPattern("AMP%06d")
            .templateId(102)
            .projectOwner("owner.two")
            .serviceAccount("svc-two")
            .configItem("CI-002")
            .allowedConfigItems(new String[] { "CI-002", "CI-003" })
            .location("us")
            .build());

        return clientApp;
    }

}