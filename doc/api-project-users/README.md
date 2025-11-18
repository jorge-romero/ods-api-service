# AAP (Ansible Automation Platform) Helm Configuration

## Overview

This directory contains Helm templates for configuring the Ansible Automation Platform (AAP) integration for the DevStack API Service. The configuration is split into two resources:

1. **ConfigMap** (`aap-configmap.yaml`) - Contains non-sensitive configuration
2. **Secret** (`aap-secret.yaml`) - Contains sensitive credentials

## Files

### aap-configmap.yaml
Contains non-sensitive AAP configuration:
- `ANSIBLE_BASE_URL` - Base URL of the AAP instance
- `ANSIBLE_TIMEOUT` - Request timeout in milliseconds
- `ANSIBLE_SSL_VERIFY` - Whether to verify SSL certificates
- `ANSIBLE_SSL_TRUSTSTORE_PATH` - Path to SSL truststore (optional)
- `ANSIBLE_SSL_TRUSTSTORE_TYPE` - Type of truststore (e.g., JKS)
- `API_PROJECT_USERS_WORKFLOW_NAME` - Name of the workflow for user management

### aap-secret.yaml
Contains sensitive AAP credentials:
- `ANSIBLE_USERNAME` - AAP username (base64 encoded)
- `ANSIBLE_PASSWORD` - AAP password (base64 encoded)
- `ANSIBLE_SSL_TRUSTSTORE_PASSWORD` - Truststore password (base64 encoded, optional)

## Configuration

### values.yaml
The default configuration is defined in `values.yaml`:

```yaml
aap:
  enabled: true
  baseUrl: "https://ansible.example.com/api/v2/"
  username: ""  # Set via environment or override
  password: ""  # Set via environment or override
  timeout: "30000"
  workflowName: "DAS - Add user to project - WF++Platformers Sandbox"
  ssl:
    verifyCertificates: "false"
    trustStorePath: ""
    trustStorePassword: ""
    trustStoreType: "JKS"
```

### Environment-Specific Overrides
Override values in environment-specific files:
- `values.dev.yaml` - Development environment
- `values.test.yaml` - Test environment
- `values.prod.yaml` - Production environment

## Usage

### Installing with Helm

#### Option 1: Set credentials via command line
```bash
helm install devstack-api ./chart \
  -f chart/values.dev.yaml \
  --set aap.username=your-username \
  --set aap.password=your-password
```

#### Option 2: Set credentials via environment variables
```bash
export AAP_USERNAME=your-username
export AAP_PASSWORD=your-password

helm install devstack-api ./chart \
  -f chart/values.dev.yaml \
  --set aap.username=$AAP_USERNAME \
  --set aap.password=$AAP_PASSWORD
```

#### Option 3: Use values file with credentials
Create a separate `secrets.yaml` file (DO NOT commit this):
```yaml
aap:
  username: "your-username"
  password: "your-password"
```

Then install:
```bash
helm install devstack-api ./chart \
  -f chart/values.dev.yaml \
  -f secrets.yaml
```

### Disabling AAP Integration
To disable AAP integration:
```bash
helm install devstack-api ./chart \
  -f chart/values.dev.yaml \
  --set aap.enabled=false
```

### Upgrading
```bash
helm upgrade devstack-api ./chart \
  -f chart/values.dev.yaml \
  --set aap.username=$AAP_USERNAME \
  --set aap.password=$AAP_PASSWORD
```

## Security Best Practices

1. **Never commit credentials** to version control
2. **Use Kubernetes Secrets** management tools like:
   - Sealed Secrets
   - External Secrets Operator
   - Vault
   - Cloud provider secret managers (AWS Secrets Manager, Azure Key Vault, etc.)
3. **Rotate credentials** regularly
4. **Use RBAC** to limit access to the Secret resource
5. **Enable encryption at rest** for Kubernetes Secrets

## CI/CD Integration

### Jenkins Example
```groovy
pipeline {
    environment {
        AAP_CREDENTIALS = credentials('aap-credentials-id')
    }
    stages {
        stage('Deploy') {
            steps {
                sh """
                    helm upgrade --install devstack-api ./chart \
                        -f chart/values.${ENV}.yaml \
                        --set aap.username=$AAP_CREDENTIALS_USR \
                        --set aap.password=$AAP_CREDENTIALS_PSW
                """
            }
        }
    }
}
```

### GitLab CI Example
```yaml
deploy:
  script:
    - |
      helm upgrade --install devstack-api ./chart \
        -f chart/values.${CI_ENVIRONMENT_NAME}.yaml \
        --set aap.username=$AAP_USERNAME \
        --set aap.password=$AAP_PASSWORD
  variables:
    AAP_USERNAME: $AAP_USERNAME
    AAP_PASSWORD: $AAP_PASSWORD
```

## Verifying the Configuration

After deployment, verify the ConfigMap and Secret:

```bash
# Check ConfigMap
kubectl get configmap devstack-api-aap-config -o yaml

# Check Secret (values are base64 encoded)
kubectl get secret devstack-api-aap-credentials -o yaml

# Decode secret values (for debugging)
kubectl get secret devstack-api-aap-credentials -o jsonpath='{.data.ANSIBLE_USERNAME}' | base64 -d
```

## Troubleshooting

### Check pod environment variables
```bash
kubectl exec -it <pod-name> -- env | grep ANSIBLE
```

### View pod logs for AAP connection issues
```bash
kubectl logs <pod-name> | grep -i ansible
```

### Common Issues

1. **Connection timeout**: Check `ANSIBLE_BASE_URL` and network connectivity
2. **Authentication failure**: Verify `ANSIBLE_USERNAME` and `ANSIBLE_PASSWORD`
3. **SSL verification errors**: Set `ssl.verifyCertificates: "false"` or configure proper truststore
4. **Pod not picking up changes**: Delete the pod to force recreation after ConfigMap/Secret changes

## References

- [Kubernetes ConfigMaps](https://kubernetes.io/docs/concepts/configuration/configmap/)
- [Kubernetes Secrets](https://kubernetes.io/docs/concepts/configuration/secret/)
- [Helm Values Files](https://helm.sh/docs/chart_template_guide/values_files/)
