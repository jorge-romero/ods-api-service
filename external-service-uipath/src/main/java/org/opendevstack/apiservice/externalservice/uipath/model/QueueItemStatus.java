package org.opendevstack.apiservice.externalservice.uipath.model;

/**
 * Enum representing the status of a UIPath queue item.
 * Based on UIPath Orchestrator API documentation.
 */
public enum QueueItemStatus {
    /**
     * The item is waiting to be processed.
     */
    NEW,

    /**
     * The item is currently being processed by a robot.
     */
    IN_PROGRESS,

    /**
     * The item has been successfully processed.
     */
    SUCCESSFUL,

    /**
     * The item processing failed with a business or application exception.
     */
    FAILED,

    /**
     * The item was abandoned, usually due to a robot disconnection or crash.
     */
    ABANDONED,

    /**
     * The item was retried after a failure.
     */
    RETRIED,

    /**
     * The item was deleted from the queue.
     */
    DELETED,

    /**
     * Unknown status.
     */
    UNKNOWN;

    /**
     * Parse a status string from UIPath API response.
     */
    public static QueueItemStatus fromString(String status) {
        if (status == null) {
            return UNKNOWN;
        }

        return switch (status.toUpperCase()) {
            case "NEW" -> NEW;
            case "INPROGRESS", "IN_PROGRESS" -> IN_PROGRESS;
            case "SUCCESSFUL" -> SUCCESSFUL;
            case "FAILED" -> FAILED;
            case "ABANDONED" -> ABANDONED;
            case "RETRIED" -> RETRIED;
            case "DELETED" -> DELETED;
            default -> UNKNOWN;
        };
    }

    /**
     * Check if the status represents a final state (completed, failed, or abandoned).
     */
    public boolean isFinalState() {
        return this == SUCCESSFUL || this == FAILED || this == ABANDONED || this == DELETED;
    }

    /**
     * Check if the status represents a successful completion.
     */
    public boolean isSuccessful() {
        return this == SUCCESSFUL;
    }

    /**
     * Check if the status represents a failure state.
     */
    public boolean isFailure() {
        return this == FAILED || this == ABANDONED || this == DELETED;
    }
}
