package org.opendevstack.apiservice.core.security.registry;

import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ApiDefinitionResolver {

    private static final Pattern VERSION_PATH_PATTERN = Pattern.compile("^/(?:api/(?:pub/)?)?(v\\d+)/(.+)$");

    private final CoreApiRegistry registry;

    public ApiDefinitionResolver(CoreApiRegistry registry) {
        this.registry = registry;
    }

    public Optional<ApiDefinition> resolve(HttpServletRequest request) {
        String uri = request.getRequestURI();
        Matcher matcher = VERSION_PATH_PATTERN.matcher(uri);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        String version = matcher.group(1);       // e.g. "v0", "v1"
        String pathAfterVersion = matcher.group(2);
        return registry.resolveBestMatch(version, pathAfterVersion);
    }
}