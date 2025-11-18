package org.opendevstack.apiservice.core.security;

import org.opendevstack.apiservice.core.config.FlowProperties;
import org.opendevstack.apiservice.core.config.FlowProperties.EndpointFlow;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Validates OAuth2 flows for endpoints
 * Checks if authenticated token meets the required flow criteria
 */
@Component
public class FlowValidator {

    private final FlowProperties flowProperties;

    public FlowValidator(FlowProperties flowProperties) {
        this.flowProperties = flowProperties;
    }

    /**
     * Validate if current token meets flow requirements for an endpoint
     */
    public ValidationResult validateEndpointFlow(String endpointPattern) {
        EndpointFlow endpointFlow = findEndpointFlow(endpointPattern);
        if (endpointFlow == null) {
            // No specific flow configuration, use global defaults
            return ValidationResult.success();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ValidationResult.failure("No authentication");
        }

        ValidationResult result = new ValidationResult();

        // Check if authentication is required
        if (!endpointFlow.isRequireAuthentication()) {
            return ValidationResult.success();
        }

        // Check if token is present
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            // Validate required flows
            if (endpointFlow.getFlows() != null && !endpointFlow.getFlows().isEmpty()) {
                boolean hasRequiredFlow = validateFlows(endpointFlow.getFlows(), jwt);
                if (!hasRequiredFlow) {
                    result.addError("Token does not match required flows: " + endpointFlow.getFlows());
                }
            }

            // Validate actor requirement (for On-Behalf-Of)
            if (endpointFlow.isRequireActor()) {
                boolean hasActor = validateActorClaim(jwt);
                if (!hasActor) {
                    result.addError("Token must have actor claim (On-Behalf-Of flow required)");
                }
            }

            // Validate delegation depth
            if (endpointFlow.getRequireDelegationDepth() > 0) {
                int currentDepth = getDelegationDepth(jwt);
                if (currentDepth < endpointFlow.getRequireDelegationDepth()) {
                    result.addError("Insufficient delegation depth. Required: " +
                        endpointFlow.getRequireDelegationDepth() + ", Actual: " + currentDepth);
                }
            }

            // Validate required scopes
            if (endpointFlow.getRequiredScopes() != null && !endpointFlow.getRequiredScopes().isEmpty()) {
                boolean hasRequiredScopes = validateScopes(endpointFlow.getRequiredScopes(), jwt);
                if (!hasRequiredScopes) {
                    result.addError("Token missing required scopes: " + endpointFlow.getRequiredScopes());
                }
            }
        }

        return result;
    }

    /**
     * Find flow configuration for an endpoint
     */
    private EndpointFlow findEndpointFlow(String endpointPattern) {
        if (flowProperties.getApis() == null) {
            return null;
        }

        // Check each API's endpoints
        for (Map.Entry<String, FlowProperties.ApiFlows> apiEntry : flowProperties.getApis().entrySet()) {
            FlowProperties.ApiFlows apiFlows = apiEntry.getValue();
            if (apiFlows.getEndpoints() != null) {
                // Check if pattern matches
                for (EndpointFlow endpointFlow : apiFlows.getEndpoints()) {
                    String pattern = endpointFlow.getPattern();
                    if (pattern != null && matchesPattern(endpointPattern, pattern)) {
                        return endpointFlow;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Simple pattern matching (supports wildcards like /**)
     */
    private boolean matchesPattern(String endpoint, String pattern) {
        if (pattern.endsWith("/**")) {
            String basePattern = pattern.substring(0, pattern.length() - 3);
            return endpoint.startsWith(basePattern);
        }
        return endpoint.equals(pattern);
    }

    /**
     * Validate if token has one of the required flows
     */
    private boolean validateFlows(List<String> requiredFlows, Jwt jwt) {
        // Get flow from token claims or scopes
        String scope = jwt.getClaimAsString("scope");
        String grantType = jwt.getClaimAsString("grant_type");

        for (String requiredFlow : requiredFlows) {
            // Check if flow matches
            if ("authorization-code".equals(requiredFlow)) {
                if ("Bearer".equals(jwt.getClaimAsString("token_type"))) {
                    return true; // Authorization code tokens appear as Bearer tokens
                }
            } else if ("client-credentials".equals(requiredFlow)) {
                if ("client_credentials".equals(grantType)) {
                    return true;
                }
            } else if ("on-behalf-of".equals(requiredFlow)) {
                // Check for actor claim (indicates OBO flow)
                String actor = jwt.getClaimAsString("actor");
                if (actor != null && !actor.isEmpty()) {
                    return true;
                }
                // Or check scope
                if (scope != null && (scope.contains("on-behalf-of") || scope.contains("delegated_access"))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Validate actor claim for On-Behalf-Of flow
     */
    private boolean validateActorClaim(Jwt jwt) {
        String actor = jwt.getClaimAsString("actor");
        return actor != null && !actor.isEmpty();
    }

    /**
     * Get delegation depth from token
     */
    private int getDelegationDepth(Jwt jwt) {
        Number depth = jwt.getClaim("delegation_depth");
        return depth != null ? depth.intValue() : 0;
    }

    /**
     * Validate required scopes
     */
    private boolean validateScopes(List<String> requiredScopes, Jwt jwt) {
        String scope = jwt.getClaimAsString("scope");
        if (scope == null || scope.isEmpty()) {
            return false;
        }

        String[] tokenScopes = scope.split(" ");
        for (String requiredScope : requiredScopes) {
            boolean found = false;
            for (String tokenScope : tokenScopes) {
                if (requiredScope.equals(tokenScope)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validation result for flow checks
     */
    @Getter
    public static class ValidationResult {
        private boolean valid = true;
        private java.util.List<String> errors = new java.util.ArrayList<>();

        public static ValidationResult success() {
            return new ValidationResult();
        }

        public static ValidationResult failure(String error) {
            ValidationResult result = new ValidationResult();
            result.valid = false;
            result.errors.add(error);
            return result;
        }

        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}
