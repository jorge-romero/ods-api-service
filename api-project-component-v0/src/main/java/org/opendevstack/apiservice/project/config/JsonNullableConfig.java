package org.opendevstack.apiservice.project.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.jackson.nullable.JsonNullable;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonNullableConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonNullableCustomizer() {
        return builder -> {
            builder.modulesToInstall(JsonNullableModule.class);
            builder.postConfigurer(this::configureJsonNullableInclusion);
        };
    }

    private void configureJsonNullableInclusion(ObjectMapper objectMapper) {
        objectMapper.configOverride(JsonNullable.class)
                .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_ABSENT, JsonInclude.Include.NON_ABSENT));
    }
}
