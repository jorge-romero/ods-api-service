# OpenShift Service - Quick Reference Guide

## Quick Start

### 1. Add Configuration

In `application.yaml`:
```yaml
openshift:
  instances:
    dev:
      api-url: https://api.dev.ocp.example.com:6443
      token: ${OPENSHIFT_DEV_TOKEN}
      namespace: devstack-dev
      trust-all-certificates: true  # Dev only!
```

### 2. Inject Service

```java
@Service
public class YourService {
    private final OpenshiftService openshiftService;
    
    public YourService(OpenshiftService openshiftService) {
        this.openshiftService = openshiftService;
    }
}
```

### 3. Use Service

```java
// Get secret
Map<String, String> secret = openshiftService.getSecret("dev", "my-secret");

// Get specific value
String password = openshiftService.getSecretValue("dev", "db-creds", "password");
```

## Common Operations

### Get Entire Secret
```java
try {
    Map<String, String> secret = openshiftService.getSecret("dev", "database-credentials");
    String username = secret.get("username");
    String password = secret.get("password");
} catch (OpenshiftException e) {
    log.error("Failed to get secret", e);
}
```

### Get Single Value
```java
try {
    String apiKey = openshiftService.getSecretValue("prod", "api-keys", "github-token");
} catch (OpenshiftException e) {
    log.error("Failed to get API key", e);
}
```

### Check Before Retrieving
```java
if (openshiftService.secretExists("dev", "my-secret")) {
    Map<String, String> secret = openshiftService.getSecret("dev", "my-secret");
    // Use secret...
}
```

### Work with Specific Namespace
```java
// Instead of using the default namespace from config
String value = openshiftService.getSecretValue(
    "dev",                  // instance
    "my-secret",           // secret name
    "key",                 // key in secret
    "custom-namespace"     // specific namespace
);
```

### List Available Instances
```java
Set<String> instances = openshiftService.getAvailableInstances();
log.info("Available: {}", instances);  // Output: [dev, test, prod]
```

### Check Instance Availability
```java
if (openshiftService.hasInstance("prod")) {
    // Safe to use prod instance
}
```

## Configuration Patterns

### Environment-Based Configuration
```yaml
# application-dev.yaml
openshift:
  instances:
    dev:
      api-url: ${OPENSHIFT_DEV_API_URL}
      token: ${OPENSHIFT_DEV_TOKEN}
      namespace: devstack-dev
      trust-all-certificates: true

# application-prod.yaml
openshift:
  instances:
    prod:
      api-url: ${OPENSHIFT_PROD_API_URL}
      token: ${OPENSHIFT_PROD_TOKEN}
      namespace: production
      trust-all-certificates: false
```

### Multiple Instances in One Environment
```yaml
openshift:
  instances:
    cluster1:
      api-url: https://api.cluster1.example.com:6443
      token: ${CLUSTER1_TOKEN}
      namespace: app-namespace
    cluster2:
      api-url: https://api.cluster2.example.com:6443
      token: ${CLUSTER2_TOKEN}
      namespace: app-namespace
```

## Code Patterns

### Pattern 1: Environment-Aware Service
```java
@Service
public class ConfigService {
    private final OpenshiftService openshiftService;
    
    @Value("${spring.profiles.active}")
    private String activeProfile;
    
    public String getConfig(String key) throws OpenshiftException {
        // Use instance name matching profile
        return openshiftService.getSecretValue(activeProfile, "app-config", key);
    }
}
```

### Pattern 2: Fallback Chain
```java
public String getSecretWithFallback(String secretName, String key) {
    try {
        return openshiftService.getSecretValue("prod", secretName, key);
    } catch (OpenshiftException e) {
        log.warn("Failed to get from prod, trying test", e);
        try {
            return openshiftService.getSecretValue("test", secretName, key);
        } catch (OpenshiftException ex) {
            log.error("Failed to get secret from any instance", ex);
            return null;
        }
    }
}
```

### Pattern 3: Cached Secrets
```java
@Service
public class SecretCacheService {
    private final OpenshiftService openshiftService;
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    
    @Cacheable("secrets")
    public String getSecretCached(String instance, String secret, String key) 
            throws OpenshiftException {
        String cacheKey = instance + ":" + secret + ":" + key;
        return cache.computeIfAbsent(cacheKey, k -> {
            try {
                return openshiftService.getSecretValue(instance, secret, key);
            } catch (OpenshiftException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
```

### Pattern 4: Dynamic Instance Selection
```java
public String getSecret(String environment) throws OpenshiftException {
    String instanceName = mapEnvironmentToInstance(environment);
    
    if (!openshiftService.hasInstance(instanceName)) {
        throw new OpenshiftException("Instance not configured: " + instanceName);
    }
    
    return openshiftService.getSecretValue(instanceName, "config", "api-key");
}

private String mapEnvironmentToInstance(String env) {
    return switch (env.toLowerCase()) {
        case "production", "prod" -> "prod";
        case "staging", "stage" -> "test";
        case "development", "dev" -> "dev";
        default -> "dev";
    };
}
```

## Error Handling

### Basic Try-Catch
```java
try {
    String value = openshiftService.getSecretValue("dev", "secret", "key");
} catch (OpenshiftException e) {
    log.error("Failed: {}", e.getMessage());
}
```

### With Retry Logic
```java
@Retryable(value = OpenshiftException.class, maxAttempts = 3)
public String getSecretWithRetry(String instance, String secret, String key) 
        throws OpenshiftException {
    return openshiftService.getSecretValue(instance, secret, key);
}
```

### Graceful Degradation
```java
public String getConfigValue(String key, String defaultValue) {
    try {
        return openshiftService.getSecretValue("prod", "config", key);
    } catch (OpenshiftException e) {
        log.warn("Failed to get config, using default", e);
        return defaultValue;
    }
}
```

## Testing

### Mock for Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock
    private OpenshiftService openshiftService;
    
    @InjectMocks
    private MyService myService;
    
    @Test
    void testGetSecret() throws OpenshiftException {
        when(openshiftService.getSecretValue("dev", "secret", "key"))
            .thenReturn("test-value");
        
        String result = myService.doSomething();
        assertEquals("test-value", result);
    }
}
```

### Integration Test
```java
@SpringBootTest
class OpenshiftServiceIntegrationTest {
    @Autowired
    private OpenshiftService openshiftService;
    
    @Test
    void testRealConnection() throws OpenshiftException {
        assumeTrue(openshiftService.hasInstance("dev"));
        
        Set<String> instances = openshiftService.getAvailableInstances();
        assertFalse(instances.isEmpty());
    }
}
```

## Troubleshooting

### Problem: "Instance not configured"
```java
// Check available instances
Set<String> available = openshiftService.getAvailableInstances();
log.info("Available instances: {}", available);

// Check specific instance
if (!openshiftService.hasInstance("myInstance")) {
    log.error("Instance 'myInstance' is not configured!");
}
```

### Problem: "Connection timeout"
```yaml
# Increase timeouts in configuration
openshift:
  instances:
    dev:
      connection-timeout: 60000  # 60 seconds
      read-timeout: 60000
```

### Problem: "SSL certificate verification failed"
```yaml
# For development only!
openshift:
  instances:
    dev:
      trust-all-certificates: true
```

### Problem: "Secret not found"
```java
// Check if secret exists first
if (!openshiftService.secretExists("dev", "my-secret")) {
    log.error("Secret does not exist!");
}

// Or check with namespace
if (!openshiftService.secretExists("dev", "my-secret", "my-namespace")) {
    log.error("Secret does not exist in namespace!");
}
```

## Getting OpenShift Token

```bash
# Login to OpenShift
oc login https://api.your-cluster.com:6443 --username=your-user

# Get token
oc whoami -t

# Use token in environment variable
export OPENSHIFT_DEV_TOKEN=$(oc whoami -t)
```

## Best Practices

1. **Use Environment Variables**: Never hardcode tokens
2. **Check Instance Availability**: Use `hasInstance()` before operations
3. **Handle Exceptions**: Always wrap in try-catch
4. **Validate SSL in Prod**: Set `trust-all-certificates: false`
5. **Use Specific Namespaces**: When working with non-default namespaces
6. **Log Appropriately**: Mask sensitive values in logs
7. **Cache When Possible**: Secrets don't change often
8. **Test Connectivity**: Verify configuration on startup
