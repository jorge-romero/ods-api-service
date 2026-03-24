package org.opendevstack.apiservice.core.engine.registry;

import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.contracts.persistence.ApiDefinitionDao;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CoreApiRegistryTest {

    @Test
    void resolveBestMatch_prefersMostSpecificBasePath() {
        ApiDefinitionDao dao = mock(ApiDefinitionDao.class);
        when(dao.findAllEnabled()).thenReturn(List.of(
                api("project-v1", "projects", "v1"),
            api("project-platform-v1", "projects/*/platforms", "v1"),
            api("project-users-v1", "projects/*/users", "v1")
        ));

        CoreApiRegistry registry = new CoreApiRegistry(dao);
        registry.refreshIndex();

        Optional<ApiDefinition> usersApi = registry.resolveBestMatch("v1", "projects/EDPP/users/john/status");
        Optional<ApiDefinition> platformsApi = registry.resolveBestMatch("v1", "projects/EDPP/platforms");
        Optional<ApiDefinition> projectsApi = registry.resolveBestMatch("v1", "projects/EDPP");

        assertTrue(usersApi.isPresent());
        assertEquals("project-users-v1", usersApi.get().getId());

        assertTrue(platformsApi.isPresent());
        assertEquals("project-platform-v1", platformsApi.get().getId());

        assertTrue(projectsApi.isPresent());
        assertEquals("project-v1", projectsApi.get().getId());
    }

    @Test
    void resolveBestMatch_allowsDescendantRoutesOfMatchedPattern() {
        ApiDefinitionDao dao = mock(ApiDefinitionDao.class);
        when(dao.findAllEnabled()).thenReturn(List.of(
                api("project-v1", "projects", "v1"),
                api("project-users-v1", "projects/*/users", "v1")
        ));

        CoreApiRegistry registry = new CoreApiRegistry(dao);
        registry.refreshIndex();

        Optional<ApiDefinition> resolved = registry.resolveBestMatch("v1", "projects/EDPP/users/u123/status");

        assertTrue(resolved.isPresent());
        assertEquals("project-users-v1", resolved.get().getId());
    }

    @Test
    void resolveBestMatch_filtersByVersion() {
        ApiDefinitionDao dao = mock(ApiDefinitionDao.class);
        when(dao.findAllEnabled()).thenReturn(List.of(
                api("project-v0", "projects", "v0"),
                api("project-v1", "projects", "v1")
        ));

        CoreApiRegistry registry = new CoreApiRegistry(dao);
        registry.refreshIndex();

        Optional<ApiDefinition> resolved = registry.resolveBestMatch("v0", "projects/EDPP");

        assertTrue(resolved.isPresent());
        assertEquals("project-v0", resolved.get().getId());
    }

    private ApiDefinition api(String id, String basePath, String version) {
        return new ApiDefinition(
                id,
                id,
                basePath,
                version,
                Collections.emptySet(),
                false,
                null,
                true
        );
    }
}
