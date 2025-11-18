package org.opendevstack.apiservice.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.Bindable;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${openapi.info.title:'API Documentation'}")
    private String title;

    @Value("${openapi.info.description:'API description not provided'}")
    private String description;

    @Value("${openapi.info.version:'1.0.0'}")
    private String version;

    @Value("${openapi.info.contact.name:'API Support'}")
    private String contactName;

    @Value("${openapi.info.contact.email:'support@example.com'}")
    private String contactEmail;

    private final Environment environment;

    public OpenApiConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        // bind the YAML list under openapi.servers to the nested ServerProperties class
        List<ServerProperties> configured = Binder.get(environment)
                .bind("openapi.servers", Bindable.listOf(ServerProperties.class))
                .orElse(List.of());

        List<Server> servers = configured.stream()
                .map(s -> new Server().url(s.getUrl()).description(s.getDescription()))
                .toList();

        // fallback to a sensible default if no servers configured
        if (servers.isEmpty()) {
            servers = List.of(new Server().url("http://localhost:8080").description("Development server"));
        }

        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact().name(contactName).email(contactEmail)))
                .servers(servers)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")));

    }

    public static class ServerProperties {
        private String url;
        private String description;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
