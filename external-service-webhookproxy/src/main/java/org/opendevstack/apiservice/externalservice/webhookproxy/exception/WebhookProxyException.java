package org.opendevstack.apiservice.externalservice.webhookproxy.exception;

/**
 * Base exception for webhook proxy related errors.
 */
public class WebhookProxyException extends Exception {
    
    public WebhookProxyException(String message) {
        super(message);
    }
    
    public WebhookProxyException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Exception thrown when authentication fails (trigger_secret mismatch)
     */
    public static class AuthenticationException extends WebhookProxyException {
        public AuthenticationException(String message) {
            super(message);
        }
        
        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Exception thrown when build trigger fails
     */
    public static class BuildTriggerException extends WebhookProxyException {
        private final int statusCode;
        
        public BuildTriggerException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }
        
        public BuildTriggerException(String message, int statusCode, Throwable cause) {
            super(message, cause);
            this.statusCode = statusCode;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
    }
    
    /**
     * Exception thrown when connection to webhook proxy fails
     */
    public static class ConnectionException extends WebhookProxyException {
        public ConnectionException(String message) {
            super(message);
        }
        
        public ConnectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Exception thrown when webhook proxy instance is not configured
     */
    public static class ConfigurationException extends WebhookProxyException {
        public ConfigurationException(String message) {
            super(message);
        }
        
        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Exception thrown when request validation fails
     */
    public static class ValidationException extends WebhookProxyException {
        public ValidationException(String message) {
            super(message);
        }
        
        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
