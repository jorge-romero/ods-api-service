package org.opendevstack.apiservice.externalservice.uipath.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a Queue Item in UIPath Orchestrator.
 * Based on UIPath Orchestrator API QueueItem entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UiPathQueueItem {
    @JsonProperty("Id")
    private Long id;
    @JsonProperty("QueueDefinitionId")
    private Long queueDefinitionId;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("ReviewStatus")
    private String reviewStatus;
    @JsonProperty("Reference")
    private String reference;
    @JsonProperty("Priority")
    private String priority;
    @JsonProperty("DeferDate")
    private LocalDateTime deferDate;
    @JsonProperty("DueDate")
    private LocalDateTime dueDate;
    @JsonProperty("StartProcessing")
    private LocalDateTime startProcessing;
    @JsonProperty("EndProcessing")
    private LocalDateTime endProcessing;
    @JsonProperty("CreationTime")
    private LocalDateTime creationTime;
    @JsonProperty("Progress")
    private String progress;
    @JsonProperty("SpecificContent")
    private Map<String, Object> specificContent;
    @JsonProperty("Output")
    private Map<String, Object> output;
    @JsonProperty("ProcessingException")
    private Map<String, Object> processingException;
    @JsonProperty("ReviewerUserId")
    private Long reviewerUserId;

    @JsonProperty("OrganizationUnitId")
    private Long organizationUnitId;

    public UiPathQueueItem() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQueueDefinitionId() {
        return queueDefinitionId;
    }

    public void setQueueDefinitionId(Long queueDefinitionId) {
        this.queueDefinitionId = queueDefinitionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public QueueItemStatus getStatusEnum() {
        return QueueItemStatus.fromString(status);
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getDeferDate() {
        return deferDate;
    }

    public void setDeferDate(LocalDateTime deferDate) {
        this.deferDate = deferDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getStartProcessing() {
        return startProcessing;
    }

    public void setStartProcessing(LocalDateTime startProcessing) {
        this.startProcessing = startProcessing;
    }

    public LocalDateTime getEndProcessing() {
        return endProcessing;
    }

    public void setEndProcessing(LocalDateTime endProcessing) {
        this.endProcessing = endProcessing;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public Map<String, Object> getSpecificContent() {
        return specificContent;
    }

    public void setSpecificContent(Map<String, Object> specificContent) {
        this.specificContent = specificContent;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }

    public Map<String, Object> getProcessingException() {
        return processingException;
    }

    public void setProcessingException(Map<String, Object> processingException) {
        this.processingException = processingException;
    }

    public Long getReviewerUserId() {
        return reviewerUserId;
    }

    public void setReviewerUserId(Long reviewerUserId) {
        this.reviewerUserId = reviewerUserId;
    }

    public Long getOrganizationUnitId() {
        return organizationUnitId;
    }

    public void setOrganizationUnitId(Long organizationUnitId) {
        this.organizationUnitId = organizationUnitId;
    }

    /**
     * Check if the queue item has finished processing (either successfully or with failure).
     */
    public boolean isFinalized() {
        return getStatusEnum().isFinalState();
    }

    /**
     * Check if the queue item finished successfully.
     */
    public boolean isSuccessful() {
        return getStatusEnum().isSuccessful();
    }

    /**
     * Check if the queue item failed.
     */
    public boolean isFailed() {
        return getStatusEnum().isFailure();
    }
}
