package org.opendevstack.apiservice.core.security.registry;

import org.opendevstack.apiservice.core.contracts.persistence.ApiDefinitionDao;
import org.opendevstack.apiservice.core.contracts.registry.ApiDefinition;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CoreApiRegistry {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final ApiDefinitionDao apiDefinitionDao;

    private final Map<String, ApiDefinition> routeIndex = new ConcurrentHashMap<>();

    public CoreApiRegistry(ApiDefinitionDao apiDefinitionDao) {
        this.apiDefinitionDao = apiDefinitionDao;
    }

    @PostConstruct
    public void init() {
        refreshIndex();
    }

    public void refreshIndex() {
        routeIndex.clear();
        List<ApiDefinition> definitions = apiDefinitionDao.findAllEnabled();
        for (ApiDefinition def : definitions) {
            String key = buildKey(def.getVersion(), def.getBasePath());
            routeIndex.put(key, def);
        }
    }

    public Optional<ApiDefinition> resolve(String version, String basePath) {
        String key = buildKey(version, basePath);
        return Optional.ofNullable(routeIndex.get(key));
    }

    public Optional<ApiDefinition> resolveBestMatch(String version, String requestPathAfterVersion) {
        String normalizedRequestPath = normalizePath(requestPathAfterVersion);
        if (normalizedRequestPath.isBlank()) {
            return Optional.empty();
        }

        return routeIndex.values().stream()
                .filter(definition -> version.equals(definition.getVersion()))
                .filter(definition -> matchesPattern(definition.getBasePath(), normalizedRequestPath))
                .max((left, right) -> Integer.compare(patternSpecificity(left.getBasePath()), patternSpecificity(right.getBasePath())));
    }

    private String buildKey(String version, String basePath) {
        String normalizedPath = basePath.startsWith("/") ? basePath.substring(1) : basePath;
        return version + "/" + normalizedPath;
    }

    private boolean matchesPattern(String basePathPattern, String requestPath) {
        String normalizedPattern = normalizePath(basePathPattern);
        return pathMatcher.match(normalizedPattern, requestPath)
                || pathMatcher.match(normalizedPattern + "/**", requestPath);
    }

    private int patternSpecificity(String basePathPattern) {
        return normalizePath(basePathPattern).length();
    }

    private String normalizePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return "";
        }
        return rawPath.startsWith("/") ? rawPath.substring(1) : rawPath;
    }
}