package org.opendevstack.apiservice.externalservice.aap.exception;

/**
 * Exception thrown when there are issues with automation platform operations.
 */
public class AutomationPlatformException extends Exception {

    private final String errorCode;

    public AutomationPlatformException(String message) {
        super(message);
        this.errorCode = "AUTOMATION_PLATFORM_ERROR";
    }

    public AutomationPlatformException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AutomationPlatformException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTOMATION_PLATFORM_ERROR";
    }

    public AutomationPlatformException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static class WorkflowExecutionException extends AutomationPlatformException {
        public WorkflowExecutionException(String workflowName, String reason) {
            super(String.format("Failed to execute workflow '%s': %s", workflowName, reason), "WORKFLOW_EXECUTION_FAILED");
        }

        public WorkflowExecutionException(String workflowName, Throwable cause) {
            super(String.format("Failed to execute workflow '%s'", workflowName), "WORKFLOW_EXECUTION_FAILED", cause);
        }
    }

    public static class ModuleExecutionException extends AutomationPlatformException {
        public ModuleExecutionException(String moduleName, String reason) {
            super(String.format("Failed to execute module '%s': %s", moduleName, reason), "MODULE_EXECUTION_FAILED");
        }

        public ModuleExecutionException(String moduleName, Throwable cause) {
            super(String.format("Failed to execute module '%s'", moduleName), "MODULE_EXECUTION_FAILED", cause);
        }
    }

    public static class ConnectionException extends AutomationPlatformException {
        public ConnectionException(String message) {
            super(message, "CONNECTION_FAILED");
        }

        public ConnectionException(String message, Throwable cause) {
            super(message, "CONNECTION_FAILED", cause);
        }
    }

    public static class JobNotFoundException extends AutomationPlatformException {
        public JobNotFoundException(String jobId) {
            super(String.format("Job with ID '%s' not found", jobId), "JOB_NOT_FOUND");
        }
    }

    public static class AuthenticationException extends AutomationPlatformException {
        public AuthenticationException(String message) {
            super(message, "AUTHENTICATION_FAILED");
        }

        public AuthenticationException(String message, Throwable cause) {
            super(message, "AUTHENTICATION_FAILED", cause);
        }
    }
}
