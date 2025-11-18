package org.opendevstack.apiservice.externalservice.aap.model;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/**
 * Represents the status of a job running on an automation platform.
 */
@Data
public class AutomationJobStatus {

    public enum Status {
        PENDING,
        RUNNING,
        SUCCESSFUL,
        FAILED,
        CANCELLED,
        ERROR
    }

    private String jobId;
    private Status status;
    private String statusMessage;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Map<String, Object> result;
    private String errorMessage;
    private int progress;

    public AutomationJobStatus() {
    }

    public AutomationJobStatus(String jobId, Status status) {
        this.jobId = jobId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // ...existing code...
}
