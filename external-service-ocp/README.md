# OpenShift External Service

This module provides a service for interacting with multiple OpenShift Container Platform (OCP) instances. It uses a factory pattern to create configured API clients and supports operations like retrieving secrets from different clusters.

## Features

- **Multiple Instance Support**: Configure and interact with multiple OpenShift clusters
- **Factory Pattern**: Efficient client creation and caching using the factory pattern
- **Secret Management**: Retrieve and decode secrets from OpenShift clusters
- **Namespace Support**: Work with secrets across different namespaces
- **Spring Configuration**: Load all configuration from Spring application properties
- **SSL/TLS Configuration**: Flexible SSL certificate handling for different environments

## Architecture

### Components

1. **OpenshiftServiceConfiguration**: Loads OpenShift instance configurations from Spring properties
2. **OpenshiftApiClientFactory**: Creates and caches API clients for different instances
3. **OpenshiftApiClient**: HTTP client for interacting with OpenShift API
4. **OpenshiftService**: High-level service interface
5. **OpenshiftServiceImpl**: Service implementation that delegates to the appropriate client

### Design Pattern

The module uses the **Factory Pattern** where:
- `OpenshiftApiClientFactory` is the factory that creates `OpenshiftApiClient` instances
- Each client is configured for a specific OpenShift instance
- Clients are cached for efficiency
- The service layer uses the factory to obtain the appropriate client

## Configuration

### Spring Configuration

Add the following configuration to your `application.yaml` or `application-{profile}.yaml`:

```yaml
openshift:
  instances:
    # First OpenShift instance
    dev:
      api-url: https://api.dev.ocp.example.com:6443
      token: your-authentication-token
      namespace: default-namespace
      connection-timeout: 30000
      read-timeout: 30000
      trust-all-certificates: false
    
    # Second OpenShift instance
    prod:
      api-url: https://api.prod.ocp.example.com:6443
      token: your-prod-token
      namespace: production
      connection-timeout: 30000
      read-timeout: 30000
      trust-all-certificates: false
```

### Configuration Properties

For each OpenShift instance:

| Property | Description | Required | Default |
|----------|-------------|----------|---------|
| `api-url` | The OpenShift API URL | Yes | - |
| `token` | Authentication token | Yes | - |
| `namespace` | Default namespace | Yes | - |
| `connection-timeout` | Connection timeout in milliseconds | No | 30000 |
| `read-timeout` | Read timeout in milliseconds | No | 30000 |
| `trust-all-certificates` | Trust all SSL certificates (DEV ONLY!) | No | false |

### Environment Variables

You can use environment variables in your configuration:

```yaml
openshift:
  instances:
    dev:
      api-url: ${OPENSHIFT_DEV_API_URL}
      token: ${OPENSHIFT_DEV_TOKEN}
      namespace: ${OPENSHIFT_DEV_NAMESPACE:devstack-dev}
```

## Usage

### Basic Usage

```java
@Service
public class MyService {
    
    private final OpenshiftService openshiftService;
    
    public MyService(OpenshiftService openshiftService) {
        this.openshiftService = openshiftService;
    }
    
    public void retrieveSecret() throws OpenshiftException {
        // Get entire secret from 'dev' instance
        Map<String, String> secretData = openshiftService.getSecret("dev", "my-secret");
        
        // Get specific value from secret
        String password = openshiftService.getSecretValue("dev", "my-secret", "password");
    }
}
```

### Working with Different Instances

```java
// Get secret from development instance
Map<String, String> devSecret = openshiftService.getSecret("dev", "database-credentials");

// Get secret from production instance
Map<String, String> prodSecret = openshiftService.getSecret("prod", "database-credentials");
```

### Working with Different Namespaces

```java
// Use default namespace (from configuration)
String apiKey = openshiftService.getSecretValue("dev", "api-keys", "github-token");

// Use specific namespace
String dbPassword = openshiftService.getSecretValue("dev", "db-creds", "password", "custom-namespace");
```

### Checking Secret Existence

```java
if (openshiftService.secretExists("dev", "my-secret")) {
    // Secret exists, proceed with retrieval
    Map<String, String> secret = openshiftService.getSecret("dev", "my-secret");
}
```

### Listing Available Instances

```java
Set<String> instances = openshiftService.getAvailableInstances();
System.out.println("Available OpenShift instances: " + instances);

// Check if specific instance is configured
if (openshiftService.hasInstance("prod")) {
    // Use production instance
}
```

## API Methods

### OpenshiftService Interface

| Method | Description |
|--------|-------------|
| `getSecret(instanceName, secretName)` | Get all data from a secret |
| `getSecret(instanceName, secretName, namespace)` | Get all data from a secret in specific namespace |
| `getSecretValue(instanceName, secretName, key)` | Get specific value from a secret |
| `getSecretValue(instanceName, secretName, key, namespace)` | Get specific value from a secret in specific namespace |
| `secretExists(instanceName, secretName)` | Check if secret exists |
| `secretExists(instanceName, secretName, namespace)` | Check if secret exists in specific namespace |
| `getAvailableInstances()` | Get all configured instance names |
| `hasInstance(instanceName)` | Check if instance is configured |

## Security Considerations

### Authentication

The service uses bearer token authentication. Ensure your tokens have appropriate permissions:

```bash
# Get token from OpenShift
oc whoami -t
```

### SSL/TLS Configuration

#### Production Environments

In production, always use proper SSL certificate validation:

```yaml
openshift:
  instances:
    prod:
      trust-all-certificates: false  # Default and recommended
```

#### Development Environments

For development with self-signed certificates:

```yaml
openshift:
  instances:
    dev:
      trust-all-certificates: true  # Use only in development!
```

⚠️ **WARNING**: Never use `trust-all-certificates: true` in production!

### Token Management

- Store tokens securely (use environment variables or secret management systems)
- Use service accounts with minimal required permissions
- Rotate tokens regularly
- Never commit tokens to version control

## Error Handling

The service throws `OpenshiftException` for various error scenarios:

```java
try {
    String secret = openshiftService.getSecretValue("dev", "my-secret", "password");
} catch (OpenshiftException e) {
    // Handle error (instance not configured, secret not found, network error, etc.)
    log.error("Failed to retrieve secret", e);
}
```

Common error scenarios:
- Instance not configured
- Invalid authentication token
- Secret not found
- Network connectivity issues
- Invalid namespace
- Key not found in secret

## Testing

Example test using the service:

```java
@SpringBootTest
class OpenshiftServiceTest {
    
    @Autowired
    private OpenshiftService openshiftService;
    
    @Test
    void testGetSecret() throws OpenshiftException {
        // Ensure test instance is configured
        assumeTrue(openshiftService.hasInstance("test"));
        
        // Test retrieving secret
        Map<String, String> secret = openshiftService.getSecret("test", "test-secret");
        assertNotNull(secret);
    }
}
```

## Dependencies

Required dependencies (already included in `pom.xml`):

- Spring Boot Starter
- Spring Boot Starter Web
- Jackson (for JSON processing)
- Lombok (for code generation)

## Examples

### Example 1: Database Credentials

```java
@Service
public class DatabaseService {
    
    private final OpenshiftService openshiftService;
    
    public DataSource createDataSource(String environment) throws OpenshiftException {
        String username = openshiftService.getSecretValue(environment, "database-creds", "username");
        String password = openshiftService.getSecretValue(environment, "database-creds", "password");
        String url = openshiftService.getSecretValue(environment, "database-creds", "url");
        
        return DataSourceBuilder.create()
            .url(url)
            .username(username)
            .password(password)
            .build();
    }
}
```

### Example 2: API Keys

```java
@Service
public class ExternalApiService {
    
    private final OpenshiftService openshiftService;
    private final RestTemplate restTemplate;
    
    public String callExternalApi() throws OpenshiftException {
        String apiKey = openshiftService.getSecretValue("prod", "api-keys", "external-api-key");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        
        return restTemplate.exchange(
            "https://api.example.com/data",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        ).getBody();
    }
}
```

### Example 3: Multi-Environment Configuration

```java
@Service
public class ConfigurationService {
    
    private final OpenshiftService openshiftService;
    
    @Value("${spring.profiles.active}")
    private String activeProfile;
    
    public Map<String, String> getEnvironmentConfig() throws OpenshiftException {
        // Use different OpenShift instances based on active profile
        String instanceName = switch (activeProfile) {
            case "dev" -> "dev";
            case "test" -> "test";
            case "prod" -> "prod";
            default -> "dev";
        };
        
        return openshiftService.getSecret(instanceName, "app-config");
    }
}
```

## Troubleshooting

### Common Issues

1. **Connection Timeout**
   - Increase `connection-timeout` value
   - Check network connectivity to OpenShift cluster
   - Verify API URL is correct

2. **Authentication Failed**
   - Verify token is valid and not expired
   - Check token has necessary permissions
   - Generate new token: `oc whoami -t`

3. **Secret Not Found**
   - Verify secret exists: `oc get secret <name> -n <namespace>`
   - Check namespace is correct
   - Ensure service account has read permissions

4. **SSL Certificate Issues**
   - For development, use `trust-all-certificates: true`
   - For production, ensure proper certificate trust chain
   - Import certificates into Java truststore if needed

## Contributing

When contributing to this module:

1. Maintain the factory pattern architecture
2. Add appropriate logging
3. Write unit tests for new features
4. Update documentation
5. Follow existing code style

## License

This module is part of the DevStack API Service project.
