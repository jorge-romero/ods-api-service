package org.opendevstack.apiservice.core.security.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.opendevstack.apiservice.core.contracts.persistence.ApiDefinitionDao;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CoreApiRegistryTest {

    private ApiDefinitionDao apiDefinitionDao;
    private CoreApiRegistry registry;

    @BeforeEach
    void setUp() {
        apiDefinitionDao = mock(ApiDefinitionDao.class);
        registry = new CoreApiRegistry(apiDefinitionDao);
    }

    @Test
    void init_loadsDefinitionsFromDao() {
        ApiDefinition def = new ApiDefinition("api-1", "Projects", "projects", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        when(apiDefinitionDao.findAllEnabled()).thenReturn(List.of(def));

        registry.init();

        Optional<ApiDefinition> result = registry.resolve("v0", "projects");
        assertTrue(result.isPresent());
        assertEquals("api-1", result.get().getId());
    }

    @Test
    void resolve_exactMatch_returnsDefinition() {
        ApiDefinition def = new ApiDefinition("api-1", "Projects", "projects", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        when(apiDefinitionDao.findAllEnabled()).thenReturn(List.of(def));
        registry.init();

        assertTrue(registry.resolve("v0", "projects").isPresent());
    }

    @Test
    void resolve_noMatch_returnsEmpty() {
        when(apiDefinitionDao.findAllEnabled()).thenReturn(List.of());
        registry.init();

        assertTrue(registry.resolve("v0", "nonexistent").isEmpty());
    }

    @Test
    void resolveBestMatch_exactPath_returnsDefinition() {
        ApiDefinition def = new ApiDefinition("api-1", "Projects", "projects", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        when(apiDefinitionDao.findAllEnabled()).thenReturn(List.of(def));
        registry.init();

        Optional<ApiDefinition> result = registry.resolveBestMatch("v0", "projects");
        assertTrue(result.isPresent());
        assertEquals("api-1", result.get().getId());
    }

    @Test
    void resolveBestMatch_subPath_matchesViaAntPattern() {
        ApiDefinition def = new ApiDefinition("api-1", "Projects", "projects", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        when(apiDefinitionDao.findAllEnabled()).thenReturn(List.of(def));
        registry.init();

        Optional<ApiDefinition> result = registry.resolveBestMatch("v0", "projects/123/tasks");
        assertTrue(result.isPresent());
        assertEquals("api-1", result.get().getId());
    }

    @Test
    void resolveBestMatch_wrongVersion_returnsEmpty() {
        ApiDefinition def = new ApiDefinition("api-1", "Projects", "projects", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        when(apiDefinitionDao.findAllEnabled()).thenReturn(List.of(def));
        registry.init();

        assertTrue(registry.resolveBestMatch("v1", "projects").isEmpty());
    }

    @Test
    void resolveBestMatch_picksMostSpecific() {
        ApiDefinition broad = new ApiDefinition("api-broad", "Broad", "projects", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        ApiDefinition specific = new ApiDefinition("api-specific", "Specific", "projects/tasks", "v0",
                Set.of(AuthType.OBO), false, null, true);
        when(apiDefinitionDao.findAllEnabled()).thenReturn(List.of(broad, specific));
        registry.init();

        Optional<ApiDefinition> result = registry.resolveBestMatch("v0", "projects/tasks/42");
        assertTrue(result.isPresent());
        assertEquals("api-specific", result.get().getId());
    }

    @Test
    void resolveBestMatch_blankPath_returnsEmpty() {
        ApiDefinition def = new ApiDefinition("api-1", "Projects", "projects", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        when(apiDefinitionDao.findAllEnabled()).thenReturn(List.of(def));
        registry.init();

        assertTrue(registry.resolveBestMatch("v0", "").isEmpty());
    }

    @Test
    void refreshIndex_clearsOldEntriesAndReloads() {
        ApiDefinition old = new ApiDefinition("api-old", "Old", "old-path", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        when(apiDefinitionDao.findAllEnabled()).thenReturn(List.of(old));
        registry.init();
        assertTrue(registry.resolve("v0", "old-path").isPresent());

        ApiDefinition updated = new ApiDefinition("api-new", "New", "new-path", "v0",
                Set.of(AuthType.OBO), false, null, true);
        when(apiDefinitionDao.findAllEnabled()).thenReturn(List.of(updated));
        registry.refreshIndex();

        assertTrue(registry.resolve("v0", "old-path").isEmpty());
        assertTrue(registry.resolve("v0", "new-path").isPresent());
    }

    @Test
    void resolve_basePathWithLeadingSlash_matches() {
        ApiDefinition def = new ApiDefinition("api-1", "Projects", "/projects", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        when(apiDefinitionDao.findAllEnabled()).thenReturn(List.of(def));
        registry.init();

        assertTrue(registry.resolve("v0", "projects").isPresent());
    }
}
