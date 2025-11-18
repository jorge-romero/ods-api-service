package org.opendevstack.apiservice.externalservice.uipath.exception;

/**
 * Exception thrown when there are issues with UIPath Orchestrator operations.
 */
public class UiPathException extends Exception {

    // Error code constants
    public static final String ERROR_CODE_UIPATH_ERROR = "UIPATH_ERROR";
    public static final String ERROR_CODE_AUTHENTICATION_FAILED = "UIPATH_AUTHENTICATION_FAILED";
    public static final String ERROR_CODE_QUEUE_ITEM_CREATION_FAILED = "QUEUE_ITEM_CREATION_FAILED";
    public static final String ERROR_CODE_QUEUE_ITEM_NOT_FOUND = "QUEUE_ITEM_NOT_FOUND";
    public static final String ERROR_CODE_CONNECTION_FAILED = "UIPATH_CONNECTION_FAILED";
    public static final String ERROR_CODE_STATUS_CHECK_FAILED = "STATUS_CHECK_FAILED";
    public static final String ERROR_CODE_INVALID_STATE = "INVALID_STATE";

    private final String errorCode;

    public UiPathException(String message) {
        super(message);
        this.errorCode = ERROR_CODE_UIPATH_ERROR;
    }

    public UiPathException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public UiPathException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ERROR_CODE_UIPATH_ERROR;
    }

    public UiPathException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Exception thrown when authentication to UIPath Orchestrator fails.
     */
    public static class AuthenticationException extends UiPathException {
        public AuthenticationException(String message) {
            super(message, ERROR_CODE_AUTHENTICATION_FAILED);
        }

        public AuthenticationException(String message, Throwable cause) {
            super(message, ERROR_CODE_AUTHENTICATION_FAILED, cause);
        }
    }

    /**
     * Exception thrown when adding a queue item fails.
     */
    public static class QueueItemCreationException extends UiPathException {
        public QueueItemCreationException(String reference, String reason) {
            super(String.format("Failed to create queue item with reference '%s': %s", reference, reason), 
                  ERROR_CODE_QUEUE_ITEM_CREATION_FAILED);
        }

        public QueueItemCreationException(String reference, Throwable cause) {
            super(String.format("Failed to create queue item with reference '%s'", reference), 
                  ERROR_CODE_QUEUE_ITEM_CREATION_FAILED, cause);
        }
    }

    /**
     * Exception thrown when a queue item is not found.
     */
    public static class QueueItemNotFoundException extends UiPathException {
        public QueueItemNotFoundException(String identifier) {
            super(String.format("Queue item with identifier '%s' not found", identifier), 
                  ERROR_CODE_QUEUE_ITEM_NOT_FOUND);
        }

        public QueueItemNotFoundException(String identifier, String searchType) {
            super(String.format("Queue item with %s '%s' not found", searchType, identifier), 
                  ERROR_CODE_QUEUE_ITEM_NOT_FOUND);
        }
    }

    /**
     * Exception thrown when connection to UIPath Orchestrator fails.
     */
    public static class ConnectionException extends UiPathException {
        public ConnectionException(String message) {
            super(message, ERROR_CODE_CONNECTION_FAILED);
        }

        public ConnectionException(String message, Throwable cause) {
            super(message, ERROR_CODE_CONNECTION_FAILED, cause);
        }
    }

    /**
     * Exception thrown when the queue item status check fails.
     */
    public static class StatusCheckException extends UiPathException {
        public StatusCheckException(String identifier, String reason) {
            super(String.format("Failed to check status for queue item '%s': %s", identifier, reason), 
                  ERROR_CODE_STATUS_CHECK_FAILED);
        }

        public StatusCheckException(String identifier, Throwable cause) {
            super(String.format("Failed to check status for queue item '%s'", identifier), 
                  ERROR_CODE_STATUS_CHECK_FAILED, cause);
        }
    }

    /**
     * Exception thrown when the queue item is in an unexpected or invalid state.
     */
    public static class InvalidStateException extends UiPathException {
        public InvalidStateException(String message) {
            super(message, ERROR_CODE_INVALID_STATE);
        }
    }
}
