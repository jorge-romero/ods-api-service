package org.opendevstack.apiservice.core.engine.registry;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiDefinitionResolverTest {

    @Test
    void resolve_supportsApiPrefixAndDelegatesFullPathAfterVersion() {
        CoreApiRegistry registry = mock(CoreApiRegistry.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/projects/EDPP/platforms");
        when(registry.resolveBestMatch(eq("v1"), eq("projects/EDPP/platforms")))
                .thenReturn(Optional.empty());

        ApiDefinitionResolver resolver = new ApiDefinitionResolver(registry);
        resolver.resolve(request);

        verify(registry).resolveBestMatch("v1", "projects/EDPP/platforms");
    }

    @Test
    void resolve_supportsApiPubPrefix() {
        CoreApiRegistry registry = mock(CoreApiRegistry.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/pub/v0/projects/EDPP");
        when(registry.resolveBestMatch(eq("v0"), eq("projects/EDPP")))
                .thenReturn(Optional.empty());

        ApiDefinitionResolver resolver = new ApiDefinitionResolver(registry);
        resolver.resolve(request);

        verify(registry).resolveBestMatch("v0", "projects/EDPP");
    }

    @Test
    void resolve_returnsEmptyWhenUriDoesNotMatchPattern() {
        CoreApiRegistry registry = mock(CoreApiRegistry.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/health");

        ApiDefinitionResolver resolver = new ApiDefinitionResolver(registry);
        Optional<ApiDefinition> resolved = resolver.resolve(request);

        assertTrue(resolved.isEmpty());
        verify(registry, never()).resolveBestMatch("v1", "health");
    }
}
