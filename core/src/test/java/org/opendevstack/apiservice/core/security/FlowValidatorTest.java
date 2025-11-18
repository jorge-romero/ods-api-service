package org.opendevstack.apiservice.core.security;

import org.opendevstack.apiservice.core.config.FlowProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlowValidatorTest {

    private FlowValidator flowValidator;
    private FlowProperties flowProperties;

    @BeforeEach
    void setUp() {
        flowProperties = new FlowProperties();
        flowValidator = new FlowValidator(flowProperties);

        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void testValidateEndpointFlowWithNoConfiguration() {
        // Given - no flow configuration
        setupAuthentication(createJwt(Map.of()));

        // When
        FlowValidator.ValidationResult result = flowValidator.validateEndpointFlow("/api/test");

        // Then
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void testValidateEndpointFlowWithNoAuthenticationAndConfiguration() {
        // Given - no authentication but with endpoint configuration requiring it
        FlowProperties.EndpointFlow endpointFlow = new FlowProperties.EndpointFlow();
        endpointFlow.setPattern("/api/test");
        endpointFlow.setRequireAuthentication(true);

        FlowProperties.ApiFlows apiFlows = new FlowProperties.ApiFlows();
        apiFlows.setEndpoints(Arrays.asList(endpointFlow));

        flowProperties.setApis(Map.of("test-api", apiFlows));

        SecurityContextHolder.clearContext();

        // When
        FlowValidator.ValidationResult result = flowValidator.validateEndpointFlow("/api/test");

        // Then
        assertFalse(result.isValid());
        assertEquals("No authentication", result.getErrorMessage());
    }

    @Test
    void testValidateEndpointFlowWithAuthenticationNotRequired() {
        // Given - endpoint that doesn't require authentication
        FlowProperties.EndpointFlow endpointFlow = new FlowProperties.EndpointFlow();
        endpointFlow.setPattern("/api/public/**");
        endpointFlow.setRequireAuthentication(false);

        FlowProperties.ApiFlows apiFlows = new FlowProperties.ApiFlows();
        apiFlows.setEndpoints(Arrays.asList(endpointFlow));

        flowProperties.setApis(Map.of("test-api", apiFlows));

        setupAuthentication(createJwt(Map.of()));

        // When
        FlowValidator.ValidationResult result = flowValidator.validateEndpointFlow("/api/public/test");

        // Then
        assertTrue(result.isValid());
    }

    // Parameterized test for successful flow validations
    static Stream<Arguments> successfulFlowScenarios() {
        return Stream.of(
            Arguments.of("authorization-code", "/api/user/**", "/api/user/profile", Map.of("token_type", "Bearer")),
            Arguments.of("client-credentials", "/api/service/**", "/api/service/data", Map.of("grant_type", "client_credentials")),
            Arguments.of("on-behalf-of", "/api/delegated/**", "/api/delegated/action", Map.of("actor", "service-account"))
        );
    }

    @ParameterizedTest(name = "{0} flow validation")
    @MethodSource("successfulFlowScenarios")
    void testSuccessfulFlowValidation(String flowType, String pattern, String endpoint, Map<String, Object> claims) {
        // Given
        FlowProperties.EndpointFlow endpointFlow = new FlowProperties.EndpointFlow();
        endpointFlow.setPattern(pattern);
        endpointFlow.setFlows(Arrays.asList(flowType));

        FlowProperties.ApiFlows apiFlows = new FlowProperties.ApiFlows();
        apiFlows.setEndpoints(Arrays.asList(endpointFlow));

        flowProperties.setApis(Map.of("test-api", apiFlows));

        Jwt jwt = createJwt(claims);
        setupAuthentication(jwt);

        // When
        FlowValidator.ValidationResult result = flowValidator.validateEndpointFlow(endpoint);

        // Then
        assertTrue(result.isValid());
    }

    @Test
    void testValidateFlowFailureWhenFlowDoesNotMatch() {
        // Given - endpoint requiring client-credentials but token is authorization-code
        FlowProperties.EndpointFlow endpointFlow = new FlowProperties.EndpointFlow();
        endpointFlow.setPattern("/api/service/**");
        endpointFlow.setFlows(Arrays.asList("client-credentials"));

        FlowProperties.ApiFlows apiFlows = new FlowProperties.ApiFlows();
        apiFlows.setEndpoints(Arrays.asList(endpointFlow));

        flowProperties.setApis(Map.of("test-api", apiFlows));

        Jwt jwt = createJwt(Map.of("token_type", "Bearer")); // Not client-credentials
        setupAuthentication(jwt);

        // When
        FlowValidator.ValidationResult result = flowValidator.validateEndpointFlow("/api/service/data");

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("does not match required flows"));
    }

    // Parameterized test for actor and delegation depth validations
    static Stream<Arguments> actorAndDepthScenarios() {
        return Stream.of(
            Arguments.of(true, 0, Map.of("actor", "service-principal"), "/api/obo/**", "/api/obo/action", true, "actor present"),
            Arguments.of(true, 0, Map.of(), "/api/obo/**", "/api/obo/action", false, "actor missing"),
            Arguments.of(false, 2, Map.of("delegation_depth", 3), "/api/deep/**", "/api/deep/action", true, "delegation depth sufficient"),
            Arguments.of(false, 2, Map.of("delegation_depth", 1), "/api/deep/**", "/api/deep/action", false, "delegation depth insufficient")
        );
    }

    @ParameterizedTest(name = "{6}")
    @MethodSource("actorAndDepthScenarios")
    void testActorAndDelegationDepthValidation(boolean requireActor, int delegationDepth, Map<String, Object> claims,
                                                String pattern, String endpoint, boolean shouldBeValid, String description) {
        // Given
        FlowProperties.EndpointFlow endpointFlow = new FlowProperties.EndpointFlow();
        endpointFlow.setPattern(pattern);
        endpointFlow.setRequireActor(requireActor);
        endpointFlow.setRequireDelegationDepth(delegationDepth);

        FlowProperties.ApiFlows apiFlows = new FlowProperties.ApiFlows();
        apiFlows.setEndpoints(Arrays.asList(endpointFlow));

        flowProperties.setApis(Map.of("test-api", apiFlows));

        Jwt jwt = createJwt(claims);
        setupAuthentication(jwt);

        // When
        FlowValidator.ValidationResult result = flowValidator.validateEndpointFlow(endpoint);

        // Then
        assertEquals(shouldBeValid, result.isValid());
        if (!shouldBeValid) {
            assertFalse(result.getErrorMessage().isEmpty());
        }
    }

    // Parameterized test for scope validations
    static Stream<Arguments> scopeValidationScenarios() {
        return Stream.of(
            Arguments.of(Arrays.asList("read:data", "write:data"), "read:data write:data admin:all", true, "all scopes present"),
            Arguments.of(Arrays.asList("read:data", "write:data"), "read:data", false, "missing write:data scope")
        );
    }

    @ParameterizedTest(name = "{3}")
    @MethodSource("scopeValidationScenarios")
    void testScopeValidation(List<String> requiredScopes, String tokenScope, boolean shouldBeValid, String description) {
        // Given
        FlowProperties.EndpointFlow endpointFlow = new FlowProperties.EndpointFlow();
        endpointFlow.setPattern("/api/scoped/**");
        endpointFlow.setRequiredScopes(requiredScopes);

        FlowProperties.ApiFlows apiFlows = new FlowProperties.ApiFlows();
        apiFlows.setEndpoints(Arrays.asList(endpointFlow));

        flowProperties.setApis(Map.of("test-api", apiFlows));

        Jwt jwt = createJwt(Map.of("scope", tokenScope));
        setupAuthentication(jwt);

        // When
        FlowValidator.ValidationResult result = flowValidator.validateEndpointFlow("/api/scoped/action");

        // Then
        assertEquals(shouldBeValid, result.isValid());
        if (!shouldBeValid) {
            assertTrue(result.getErrorMessage().contains("missing required scopes"));
        }
    }

    @Test
    void testPatternMatchingExact() {
        // Given - exact pattern match
        FlowProperties.EndpointFlow endpointFlow = new FlowProperties.EndpointFlow();
        endpointFlow.setPattern("/api/exact");
        endpointFlow.setRequireActor(true);

        FlowProperties.ApiFlows apiFlows = new FlowProperties.ApiFlows();
        apiFlows.setEndpoints(Arrays.asList(endpointFlow));

        flowProperties.setApis(Map.of("test-api", apiFlows));

        Jwt jwt = createJwt(Map.of("actor", "service"));
        setupAuthentication(jwt);

        // When
        FlowValidator.ValidationResult result = flowValidator.validateEndpointFlow("/api/exact");

        // Then
        assertTrue(result.isValid());
    }

    @Test
    void testPatternMatchingWildcard() {
        // Given - wildcard pattern match
        FlowProperties.EndpointFlow endpointFlow = new FlowProperties.EndpointFlow();
        endpointFlow.setPattern("/api/wildcard/**");
        endpointFlow.setRequireActor(true);

        FlowProperties.ApiFlows apiFlows = new FlowProperties.ApiFlows();
        apiFlows.setEndpoints(Arrays.asList(endpointFlow));

        flowProperties.setApis(Map.of("test-api", apiFlows));

        Jwt jwt = createJwt(Map.of("actor", "service"));
        setupAuthentication(jwt);

        // When
        FlowValidator.ValidationResult result = flowValidator.validateEndpointFlow("/api/wildcard/sub/path");

        // Then
        assertTrue(result.isValid());
    }

    @Test
    void testValidationResultSuccess() {
        // When
        FlowValidator.ValidationResult result = FlowValidator.ValidationResult.success();

        // Then
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void testValidationResultFailure() {
        // When
        FlowValidator.ValidationResult result = FlowValidator.ValidationResult.failure("Error occurred");

        // Then
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals("Error occurred", result.getErrorMessage());
    }

    @Test
    void testValidationResultAddError() {
        // Given
        FlowValidator.ValidationResult result = new FlowValidator.ValidationResult();

        // When
        result.addError("First error");
        result.addError("Second error");

        // Then
        assertFalse(result.isValid());
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getErrorMessage().contains("First error"));
        assertTrue(result.getErrorMessage().contains("Second error"));
    }

    @Test
    void testMultipleValidationErrors() {
        // Given - endpoint with multiple requirements
        FlowProperties.EndpointFlow endpointFlow = new FlowProperties.EndpointFlow();
        endpointFlow.setPattern("/api/strict/**");
        endpointFlow.setFlows(Arrays.asList("on-behalf-of"));
        endpointFlow.setRequireActor(true);
        endpointFlow.setRequiredScopes(Arrays.asList("admin:all"));

        FlowProperties.ApiFlows apiFlows = new FlowProperties.ApiFlows();
        apiFlows.setEndpoints(Arrays.asList(endpointFlow));

        flowProperties.setApis(Map.of("test-api", apiFlows));

        Jwt jwt = createJwt(Map.of("scope", "read:data")); // Missing actor and wrong flow
        setupAuthentication(jwt);

        // When
        FlowValidator.ValidationResult result = flowValidator.validateEndpointFlow("/api/strict/action");

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().size() >= 2);
    }

    // Helper methods

    private Jwt createJwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("test-user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claims(c -> c.putAll(claims))
                .build();
    }

    private void setupAuthentication(Jwt jwt) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);

        SecurityContextHolder.setContext(securityContext);
    }
}
