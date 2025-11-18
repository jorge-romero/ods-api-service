# DevStack API Service
 
Stateless central server application that manages DevStack project lifecycles.
The system will expose RESTful APIs for third-party client applications and future CLI tools, while minimizing server-side data storage and leveraging external identity providers.

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Build Options](#build-options)
- [Running the Application](#running-the-application)
- [Development Workflow](#development-workflow)
- [Docker Support](#docker-support)
- [Advanced Usage](#advanced-usage)
- [Troubleshooting](#troubleshooting)

## 🔧 Prerequisites

Before using the Makefile, ensure you have the following installed:

### Required
- **Java 17+** - The project uses Java 17
- **Maven** - Maven wrapper (`mvnw`) is included in the project
- **Make** - For running Makefile commands

### Optional (for specific features)
- **GraalVM with native-image** - Required for native binary compilation
- **Docker** - Required for Docker image building

### Installing GraalVM Native Image
```bash
# Install GraalVM native-image component
gu install native-image
```

## 🚀 Quick Start

The easiest way to get started is using the Makefile:

```bash
# Show all available commands
make help

# Build and run the application
make quick-start
```

This will build the JAR and start the application on `http://localhost:8080`.

## 🏗️ Build Options

### Standard JAR Build
Build a traditional Spring Boot JAR file:
```bash
make jar
```
- Output: `core/target/core-0.0.1-SNAPSHOT.jar`
- Includes all dependencies
- Standard Spring Boot startup time

### Native Binary Build
Build a native binary using GraalVM (requires GraalVM installation):
```bash
make native
```
- Output: `core/target/core`
- Fast startup time
- Lower memory footprint
- Requires GraalVM with native-image

### Docker Native Build
Build a native Docker image using GraalVM (requires GraalVM installation):
```bash
make docker-native
```
- Output: Docker image `devstack-api-service-native:latest`
- Ultra-fast startup time
- Minimal container size
- Requires GraalVM with native-image

### Build Everything
Build JAR, native binary, and Docker images (if GraalVM is available):
```bash
make all
```

## 🏃 Running the Application

### Run JAR Version
```bash
make run-jar
```

### Run Native Binary
```bash
make run-native
```

### Run with Docker
```bash
make run-docker
```

### Run with Docker Native
```bash
make run-docker-native
```

### Access Points
Once running, the application is available at:
- **Main Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Actuator Health**: http://localhost:8080/actuator/health

## 💻 Development Workflow

### Quick Development Cycle
Clean, compile, and test in one command:
```bash
make dev
```

### Running Tests
```bash
make test
```

### Code Quality
```bash
# Format code
make format

# Run static analysis
make lint
```

### Full CI/CD Build
Complete build with verification:
```bash
make ci
```

## 🐳 Docker Support

### Build Docker Image (Standard)
```bash
make docker
```
Builds a standard Docker image with the Spring Boot JAR.

### Build Docker Image (Native)
```bash
make docker-native
```
Builds a native Docker image using GraalVM for ultra-fast startup and minimal size.

### Run Docker Container (Standard)
```bash
make run-docker
```

### Run Docker Container (Native)
```bash
make run-docker-native
```

The Docker images will be tagged as:
- Standard: `devstack-api-service:latest`
- Native: `devstack-api-service-native:latest`

## 🔧 Advanced Usage

### Clean Operations
```bash
# Clean build artifacts
make clean

# Clean everything including Docker images
make clean-all
```

### Project Information
```bash
# Show build information
make info

# Show project structure
make structure
```

### Manual Maven Commands
You can still use Maven directly if needed:
```bash
# Standard build
./mvnw clean package

# Native build
./mvnw clean package -Pnative -DskipTests
```

## 🛠️ Available Makefile Targets

| Target | Description |
|--------|-------------|
| `help` | Display available commands |
| `jar` | Build Spring Boot JAR |
| `native` | Build native binary with GraalVM |
| `docker` | Build Docker image (standard) |
| `docker-native` | Build Docker image (native) |
| `all` | Build JAR, native, and Docker images |
| `run-jar` | Run JAR application |
| `run-native` | Run native binary |
| `run-docker` | Run Docker container (standard) |
| `run-docker-native` | Run Docker container (native) |
| `dev` | Quick development cycle |
| `ci` | Full CI/CD build |
| `quick-start` | Build and run JAR |
| `test` | Run tests |
| `clean` | Clean build artifacts |
| `clean-all` | Clean everything including Docker |
| `format` | Format code |
| `lint` | Static code analysis |
| `info` | Show build information |
| `structure` | Show project structure |

## 🚨 Troubleshooting

### Common Issues

#### Java Version Error
```
Error: Java 17 or higher is required
```
**Solution**: Install Java 17+ and ensure it's in your PATH.

#### Native Image Not Found
```
Error: native-image not found. Please install GraalVM and native-image
```
**Solution**: Install GraalVM and run `gu install native-image`.

#### Maven Wrapper Permission Error
```
Permission denied: ./mvnw
```
**Solution**: The Makefile automatically fixes this, but you can manually run:
```bash
chmod +x ./mvnw
```

#### Port Already in Use
```
Port 8080 was already in use
```
**Solution**: Stop other applications using port 8080 or change the port in `application.properties`.

### Build Performance Tips

1. **Skip Tests for Faster Builds**: Tests are automatically skipped in JAR/native builds
2. **Native Build Memory**: Native builds require significant memory (4GB+ recommended)
3. **Docker Layer Caching**: Docker builds reuse layers for faster subsequent builds
4. **Native Docker Benefits**: Native Docker images start ~10x faster and use ~50% less memory
5. **Choose the Right Build**: Use standard Docker for development, native Docker for production

## 📁 Project Structure

```
devstack-api-service/
├── Makefile                    # Build automation
├── pom.xml                     # Parent POM
├── mvnw                        # Maven wrapper
├── core/                       # Main application module
│   ├── pom.xml                # Core module POM
│   ├── src/                   # Source code
│   └── target/                # Build output
└── docker/                     # Docker configuration
    ├── Dockerfile             # Standard container definition
    └── Docker.native          # Native container definition
```

## 🤝 Contributing

1. Use `make dev` for development builds
2. Run `make format` and `make lint` before committing
3. Ensure `make ci` passes before submitting PRs
