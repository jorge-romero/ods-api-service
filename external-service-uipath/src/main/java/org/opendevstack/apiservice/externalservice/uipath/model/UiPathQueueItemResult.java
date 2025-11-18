package org.opendevstack.apiservice.externalservice.uipath.model;

import java.util.Optional;

/**
 * Result object for UIPath queue item checks.
 * Provides a generic way to return the status and details of a queue item lookup by reference.
 */
public class UiPathQueueItemResult {

    private final ResultStatus resultStatus;
    private final QueueItemStatus queueItemStatus;
    private final String message;
    private final String errorDetails;
    private final UiPathQueueItem queueItem;

    /**
     * Enum representing the overall result of checking a queue item.
     */
    public enum ResultStatus {
        /**
         * No UIPath reference was provided (considered successful).
         */
        NO_REFERENCE,

        /**
         * Queue item not found for the given reference.
         */
        NOT_FOUND,

        /**
         * Queue item is still being processed.
         */
        IN_PROGRESS,

        /**
         * Queue item completed successfully.
         */
        SUCCESS,

        /**
         * Queue item failed or was abandoned.
         */
        FAILURE,

        /**
         * Error occurred while checking the queue item status.
         */
        ERROR
    }

    private UiPathQueueItemResult(ResultStatus resultStatus, QueueItemStatus queueItemStatus,
                                  String message, String errorDetails, UiPathQueueItem queueItem) {
        this.resultStatus = resultStatus;
        this.queueItemStatus = queueItemStatus;
        this.message = message;
        this.errorDetails = errorDetails;
        this.queueItem = queueItem;
    }

    /**
     * Creates a result when no reference is provided.
     */
    public static UiPathQueueItemResult noReference() {
        return new UiPathQueueItemResult(
                ResultStatus.NO_REFERENCE,
                null,
                "No UIPath reference provided",
                null,
                null
        );
    }

    /**
     * Creates a result when the queue item is not found.
     */
    public static UiPathQueueItemResult notFound(String reference) {
        return new UiPathQueueItemResult(
                ResultStatus.NOT_FOUND,
                null,
                "UIPath queue item not found",
                "No queue item found for reference: " + reference,
                null
        );
    }

    /**
     * Creates a result when the queue item is in progress.
     */
    public static UiPathQueueItemResult inProgress(UiPathQueueItem queueItem) {
        return new UiPathQueueItemResult(
                ResultStatus.IN_PROGRESS,
                queueItem.getStatusEnum(),
                "UIPath process is " + queueItem.getStatusEnum().toString().toLowerCase(),
                null,
                queueItem
        );
    }

    /**
     * Creates a result when the queue item completed successfully.
     */
    public static UiPathQueueItemResult success(UiPathQueueItem queueItem) {
        return new UiPathQueueItemResult(
                ResultStatus.SUCCESS,
                queueItem.getStatusEnum(),
                "UIPath process completed successfully",
                null,
                queueItem
        );
    }

    /**
     * Creates a result when the queue item failed.
     */
    public static UiPathQueueItemResult failure(UiPathQueueItem queueItem) {
        return new UiPathQueueItemResult(
                ResultStatus.FAILURE,
                queueItem.getStatusEnum(),
                "UIPath process failed with status: " + queueItem.getStatusEnum(),
                "UIPath status: " + queueItem.getStatusEnum(),
                queueItem
        );
    }

    /**
     * Creates a result when an error occurs during status check.
     */
    public static UiPathQueueItemResult error(String message, String errorDetails) {
        return new UiPathQueueItemResult(
                ResultStatus.ERROR,
                null,
                message,
                errorDetails,
                null
        );
    }

    // Getters

    public ResultStatus getResultStatus() {
        return resultStatus;
    }

    public QueueItemStatus getQueueItemStatus() {
        return queueItemStatus;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public Optional<UiPathQueueItem> getQueueItem() {
        return Optional.ofNullable(queueItem);
    }

    /**
     * Check if the result represents a final state (completed, not in progress).
     */
    public boolean isFinalState() {
        return resultStatus == ResultStatus.SUCCESS
                || resultStatus == ResultStatus.FAILURE
                || resultStatus == ResultStatus.NOT_FOUND
                || resultStatus == ResultStatus.ERROR
                || resultStatus == ResultStatus.NO_REFERENCE;
    }

    /**
     * Check if the result represents a successful outcome.
     */
    public boolean isSuccess() {
        return resultStatus == ResultStatus.SUCCESS || resultStatus == ResultStatus.NO_REFERENCE;
    }

    /**
     * Check if the result represents a failure outcome.
     */
    public boolean isFailure() {
        return resultStatus == ResultStatus.FAILURE
                || resultStatus == ResultStatus.NOT_FOUND
                || resultStatus == ResultStatus.ERROR;
    }
}
