# Webhook Proxy Service

Service for triggering ODS release manager builds via the OpenDevStack Webhook Proxy.

## Overview

The Webhook Proxy Service provides a Java interface to trigger builds through the [ODS Webhook Proxy](https://github.com/opendevstack/ods-core/tree/master/jenkins/webhook-proxy). The webhook proxy acts as a bridge between external triggers and Jenkins pipelines in OpenShift.

## Architecture

The service follows the factory pattern used in other external services:

- **Configuration**: `WebhookProxyConfiguration` - Manages cluster configurations
- **Client Factory**: `WebhookProxyClientFactory` - Creates and caches webhook proxy clients
- **Client**: `WebhookProxyClient` - Low-level HTTP client for webhook proxy API
- **Service**: `WebhookProxyService` - High-level service interface
- **DTOs**: Request/response objects for type-safe API calls

## Configuration

Add the following to your `application.yaml`:

```yaml
externalservice:
  webhook-proxy:
    clusters:
      cluster-a:
        cluster-base: apps.cluster-a.ocp.example.com
        connection-timeout: 30000
        read-timeout: 30000
        trust-all-certificates: false
        default-jenkinsfile-path: Jenkinsfile
      us-prod:
        cluster-base: apps.us-prod.ocp.example.com
        connection-timeout: 60000
        read-timeout: 60000
        trust-all-certificates: false
        default-jenkinsfile-path: Jenkinsfile
```

### Configuration Properties

| Property | Description | Default |
|----------|-------------|---------|
| `cluster-base` | Base domain for the cluster (without protocol) | Required |
| `connection-timeout` | Connection timeout in milliseconds | 30000 |
| `read-timeout` | Read timeout in milliseconds | 30000 |
| `trust-all-certificates` | Disable SSL verification (dev only!) | false |
| `default-jenkinsfile-path` | Default Jenkinsfile path | Jenkinsfile |


## URL Construction

Webhook proxy URLs are constructed dynamically based on:
- **Cluster base domain** (from configuration)
- **Project key** (provided at runtime)

**Format**: `https://webhook-proxy-{projectKey}-cd.{clusterBase}`

**Example**:
- Project key: `example-project`
- Cluster base: `apps.cluster-a.ocp.example.com`
- Result: `https://webhook-proxy-example-project-cd.apps.cluster-a.ocp.example.com`

## Usage

### Basic Usage

```java
@Service
public class MyReleaseService {
    
    @Autowired
    private WebhookProxyService webhookProxyService;
    
    public void triggerRelease() throws WebhookProxyException {
        // Build the request
        WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
            .branch("master")
            .repository("ods-project-quickstarters")
            .project("opendevstack")
            .build();
        
        // Add environment variables
        request.addEnv("RELEASE_VERSION", "1.0.0");
        request.addEnv("ENVIRONMENT", "DEV");
        
        // Trigger the build
        WebhookProxyBuildResponse response = webhookProxyService.triggerBuild(
            "cluster-b",           // cluster name
            "example-project",             // project key
            request,             // build request
            "my-trigger-secret"  // trigger secret
        );
        
        if (response.isSuccess()) {
            System.out.println("Build triggered successfully!");
        }
    }
}
```

### With Custom Jenkinsfile and Component

```java
WebhookProxyBuildResponse response = webhookProxyService.triggerBuild(
    "cluster-a",
    "example-project",
    request,
    triggerSecret,
    "release-manager.Jenkinsfile",  // custom Jenkinsfile path
    "my-component"                   // component name
);
```

### Building Complex Requests

```java
// Using builder pattern
WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
    .branch("feature/new-release")
    .repository("my-repository")
    .project("myproject")
    .env(Arrays.asList(
        new EnvPair("RELEASE_VERSION", "2.0.0"),
        new EnvPair("TARGET_ENV", "staging"),
        new EnvPair("SKIP_TESTS", "false")
    ))
    .build();

// Or using addEnv() method
WebhookProxyBuildRequest request = new WebhookProxyBuildRequest();
request.setBranch("master");
request.setRepository("my-repository");
request.setProject("myproject");
request.addEnv("PARAM1", "value1");
request.addEnv("PARAM2", "value2");
```

## Triggering Release Manager

T trigger the Release Manager, you must provide specific parameters depending on the deployment scenario:

1. Developer Preview
* **environment**: DEV
* **version**: WIP
* **branch**: master (or default branch of the release manager repository)

**Example**:
```java
WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
    .branch("master")
    .repository("example-project-releasemanager")
    .project("example-project")
    .build();
request.addEnv("environment", "DEV");
request.addEnv("version", "WIP");

WebhookProxyBuildResponse response = webhookProxyService.triggerBuild(
    "cluster-a",
    "example-project",
    request,
    triggerSecret
);
```

2. Deploy to D/QA/Prod
* **environment**: DEV / QA / PROD
* **version**: the changeId (e.g., CH00001)
* **branch**:
  * First deploy to D: use default branch (e.g., master)
  * Subsequent deploys: use release/changeId
* **changeId**: The value of the changeId (e.g., CH00001)

**Example**:
```java
// First deploy to D
WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
    .branch("master")
    .repository("example-project-releasemanager")
    .project("example-project")
    .build();
request.addEnv("environment", "DEV");
request.addEnv("version", "CH00001");
request.addEnv("changeId", "CH00001");

WebhookProxyBuildResponse response = webhookProxyService.triggerBuild(
    "cluster-a",
    "example-project",
    request,
    triggerSecret
);

// Subsequent deploys (QA/PROD or redeploy to D)
WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
    .branch("release/CH00001")
    .repository("example-project-releasemanager")
    .project("example-project")
    .build();
request.addEnv("environment", "QA");
request.addEnv("version", "CH00001");
request.addEnv("changeId", "CH00001");

WebhookProxyBuildResponse response = webhookProxyService.triggerBuild(
    "cluster-a",
    "example-project",
    request,
    triggerSecret
);
```

## Triggering a Component Build
To trigger the build of a specific component (e.g., microservice), provide the component name and optionally a custom Jenkinsfile path:

**Example**:

```java
WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
    .branch("master")
    .repository("example-project-component")
    .project("example-project")
    .build();

WebhookProxyBuildResponse response = webhookProxyService.triggerBuild(
    "cluster-a",
    "example-project",
    request,
    triggerSecret,
    "Jenkinsfile",    // Jenkinsfile path
    "my-component"           // component name
);
```

## Exception Handling

The service provides specific exception types:

```java
try {
    webhookProxyService.triggerBuild(...);
} catch (WebhookProxyException.AuthenticationException e) {
    // Trigger secret is invalid
} catch (WebhookProxyException.BuildTriggerException e) {
    // Build trigger failed
    int statusCode = e.getStatusCode();
} catch (WebhookProxyException.ConnectionException e) {
    // Cannot connect to webhook proxy
} catch (WebhookProxyException.ConfigurationException e) {
    // Cluster not configured
} catch (WebhookProxyException.ValidationException e) {
    // Request validation failed
}
```



## Webhook Proxy API

The service calls the webhook proxy `/build` endpoint:

**Endpoint**: `POST /build`

**Query Parameters**:
- `trigger_secret` (required) - Authentication secret
- `jenkinsfile_path` (optional) - Path to Jenkinsfile
- `component` (optional) - Component name

**Request Body**:
```json
{
  "branch": "master",
  "repository": "ods-project-quickstarters",
  "project": "opendevstack",
  "env": [
    {"name": "RELEASE_VERSION", "value": "1.0.0"},
    {"name": "ENVIRONMENT", "value": "production"}
  ]
}
```



## Security Considerations

1. **Trigger Secret**: Store trigger secrets securely (e.g., in Kubernetes secrets, not in code)
2. **SSL Verification**: Always use `trust-all-certificates: false` in production
3. **Network Access**: Ensure your service can reach the webhook proxy endpoints

## Related Documentation

- [ODS Webhook Proxy Source](https://github.com/opendevstack/ods-core/tree/master/jenkins/webhook-proxy)
- [ODS Release Manager](https://github.com/opendevstack/ods-jenkins-shared-library)
- [OpenDevStack Documentation](https://www.opendevstack.org/)


