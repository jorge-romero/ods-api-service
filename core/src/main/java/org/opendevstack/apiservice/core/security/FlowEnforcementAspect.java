package org.opendevstack.apiservice.core.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Aspect to enforce OAuth2 flow requirements
 * Intercepts methods annotated with @RequireFlow
 */
@Aspect
@Component
@Order(100) // Run after Spring Security's authorization
public class FlowEnforcementAspect {

    @Around("@annotation(requireFlow)")
    public Object enforceFlowRequirement(ProceedingJoinPoint joinPoint, RequireFlow requireFlow) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new InsufficientFlowException("Authentication required");
        }

        if (!(authentication.getPrincipal() instanceof Jwt)) {
            throw new InsufficientFlowException("JWT token required");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        FlowValidator.ValidationResult result = validateFlowRequirements(requireFlow, jwt);

        if (!result.isValid()) {
            return ResponseEntity.status(403).body(
                new org.opendevstack.apiservice.core.dto.ApiResponse<>(
                    false, null, "Flow validation failed: " + result.getErrorMessage()
                )
            );
        }

        return joinPoint.proceed();
    }

    private FlowValidator.ValidationResult validateFlowRequirements(RequireFlow requireFlow, Jwt jwt) {
        FlowValidator.ValidationResult result = new FlowValidator.ValidationResult();

        validateRequiredFlows(requireFlow, jwt, result);
        validateActorRequirement(requireFlow, jwt, result);
        validateDelegationDepth(requireFlow, jwt, result);
        validateRequiredScopes(requireFlow, jwt, result);

        return result;
    }

    private void validateRequiredFlows(RequireFlow requireFlow, Jwt jwt, FlowValidator.ValidationResult result) {
        if (requireFlow.value().length > 0) {
            boolean hasRequiredFlow = validateFlows(requireFlow.value(), jwt);
            if (!hasRequiredFlow) {
                result.addError("Token does not match required flows: " +
                    String.join(", ", requireFlow.value()));
            }
        }
    }

    private void validateActorRequirement(RequireFlow requireFlow, Jwt jwt, FlowValidator.ValidationResult result) {
        if (requireFlow.requireActor()) {
            String actor = jwt.getClaimAsString("actor");
            if (actor == null || actor.isEmpty()) {
                result.addError("Actor claim required (On-Behalf-Of flow)");
            }
        }
    }

    private void validateDelegationDepth(RequireFlow requireFlow, Jwt jwt, FlowValidator.ValidationResult result) {
        if (requireFlow.requireDelegationDepth() > 0) {
            Number depth = jwt.getClaim("delegation_depth");
            int currentDepth = depth != null ? depth.intValue() : 0;
            if (currentDepth < requireFlow.requireDelegationDepth()) {
                result.addError("Insufficient delegation depth. Required: " +
                    requireFlow.requireDelegationDepth() + ", Actual: " + currentDepth);
            }
        }
    }

    private void validateRequiredScopes(RequireFlow requireFlow, Jwt jwt, FlowValidator.ValidationResult result) {
        if (requireFlow.scopes().length > 0) {
            String scope = jwt.getClaimAsString("scope");
            if (scope == null || scope.isEmpty()) {
                result.addError("Token has no scopes");
            } else {
                validateEachScope(requireFlow.scopes(), scope.split(" "), result);
            }
        }
    }

    private void validateEachScope(String[] requiredScopes, String[] tokenScopes, FlowValidator.ValidationResult result) {
        for (String requiredScope : requiredScopes) {
            if (!isScopePresent(requiredScope, tokenScopes)) {
                result.addError("Missing required scope: " + requiredScope);
            }
        }
    }

    private boolean isScopePresent(String requiredScope, String[] tokenScopes) {
        for (String tokenScope : tokenScopes) {
            if (requiredScope.equals(tokenScope)) {
                return true;
            }
        }
        return false;
    }

    private boolean validateFlows(String[] requiredFlows, Jwt jwt) {
        for (String requiredFlow : requiredFlows) {
            if (isFlowValid(requiredFlow, jwt)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFlowValid(String requiredFlow, Jwt jwt) {
        switch (requiredFlow) {
            case "authorization-code":
                return isAuthorizationCodeFlow(jwt);
            case "client-credentials":
                return isClientCredentialsFlow(jwt);
            case "on-behalf-of":
                return isOnBehalfOfFlow(jwt);
            default:
                return false;
        }
    }

    private boolean isAuthorizationCodeFlow(Jwt jwt) {
        return "Bearer".equals(jwt.getClaimAsString("token_type"));
    }

    private boolean isClientCredentialsFlow(Jwt jwt) {
        String grantType = jwt.getClaimAsString("grant_type");
        return "client_credentials".equals(grantType);
    }

    private boolean isOnBehalfOfFlow(Jwt jwt) {
        String actor = jwt.getClaimAsString("actor");
        if (actor != null && !actor.isEmpty()) {
            return true;
        }
        String scope = jwt.getClaimAsString("scope");
        return scope != null && (scope.contains("on-behalf-of") || scope.contains("delegated_access"));
    }

    /**
     * Exception thrown when flow requirements are not met
     */
    public static class InsufficientFlowException extends RuntimeException {
        public InsufficientFlowException(String message) {
            super(message);
        }
    }
}
