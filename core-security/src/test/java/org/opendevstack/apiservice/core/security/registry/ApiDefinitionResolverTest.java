package org.opendevstack.apiservice.core.security.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ApiDefinitionResolverTest {

    private CoreApiRegistry registry;
    private ApiDefinitionResolver resolver;

    @BeforeEach
    void setUp() {
        registry = mock(CoreApiRegistry.class);
        resolver = new ApiDefinitionResolver(registry);
    }

    @Test
    void resolve_standardVersionedPath_delegatesToRegistry() {
        ApiDefinition expected = new ApiDefinition("api-1", "Projects", "projects", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        when(registry.resolveBestMatch("v0", "projects")).thenReturn(Optional.of(expected));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v0/projects");

        Optional<ApiDefinition> result = resolver.resolve(request);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void resolve_publicApiPath_delegatesToRegistry() {
        ApiDefinition expected = new ApiDefinition("api-pub", "Health", "health", "v0",
                Set.of(AuthType.NONE), true, null, true);
        when(registry.resolveBestMatch("v0", "health")).thenReturn(Optional.of(expected));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/pub/v0/health");

        Optional<ApiDefinition> result = resolver.resolve(request);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void resolve_directVersionPath_delegatesToRegistry() {
        ApiDefinition expected = new ApiDefinition("api-1", "Projects", "projects", "v1",
                Set.of(AuthType.OBO), false, null, true);
        when(registry.resolveBestMatch("v1", "projects")).thenReturn(Optional.of(expected));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/projects");

        Optional<ApiDefinition> result = resolver.resolve(request);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void resolve_noVersionInPath_returnsEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");

        Optional<ApiDefinition> result = resolver.resolve(request);

        assertTrue(result.isEmpty());
        verifyNoInteractions(registry);
    }

    @Test
    void resolve_rootPath_returnsEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");

        Optional<ApiDefinition> result = resolver.resolve(request);

        assertTrue(result.isEmpty());
    }

    @Test
    void resolve_registryReturnsEmpty_returnsEmpty() {
        when(registry.resolveBestMatch("v0", "unknown")).thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v0/unknown");

        Optional<ApiDefinition> result = resolver.resolve(request);

        assertTrue(result.isEmpty());
    }
}
