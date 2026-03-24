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
    private PolicyRule activeRule;
    private Map<String, Object> requestBody;

    public PolicyContext(String clientId, String subject, Map<String, Object> claims,
                         ApiDefinition apiDefinition, HttpServletRequest request) {
        this.clientId = clientId;
        this.subject = subject;
        this.claims = claims;
        this.apiDefinition = apiDefinition;
        this.request = request;
    }

    public PolicyContext withRule(PolicyRule rule) {
        this.activeRule = rule;
        return this;
    }

    public PolicyContext withRequestBody(Map<String, Object> body) {
        this.requestBody = body;
        return this;
    }
}
