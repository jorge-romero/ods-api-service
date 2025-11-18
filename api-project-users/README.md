# Project Users API Module

This module provides REST API endpoints for managing users within projects, including adding, removing, updating roles, and querying user assignments. The module integrates with an external automation platform (Ansible Automation Platform) to handle the actual user provisioning and management.

## Architecture

The module consists of two main components:

### 1. api-project-users
The main API module that exposes REST endpoints for project user management.

**Key Components:**
- **Controller**: `ProjectUserController` - REST endpoints
- **Service**: `ProjectUserService` - Business logic
- **DTOs**: Request/Response objects
- **Models**: Domain objects
- **Exceptions**: Custom exception handling

### 2. external-service-app  
The external service integration module that provides generic integration with automation platforms like Ansible.

**Key Components:**
- **Service**: `AutomationPlatformService` - Generic automation platform interface
- **Implementation**: `AnsibleAutomationPlatformService` - Ansible-specific implementation
- **Models**: Execution results and job status tracking
- **Configuration**: HTTP client and platform settings

## API Endpoints

All endpoints follow the pattern: `/api/v1/project/{projectKey}/users`

### Add User to Project
```
POST /api/v1/project/{projectKey}/users
```

**Request Body:**
```json
{
  "environment": "dev",
  "user": "john.doe",
  "account": "john.doe@company.com", 
  "role": "TEAM",
  "notes": "Adding developer for feature implementation"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User added to project successfully",
  "data": {
    "project": "my-project",
    "environment": "dev", 
    "user": "john.doe",
    "account": "john.doe@company.com",
    "role": "TEAM",
    "requestedAt": "2025-10-06T10:30:00",
    "requestedBy": "system",
    "active": true,
    "automationJobId": "job-12345"
  },
  "timestamp": "2025-10-06T10:30:00"
}
```

## User Roles

The system supports the following roles:
- **MANAGER**: Project management permissions
- **STAKEHOLDER**: View and review permissions  
- **TEAM**: Development and contribution permissions
- **VM**: Virtual machine or service account permissions

## Configuration

### Ansible Automation Platform Configuration
Add these properties to your `application.properties`:

```properties
# Ansible Automation Platform Configuration
automation.platform.ansible.base-url=http://your-ansible-server/api/v2
automation.platform.ansible.username=your-username
automation.platform.ansible.password=your-password
automation.platform.ansible.timeout=30000
```

### Required Workflows
The following workflows must be configured in your Ansible Automation Platform:

1. **add-user-to-project**: Adds a user to a project with specified role
Configure it in your application.yaml using this yaml
```yaml
apis:
  project-users:
    ansible-workflow-name: name of the workflow
```

### Workflow Parameters
- `project`: Project identifier
- `environment`: Environment name
- `user`: User identifier  
- `account`: Account identifier
- `role`: User role (for add/update operations)
- `notes`: Optional notes

## Integration with Core Application

To integrate these modules with your core application:

1. **Add module dependencies** to your core `pom.xml`:
```xml
<dependency>
    <groupId>org.opendevstack.apiservice</groupId>
    <artifactId>api-project-users</artifactId>
    <version>${project.version}</version>
</dependency>

<dependency>
    <groupId>org.opendevstack.apiservice</groupId>
    <artifactId>external-service-app</artifactId>
    <version>${project.version}</version>
</dependency>
```

2. **Update component scanning** in your main application:
```java
@SpringBootApplication(scanBasePackages = { 
    "org.opendevstack.apiservice.core",
    "org.opendevstack.apiservice.projectusers", 
    "org.opendevstack.apiservice.externalservice"
})
```

## Error Handling

The API provides comprehensive error handling with structured error responses:

- **400 Bad Request**: Invalid request data
- **404 Not Found**: Resource not found (project, user)
- **409 Conflict**: Resource already exists  
- **500 Internal Server Error**: System errors, automation platform failures

## Security Considerations (TODO)

1. **Authentication**: Integrate with your existing authentication system
2. **Authorization**: Implement role-based access controls
3. **Audit Logging**: All operations are logged for audit purposes
4. **Secure Configuration**: Store Ansible credentials securely (consider using Spring Cloud Config or HashiCorp Vault)

## Monitoring and Observability

The modules provide comprehensive logging and can be monitored through:

1. **Application Logs**: Structured logging for all operations
2. **Metrics**: Integration with Spring Boot Actuator
3. **Health Checks**: Automation platform connectivity checks
4. **Distributed Tracing**: Support for distributed tracing systems with Opentelemetry and Dynatrace

## Testing

Both modules include comprehensive test suites:

- **Unit Tests**: Business logic validation
- **Integration Tests**: API endpoint testing  
- **Mock Tests**: Automation platform interaction testing

Run tests with:
```bash
./mvnw test
```

## Development

### Building the Modules
```bash
# Build all modules
./mvnw clean install

# Build specific module
./mvnw clean install -pl api-project-users
./mvnw clean install -pl external-service-app
```

### Running Locally
```bash
# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### API Documentation
Swagger UI is available at: `http://localhost:8080/swagger-ui.html`

