package org.opendevstack.apiservice.core.security.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.opendevstack.apiservice.core.security.flow.AuthFlowResolver;
import org.opendevstack.apiservice.core.security.flow.AuthFlowValidator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthTypeEnforcementFilterTest {

    private AuthFlowResolver flowResolver;
    private AuthFlowValidator ccValidator;
    private AuthFlowValidator oboValidator;
    private AuthTypeEnforcementFilter filter;
    private FilterChain filterChain;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        flowResolver = new AuthFlowResolver();
        ccValidator = mock(AuthFlowValidator.class);
        when(ccValidator.getSupportedFlow()).thenReturn(AuthType.CLIENT_CREDENTIALS);
        when(ccValidator.validate(any())).thenReturn(true);

        oboValidator = mock(AuthFlowValidator.class);
        when(oboValidator.getSupportedFlow()).thenReturn(AuthType.OBO);
        when(oboValidator.validate(any())).thenReturn(true);

        filter = new AuthTypeEnforcementFilter(flowResolver, List.of(ccValidator, oboValidator));
        filterChain = mock(FilterChain.class);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noApiDefinition_continuesChain() throws Exception {
        // No API_DEFINITION_ATTR set
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void publicApi_continuesChain() throws Exception {
        ApiDefinition apiDef = new ApiDefinition("api-1", "Public", "/pub", "v0",
                Set.of(AuthType.NONE), true, null, true);
        request.setAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR, apiDef);

        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void securedApi_noJwtAuthentication_throwsCredentialsNotFound() {
        ApiDefinition apiDef = new ApiDefinition("api-1", "Secured", "/sec", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        request.setAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR, apiDef);

        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> filter.doFilterInternal(request, response, filterChain));
    }

    @Test
    void securedApi_clientCredentials_allowedFlow_continuesChain() throws Exception {
        ApiDefinition apiDef = new ApiDefinition("api-1", "Secured", "/sec", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        request.setAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR, apiDef);

        setJwtAuth(Map.of("roles", List.of("ADMIN"))); // no scp → CLIENT_CREDENTIALS

        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void securedApi_obo_allowedFlow_continuesChain() throws Exception {
        ApiDefinition apiDef = new ApiDefinition("api-1", "Secured", "/sec", "v0",
                Set.of(AuthType.OBO), false, null, true);
        request.setAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR, apiDef);

        setJwtAuth(Map.of("scp", "api.read")); // scp present → OBO

        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void securedApi_wrongFlow_throwsAccessDenied() {
        // API only allows OBO, but token is CLIENT_CREDENTIALS
        ApiDefinition apiDef = new ApiDefinition("api-1", "Secured", "/sec", "v0",
                Set.of(AuthType.OBO), false, null, true);
        request.setAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR, apiDef);

        setJwtAuth(Map.of("roles", List.of("ADMIN"))); // no scp → CLIENT_CREDENTIALS

        assertThrows(AccessDeniedException.class,
                () -> filter.doFilterInternal(request, response, filterChain));
    }

    @Test
    void securedApi_flowValidatorFails_throwsAccessDenied() {
        ApiDefinition apiDef = new ApiDefinition("api-1", "Secured", "/sec", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS), false, null, true);
        request.setAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR, apiDef);

        setJwtAuth(Map.of("roles", List.of("ADMIN")));
        when(ccValidator.validate(any())).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> filter.doFilterInternal(request, response, filterChain));
    }

    @Test
    void securedApi_bothFlowsAllowed_clientCredentialsToken_passes() throws Exception {
        ApiDefinition apiDef = new ApiDefinition("api-1", "Secured", "/sec", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS, AuthType.OBO), false, null, true);
        request.setAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR, apiDef);

        setJwtAuth(Map.of("roles", List.of("ADMIN")));

        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void securedApi_bothFlowsAllowed_oboToken_passes() throws Exception {
        ApiDefinition apiDef = new ApiDefinition("api-1", "Secured", "/sec", "v0",
                Set.of(AuthType.CLIENT_CREDENTIALS, AuthType.OBO), false, null, true);
        request.setAttribute(AuthTypeEnforcementFilter.API_DEFINITION_ATTR, apiDef);

        setJwtAuth(Map.of("scp", "api.read"));

        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    private void setJwtAuth(Map<String, Object> claims) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "test-subject")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
