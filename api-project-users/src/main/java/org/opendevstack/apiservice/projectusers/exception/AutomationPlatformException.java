package org.opendevstack.apiservice.projectusers.exception;

/**
 * Exception thrown when automation platform operations fail.
 */
public class AutomationPlatformException extends ProjectUserException {
    public AutomationPlatformException(String message, Throwable cause) {
        super(message, ErrorCodes.AUTOMATION_PLATFORM_ERROR, cause);
    }
}
