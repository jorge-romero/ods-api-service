package org.opendevstack.apiservice.core.security;

import org.opendevstack.apiservice.core.dto.ApiResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowEnforcementAspectTest {

    @Mock
    private FlowValidator flowValidator;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private FlowEnforcementAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new FlowEnforcementAspect();
        SecurityContextHolder.setContext(securityContext);
    }

    // Parameterized test for successful flow validations
    static Stream<Arguments> successfulFlowScenarios() {
        return Stream.of(
            Arguments.of("client-credentials", Map.of("grant_type", "client_credentials"), "client-credentials flow"),
            Arguments.of("authorization-code", Map.of("token_type", "Bearer"), "authorization-code flow"),
            Arguments.of("on-behalf-of", Map.of("actor", "service-account"), "on-behalf-of with actor"),
            Arguments.of("on-behalf-of", Map.of("scope", "on-behalf-of delegated_access"), "on-behalf-of with scope")
        );
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("successfulFlowScenarios")
    void testSuccessfulFlowValidation(String flowType, Map<String, Object> claims, String description) throws Throwable {
        // Given
        RequireFlow requireFlow = createRequireFlow(new String[]{flowType}, false, 0, new String[]{});
        Jwt jwt = createJwt(claims);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = aspect.enforceFlowRequirement(joinPoint, requireFlow);

        // Then
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void testEnforceFlowRequirementWithNoAuthentication() throws Throwable {
        // Given
        RequireFlow requireFlow = createRequireFlow(new String[]{"client-credentials"}, false, 0, new String[]{});
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        assertThrows(FlowEnforcementAspect.InsufficientFlowException.class,
                () -> aspect.enforceFlowRequirement(joinPoint, requireFlow));

        verify(joinPoint, never()).proceed();
    }

    @Test
    void testEnforceFlowRequirementWithNonJwtAuthentication() throws Throwable {
        // Given
        RequireFlow requireFlow = createRequireFlow(new String[]{"client-credentials"}, false, 0, new String[]{});
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not-a-jwt");

        // When & Then
        assertThrows(FlowEnforcementAspect.InsufficientFlowException.class,
                () -> aspect.enforceFlowRequirement(joinPoint, requireFlow));

        verify(joinPoint, never()).proceed();
    }

    @Test
    void testEnforceFlowRequirementFailureReturns403() throws Throwable {
        // Given
        RequireFlow requireFlow = createRequireFlow(new String[]{"client-credentials"}, false, 0, new String[]{});
        Jwt jwt = createJwt(Map.of("grant_type", "authorization_code")); // Wrong flow

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);

        // When
        Object result = aspect.enforceFlowRequirement(joinPoint, requireFlow);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof ResponseEntity);
        ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertEquals(403, response.getStatusCode().value());

        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Flow validation failed"));

        verify(joinPoint, never()).proceed();
    }

    // Parameterized test for successful actor validations
    static Stream<Arguments> successfulActorScenarios() {
        return Stream.of(
            Arguments.of(Map.of("actor", "service-principal"), true, 0, "actor present"),
            Arguments.of(Map.of("delegation_depth", 3), false, 2, "delegation depth sufficient")
        );
    }

    @ParameterizedTest(name = "{3}")
    @MethodSource("successfulActorScenarios")
    void testSuccessfulActorValidation(Map<String, Object> claims, boolean requireActor, int delegationDepth, String description) throws Throwable {
        // Given
        RequireFlow requireFlow = createRequireFlow(new String[]{}, requireActor, delegationDepth, new String[]{});
        Jwt jwt = createJwt(claims);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = aspect.enforceFlowRequirement(joinPoint, requireFlow);

        // Then
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    // Parameterized test for 403 failure scenarios
    static Stream<Arguments> failureScenarios() {
        return Stream.of(
            Arguments.of(new String[]{}, true, 0, new String[]{}, Map.of(), "Actor claim required"),
            Arguments.of(new String[]{}, false, 2, new String[]{}, Map.of("delegation_depth", 1), "Insufficient delegation depth"),
            Arguments.of(new String[]{}, false, 0, new String[]{"read:data", "write:data"}, Map.of("scope", "read:data"), "Missing required scope"),
            Arguments.of(new String[]{}, false, 0, new String[]{"read:data"}, Map.of(), "Token has no scopes")
        );
    }

    @ParameterizedTest(name = "{5}")
    @MethodSource("failureScenarios")
    void testFlowValidationFailures(String[] flows, boolean requireActor, int delegationDepth, 
                                     String[] scopes, Map<String, Object> claims, String expectedError) throws Throwable {
        // Given
        RequireFlow requireFlow = createRequireFlow(flows, requireActor, delegationDepth, scopes);
        Jwt jwt = createJwt(claims);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);

        // When
        Object result = aspect.enforceFlowRequirement(joinPoint, requireFlow);

        // Then
        assertTrue(result instanceof ResponseEntity);
        ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertEquals(403, response.getStatusCode().value());

        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertTrue(apiResponse.getMessage().contains(expectedError));

        verify(joinPoint, never()).proceed();
    }

    @Test
    void testRequiredScopesSuccess() throws Throwable {
        // Given
        RequireFlow requireFlow = createRequireFlow(new String[]{}, false, 0, new String[]{"read:data", "write:data"});
        Jwt jwt = createJwt(Map.of("scope", "read:data write:data admin:all"));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = aspect.enforceFlowRequirement(joinPoint, requireFlow);

        // Then
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void testMultipleFlowsOneMatches() throws Throwable {
        // Given
        RequireFlow requireFlow = createRequireFlow(
                new String[]{"authorization-code", "client-credentials"}, false, 0, new String[]{});
        Jwt jwt = createJwt(Map.of("grant_type", "client_credentials"));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = aspect.enforceFlowRequirement(joinPoint, requireFlow);

        // Then
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void testEmptyFlowRequirementAllowsAnyFlow() throws Throwable {
        // Given
        RequireFlow requireFlow = createRequireFlow(new String[]{}, false, 0, new String[]{});
        Jwt jwt = createJwt(Map.of());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = aspect.enforceFlowRequirement(joinPoint, requireFlow);

        // Then
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void testInsufficientFlowExceptionMessage() {
        // When
        FlowEnforcementAspect.InsufficientFlowException exception =
                new FlowEnforcementAspect.InsufficientFlowException("Custom error message");

        // Then
        assertEquals("Custom error message", exception.getMessage());
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

    private RequireFlow createRequireFlow(String[] flows, boolean requireActor,
                                          int requireDelegationDepth, String[] scopes) {
        return new RequireFlow() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return RequireFlow.class;
            }

            @Override
            public String[] value() {
                return flows;
            }

            @Override
            public boolean requireActor() {
                return requireActor;
            }

            @Override
            public int requireDelegationDepth() {
                return requireDelegationDepth;
            }

            @Override
            public String[] scopes() {
                return scopes;
            }

            @Override
            public String message() {
                return "Insufficient OAuth2 flow permissions";
            }
        };
    }
    @Test
    void testValidateFlowsWithSingleValidFlow() {
        FlowEnforcementAspect testAspect = new FlowEnforcementAspect();
        Jwt jwt = createJwt(Map.of("grant_type", "client_credentials"));
        String[] requiredFlows = {"client-credentials"};
        boolean result = invokeValidateFlows(testAspect, requiredFlows, jwt);
        assertTrue(result);
    }

    @Test
    void testValidateFlowsWithMultipleFlowsOneValid() {
        FlowEnforcementAspect testAspect = new FlowEnforcementAspect();
        Jwt jwt = createJwt(Map.of("token_type", "Bearer"));
        String[] requiredFlows = {"client-credentials", "authorization-code"};
        boolean result = invokeValidateFlows(testAspect, requiredFlows, jwt);
        assertTrue(result);
    }

    @Test
    void testValidateFlowsWithNoValidFlow() {
        FlowEnforcementAspect testAspect = new FlowEnforcementAspect();
        Jwt jwt = createJwt(Map.of("grant_type", "other"));
        String[] requiredFlows = {"client-credentials"};
        boolean result = invokeValidateFlows(testAspect, requiredFlows, jwt);
        assertFalse(result);
    }

    static Stream<Arguments> isFlowValidScenarios() {
        return Stream.of(
            Arguments.of("authorization-code", Map.of("token_type", "Bearer"), true, "authorization-code with Bearer token"),
            Arguments.of("client-credentials", Map.of("grant_type", "client_credentials"), true, "client-credentials flow"),
            Arguments.of("on-behalf-of", Map.of("actor", "service-account"), true, "on-behalf-of with actor"),
            Arguments.of("on-behalf-of", Map.of("scope", "on-behalf-of delegated_access"), true, "on-behalf-of with scope"),
            Arguments.of("unknown-flow", Map.of(), false, "unknown flow type")
        );
    }

    @ParameterizedTest(name = "{3}")
    @MethodSource("isFlowValidScenarios")
    void testIsFlowValid(String flow, Map<String, Object> claims, boolean expected, String description) {
        FlowEnforcementAspect testAspect = new FlowEnforcementAspect();
        Jwt jwt = createJwt(claims);
        boolean result = invokeIsFlowValid(testAspect, flow, jwt);
        assertEquals(expected, result);
    }

    // Reflection helpers to access private methods
    private boolean invokeValidateFlows(FlowEnforcementAspect aspect, String[] flows, Jwt jwt) {
        try {
            java.lang.reflect.Method m = FlowEnforcementAspect.class.getDeclaredMethod("validateFlows", String[].class, Jwt.class);
            m.setAccessible(true);
            return (boolean) m.invoke(aspect, flows, jwt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean invokeIsFlowValid(FlowEnforcementAspect aspect, String flow, Jwt jwt) {
        try {
            java.lang.reflect.Method m = FlowEnforcementAspect.class.getDeclaredMethod("isFlowValid", String.class, Jwt.class);
            m.setAccessible(true);
            return (boolean) m.invoke(aspect, flow, jwt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
