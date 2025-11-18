# Makefile for DevStack API Service
# Supports building Spring Boot JAR and Native applications

# Project variables
PROJECT_NAME := devstack-api-service
VERSION := 0.0.1-SNAPSHOT
JAVA_VERSION := 21
MAIN_CLASS := org.opendevstack.apiservice.core.DevstackApiServiceApplication

# Maven configuration
MAVEN_WRAPPER := ./mvnw
MAVEN_OPTS := -Dmaven.compiler.release=$(JAVA_VERSION)
MAVEN_PROFILES_CLEAN := clean
MAVEN_PROFILES_COMPILE := compile
MAVEN_PROFILES_PACKAGE := package
MAVEN_PROFILES_TEST := test
MAVEN_PROFILES_NATIVE := native

# Build directories
BUILD_DIR := core/target
JAR_FILE := docker/app.jar
NATIVE_BINARY := docker/app

# Spring config folder to use when running locally (contains application*.yaml files)
# Defaults to the `chart/config` folder in the repo. You can override by setting
# SPRING_CONFIG_DIR on the make command line or environment, e.g.
#   make run-jar SPRING_CONFIG_DIR=/path/to/config/
SPRING_CONFIG_DIR ?= $(CURDIR)/chart/config/
# Argument passed to the JVM/native binary
SPRING_CONFIG_ARG := --spring.config.location=file:$(SPRING_CONFIG_DIR) --spring.profiles.active=local

# Docker configuration
DOCKER_FOLDER := docker
DOCKER_IMAGE := $(PROJECT_NAME)
DOCKER_TAG := latest
DOCKERFILE := $(DOCKER_FOLDER)/Dockerfile
DOCKERFILE_NATIVE := $(DOCKER_FOLDER)/Docker.native
DOCKER_IMAGE_NATIVE := $(PROJECT_NAME)-native

# Colors for output
RED := \033[0;31m
GREEN := \033[0;32m
YELLOW := \033[1;33m
BLUE := \033[0;34m
NC := \033[0m # No Color

.PHONY: help clean compile test package jar native docker docker-native run-jar run-native run-docker run-docker-native install verify lint format check-java check-maven check-config

# Default target
.DEFAULT_GOAL := help

## Display this help message
help:
	@echo "$(BLUE)DevStack API Service Build System$(NC)"
	@echo "=================================="
	@echo ""
	@echo "$(YELLOW)Available targets:$(NC)"
	@echo ""
	@grep -E '^## ' $(MAKEFILE_LIST) | sed 's/^## /  /' | sort
	@echo ""
	@echo "$(YELLOW)Build Profiles:$(NC)"
	@echo "  • Standard JAR:     make jar"
	@echo "  • Native Binary:    make native"
	@echo "  • Docker Image:     make docker"
	@echo "  • Docker Native:    make docker-native"
	@echo ""

## Check if Java 17+ is available
check-java:
	@echo "$(BLUE)Checking Java version...$(NC)"
	@java -version 2>&1 | head -n 1
	@if ! java -version 2>&1 | grep -E "(version \"1[7-9]|version \"[2-9][0-9])" > /dev/null; then \
		echo "$(RED)Error: Java 17 or higher is required$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)✓ Java version OK$(NC)"

## Check if Maven wrapper is available
check-maven:
	@echo "$(BLUE)Checking Maven wrapper...$(NC)"
	@if [ ! -f $(MAVEN_WRAPPER) ]; then \
		echo "$(RED)Error: Maven wrapper not found$(NC)"; \
		exit 1; \
	fi
	@chmod +x $(MAVEN_WRAPPER)
	@$(MAVEN_WRAPPER) -version | head -n 1
	@echo "$(GREEN)✓ Maven wrapper OK$(NC)"

## Clean all build artifacts
clean: check-maven
	@echo "$(BLUE)Cleaning build artifacts...$(NC)"
	$(MAVEN_WRAPPER) clean
	@echo "$(GREEN)✓ Clean complete$(NC)"

## Compile the project
compile: check-java check-maven
	@echo "$(BLUE)Compiling project...$(NC)"
	$(MAVEN_WRAPPER) $(MAVEN_OPTS) compile
	@echo "$(GREEN)✓ Compilation complete$(NC)"

## Run tests
test: check-java check-maven
	@echo "$(BLUE)Running tests...$(NC)"
	$(MAVEN_WRAPPER) $(MAVEN_OPTS) test
	@echo "$(GREEN)✓ Tests complete$(NC)"

## Package without running tests
package: check-java check-maven
	@echo "$(BLUE)Packaging application (skipping tests)...$(NC)"
	$(MAVEN_WRAPPER) $(MAVEN_OPTS) package -DskipTests
	@echo "$(GREEN)✓ Packaging complete$(NC)"

## Build standard Spring Boot JAR
jar: clean check-java check-maven
	@echo "$(BLUE)Building Spring Boot JAR...$(NC)"
	$(MAVEN_WRAPPER) $(MAVEN_OPTS) clean package -DskipTests
	@if [ -f $(JAR_FILE) ]; then \
		echo "$(GREEN)✓ JAR built successfully: $(JAR_FILE)$(NC)"; \
		ls -lh $(JAR_FILE); \
	else \
		echo "$(RED)✗ JAR build failed$(NC)"; \
		exit 1; \
	fi

## Build native binary using GraalVM
native: clean check-java check-maven
	@echo "$(BLUE)Building native binary with GraalVM...$(NC)"
	@echo "$(YELLOW)Note: This requires GraalVM with native-image installed$(NC)"
	@if ! command -v native-image >/dev/null 2>&1; then \
		echo "$(RED)Error: native-image not found. Please install GraalVM and native-image$(NC)"; \
		echo "$(YELLOW)Run: gu install native-image$(NC)"; \
		exit 1; \
	fi
	$(MAVEN_WRAPPER) $(MAVEN_OPTS) clean package -Pnative -DskipTests
	@if [ -f $(NATIVE_BINARY) ]; then \
		echo "$(GREEN)✓ Native binary built successfully: $(NATIVE_BINARY)$(NC)"; \
		ls -lh $(NATIVE_BINARY); \
	else \
		echo "$(RED)✗ Native build failed$(NC)"; \
		exit 1; \
	fi

## Install dependencies and build JAR
install: jar

## Verify build with tests
verify: check-java check-maven
	@echo "$(BLUE)Verifying build with tests...$(NC)"
	$(MAVEN_WRAPPER) $(MAVEN_OPTS) clean verify
	@echo "$(GREEN)✓ Verification complete$(NC)"

## Build Docker image
docker: jar
	@echo "$(BLUE)Building Docker image...$(NC)"
	@if [ ! -f $(DOCKERFILE) ]; then \
		echo "$(RED)Error: Dockerfile not found at $(DOCKERFILE)$(NC)"; \
		exit 1; \
	fi
	cd docker
	docker build -f $(DOCKERFILE) -t $(DOCKER_IMAGE):$(DOCKER_TAG) $(DOCKER_FOLDER)
	@echo "$(GREEN)✓ Docker image built: $(DOCKER_IMAGE):$(DOCKER_TAG)$(NC)"

## Build Docker native image
docker-native: native
	@echo "$(BLUE)Building Docker native image...$(NC)"
	@if [ ! -f $(DOCKERFILE_NATIVE) ]; then \
		echo "$(RED)Error: Native Dockerfile not found at $(DOCKERFILE_NATIVE)$(NC)"; \
		exit 1; \
	fi
	@if [ ! -f $(NATIVE_BINARY) ]; then \
		echo "$(RED)Error: Native binary not found at $(NATIVE_BINARY)$(NC)"; \
		exit 1; \
	fi
	docker build -f $(DOCKERFILE_NATIVE) -t $(DOCKER_IMAGE_NATIVE):$(DOCKER_TAG) $(DOCKER_FOLDER)
	@echo "$(GREEN)✓ Docker native image built: $(DOCKER_IMAGE_NATIVE):$(DOCKER_TAG)$(NC)"

## Run the JAR application
run-jar:
	@echo "$(BLUE)Running Spring Boot JAR...$(NC)"
	@echo "$(YELLOW)Access the application at: http://localhost:8080$(NC)"
	@echo "$(YELLOW)Swagger UI available at: http://localhost:8080/swager/swagger-ui.html$(NC)"
	@echo "$(YELLOW)Using Spring config directory: $(SPRING_CONFIG_DIR)$(NC)"
	@echo "$(YELLOW)Press Ctrl+C to stop$(NC)"
	java -jar $(JAR_FILE) $(SPRING_CONFIG_ARG)

## Run the native binary
run-native: native
	@echo "$(BLUE)Running native binary...$(NC)"
	@echo "$(YELLOW)Access the application at: http://localhost:8080$(NC)"
	@echo "$(YELLOW)Swagger UI available at: http://localhost:8080/swager/swagger-ui.html$(NC)"
	@echo "$(YELLOW)Using Spring config directory: $(SPRING_CONFIG_DIR)$(NC)"
	@echo "$(YELLOW)Press Ctrl+C to stop$(NC)"
	$(NATIVE_BINARY) $(SPRING_CONFIG_ARG)

## Run Docker container
run-docker: docker
	@echo "$(BLUE)Running Docker container...$(NC)"
	@echo "$(YELLOW)Access the application at: http://localhost:8080$(NC)"
	@echo "$(YELLOW)Press Ctrl+C to stop$(NC)"
	docker run -p 8080:8080 --rm $(DOCKER_IMAGE):$(DOCKER_TAG)

## Run Docker native container
run-docker-native: docker-native
	@echo "$(BLUE)Running Docker native container...$(NC)"
	@echo "$(YELLOW)Access the application at: http://localhost:8080$(NC)"
	@echo "$(YELLOW)Swagger UI available at: http://localhost:8080/swager/swagger-ui.html$(NC)"
	@echo "$(YELLOW)Press Ctrl+C to stop$(NC)"
	docker run -p 8080:8080 --rm $(DOCKER_IMAGE_NATIVE):$(DOCKER_TAG)

## Format code using Spring Boot's formatting rules
format: check-maven
	@echo "$(BLUE)Formatting code...$(NC)"
	$(MAVEN_WRAPPER) spring-javaformat:apply
	@echo "$(GREEN)✓ Code formatting complete$(NC)"

## Run static code analysis
lint: check-maven
	@echo "$(BLUE)Running static code analysis...$(NC)"
	$(MAVEN_WRAPPER) checkstyle:check spotbugs:check
	@echo "$(GREEN)✓ Static analysis complete$(NC)"

## Show build information
info:
	@echo "$(BLUE)Build Information$(NC)"
	@echo "=================="
	@echo "Project Name:    $(PROJECT_NAME)"
	@echo "Version:         $(VERSION)"
	@echo "Java Version:    $(JAVA_VERSION)"
	@echo "Main Class:      $(MAIN_CLASS)"
	@echo "JAR File:        $(JAR_FILE)"
	@echo "Native Binary:   $(NATIVE_BINARY)"
	@echo "Docker Image:    $(DOCKER_IMAGE):$(DOCKER_TAG)"
	@echo "Docker Native:   $(DOCKER_IMAGE_NATIVE):$(DOCKER_TAG)"

## Check Spring config directory for application*.yaml files
check-config:
	@echo "$(BLUE)Checking Spring config directory: $(SPRING_CONFIG_DIR)$(NC)"
	@if [ -d "$(SPRING_CONFIG_DIR)" ]; then \
		configs=$$(ls -1 $(SPRING_CONFIG_DIR)/application*.y* 2>/dev/null || true); \
		if [ -z "$$configs" ]; then \
			echo "$(YELLOW)No application*.yaml files found in $(SPRING_CONFIG_DIR)$(NC)"; \
			exit 1; \
		else \
			echo "$(GREEN)Found config files:$(NC)"; \
			ls -lh $(SPRING_CONFIG_DIR)application*.y*; \
		fi; \
	else \
		echo "$(RED)Config directory not found: $(SPRING_CONFIG_DIR)$(NC)"; \
		exit 1; \
	fi

## Quick development cycle: clean, compile, test
dev: clean compile test
	@echo "$(GREEN)✓ Development cycle complete$(NC)"

## Full CI/CD build: clean, verify, package
ci: clean verify jar
	@echo "$(GREEN)✓ CI/CD build complete$(NC)"

## Build both JAR and native (if GraalVM available)
all: jar docker
	@if command -v native-image >/dev/null 2>&1; then \
		$(MAKE) native docker-native; \
		echo "$(GREEN)✓ All builds complete (JAR, native, docker, docker-native)$(NC)"; \
	else \
		echo "$(YELLOW)⚠ Native builds skipped (GraalVM not available)$(NC)"; \
		echo "$(GREEN)✓ JAR and Docker builds complete$(NC)"; \
	fi

## Quick start for development
quick-start: jar run-jar

## Clean everything including Docker images
clean-all: clean
	@echo "$(BLUE)Cleaning Docker images...$(NC)"
	@docker images -q $(DOCKER_IMAGE) | xargs -r docker rmi -f 2>/dev/null || true
	@docker images -q $(DOCKER_IMAGE_NATIVE) | xargs -r docker rmi -f 2>/dev/null || true
	@echo "$(BLUE)Cleaning app.jar$(NC)"
	@rm -f $(JAR_FILE)
	@echo "$(GREEN)✓ Full clean complete$(NC)"
