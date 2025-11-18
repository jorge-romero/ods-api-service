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
@ConfigurationProperties(prefix = "app.on-behalf-of")
public class OnBehalfOfProperties {

    private boolean enabled = true;

    private TokenExchange tokenExchange = new TokenExchange();

    private Flow flow = new Flow();

    private Delegation delegation = new Delegation();

    private Security security = new Security();

    private Provider provider = new Provider();

    @Getter
    @Setter
    public static class TokenExchange {
        private String grantType = "urn:ietf:params:oauth:grant-type:token-exchange";
        private List<String> supportedSubjectTokenTypes;
        private String requestedTokenType = "urn:ietf:params:oauth:token-type:access_token";
        private String defaultAudience;
    }

    @Getter
    @Setter
    public static class Flow {
        private boolean validateActor = false;
        private List<String> allowedActorClients;
        private List<String> requiredScopes;
        private int delegatedTokenLifetime = 3600;
    }

    @Getter
    @Setter
    public static class Delegation {
        private Map<String, ServiceDelegation> rules;

        @Getter
        @Setter
        public static class ServiceDelegation {
            private List<String> canDelegateTo;
            private List<String> allowedScopes;
            private int maxDelegationDepth = 1;
        }
    }

    @Getter
    @Setter
    public static class Security {
        private boolean requireActorClaim = false;
        private String introspectionEndpoint;
        private AuditLogging auditLogging = new AuditLogging();

        @Getter
        @Setter
        public static class AuditLogging {
            private boolean enabled = true;
            private boolean logSuccessfulExchanges = true;
            private boolean logFailedExchanges = true;
            private boolean includeTokenScope = false;
        }
    }

    @Getter
    @Setter
    public static class Provider {
        private String type = "keycloak";
        private Settings settings = new Settings();

        @Getter
        @Setter
        public static class Settings {
            private Keycloak keycloak = new Keycloak();
            private Auth0 auth0 = new Auth0();
            private Generic generic = new Generic();

            @Getter
            @Setter
            public static class Keycloak {
                private String tokenExchangePolicy = "on-behalf-of";
            }

            @Getter
            @Setter
            public static class Auth0 {
                private String organization;
            }

            @Getter
            @Setter
            public static class Generic {
                private String tokenExchangeUrl;
                private String clientAuthMethod = "client_secret_basic";
            }
        }
    }
}
