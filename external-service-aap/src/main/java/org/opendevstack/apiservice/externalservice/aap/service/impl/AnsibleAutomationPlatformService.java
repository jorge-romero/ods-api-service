package org.opendevstack.apiservice.externalservice.aap.service.impl;

import org.opendevstack.apiservice.externalservice.aap.exception.AutomationPlatformException;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationJobStatus;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of AutomationPlatformService for Ansible Automation Platform.
 * This service provides integration with Ansible AWX/Tower for executing workflows and modules.
 */
@Service("automationPlatformService")
public class AnsibleAutomationPlatformService implements AutomationPlatformService {

    private static final Logger logger = LoggerFactory.getLogger(AnsibleAutomationPlatformService.class);

    private final RestTemplate restTemplate;

    @Value("${automation.platform.ansible.base-url:http://localhost:8080/api/v2}")
    private String baseUrl;

    @Value("${automation.platform.ansible.username:admin}")
    private String username;

    @Value("${automation.platform.ansible.password:password}")
    private String password;

    @Value("${automation.platform.ansible.timeout:30000}")
    private int timeoutMs;

    public AnsibleAutomationPlatformService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public AutomationExecutionResult executeWorkflow(String workflowName, Map<String, Object> parameters) throws AutomationPlatformException {
        logger.info("Executing workflow '{}' with parameters: {}", workflowName, parameters);
        
        try {
            // Create headers with authentication
            HttpHeaders headers = createAuthHeaders();
            
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("extra_vars", parameters);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // Execute workflow job template with proper URL encoding
            String encodedWorkflowName = UriUtils.encodePath(workflowName, StandardCharsets.UTF_8);
            String url = baseUrl + "/workflow_job_templates/" + encodedWorkflowName + "/launch/";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String jobId = String.valueOf(responseBody.get("id"));
                String status = String.valueOf(responseBody.get("status"));
                
                AutomationExecutionResult result = new AutomationExecutionResult(jobId, status, true, "Workflow executed successfully");
                result.setMetadata(responseBody);
                
                logger.info("Workflow '{}' executed successfully with job ID: {}", workflowName, jobId);
                return result;
            } else {
                throw new AutomationPlatformException.WorkflowExecutionException(workflowName, "Unexpected response status: " + response.getStatusCode());
            }
            
        } catch (RestClientException e) {
            logger.error("Failed to execute workflow '{}': {}", workflowName, e.getMessage(), e);
            throw new AutomationPlatformException.WorkflowExecutionException(workflowName, e);
        }
    }

    @Override
    @Async
    public CompletableFuture<AutomationExecutionResult> executeWorkflowAsync(String workflowName, Map<String, Object> parameters) {
        try {
            AutomationExecutionResult result = executeWorkflow(workflowName, parameters);
            return CompletableFuture.completedFuture(result);
        } catch (AutomationPlatformException e) {
            logger.error("Async workflow execution failed: {}", e.getMessage(), e);
            AutomationExecutionResult errorResult = AutomationExecutionResult.failure(
                UUID.randomUUID().toString(), 
                "Async execution failed: " + e.getMessage(),
                e.getErrorCode()
            );
            return CompletableFuture.completedFuture(errorResult);
        }
    }

    @Override
    public AutomationJobStatus getJobStatus(String jobId) throws AutomationPlatformException {
        logger.debug("Checking status for job ID: {}", jobId);

        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String encodedJobId = UriUtils.encodePath(jobId, StandardCharsets.UTF_8);
            String url = baseUrl + "/jobs/" + encodedJobId + "/";
            return fetchJobStatus(jobId, url, request);
        } catch (RestClientException e) {
            logger.error("Failed to get job status for ID '{}': {}", jobId, e.getMessage(), e);
            throw new AutomationPlatformException("Failed to get job status", e);
        }
    }

    @Override
    public AutomationJobStatus getWorkflowJobStatus(String workflowId) throws AutomationPlatformException {
        logger.debug("Checking workflow status for job ID: {}", workflowId);

        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String encodedWorkflowId = UriUtils.encodePath(workflowId, StandardCharsets.UTF_8);
            String url = baseUrl + "/workflow_jobs/" + encodedWorkflowId + "/";
            return fetchJobStatus(workflowId, url, request);
        } catch (RestClientException e) {
            logger.error("Failed to get workflow job status for ID '{}': {}", workflowId, e.getMessage(), e);
            throw new AutomationPlatformException("Failed to get workflow job status", e);
        }
    }

    private AutomationJobStatus fetchJobStatus(String jobId, String url, HttpEntity<Void> request) throws AutomationPlatformException {
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                AutomationJobStatus status = new AutomationJobStatus();
                status.setJobId(jobId);
                status.setStatus(parseJobStatus(String.valueOf(responseBody.get("status"))));
                status.setStatusMessage(String.valueOf(responseBody.get("result_traceback")));
                status.setResult(responseBody);

                return status;
            } else {
                throw new AutomationPlatformException.JobNotFoundException(jobId);
            }
        } catch (RestClientException e) {
            logger.debug("Job not found at {}: {}", url, e.getMessage());
            throw new AutomationPlatformException.JobNotFoundException(jobId);
        }
    }

    @Override
    public boolean validateConnection() {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            String url = baseUrl + "/ping/";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            
            boolean isValid = response.getStatusCode().is2xxSuccessful();
            logger.debug("Connection validation: {}", isValid ? "successful" : "failed");
            return isValid;
            
        } catch (Exception e) {
            logger.warn("Connection validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            // Use validateConnection for health checks, but don't log warnings on failure
            // as health checks are frequent and failures are expected to be handled by the health indicator
            return validateConnection();
        } catch (Exception e) {
            logger.debug("Health check failed: {}", e.getMessage());
            return false;
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private AutomationJobStatus.Status parseJobStatus(String status) {
        if (status == null) {
            return AutomationJobStatus.Status.PENDING;
        }
        
        return switch (status.toLowerCase()) {
            case "pending" -> AutomationJobStatus.Status.PENDING;
            case "running" -> AutomationJobStatus.Status.RUNNING;
            case "successful" -> AutomationJobStatus.Status.SUCCESSFUL;
            case "failed" -> AutomationJobStatus.Status.FAILED;
            case "canceled", "cancelled" -> AutomationJobStatus.Status.CANCELLED;
            default -> AutomationJobStatus.Status.ERROR;
        };
    }
}
