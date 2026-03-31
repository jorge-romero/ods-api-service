package org.opendevstack.apiservice.project.facade.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.entity.ClientAppProjectFlavorEntity;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectCreationException;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.serviceproject.exception.ProjectExistenceServiceException;
import org.opendevstack.apiservice.serviceproject.service.GenerateProjectKeyService;
import org.opendevstack.apiservice.serviceproject.service.ProjectExistenceService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectCreationCommandBuilderTest {

    private static final UUID CLIENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private GenerateProjectKeyService generateProjectKeyService;
    
    @Mock
    private ProjectExistenceService projectExistenceService;

    private ProjectCreationCommandBuilder sut;
    
    private AutoCloseable mocks;

    @BeforeEach
    void set_up() {
        mocks = MockitoAnnotations.openMocks(this);
        sut = new ProjectCreationCommandBuilder(generateProjectKeyService, projectExistenceService);
    }

    @AfterEach
    void tear_down() throws Exception {
        mocks.close();
    }

    @Test
    void build_resolves_defaults_from_flavor_when_request_fields_are_missing() throws ProjectExistenceServiceException {
        ClientAppProjectFlavorEntity flavor = build_flavor("DLSS", "CI-001", new String[] {}, "eu", "owner1");
        ClientAppEntity clientApp = build_client_app(List.of(flavor));
        CreateProjectRequest request = build_request("DLSS", null, "KEY01");
        request.setOwner(null);
        request.setLocation(null);

        when(projectExistenceService.isProjectFound("KEY01")).thenReturn(false);

        ProjectCreationCommand result = sut.build(request, clientApp);

        assertEquals("DLSS", result.getProjectFlavor());
        assertEquals("CI-001", result.getConfigurationItem());
        assertEquals("owner1", result.getOwner());
        assertEquals("eu", result.getLocation());
        assertEquals("KEY01", result.getProjectKey());
    }

    @Test
    void build_resolves_flavor_from_configuration_item_when_flavor_is_not_provided() throws ProjectExistenceServiceException {
        ClientAppProjectFlavorEntity flavor = build_flavor("DLSS", "CI-001", new String[] {}, "eu", "owner1");
        ClientAppEntity clientApp = build_client_app(List.of(flavor));
        CreateProjectRequest request = build_request(null, "CI-001", "KEY01");

        when(projectExistenceService.isProjectFound("KEY01")).thenReturn(false);

        ProjectCreationCommand result = sut.build(request, clientApp);

        assertEquals("DLSS", result.getProjectFlavor());
        assertEquals("CI-001", result.getConfigurationItem());
    }

    @Test
    void build_generates_project_key_when_request_project_key_is_null() throws ProjectExistenceServiceException, org.opendevstack.apiservice.serviceproject.exception.ProjectKeyGenerationException {
        ClientAppProjectFlavorEntity flavor = build_flavor("DLSS", "CI-001", new String[] {}, "eu", "owner1");
        ClientAppEntity clientApp = build_client_app(List.of(flavor));
        CreateProjectRequest request = build_request("DLSS", null, null);

        when(generateProjectKeyService.generateProjectKey("DLSS%06d")).thenReturn("DLSS000001");
        when(projectExistenceService.isProjectFound("DLSS000001")).thenReturn(false);

        ProjectCreationCommand result = sut.build(request, clientApp);

        assertEquals("DLSS000001", result.getProjectKey());
        verify(generateProjectKeyService).generateProjectKey("DLSS%06d");
    }

    @Test
    void build_throws_validation_exception_when_project_key_already_exists() throws ProjectExistenceServiceException {
        ClientAppProjectFlavorEntity flavor = build_flavor("DLSS", "CI-001", new String[] {}, "eu", "owner1");
        ClientAppEntity clientApp = build_client_app(List.of(flavor));
        CreateProjectRequest request = build_request("DLSS", null, "KEY01");

        when(projectExistenceService.isProjectFound("KEY01")).thenReturn(true);

        ProjectValidationException ex = assertThrows(ProjectValidationException.class,
                () -> sut.build(request, clientApp));
        assertEquals(ErrorKey.PROJECT_ALREADY_EXISTS, ex.getErrorKey());
    }

    @Test
    void build_throws_validation_exception_when_flavor_and_config_item_are_missing() {
        ClientAppProjectFlavorEntity flavor = build_flavor("DLSS", "CI-001", new String[] {}, "eu", "owner1");
        ClientAppEntity clientApp = build_client_app(List.of(flavor));
        CreateProjectRequest request = build_request(null, null, "KEY01");

        ProjectValidationException ex = assertThrows(ProjectValidationException.class,
                () -> sut.build(request, clientApp));
        assertEquals(ErrorKey.BAD_REQUEST_FLAVOR_CONFIG_ITEM, ex.getErrorKey());
    }

    @Test
    void build_throws_validation_exception_when_configuration_item_matches_multiple_flavors() {
        ClientAppEntity clientApp = build_client_app(List.of(
                build_flavor("DLSS", "CI-001", new String[] {}, "eu", "owner1"),
                build_flavor("AMP", "CI-001", new String[] {}, "eu", "owner2")));
        CreateProjectRequest request = build_request(null, "CI-001", "KEY01");

        ProjectValidationException ex = assertThrows(ProjectValidationException.class,
                () -> sut.build(request, clientApp));
        assertEquals(ErrorKey.INVALID_CONFIG_ITEM, ex.getErrorKey());
    }

    @Test
    void build_throws_project_key_generation_exception_when_generation_fails() throws Exception {
        ClientAppProjectFlavorEntity flavor = build_flavor("DLSS", "CI-001", new String[] {}, "eu", "owner1");
        ClientAppEntity clientApp = build_client_app(List.of(flavor));
        CreateProjectRequest request = build_request("DLSS", null, null);

        when(generateProjectKeyService.generateProjectKey("DLSS%06d"))
                .thenThrow(new org.opendevstack.apiservice.serviceproject.exception.ProjectKeyGenerationException("fail"));

        assertThrows(ProjectCreationException.class, () -> sut.build(request, clientApp));
    }

    private CreateProjectRequest build_request(String flavor, String configItem, String projectKey) {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectKey(projectKey);
        request.setProjectFlavor(flavor);
        request.setConfigurationItem(configItem);
        request.setProjectName("Test Project");
        request.setProjectDescription("A test project");
        request.setOwner("testowner");
        request.setLocation("eu");
        request.setX2OdsAccount("x2test");
        return request;
    }

    private ClientAppEntity build_client_app(List<ClientAppProjectFlavorEntity> flavors) {
        ClientAppEntity entity = ClientAppEntity.builder()
                .clientId(CLIENT_ID)
                .clientName("Test App")
                .build();
        entity.setProjectFlavors(flavors);
        return entity;
    }

    private ClientAppProjectFlavorEntity build_flavor(
            String name, String configItem, String[] allowedConfigItems, String location, String projectOwner) {
        return ClientAppProjectFlavorEntity.builder()
                .name(name)
                .configItem(configItem)
                .allowedConfigItems(allowedConfigItems)
                .projectKeyPattern(name + "%06d")
                .location(location)
                .projectOwner(projectOwner)
                .build();
    }
}
