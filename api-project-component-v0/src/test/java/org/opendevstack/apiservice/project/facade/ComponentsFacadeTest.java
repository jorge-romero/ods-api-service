package org.opendevstack.apiservice.project.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentExtendedInfo;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.opendevstack.apiservice.project.exception.ComponentAlreadyExistsException;
import org.opendevstack.apiservice.project.exception.ComponentCreationException;
import org.opendevstack.apiservice.project.exception.ComponentDeletionException;
import org.opendevstack.apiservice.project.exception.ComponentNotFoundException;
import org.opendevstack.apiservice.project.exception.ComponentRegistrationException;
import org.opendevstack.apiservice.project.mapper.MarketplaceMapper;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.ComponentsStatusDTO;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opendevstack.apiservice.project.util.TestObjectsBuilder.buildTestCatalogItem;
import static org.opendevstack.apiservice.project.util.TestObjectsBuilder.buildTestCreateComponentRequest;
import static org.opendevstack.apiservice.project.util.TestObjectsBuilder.buildTestMarketplaceComponent;

@ExtendWith(MockitoExtension.class)
class ComponentsFacadeTest {

    private final MarketplaceMapper marketplaceMapper = Mappers.getMapper(MarketplaceMapper.class);

    @Mock
    private MarketplaceService marketplaceExternalService;

    private ComponentsFacade componentsFacade;

    @BeforeEach
    void setup() {
        componentsFacade = new ComponentsFacade(marketplaceExternalService, marketplaceMapper);
    }

    @Test
    void get_project_component_returns_mapped_component_when_marketplace_returns_data() throws MarketplaceException {
        ProjectComponentExtendedInfo marketplaceComponent = buildTestMarketplaceComponent();
        CatalogItem testCatalogItem = buildTestCatalogItem();

        when(marketplaceExternalService.getProjectComponent("testProject", "testComponent"))
                .thenReturn(marketplaceComponent);
        when(marketplaceExternalService.getCatalogItem(anyString()))
                .thenReturn(testCatalogItem);

        Component retrievedComponent = componentsFacade.getProjectComponent("testProject", "testComponent");

        assertThat(retrievedComponent).isNotNull();
        assertThat(retrievedComponent.getId()).isNotNull();
        assertThat(retrievedComponent.getStatus()).isEqualTo(ComponentsStatusDTO.RUNNING);
        verify(marketplaceExternalService).getProjectComponent("testProject", "testComponent");
    }

    @Test
    void get_project_component_throws_not_found_when_marketplace_returns_null() throws MarketplaceException {
        when(marketplaceExternalService.getProjectComponent("testProject", "testComponent"))
                .thenReturn(null);

        assertThatThrownBy(() -> componentsFacade.getProjectComponent("testProject", "testComponent"))
                .isInstanceOf(ComponentNotFoundException.class)
                .hasMessage("Component 'testComponent' not found for project 'testProject'");
        verify(marketplaceExternalService).getProjectComponent("testProject", "testComponent");
    }

    @Test
    void create_project_component_returns_mapped_component_when_marketplace_creates_component() throws MarketplaceException {
        CreateComponentRequest request = buildTestCreateComponentRequest();

        when(marketplaceExternalService.provisionProjectComponent(eq("testProject"), anyList()))
                .thenReturn(true);

        componentsFacade.provisionProjectComponent("testProject", request);

        verify(marketplaceExternalService).provisionProjectComponent(eq("testProject"), anyList());
    }

    @Test
    void create_project_component_throws_creation_exception_when_marketplace_returns_null() throws MarketplaceException {
        CreateComponentRequest request = buildTestCreateComponentRequest();

        when(marketplaceExternalService.provisionProjectComponent(eq("testProject"), anyList()))
                .thenReturn(false);

        assertThatThrownBy(() -> componentsFacade.provisionProjectComponent("testProject", request))
                .isInstanceOf(ComponentCreationException.class)
                .hasMessage("Failed to create component for project 'testProject'");
        verify(marketplaceExternalService).provisionProjectComponent(eq("testProject"), anyList());
    }

    @Test
    void create_project_component_throws_already_exists_when_marketplace_returns_conflict() throws MarketplaceException {
        CreateComponentRequest request = buildTestCreateComponentRequest();
        HttpClientErrorException conflict = HttpClientErrorException.Conflict.create(
            HttpStatus.CONFLICT,
            "Conflict",
            HttpHeaders.EMPTY,
            new byte[0],
            null
        );

        when(marketplaceExternalService.provisionProjectComponent(eq("testProject"), anyList()))
            .thenThrow(new MarketplaceException("This component name already exists, please choose another name.", conflict));

        assertThatThrownBy(() -> componentsFacade.provisionProjectComponent("testProject", request))
            .isInstanceOf(ComponentAlreadyExistsException.class)
            .hasMessage("This component name already exists, please choose another name.");
        verify(marketplaceExternalService).provisionProjectComponent(eq("testProject"), anyList());
    }

    @Test
    void register_project_component_ends_successfully_when_marketplace_registration_succeeds() throws MarketplaceException {
        CreateComponentRequest request = buildTestCreateComponentRequest();
        String projectId = "testProjectId";

        doNothing().when(marketplaceExternalService).registerProjectComponent(eq(projectId), anyString(), anyString(), anyList());

        componentsFacade.registerProjectComponent(projectId, request);

        verify(marketplaceExternalService).registerProjectComponent(eq(projectId), anyString(), anyString(), anyList());
    }

    @Test
    void register_project_component_passes_correct_component_id_and_product_id_to_marketplace() throws MarketplaceException {
        CreateComponentRequest request = buildTestCreateComponentRequest();
        // name = "testcomponent", productId = "testProductId"
        String projectId = "testProjectId";

        doNothing().when(marketplaceExternalService).registerProjectComponent(
                eq(projectId), eq("testcomponent"), eq("testProductId"), anyList());

        componentsFacade.registerProjectComponent(projectId, request);

        verify(marketplaceExternalService).registerProjectComponent(
                eq(projectId), eq("testcomponent"), eq("testProductId"), anyList());
    }

    @Test
    void register_project_component_throws_registration_exception_wrapping_original_cause() throws MarketplaceException {
        CreateComponentRequest request = buildTestCreateComponentRequest();
        String projectId = "testProjectId";
        MarketplaceException originalCause = new MarketplaceException("downstream error", new RuntimeException("root"));

        doThrow(originalCause)
                .when(marketplaceExternalService)
                .registerProjectComponent(eq(projectId), anyString(), anyString(), anyList());

        assertThatThrownBy(() -> componentsFacade.registerProjectComponent(projectId, request))
                .isInstanceOf(ComponentRegistrationException.class)
                .hasMessage("Failed to register component 'testcomponent' for project 'testProjectId': downstream error")
                .hasCause(originalCause);

        verify(marketplaceExternalService).registerProjectComponent(eq(projectId), anyString(), anyString(), anyList());
    }

    @Test
    void delete_project_component_ends_successfully_for_existing_component() throws MarketplaceException {
        componentsFacade.deleteProjectComponent("testProject", "testComponent");

        verify(marketplaceExternalService).deleteProjectComponent("testProject", "testComponent");
    }

    @Test
    void delete_project_component_throws_component_deletion_exception_when_marketplace_exception_is_thrown() throws MarketplaceException {
        doThrow(new MarketplaceException("Test exception"))
                .when(marketplaceExternalService).deleteProjectComponent("testProject", "testComponent");

        assertThatThrownBy(() -> componentsFacade.deleteProjectComponent("testProject", "testComponent"))
                .isInstanceOf(ComponentDeletionException.class)
                .hasMessageContaining("Test exception");
        verify(marketplaceExternalService).deleteProjectComponent("testProject", "testComponent");
    }
}