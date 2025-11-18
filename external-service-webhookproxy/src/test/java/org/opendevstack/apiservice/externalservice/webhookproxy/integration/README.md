# Webhook Proxy Integration Tests

## Overview

This directory contains integration tests for the Webhook Proxy Service. These tests verify the service can successfully communicate with the ODS Webhook Proxy and trigger builds.

## Prerequisites

### 1. Environment Variables

Set the following environment variables before running the tests:

#### Required:
- **`WEBHOOK_PROXY_TRIGGER_SECRET`**: The trigger secret for authenticating with the webhook proxy
  - Get this from the `webhook-proxy` secret in the `{project}-cd` namespace
  - Example: `oc get secret webhook-proxy -n example-project-cd -o jsonpath='{.data.trigger_secret}' | base64 -d`

#### Optional (with defaults):
- **`WEBHOOK_PROXY_CLUSTER_A_CLUSTER_BASE`**: Cluster base domain (default: `apps.cluster-a.ocp.example.com`)
- **`WEBHOOK_PROXY_CLUSTER_NAME`**: Cluster name (default: `cluster-a`)
- **`WEBHOOK_PROXY_PROJECT_KEY`**: Project key (default: `example-project`)
- **`WEBHOOK_PROXY_TEST_BRANCH`**: Branch to use for test builds (default: `master`)
- **`WEBHOOK_PROXY_TEST_REPOSITORY`**: Repository name (default: `ods-release-manager`)

### 2. Network Access

Ensure you can reach the webhook proxy at:
```
https://webhook-proxy-{projectKey}-cd.{clusterBase}
```

Example: `https://webhook-proxy-example-project-cd.apps.cluster-a.ocp.example.com`

### 3. Valid Trigger Secret

The trigger secret must match the webhook proxy configuration. To retrieve it from OpenShift:

```bash
# Get trigger secret from OpenShift
oc login <your-cluster>
oc project example-project-cd
oc get secret webhook-proxy -o jsonpath='{.data.trigger_secret}' | base64 -d
```

## Running the Tests

### All Tests (Dry Run - No Builds)

These tests only verify configuration and validation, they don't trigger actual builds:

```bash
# Export the trigger secret
export WEBHOOK_PROXY_TRIGGER_SECRET="your-secret-here"

# Run validation and configuration tests
mvn test -Dtest=WebhookProxyIntegrationTest#testServiceConfiguration
mvn test -Dtest=WebhookProxyIntegrationTest#testGetWebhookProxyUrl
mvn test -Dtest=WebhookProxyIntegrationTest#testValidationErrors
mvn test -Dtest=WebhookProxyIntegrationTest#testConfigurationErrors
```

### Individual Tests

#### 1. Check Service Configuration
```bash
mvn test -Dtest=WebhookProxyIntegrationTest#testServiceConfiguration -Dspring.profiles.active=local
```

#### 2. Verify URL Construction
```bash
mvn test -Dtest=WebhookProxyIntegrationTest#testGetWebhookProxyUrl -Dspring.profiles.active=local
```

#### 3. Trigger a Basic Build (⚠️ TRIGGERS REAL BUILD)
```bash
export WEBHOOK_PROXY_TRIGGER_SECRET="your-secret"
mvn test -Dtest=WebhookProxyIntegrationTest#testTriggerBuildBasic -Dspring.profiles.active=local
```

#### 4. Trigger Build with Environment Variables (⚠️ TRIGGERS REAL BUILD)
```bash
mvn test -Dtest=WebhookProxyIntegrationTest#testTriggerBuildWithEnvironmentVariables -Dspring.profiles.active=local
```

#### 5. Test Authentication Failure
```bash
mvn test -Dtest=WebhookProxyIntegrationTest#testAuthenticationFailure -Dspring.profiles.active=local
```

#### 6. Print Current Configuration
```bash
mvn test -Dtest=WebhookProxyIntegrationTest#printCurrentConfiguration -Dspring.profiles.active=local
```

### Run All Tests (⚠️ Some will trigger real builds)

```bash
export WEBHOOK_PROXY_TRIGGER_SECRET="your-secret"
export WEBHOOK_PROXY_PROJECT_KEY="sample-project"
mvn test -Dtest=WebhookProxyIntegrationTest -Dspring.profiles.active=local
```

## Test Descriptions

### Safe Tests (No Build Triggers)

1. **`testServiceConfiguration`**: Verifies service is properly configured and clusters are available
2. **`testGetWebhookProxyUrl`**: Tests URL construction for different cluster/project combinations
3. **`testValidationErrors`**: Tests request validation (missing fields, invalid data)
4. **`testConfigurationErrors`**: Tests error handling for non-existent clusters
5. **`printCurrentConfiguration`**: Prints current configuration for debugging

### Tests That Trigger Builds (⚠️ CAUTION)

1. **`testTriggerBuildBasic`**: Triggers a basic build with minimal configuration
2. **`testTriggerBuildWithEnvironmentVariables`**: Triggers a build with custom env vars
3. **`testTriggerBuildWithCustomJenkinsfile`**: Triggers a build with custom Jenkinsfile path

### Authentication Tests

1. **`testAuthenticationFailure`**: Verifies authentication fails with invalid trigger secret

## Disabling/Enabling Tests

All tests are disabled by default with `@Disabled` annotations. To enable a test:

1. Open the test file
2. Find the test method
3. Remove or comment out the `@Disabled` annotation
4. Run the test

Example:
```java
// @Disabled("Remove this annotation to test")
@Test
void testServiceConfiguration() {
    // test code
}
```

## Environment Setup Examples

```bash
# Set environment variables
export WEBHOOK_PROXY_TRIGGER_SECRET="my-secret-here"
export WEBHOOK_PROXY_PROJECT_KEY="example-project"
export WEBHOOK_PROXY_CLUSTER_A_CLUSTER_BASE="apps.cluster-a.ocp.example.com"

# Run a test
mvn test -Dtest=WebhookProxyIntegrationTest#testServiceConfiguration -Dspring.profiles.active=local
```

# Run a test
```
mvn test -D"test=WebhookProxyIntegrationTest#testServiceConfiguration" -D"spring.profiles.active=local"
```

### Using .env file (with direnv or similar)
Create a `.env` file in the project root:
```bash
export WEBHOOK_PROXY_TRIGGER_SECRET="my-secret-here"
export WEBHOOK_PROXY_PROJECT_KEY="example-project"
export WEBHOOK_PROXY_CLUSTER_A_CLUSTER_BASE="apps.cluster-a.ocp.example.com"
export WEBHOOK_PROXY_TEST_BRANCH="master"
export WEBHOOK_PROXY_TEST_REPOSITORY="ods-release-manager"
```

Then source it:
```bash
source .env
```
