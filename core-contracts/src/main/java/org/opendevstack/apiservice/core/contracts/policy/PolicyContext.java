package org.opendevstack.apiservice.core.contracts.policy;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;

import java.util.Map;

@Getter
@AllArgsConstructor
public class PolicyContext {

    private final String clientId;
    private final String subject;
    private final Map<String, Object> claims;
    private final ApiDefinition apiDefinition;
    private final HttpServletRequest request;
    private final PolicyRule activeRule;
    private final Map<String, Object> requestBody;

    public PolicyContext(String clientId, String subject, Map<String, Object> claims,
                         ApiDefinition apiDefinition, HttpServletRequest request) {
        this(clientId, subject, claims, apiDefinition, request, null, null);
    }

    /**
     * Returns a new context with the given rule set, leaving this instance unchanged.
     */
    public PolicyContext withRule(PolicyRule rule) {
        return new PolicyContext(clientId, subject, claims, apiDefinition, request, rule, requestBody);
    }

    /**
     * Returns a new context with the given request body, leaving this instance unchanged.
     */
    public PolicyContext withRequestBody(Map<String, Object> body) {
        return new PolicyContext(clientId, subject, claims, apiDefinition, request, activeRule, body);
    }
}
