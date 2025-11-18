package org.opendevstack.apiservice.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security.flows")
public class FlowProperties {

    private Global global = new Global();

    private Map<String, ApiFlows> apis;

    @Getter
    @Setter
    public static class Global {
        private List<String> enabledFlows;
        private String defaultFlow = "client-credentials";
    }

    @Getter
    @Setter
    public static class ApiFlows {
        private String defaultFlow;
        private List<EndpointFlow> endpoints;
    }

    @Getter
    @Setter
    public static class EndpointFlow {
        private String pattern;  // Now as a property instead of map key
        private List<String> flows;
        private List<String> roles;
        private boolean permitAll = false;
        private boolean requireActor = false;
        private boolean requireAuthentication = true;
        private int requireDelegationDepth = 0;
        private List<String> requiredScopes;
    }
}
