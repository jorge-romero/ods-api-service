package org.opendevstack.apiservice.core.engine.admin;

import org.opendevstack.apiservice.core.engine.authorization.PolicyCacheService;
import org.opendevstack.apiservice.core.engine.registry.CoreApiRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class PolicyAdminController {

    private final PolicyCacheService policyCacheService;
    private final CoreApiRegistry apiRegistry;

    public PolicyAdminController(PolicyCacheService policyCacheService,
                                 CoreApiRegistry apiRegistry) {
        this.policyCacheService = policyCacheService;
        this.apiRegistry = apiRegistry;
    }

    @PostMapping("/policies/refresh")
    public ResponseEntity<String> refreshPolicies(
            @RequestParam(required = false) String apiId) {
        if (apiId != null) {
            policyCacheService.invalidate(apiId);
        } else {
            policyCacheService.invalidateAll();
        }
        return ResponseEntity.ok("Policy cache refreshed");
    }

    @PostMapping("/registry/refresh")
    public ResponseEntity<String> refreshRegistry() {
        apiRegistry.refreshIndex();
        return ResponseEntity.ok("API registry refreshed");
    }
}
