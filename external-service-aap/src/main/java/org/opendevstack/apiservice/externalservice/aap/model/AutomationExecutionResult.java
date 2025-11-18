package org.opendevstack.apiservice.externalservice.aap.model;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/**
 * Represents the result of an automation platform execution.
 */
@Data
public class AutomationExecutionResult {

    private String jobId;
    private String status;
    private boolean successful;
    private String message;
    private Map<String, Object> output;
    private Map<String, Object> metadata;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorDetails;

    public AutomationExecutionResult() {
    }

    public AutomationExecutionResult(String jobId, String status, boolean successful, String message) {
        this.jobId = jobId;
        this.status = status;
        this.successful = successful;
        this.message = message;
        this.startTime = LocalDateTime.now();
    }

    // Static factory methods
    public static AutomationExecutionResult success(String jobId, String message) {
        return new AutomationExecutionResult(jobId, "SUCCESS", true, message);
    }

    public static AutomationExecutionResult failure(String jobId, String message, String errorDetails) {
        AutomationExecutionResult result = new AutomationExecutionResult(jobId, "FAILED", false, message);
        result.setErrorDetails(errorDetails);
        result.setEndTime(LocalDateTime.now());
        return result;
    }

    // ...existing code...
}
