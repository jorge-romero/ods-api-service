package org.opendevstack.apiservice.externalservice.projectsinfoservice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
@AllArgsConstructor
@Slf4j
public class CacheConfiguration {

    public static final String CUSTOM_CACHE_MANAGER_NAME = "customCacheManager";

    private final CacheSpecPropertiesConfiguration cacheSpecProperties;

    @Bean(CUSTOM_CACHE_MANAGER_NAME)
    public CacheManager customCacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();

        List<Cache> caches = cacheSpecProperties.getSpecs().entrySet().stream()
                .map(entry -> createCache(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        manager.setCaches(caches);
        return manager;
    }

    // Update this method in case we need to support different cache types
    private Cache createCache(String name, CacheSpecPropertiesConfiguration.CacheSpec spec) {
        RemovalListener<Object, Object> listener = (key, value, cause) -> {
            if (cause.wasEvicted()) {
                log.debug("Eviction from cache '{}': key={}, cause={}", name, key, cause);
                // Add custom logic here (e.g., notify, audit, etc.)
            } else {
                log.debug("Removing from cache '{}': key={}, cause={}", name, key, cause);
            }
        };

        return new CaffeineCache(
                name,
                Caffeine.newBuilder()
                        .expireAfterWrite(spec.getTtl(), TimeUnit.SECONDS)
                        .maximumSize(spec.getMaxSize())
                        .evictionListener(listener)
                        .removalListener(listener)
                        .build()
        );
    }

}
