package org.opendevstack.apiservice.core.engine.admin;

import org.opendevstack.apiservice.core.config.CacheConfig;
import org.opendevstack.apiservice.core.engine.registry.CoreApiRegistry;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class PolicyAdminController {

    private final CacheManager cacheManager;
    private final CoreApiRegistry apiRegistry;

    public PolicyAdminController(CacheManager cacheManager,
                                 CoreApiRegistry apiRegistry) {
        this.cacheManager = cacheManager;
        this.apiRegistry = apiRegistry;
    }

    @PostMapping("/policies/refresh")
    public ResponseEntity<String> refreshPolicies(
            @RequestParam(required = false) String apiId) {
        Cache cache = cacheManager.getCache(CacheConfig.POLICIES_CACHE);
        if (cache == null) {
            return ResponseEntity.ok("Policy cache not found");
        }
        if (apiId != null) {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                    (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();
            nativeCache.asMap().keySet().removeIf(k -> k.toString().startsWith(apiId + "::"));
        } else {
            cache.invalidate();
        }
        return ResponseEntity.ok("Policy cache refreshed");
    }

    @PostMapping("/registry/refresh")
    public ResponseEntity<String> refreshRegistry() {
        apiRegistry.refreshIndex();
        return ResponseEntity.ok("API registry refreshed");
    }
}
