# External Service Configuration Guide

## Overview
The DevStack API Service includes an external service module that provides integration with automation platforms like Ansible Automation Platform (AAP). This guide explains how to configure and use these external services.

## Configuration Properties

### Base External Service Properties

```yaml
automation:
  platform:
    ansible:
      enabled: true                                    # Enable/disable Ansible integration
      base-url: ${ANSIBLE_BASE_URL:http://localhost:8080/api/v2}  # AAP API base URL
      username: ${ANSIBLE_USERNAME:admin}             # AAP username
      password: ${ANSIBLE_PASSWORD:password}          # AAP password
      timeout: ${ANSIBLE_TIMEOUT:30000}              # Request timeout in milliseconds
```

## Environment Variables

For production deployments, use environment variables to configure sensitive information:

| Variable | Description | Required |
|----------|-------------|----------|
| `ANSIBLE_ENABLED` | Enable/disable Ansible integration | No (default: true) |
| `ANSIBLE_BASE_URL` | AAP API base URL | Yes |
| `ANSIBLE_USERNAME` | AAP username | Yes |
| `ANSIBLE_PASSWORD` | AAP password | Yes |
| `ANSIBLE_TIMEOUT` | Request timeout in ms | No (default: 60000) |

## Service Features

### Automation Platform Service

The `AutomationPlatformService` provides the following capabilities:

1. **Workflow Execution**: Execute Ansible workflows synchronously or asynchronously
2. **Job Status Monitoring**: Check the status of running jobs
3. **Connection Validation**: Validate connectivity to the automation platform

## Usage Examples

### Service Injection
```java
@Service
public class MyService {
    
    private final AutomationPlatformService automationService;
    
    public MyService(AutomationPlatformService automationService) {
        this.automationService = automationService;
    }
    
    public void executeDeployment(Map<String, Object> params) {
        AutomationExecutionResult result = automationService.executeWorkflow("deploy-app", params);
        // Handle result...
    }
}
```

### Asynchronous Execution
```java
CompletableFuture<AutomationExecutionResult> future = 
    automationService.executeWorkflowAsync("long-running-task", parameters);
    
future.thenAccept(result -> {
    // Handle completion
});
```

## Configuration Validation

The service validates configuration on startup:

1. **Property Binding**: Configuration properties are validated using `@ConfigurationProperties`
2. **Conditional Loading**: Services load only when `automation.platform.ansible.enabled=true`
3. **Connection Testing**: Health checks validate platform connectivity
4. **Logging**: Configuration details are logged (passwords masked)

## Troubleshooting

### Common Issues

1. **Service Not Loading**
   - Check `automation.platform.ansible.enabled` property
   - Verify component scanning includes external service package

2. **Connection Failures**
   - Verify `base-url` is correct and accessible
   - Check username/password credentials
   - Validate network connectivity and firewall rules

3. **Timeout Issues**
   - Increase `timeout` value for slow operations
   - Check network latency to AAP server

### Logging Configuration

Enable debug logging for troubleshooting:

```yaml
logging:
  level:
    org.opendevstack.apiservice.externalservice: DEBUG
    org.springframework.web.client: DEBUG
```
