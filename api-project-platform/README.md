# Project Users API Module

This module provides REST API endpoint to obtain all platforms for a given project.

## Integration with Core Application

To integrate these modules with your core application:

1. **Add module dependencies** to your core `pom.xml`:

```xml

<dependency>
    <groupId>org.opendevstack.apiservice</groupId>
    <artifactId>api-project-platform</artifactId>
    <version>${project.version}</version>
</dependency>

```
## Error Handling

The API provides comprehensive error handling with structured error responses:

- **400 Bad Request**: Invalid request data
- **404 Not Found**: Resource not found (project, user)
- **409 Conflict**: Resource already exists
- **500 Internal Server Error**: System errors, automation platform failures

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
./mvnw clean install -pl api-project-platform
./mvnw clean install -pl external-service-app
```

### Running Locally

```bash
# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### API Documentation

Swagger UI is available at: `http://localhost:8080/swagger-ui.html`

