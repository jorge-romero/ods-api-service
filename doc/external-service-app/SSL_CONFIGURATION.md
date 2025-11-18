# SSL Certificate Configuration Guide

This guide explains how to enable or disable SSL certificate verification in the DevStack API Service external service calls.

## Configuration Properties

The SSL certificate verification behavior is controlled by the following configuration properties:

```yaml
automation:
  platform:
    ansible:
      ssl:
        verify-certificates: true  # Enable/disable SSL certificate verification
        trust-store-path: ""       # Optional: Path to custom trust store
        trust-store-password: ""   # Optional: Trust store password
        trust-store-type: "JKS"    # Optional: Trust store type (JKS, PKCS12, etc.)
```

## Environment-Specific Configuration

### Development Environment (`application-dev.yaml`)
```yaml
automation:
  platform:
    ansible:
      ssl:
        verify-certificates: false  # Disabled for development convenience
```

### Production Environment (`application-prod.yaml`)
```yaml
automation:
  platform:
    ansible:
      ssl:
        verify-certificates: true   # Always enabled in production
        trust-store-path: ${ANSIBLE_SSL_TRUSTSTORE_PATH:}
        trust-store-password: ${ANSIBLE_SSL_TRUSTSTORE_PASSWORD:}
```

## Usage Examples

### 1. Disable SSL Verification (Development Only)

Set the property to `false` to disable SSL certificate verification:

```yaml
automation:
  platform:
    ansible:
      ssl:
        verify-certificates: false
```

**⚠️ WARNING**: This configuration should **NEVER** be used in production environments as it makes the application vulnerable to man-in-the-middle attacks.

### 2. Enable SSL Verification with System Trust Store (Production)

```yaml
automation:
  platform:
    ansible:
      ssl:
        verify-certificates: true
```

This uses the JVM's default trust store and is the recommended configuration for production.

### 3. Enable SSL Verification with Custom Trust Store (Advanced)

```yaml
automation:
  platform:
    ansible:
      ssl:
        verify-certificates: true
        trust-store-path: "/path/to/custom-truststore.jks"
        trust-store-password: "truststore-password"
        trust-store-type: "JKS"
```

## Environment Variables

You can also configure these properties using environment variables:

```bash
# Disable SSL verification
export ANSIBLE_SSL_VERIFY=false

# Enable with custom trust store
export ANSIBLE_SSL_VERIFY=true
export ANSIBLE_SSL_TRUSTSTORE_PATH=/path/to/truststore.jks
export ANSIBLE_SSL_TRUSTSTORE_PASSWORD=mypassword
export ANSIBLE_SSL_TRUSTSTORE_TYPE=JKS
```

## Security Considerations

### Production Environments
- **Always** enable SSL certificate verification (`verify-certificates: true`)
- Use proper CA-signed certificates on your external services
- If using self-signed certificates, add them to a custom trust store

### Development Environments
- Disabling SSL verification (`verify-certificates: false`) is acceptable for local development
- Consider using self-signed certificates with a custom trust store for better security even in development

### Certificate Management
- Keep trust store files secure and limit access
- Rotate trust store passwords regularly
- Monitor certificate expiration dates
- Use automation for certificate deployment in production

## Testing SSL Configuration

You can test the SSL configuration by:

1. **Health Check Endpoint**: The application health check will validate the external service connection
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. **Application Logs**: Check the startup logs for SSL configuration messages:
   ```
   INFO  - SSL certificate verification is ENABLED
   WARN  - SSL certificate verification is DISABLED - this should only be used in development environments
   ```

3. **Service Validation**: Use the automation platform service's `validateConnection()` method to test connectivity.

## Troubleshooting

### Common Issues

1. **Certificate Validation Errors**
   - Ensure the external service uses a valid SSL certificate
   - Check if the certificate is signed by a trusted CA
   - Verify the certificate hasn't expired

2. **Hostname Verification Errors**
   - Ensure the certificate's Common Name (CN) or Subject Alternative Name (SAN) matches the hostname in the URL
   - Check for typos in the base URL configuration

3. **Trust Store Issues**
   - Verify the trust store file exists and is readable
   - Check the trust store password is correct
   - Ensure the trust store contains the required certificates

### Debug Logging

Enable debug logging for SSL-related issues:

```yaml
logging:
  level:
    javax.net.ssl: DEBUG
    org.opendevstack.apiservice.externalservice: DEBUG
```

## Best Practices

1. **Use Environment-Specific Configurations**: Different settings for dev, test, and production
2. **Secure Credential Storage**: Use secure vaults or encrypted configuration for passwords
3. **Certificate Monitoring**: Implement monitoring for certificate expiration
4. **Regular Security Reviews**: Periodically review SSL configurations
5. **Documentation**: Keep SSL configuration documentation up to date

## Migration Guide

### From Previous Versions
If upgrading from a version without SSL configuration:

1. Add the SSL configuration block to your `application.yaml`
2. Set `verify-certificates: true` for production environments
3. Test the configuration in a non-production environment first
4. Update any deployment scripts to include SSL-related environment variables

### Default Behavior
- If no SSL configuration is provided, certificate verification is **enabled** by default
- This ensures secure-by-default behavior for production deployments