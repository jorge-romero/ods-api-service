# Webhook Proxy Service - Quick Reference

## Quick Start

### 1. Add Configuration
```yaml
externalservice:
  webhook-proxy:
    clusters:
      cluster-a:
        cluster-base: apps.cluster-a.ocp.example.com
```

### 2. Inject Service
```java
@Autowired
private WebhookProxyService webhookProxyService;
```

### 3. Trigger Build
```java
WebhookProxyBuildRequest request = WebhookProxyBuildRequest.builder()
    .branch("master")
    .repository("my-repo")
    .project("myproject")
    .addEnv("RELEASE_VERSION", "1.0.0")
    .build();

WebhookProxyBuildResponse response = webhookProxyService.triggerBuild(
    "cluster-a",      // cluster
    "example-project",        // project key
    request,
    triggerSecret   // from secure storage
);
```

## URL Pattern

```
https://webhook-proxy-{projectKey}-cd.{clusterBase}
```

Example:
- Project: `example-project`
- Cluster Base: `apps.cluster-a.ocp.example.com`
- Result: `https://webhook-proxy-example-project-cd.apps.cluster-a.ocp.example.com`

## Common Operations

### Get Webhook Proxy URL
```java
String url = webhookProxyService.getWebhookProxyUrl("cluster-a", "example-project");
```

### Check Available Clusters
```java
Set<String> clusters = webhookProxyService.getAvailableClusters();
boolean hasCluster = webhookProxyService.hasCluster("cluster-a");
```

### Add Environment Variables
```java
request.addEnv("VAR_NAME", "value");
```

### Custom Jenkinsfile
```java
webhookProxyService.triggerBuild(
    clusterName, projectKey, request, secret,
    "custom/Jenkinsfile",  // jenkinsfile path
    "my-component"         // component name
);
```

## Exception Handling
```java
try {
    webhookProxyService.triggerBuild(...);
} catch (WebhookProxyException.AuthenticationException e) {
    // Invalid trigger secret
} catch (WebhookProxyException.BuildTriggerException e) {
    // Build failed (check e.getStatusCode())
} catch (WebhookProxyException.ConnectionException e) {
    // Cannot reach webhook proxy
} catch (WebhookProxyException.ConfigurationException e) {
    // Cluster not configured
} catch (WebhookProxyException.ValidationException e) {
    // Invalid request
}
```

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `cluster-base` | Required | Cluster domain (e.g., `apps.cluster-a.ocp.example.com`) |
| `connection-timeout` | 30000 | Connection timeout (ms) |
| `read-timeout` | 30000 | Read timeout (ms) |
| `trust-all-certificates` | false | Disable SSL verification (dev only!) |
| `default-jenkinsfile-path` | Jenkinsfile | Default Jenkinsfile path |

## Request Fields

### Required
- `branch`: Git branch name
- `repository`: Repository name
- `project`: Project key

### Optional
- `env`: List of environment variables (name/value pairs)
