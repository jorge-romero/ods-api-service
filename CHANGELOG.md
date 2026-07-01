# Changelog

All notable changes to the DevStack API Service project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

#### Security (`core-security`)
- Support for **multiple JWT issuers**: each trusted issuer pairs an issuer URI (`iss` claim) with a JWK set URI, selected by the incoming token's `iss` claim (`OAUTH2_ISSUER_V1`/`OAUTH2_JWK_SET_URI_V1`, `OAUTH2_ISSUER_V2`/`OAUTH2_JWK_SET_URI_V2`).
- Shared **audience validation** across all configured issuers (`OAUTH2_AUDIENCE`, `OAUTH2_AUDIENCE2`); a valid token must carry at least one accepted `aud` value.
- `JwtUtils` audience and scope extraction helpers, including `tokenMatchesScopeAudience` to detect tokens that already target a given audience and scope.

#### External Service Marketplace (`external-service-marketplace`)
- **OBO exchange bypass**: when the incoming token already targets the configured bypass audience and scope, the token is forwarded as-is instead of performing an On-Behalf-Of exchange (`MARKETPLACE_BYPASS_AUDIENCE`, `MARKETPLACE_BYPASS_SCOPE`).

### Changed
- Refactored `SecurityConfig` to resolve the authentication manager per issuer using `JwtIssuerAuthenticationManagerResolver`.
- Moved OAuth2 resource server configuration from Spring's `spring.security.oauth2.resourceserver` properties to application-managed `app` security properties.
- `ClientCredentialFlowValidator` now accepts the client identifier from either the `appid` (v1 tokens) or `azp` (v2 tokens) claim.

## [0.0.3] - 2026-03-03

### Added

- Created **New API module** for managing EDP Projects with create and retrieve endpoints.
- Created **New module** external service projects to manage EDP Projects.

### External Service Jira (`external-service-jira`)
- **New module** for checking project existance in Jira (Server)
- Caching for the client

#### External Service API Module (`external-service-api`)
- **New module** for standardizing external service integrations
- `ExternalService` interface providing common contract for all external services
- `isHealthy()` method for health check implementation across all external services
- `AbstractExternalServiceHealthIndicator` base class for standardized health monitoring

#### Health Monitoring & Observability
- **Spring Boot Actuator** health indicators for all external services:

#### Persistance to keep project lifecycle

### Changed
- Updated all external service implementations to extend `ExternalService` interface
- Use of lombok.extern.slf4j.Slf4j to remove boilerplate code.
- Use **io.fabric8** in the `external-service-ocp`
- Add caching to `external-service-ocp` client factory.

### Dependencies
- Updated Spring Boot version
- Added health indicator dependencies across external service modules

## [0.0.3] - 2026-03-03

### Changed
##### Projects Info Service
- make it possible to use extrernal api ([#2](https://github.com/opendevstack/ods-api-service/pull/2))

## [0.0.1-SNAPSHOT] - 2025-11-27

### Added

#### Core Features
- **Spring Boot 3.5.7** based REST API service for managing DevStack project lifecycles
- **Java 21** support with Maven multi-module architecture
- **OpenAPI/Swagger** documentation and code generation
- **OAuth2 Resource Server** authentication and authorization
- **Spring Boot Actuator** for monitoring and management endpoints
- **OpenTelemetry** instrumentation for distributed tracing and observability

#### API Modules

##### Project Platform API (`api-project-platform`)
- REST endpoints for managing project platform information
- Get platform information for projects (`GET /api/v1/projects/{projectKey}/platforms`)
- Returns disabled platforms and categorized platform links
- OpenAPI specification: `api-project-platform.yaml`

##### Project Users API (`api-project-users`)
- REST endpoints for managing project users and roles
- Trigger membership requests (`POST /api/v1/project/{projectKey}/users`)
- Retrieve user information by username (`GET /api/v1/projects/{projectKey}/users/{username}`)
- Get all users in a project (`GET /api/v1/project/{projectKey}/users`)
- Revoke user access from projects (`DELETE /api/v1/project/{projectKey}/users/{username}`)
- OpenAPI specification: `api-project-users.yaml`

#### External Service Integrations

##### Ansible Automation Platform (AAP) (`external-service-aap`)
- Integration with Ansible Automation Platform
- Workflow automation capabilities

##### OpenShift Container Platform (OCP) (`external-service-ocp`)
- OpenShift/Kubernetes cluster integration
- Project and resource management on OpenShift
- Comprehensive documentation in `/doc/external-service-ocp/`

##### Bitbucket (`external-service-bitbucket`)
- Bitbucket API v89 integration
- Source code repository management
- OpenAPI client generated from `openapi-bitbucket-v89.yaml`

##### UIPath (`external-service-uipath`)
- UIPath RPA platform integration
- Process automation capabilities

##### Webhook Proxy (`external-service-webhookproxy`)
- Webhook proxy service integration
- Event-driven architecture support
- Documentation in `/doc/external-service-webhookproxy/`

##### Projects Info Service (`external-service-projects-info-service`)
- Project information retrieval service
- Centralized project metadata management

#### Build & Deployment

##### Multi-Build Support
- **Standard JAR Build**: Traditional Spring Boot executable JAR
- **GraalVM Native Binary**: Fast startup, low memory footprint native compilation
- **Docker Images**: Both standard and native Docker image builds
- **Makefile**: Comprehensive build automation with targets:
  - `make jar` - Build Spring Boot JAR
  - `make native` - Build GraalVM native binary
  - `make docker-native` - Build native Docker image
  - `make quick-start` - Build and run immediately
  - `make all` - Build everything

##### CI/CD Pipeline
- **GitHub Actions** workflow: `release-latest.yml`
  - Automated builds on push to `master` branch
  - Sequential job execution with workspace sharing via artifacts
  - Automatic Docker image build and push
  - Latest release creation with artifacts
  - Supports both PR and manual workflow dispatch

#### Development Tools
- **Maven Wrapper** included (`mvnw` / `mvnw.cmd`)
- **JaCoCo** code coverage reporting
- **SpotBugs** static analysis integration
- **Spring Java Format** code formatting
- **Surefire** test reporting

#### Documentation
- Comprehensive `README.md` with:
  - Prerequisites and setup instructions
  - Build options and usage examples
  - Development workflow guides
  - Docker support documentation
  - Troubleshooting section
- Module-specific documentation in `/doc/` directory
- OpenAPI specifications for all API modules

#### Configuration
- Externalized configuration via `application.yaml`
- Spring profiles support
- Environment-based configuration management

#### Security
- OAuth2 Resource Server configuration
- CORS support
- Spring Security integration
- SSL/TLS configuration support

### Technical Stack

#### Frameworks & Libraries
- **Spring Boot**: 3.5.11
- **Spring Security**: OAuth2 Resource Server
- **SpringDoc OpenAPI**: 2.8.13
- **OpenTelemetry**: 2.20.1
- **Lombok**: 1.18.42
- **Jackson Databind Nullable**: 0.2.7
- **io.Fabric8**: 7.5.2

#### Build Tools
- **Maven**: 3.x
- **GraalVM Native Build Tools**: 0.11.1
- **OpenAPI Generator Maven Plugin**: 7.15.0
- **Git Commit ID Plugin**: 4.9.10

#### Testing & Quality
- **JUnit 5**: Spring Boot Test
- **JaCoCo**: 0.8.13
- **SpotBugs**: 4.9.6
- **Maven Surefire**: 3.5.4

### Infrastructure
- **Java Runtime**: Java 21
- **Container Support**: Docker with native image option
- **Target Platforms**: Linux, macOS, Windows (via Maven wrapper)

---

## Release Notes

This is the initial release of the DevStack API Service - a stateless, microservices-based platform for managing DevStack project lifecycles. The service provides RESTful APIs for third-party applications and CLI tools, with minimal server-side data storage and integration with external identity providers.

### Key Highlights
- ✅ Production-ready Spring Boot 3.5.11 application
- ✅ Complete API documentation with OpenAPI/Swagger
- ✅ Multiple deployment options (JAR, Native Binary, Docker)
- ✅ Comprehensive external service integrations
- ✅ Observable with OpenTelemetry support
- ✅ Secure with OAuth2 authentication

```

For detailed documentation, see [README.md](README.md).
